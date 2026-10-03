package com.complexindustries.mekanism.content.solidifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import com.complexindustries.mekanism.content.block.ChemicalSolidifierBlock;
import com.complexindustries.mekanism.recipe.ChemicalSolidifierRecipe;
import com.complexindustries.mekanism.registration.MCIRecipeTypes;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.Upgrade;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.vanilla_input.SingleChemicalRecipeInput;
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
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.lookup.ISingleRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleChemical;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.interfaces.IHasDumpButton;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.upgrade.AdvancedMachineUpgradeData;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityChemicalSolidifierFactory extends TileEntityConfigurableMachine
        implements IHasDumpButton, ISingleRecipeLookupHandler.ChemicalRecipeLookupHandler<ChemicalSolidifierRecipe> {

    public static final int BASE_TICKS_REQUIRED = 40;
    public static final long BASE_ENERGY_PER_TICK = 200L;

    public FactoryTier tier;
    public int[] progress;
    private boolean[] activeStates;
    private int ticksRequired = BASE_TICKS_REQUIRED;
    private long lastUsage = 0L;

    public IChemicalTank[] inputTank;
    public List<IChemicalTank> inputChemicalTanks;
    public IChemicalTank chemicalTank;
    public MachineEnergyContainer<TileEntityChemicalSolidifierFactory> energyContainer;
    EnergyInventorySlot energySlot;
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
        this.outputSlots = new ArrayList<>(this.tier.processes);
        this.inputChemicalTanks = new ArrayList<>(this.tier.processes);
        this.inputTank = new IChemicalTank[this.tier.processes];
    }

    public TileEntityChemicalSolidifierFactory(BlockPos pos, BlockState state) {
        super(state.getBlockHolder(), pos, state);

        if (this.upgradeComponent == null) {
            this.upgradeComponent = new TileComponentUpgrade(this);
        }

        configComponent.setupItemIOConfig(Collections.emptyList(), outputSlots, energySlot, false);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);
        mekanism.common.tile.component.config.ConfigInfo chemicalConfig = configComponent.getConfig(TransmissionType.CHEMICAL);
        if (chemicalConfig != null) {
            chemicalConfig.addSlotInfo(mekanism.common.tile.component.config.DataType.INPUT,
                    mekanism.common.tile.component.TileComponentConfig.createInfo(TransmissionType.CHEMICAL, true, false, inputChemicalTanks));
            chemicalConfig.setCanEject(false);
        }

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);
    }

    public int getXPos(int processIndex) {
        int baseX = tier == FactoryTier.BASIC ? 55 : tier == FactoryTier.ADVANCED ? 35 : tier == FactoryTier.ELITE ? 29 : 27;
        int baseXMult = tier == FactoryTier.BASIC ? 38 : tier == FactoryTier.ADVANCED ? 26 : 19;
        return baseX + (processIndex * baseXMult);
    }

    @NotNull
    @Override
    public Component getName() {
        if (hasCustomName()) {
            return getCustomName();
        }
        return Component.translatable("container.mekanism_complex_industries." + tier.getBaseTier().getLowerName() + "_chemical_solidifier_factory");
    }

    public long getMaxChemical() {
        int factor = tier == FactoryTier.BASIC ? 2 : tier == FactoryTier.ADVANCED ? 4 : tier == FactoryTier.ELITE ? 8 : 16;
        return (long) factor * 10L * FluidType.BUCKET_VOLUME;
    }

    public long getMaxChemicalPerTank() {
        return 10_000L * tier.processes;
    }

    public long getMaxEnergy() {
        return (tier.processes + 2) * 40_000L;
    }

    @NotNull
    @Override
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener) {
        ChemicalTankHelper builder = ChemicalTankHelper.forSideWithConfig(this);
        inputChemicalTanks.clear();
        long cap = getMaxChemicalPerTank();
        for (int i = 0; i < tier.processes; i++) {
            inputTank[i] = BasicChemicalTank.inputModern(cap, this::containsRecipe, listener);
            builder.addTank(inputTank[i]);
            inputChemicalTanks.add(inputTank[i]);
        }
        chemicalTank = inputTank[0];
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
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 7, 13));

        outputSlots.clear();
        for (int i = 0; i < tier.processes; i++) {
            OutputInventorySlot out = OutputInventorySlot.at(listener, getXPos(i), 70);
            outputSlots.add(out);
            builder.addSlot(out).tracksWarnings(slot -> slot.warning(mekanism.common.inventory.warning.WarningTracker.WarningType.NO_SPACE_IN_OUTPUT,
                    () -> !out.isEmpty() && out.getStack().getCount() >= out.getLimit(out.getStack())));
        }
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.fillContainerOrConvert();

        // 1. Auto-balance chemicals across all input tanks if sorting is enabled
        if (sorting) {
            ChemicalStack totalStack = ChemicalStack.EMPTY;
            long totalAmount = 0L;
            for (IChemicalTank tank : inputTank) {
                if (!tank.isEmpty()) {
                    if (totalStack.isEmpty()) {
                        totalStack = tank.getStack();
                    }
                    totalAmount += tank.getStored();
                }
            }
            if (!totalStack.isEmpty() && totalAmount > 0) {
                long perTank = totalAmount / tier.processes;
                long remainder = totalAmount % tier.processes;
                for (int i = 0; i < tier.processes; i++) {
                    long target = perTank + (i < remainder ? 1 : 0);
                    if (target > 0) {
                        inputTank[i].setStack(totalStack.copyWithAmount(target));
                    } else {
                        inputTank[i].setEmpty();
                    }
                }
            }
        }

        // 2. Parallel recipe processing
        long prevEnergy = energyContainer.getEnergy();
        if (canFunction()) {
            long energyPerTick = MekanismUtils.getEnergyPerTick(this, BASE_ENERGY_PER_TICK);
            for (int i = 0; i < tier.processes; i++) {
                IChemicalTank tank = inputTank[i];
                ChemicalStack chem = tank.getStack();
                ChemicalSolidifierRecipe recipe = chem.isEmpty() ? null : getRecipeType().getInputCache().findFirstRecipe(level, chem);
                OutputInventorySlot outSlot = (OutputInventorySlot) outputSlots.get(i);
                if (recipe != null && recipe.test(chem)) {
                    ItemStack outStack = recipe.getOutput(chem);
                    if (outSlot.insertItem(outStack, Action.SIMULATE, AutomationType.INTERNAL).isEmpty()) {
                        if (energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL) >= energyPerTick) {
                            energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
                            progress[i]++;
                            activeStates[i] = true;
                            if (progress[i] >= ticksRequired) {
                                tank.extract(recipe.getInput().amount(), Action.EXECUTE, AutomationType.INTERNAL);
                                outSlot.insertItem(outStack.copy(), Action.EXECUTE, AutomationType.INTERNAL);
                                progress[i] = 0;
                                markForSave();
                            }
                            continue;
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
        for (boolean b : activeStates) {
            if (b) {
                isActive = true;
                break;
            }
        }
        setActive(isActive);
        lastUsage = isActive ? Math.max(0, prevEnergy - energyContainer.getEnergy()) : 0L;
        return sendUpdatePacket;
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

    @Override
    public void setActive(boolean active) {
        super.setActive(active);
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            if (state.hasProperty(ChemicalSolidifierBlock.ACTIVE) && state.getValue(ChemicalSolidifierBlock.ACTIVE) != active) {
                level.setBlock(worldPosition, state.setValue(ChemicalSolidifierBlock.ACTIVE, active), 3);
            }
        }
    }

    @Override
    public boolean getActive() {
        if (isRemote()) {
            BlockState state = getBlockState();
            if (state.hasProperty(ChemicalSolidifierBlock.ACTIVE)) {
                return state.getValue(ChemicalSolidifierBlock.ACTIVE);
            }
        }
        return super.getActive();
    }

    @NotNull
    @Override
    public Set<Upgrade> getSupportedUpgrade() {
        return EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING);
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
    public void dump() {
        for (IChemicalTank tank : inputTank) {
            tank.setEmpty();
        }
    }

    public double getScaledProgress(int i, int process) {
        if (process < 0 || process >= progress.length || ticksRequired <= 0) {
            return 0.0;
        }
        return (double) progress[process] * i / ticksRequired;
    }

    public int getTicksRequired() {
        return ticksRequired;
    }

    public long getLastUsage() {
        return lastUsage;
    }

    public MachineEnergyContainer<TileEntityChemicalSolidifierFactory> getEnergyContainer() {
        return energyContainer;
    }

    public TileComponentUpgrade getUpgradeComponent() {
        return upgradeComponent;
    }

    public IChemicalTank getChemicalTank() {
        return chemicalTank;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.trackArray(progress);
        container.track(SyncableInt.create(this::getTicksRequired, val -> ticksRequired = val));
        container.track(mekanism.common.inventory.container.sync.SyncableBoolean.create(this::isSorting, this::setSorting));
    }

    @Override
    public void loadAdditional(@NotNull net.minecraft.nbt.CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        if (nbt.contains("sorting")) {
            this.sorting = nbt.getBoolean("sorting");
        }
    }

    @Override
    public void saveAdditional(@NotNull net.minecraft.nbt.CompoundTag nbtTags, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(nbtTags, provider);
        nbtTags.putBoolean("sorting", sorting);
    }

    @Nullable
    @Override
    public AdvancedMachineUpgradeData getUpgradeData(HolderLookup.Provider provider) {
        ChemicalStack totalChem = ChemicalStack.EMPTY;
        long sum = 0;
        for (IChemicalTank tank : inputTank) {
            if (!tank.isEmpty()) {
                if (totalChem.isEmpty()) {
                    totalChem = tank.getStack();
                }
                sum += tank.getStored();
            }
        }
        IChemicalTank storedTank = chemicalTank;
        if (!totalChem.isEmpty()) {
            storedTank = BasicChemicalTank.inputModern(getMaxChemical(), c -> true, null);
            storedTank.setStack(totalChem.copyWithAmount(sum));
        }
        return new AdvancedMachineUpgradeData(
                provider,
                redstone,
                getControlType(),
                getEnergyContainer(),
                progress,
                new long[tier.processes],
                storedTank,
                null,
                energySlot,
                Collections.emptyList(),
                outputSlots,
                false,
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
            if (data.stored != null && !data.stored.isEmpty()) {
                inputTank[0].setStack(data.stored.getStack());
            }

            if (data.progress != null) {
                System.arraycopy(data.progress, 0, progress, 0, Math.min(data.progress.length, progress.length));
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

    @Override
    public @NotNull IMekanismRecipeTypeProvider<SingleChemicalRecipeInput, ChemicalSolidifierRecipe, SingleChemical<ChemicalSolidifierRecipe>> getRecipeType() {
        return MCIRecipeTypes.SOLIDIFYING;
    }

    @Override
    public @Nullable IRecipeViewerRecipeType<ChemicalSolidifierRecipe> recipeViewerType() {
        return null;
    }

    @Override
    public @Nullable ChemicalSolidifierRecipe getRecipe(int cacheIndex) {
        if (cacheIndex >= 0 && cacheIndex < tier.processes) {
            return getRecipeType().getInputCache().findFirstRecipe(level, inputTank[cacheIndex].getStack());
        }
        return null;
    }

    @Override
    public @NotNull CachedRecipe<ChemicalSolidifierRecipe> createNewCachedRecipe(@NotNull ChemicalSolidifierRecipe recipe, int cacheIndex) {
        return new com.complexindustries.mekanism.recipe.SolidifyingCachedRecipe(recipe, () -> false,
                mekanism.api.recipes.inputs.InputHelper.getInputHandler(inputTank[cacheIndex], mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT),
                mekanism.api.recipes.outputs.OutputHelper.getOutputHandler(outputSlots.get(cacheIndex), mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE));
    }
}
