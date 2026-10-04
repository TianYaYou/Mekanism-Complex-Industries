package com.complexindustries.mekanism.content.soaker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import com.complexindustries.mekanism.content.block.ChemicalSoakingFactoryBlock;
import com.complexindustries.mekanism.recipe.ChemicalSoakingRecipe;
import com.complexindustries.mekanism.registration.MCIRecipeTypes;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.Upgrade;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.TwoInputCachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.recipes.vanilla_input.SingleItemChemicalRecipeInput;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.lookup.IDoubleRecipeLookupHandler.ItemChemicalRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.ItemChemical;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.interfaces.IHasDumpButton;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.upgrade.AdvancedMachineUpgradeData;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityChemicalSoakingFactory extends TileEntityConfigurableMachine
        implements IHasDumpButton, ItemChemicalRecipeLookupHandler<ChemicalSoakingRecipe>, mekanism.common.tile.interfaces.ITierUpgradable {

    public static final int BASE_TICKS_REQUIRED = 200;
    public static final long BASE_ENERGY_PER_TICK = 200L;

    public FactoryTier tier;
    public int[] progress;
    private boolean[] activeStates;
    private int ticksRequired = BASE_TICKS_REQUIRED;
    private long lastUsage = 0L;

    public IChemicalTank chemicalTank;
    public ChemicalInventorySlot extraSlot;
    public EnergyInventorySlot energySlot;
    public MachineEnergyContainer<TileEntityChemicalSoakingFactory> energyContainer;
    public List<IInventorySlot> inputSlots;
    public List<IInventorySlot> outputSlots;

    private boolean sorting = true;

    public boolean isSorting() {
        return sorting;
    }

    public void setSorting(boolean sorting) {
        this.sorting = sorting;
        markForSave();
    }

    @Override
    protected void presetVariables() {
        super.presetVariables();
        FactoryTier foundTier = Attribute.getTier(getBlockHolder(), FactoryTier.class);
        this.tier = foundTier != null ? foundTier : FactoryTier.BASIC;
        this.progress = new int[this.tier.processes];
        this.activeStates = new boolean[this.tier.processes];
        this.inputSlots = new ArrayList<>(this.tier.processes);
        this.outputSlots = new ArrayList<>(this.tier.processes);
    }

    public TileEntityChemicalSoakingFactory(BlockPos pos, BlockState state) {
        super(state.getBlockHolder(), pos, state);
        if (this.upgradeComponent == null) {
            this.upgradeComponent = new TileComponentUpgrade(this);
        }
        configComponent.setupItemIOConfig(inputSlots, outputSlots, energySlot, false);
        ConfigInfo itemConfig = configComponent.getConfig(TransmissionType.ITEM);
        if (itemConfig != null && extraSlot != null) {
            itemConfig.addSlotInfo(DataType.EXTRA, new InventorySlotInfo(true, true, extraSlot));
        }
        configComponent.setupInputConfig(TransmissionType.CHEMICAL, chemicalTank);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);
    }

    public long getMaxChemical() {
        return 10_000L * tier.processes;
    }

    public int getXPos(int process) {
        int baseX = tier == FactoryTier.BASIC ? 55 : 49;
        int baseXMult = tier == FactoryTier.BASIC ? 36 : tier == FactoryTier.ADVANCED ? 22 : 20;
        return baseX + (process * baseXMult);
    }

    @NotNull
    @Override
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener) {
        ChemicalTankHelper builder = ChemicalTankHelper.forSideWithConfig(this);
        chemicalTank = BasicChemicalTank.createModern(getMaxChemical(), ConstantPredicates.alwaysTrueBi(),
                (chem, automationType) -> containsRecipeB(chem), this::containsRecipeB, listener);
        builder.addTank(chemicalTank);
        return builder.build();
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, listener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this);
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 27, 13));
        builder.addSlot(extraSlot = ChemicalInventorySlot.fillOrConvert(chemicalTank, this::getLevel, listener, 27, 57));
        extraSlot.setSlotOverlay(SlotOverlay.MINUS);

        inputSlots.clear();
        outputSlots.clear();
        for (int i = 0; i < tier.processes; i++) {
            int xPos = getXPos(i);
            InputInventorySlot inSlot = InputInventorySlot.at(
                    item -> containsRecipeA(item),
                    this::containsRecipeA,
                    listener, xPos, 13);
            OutputInventorySlot outSlot = OutputInventorySlot.at(listener, xPos, 57);

            inputSlots.add(inSlot);
            outputSlots.add(outSlot);

            builder.addSlot(inSlot);
            builder.addSlot(outSlot).tracksWarnings(slot -> slot.warning(mekanism.common.inventory.warning.WarningTracker.WarningType.NO_SPACE_IN_OUTPUT,
                    () -> !outSlot.isEmpty() && outSlot.getStack().getCount() >= outSlot.getLimit(outSlot.getStack())));
        }
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        extraSlot.fillTankOrConvert();

        // 1. Auto-sorting item inputs across lanes if enabled
        if (sorting && tier.processes > 1) {
            for (int i = 0; i < tier.processes; i++) {
                IInventorySlot slotA = inputSlots.get(i);
                if (!slotA.isEmpty()) {
                    for (int j = i + 1; j < tier.processes; j++) {
                        IInventorySlot slotB = inputSlots.get(j);
                        if (slotB.isEmpty() && slotA.getStack().getCount() > 1) {
                            int transfer = slotA.getStack().getCount() / 2;
                            if (transfer > 0) {
                                ItemStack split = slotA.extractItem(transfer, Action.EXECUTE, AutomationType.INTERNAL);
                                slotB.insertItem(split, Action.EXECUTE, AutomationType.INTERNAL);
                            }
                        } else if (!slotB.isEmpty() && InventoryUtils.areItemsStackable(slotA.getStack(), slotB.getStack())) {
                            int diff = slotA.getStack().getCount() - slotB.getStack().getCount();
                            if (diff > 1) {
                                int transfer = diff / 2;
                                ItemStack split = slotA.extractItem(transfer, Action.EXECUTE, AutomationType.INTERNAL);
                                slotB.insertItem(split, Action.EXECUTE, AutomationType.INTERNAL);
                            }
                        }
                    }
                }
            }
        }

        // 2. Parallel recipe processing
        long prevEnergy = energyContainer.getEnergy();
        if (canFunction()) {
            long energyPerTick = MekanismUtils.getEnergyPerTick(this, BASE_ENERGY_PER_TICK);
            for (int i = 0; i < tier.processes; i++) {
                IInventorySlot inSlot = inputSlots.get(i);
                OutputInventorySlot outSlot = (OutputInventorySlot) outputSlots.get(i);
                ItemStack inStack = inSlot.getStack();
                ChemicalStack chemStack = chemicalTank.getStack();

                if (!inStack.isEmpty() && !chemStack.isEmpty()) {
                    ChemicalSoakingRecipe recipe = getRecipeType().getInputCache().findFirstRecipe(level, inStack, chemStack);
                    if (recipe != null && recipe.test(inStack, chemStack)) {
                        ItemStack outStack = recipe.getOutput(inStack, chemStack);
                        if (outSlot.insertItem(outStack, Action.SIMULATE, AutomationType.INTERNAL).isEmpty()) {
                            if (energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL) >= energyPerTick) {
                                energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
                                progress[i]++;
                                activeStates[i] = true;
                                if (progress[i] >= ticksRequired) {
                                    inSlot.extractItem((int) recipe.getItemInput().getNeededAmount(inStack), Action.EXECUTE, AutomationType.INTERNAL);
                                    chemicalTank.extract(recipe.getChemicalInput().getNeededAmount(chemStack), Action.EXECUTE, AutomationType.INTERNAL);
                                    outSlot.insertItem(outStack.copy(), Action.EXECUTE, AutomationType.INTERNAL);
                                    progress[i] = 0;
                                    markForSave();
                                }
                                continue;
                            }
                        }
                    }
                }
                progress[i] = 0;
                activeStates[i] = false;
            }
        } else {
            Arrays.fill(activeStates, false);
        }

        boolean isActive = false;
        for (boolean active : activeStates) {
            if (active) {
                isActive = true;
                break;
            }
        }
        setActive(isActive);
        lastUsage = prevEnergy - energyContainer.getEnergy();
        return sendUpdatePacket;
    }

    public long getLastUsage() {
        return lastUsage;
    }

    public double getScaledProgress(int index, int process) {
        if (ticksRequired == 0) return 0;
        return (double) progress[process] / ticksRequired;
    }

    @Override
    public void dump() {
        chemicalTank.setEmpty();
    }

    @Override
    public void setActive(boolean active) {
        super.setActive(active);
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            if (state.hasProperty(ChemicalSoakingFactoryBlock.ACTIVE) && state.getValue(ChemicalSoakingFactoryBlock.ACTIVE) != active) {
                level.setBlock(worldPosition, state.setValue(ChemicalSoakingFactoryBlock.ACTIVE, active), 3);
            }
        }
    }

    @Override
    public boolean getActive() {
        if (isRemote()) {
            BlockState state = getBlockState();
            if (state.hasProperty(ChemicalSoakingFactoryBlock.ACTIVE)) {
                return state.getValue(ChemicalSoakingFactoryBlock.ACTIVE);
            }
        }
        return super.getActive();
    }

    @NotNull
    @Override
    public Set<Upgrade> getSupportedUpgrade() {
        return EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING);
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.SPEED) {
            ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
        }
        if (upgrade == Upgrade.MUFFLING && upgradeComponent != null) {
            while (upgradeComponent.getUpgrades(Upgrade.MUFFLING) > 1) {
                upgradeComponent.removeUpgrade(Upgrade.MUFFLING, false);
            }
        }
    }

    public int getMaxUpgrades(Upgrade upgrade) {
        if (upgrade == Upgrade.MUFFLING) {
            return 1;
        }
        return upgrade.getMax();
    }

    public float getVolume() {
        if (upgradeComponent != null && upgradeComponent.isUpgradeInstalled(Upgrade.MUFFLING)) {
            return 0.0F;
        }
        return 1.0F;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        for (int i = 0; i < tier.processes; i++) {
            int idx = i;
            container.track(SyncableInt.create(() -> progress[idx], val -> progress[idx] = val));
        }
    }

    @Override
    public @NotNull IMekanismRecipeTypeProvider<SingleItemChemicalRecipeInput, ChemicalSoakingRecipe, ItemChemical<ChemicalSoakingRecipe>> getRecipeType() {
        return MCIRecipeTypes.CHEMICAL_SOAKING;
    }

    @Override
    public @Nullable IRecipeViewerRecipeType<ChemicalSoakingRecipe> recipeViewerType() {
        return null;
    }

    @Override
    public @Nullable ChemicalSoakingRecipe getRecipe(int cacheIndex) {
        if (cacheIndex >= 0 && cacheIndex < tier.processes) {
            ItemStack inStack = inputSlots.get(cacheIndex).getStack();
            ChemicalStack chemStack = chemicalTank.getStack();
            if (!inStack.isEmpty() && !chemStack.isEmpty()) {
                return getRecipeType().getInputCache().findFirstRecipe(level, inStack, chemStack);
            }
        }
        return null;
    }

    @NotNull
    @Override
    public CachedRecipe<ChemicalSoakingRecipe> createNewCachedRecipe(@NotNull ChemicalSoakingRecipe recipe, int cacheIndex) {
        return TwoInputCachedRecipe.itemChemicalToItem(recipe, () -> false,
                InputHelper.getInputHandler(inputSlots.get(cacheIndex), RecipeError.NOT_ENOUGH_INPUT),
                InputHelper.getConstantInputHandler(chemicalTank),
                OutputHelper.getOutputHandler(outputSlots.get(cacheIndex), RecipeError.NOT_ENOUGH_OUTPUT_SPACE));
    }

    public MachineEnergyContainer<TileEntityChemicalSoakingFactory> getEnergyContainer() {
        return energyContainer;
    }

    public TileComponentUpgrade getUpgradeComponent() {
        return upgradeComponent;
    }

    @Nullable
    @Override
    public AdvancedMachineUpgradeData getUpgradeData(HolderLookup.Provider provider) {
        return new AdvancedMachineUpgradeData(
                provider,
                redstone,
                getControlType(),
                getEnergyContainer(),
                progress,
                new long[tier.processes],
                chemicalTank,
                extraSlot,
                energySlot,
                inputSlots,
                outputSlots,
                sorting,
                getComponents()
        );
    }

    @Override
    public void parseUpgradeData(HolderLookup.Provider provider, @NotNull IUpgradeData upgradeData) {
        if (upgradeData instanceof AdvancedMachineUpgradeData data) {
            redstone = data.redstone;
            setControlType(data.controlType);
            getEnergyContainer().setEnergy(data.energyContainer.getEnergy());
            energySlot.deserializeNBT(provider, data.energySlot.serializeNBT(provider));
            if (data.chemicalSlot != null && !data.chemicalSlot.isEmpty()) {
                extraSlot.deserializeNBT(provider, data.chemicalSlot.serializeNBT(provider));
            }
            if (data.stored != null && !data.stored.isEmpty()) {
                chemicalTank.setStack(data.stored.getStack());
            }

            if (data.progress != null) {
                System.arraycopy(data.progress, 0, progress, 0, Math.min(data.progress.length, progress.length));
            }
            if (data.inputSlots != null) {
                for (int i = 0; i < Math.min(data.inputSlots.size(), inputSlots.size()); i++) {
                    inputSlots.get(i).setStack(data.inputSlots.get(i).getStack());
                }
            }
            if (data.outputSlots != null) {
                for (int i = 0; i < Math.min(data.outputSlots.size(), outputSlots.size()); i++) {
                    outputSlots.get(i).setStack(data.outputSlots.get(i).getStack());
                }
            }
            for (ITileComponent component : getComponents()) {
                component.read(data.components, provider);
            }
        } else {
            super.parseUpgradeData(provider, upgradeData);
        }
    }
}
