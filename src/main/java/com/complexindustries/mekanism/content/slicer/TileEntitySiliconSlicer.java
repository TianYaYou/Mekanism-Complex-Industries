package com.complexindustries.mekanism.content.slicer;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import com.complexindustries.mekanism.content.block.BlockSiliconSlicer;
import com.complexindustries.mekanism.recipe.SiliconSlicingRecipe;
import com.complexindustries.mekanism.recipe.cache.SiliconSlicingCachedRecipe;
import com.complexindustries.mekanism.registration.MCIBlocks;
import com.complexindustries.mekanism.registration.MCIChemicals;
import com.complexindustries.mekanism.registration.MCIRecipeTypes;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.Upgrade;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.ILongInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.recipes.vanilla_input.SingleItemChemicalRecipeInput;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.lookup.IDoubleRecipeLookupHandler.ItemChemicalRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.ItemChemical;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.interfaces.IHasDumpButton;
import mekanism.common.tile.prefab.TileEntityProgressMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntitySiliconSlicer extends TileEntityProgressMachine<SiliconSlicingRecipe>
        implements ItemChemicalRecipeLookupHandler<SiliconSlicingRecipe>, IHasDumpButton {

    private static final List<RecipeError> TRACKED_ERROR_TYPES = List.of(
            RecipeError.NOT_ENOUGH_ENERGY,
            RecipeError.NOT_ENOUGH_INPUT,
            RecipeError.NOT_ENOUGH_SECONDARY_INPUT,
            RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
            RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
    );

    public static final long MAX_CHEMICAL = 1_000L;
    public static final long MIN_OPERATING_CHEMICAL = 800L; // 80% threshold required to operate
    public static final int BASE_TICKS_REQUIRED = 200; // 10 seconds default

    public IChemicalTank chemicalTank;
    public InputInventorySlot inputSlot;
    public OutputInventorySlot outputSlot;
    public ChemicalInventorySlot secondarySlot;
    public EnergyInventorySlot energySlot;

    private MachineEnergyContainer<TileEntitySiliconSlicer> energyContainer;
    private final IInputHandler<@NotNull ItemStack> itemInputHandler;
    private final ILongInputHandler<@NotNull ChemicalStack> chemicalInputHandler;
    private final IOutputHandler<@NotNull ItemStack> outputHandler;

    private double nitrogenLossBuffer = 0.0;

    public TileEntitySiliconSlicer(BlockPos pos, BlockState state) {
        super(MCIBlocks.SILICON_SLICER, pos, state, TRACKED_ERROR_TYPES, BASE_TICKS_REQUIRED);
        if (this.upgradeComponent == null) {
            this.upgradeComponent = new TileComponentUpgrade(this);
        }
        configComponent.setupItemIOExtraConfig(inputSlot, outputSlot, secondarySlot, energySlot);
        configComponent.setupInputConfig(TransmissionType.CHEMICAL, chemicalTank);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);

        itemInputHandler = InputHelper.getInputHandler(inputSlot, RecipeError.NOT_ENOUGH_INPUT);
        chemicalInputHandler = InputHelper.getInputHandler(chemicalTank, RecipeError.NOT_ENOUGH_SECONDARY_INPUT);
        outputHandler = OutputHelper.getOutputHandler(outputSlot, RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
    }

    @NotNull
    @Override
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        ChemicalTankHelper builder = ChemicalTankHelper.forSideWithConfig(this);
        builder.addTank(chemicalTank = BasicChemicalTank.createModern(MAX_CHEMICAL, ConstantPredicates.alwaysTrueBi(),
                (chem, automationType) -> containsRecipeB(chem), this::containsRecipeB, recipeCacheListener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, recipeCacheUnpauseListener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this);
        builder.addSlot(secondarySlot = ChemicalInventorySlot.fillOrConvert(chemicalTank, this::getLevel, listener, 6, 56));
        secondarySlot.setSlotOverlay(SlotOverlay.MINUS);

        builder.addSlot(inputSlot = InputInventorySlot.at(
                item -> containsRecipeA(item),
                this::containsRecipeA,
                recipeCacheListener, 54, 40))
                .tracksWarnings(slot -> slot.warning(WarningType.NO_MATCHING_RECIPE, getWarningCheck(RecipeError.NOT_ENOUGH_INPUT)));

        builder.addSlot(outputSlot = OutputInventorySlot.at(recipeCacheUnpauseListener, 116, 40))
                .tracksWarnings(slot -> slot.warning(WarningType.NO_SPACE_IN_OUTPUT, getWarningCheck(RecipeError.NOT_ENOUGH_OUTPUT_SPACE)));

        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 141, 20));
        energySlot.setSlotOverlay(SlotOverlay.POWER);
        return builder.build();
    }

    @Override
    public boolean canFunction() {
        // Operational rule: Nitrogen < 80% (800 mB) does not work
        return super.canFunction() && chemicalTank != null && chemicalTank.getStored() >= MIN_OPERATING_CHEMICAL;
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        secondarySlot.fillTankOrConvert();

        // Environmental dissipation (0.1% per tick)
        if (!chemicalTank.isEmpty() && isNitrogen(chemicalTank.getStack())) {
            long nStored = chemicalTank.getStored();
            nitrogenLossBuffer += nStored * 0.001;
            long toDeduct = (long) nitrogenLossBuffer;
            if (toDeduct > 0) {
                long actualLoss = Math.min(toDeduct, nStored);
                chemicalTank.shrinkStack(actualLoss, Action.EXECUTE);
                nitrogenLossBuffer -= actualLoss;
            }
        }

        recipeCacheLookupMonitor.updateAndProcess();
        return sendUpdatePacket;
    }

    private boolean isNitrogen(ChemicalStack stack) {
        return stack.is(MCIChemicals.NITROGEN.get()) || stack.getChemical().getRegistryName().getPath().contains("nitrogen");
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
            if (state.hasProperty(BlockSiliconSlicer.ACTIVE) && state.getValue(BlockSiliconSlicer.ACTIVE) != active) {
                level.setBlock(worldPosition, state.setValue(BlockSiliconSlicer.ACTIVE, active), 3);
            }
        }
    }

    @Override
    public boolean getActive() {
        if (isRemote()) {
            BlockState state = getBlockState();
            if (state.hasProperty(BlockSiliconSlicer.ACTIVE)) {
                return state.getValue(BlockSiliconSlicer.ACTIVE);
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
    public @NotNull IMekanismRecipeTypeProvider<SingleItemChemicalRecipeInput, SiliconSlicingRecipe, ItemChemical<SiliconSlicingRecipe>> getRecipeType() {
        return MCIRecipeTypes.SILICON_SLICING;
    }

    @Override
    public @Nullable IRecipeViewerRecipeType<SiliconSlicingRecipe> recipeViewerType() {
        return null;
    }

    @Override
    public @Nullable SiliconSlicingRecipe getRecipe(int cacheIndex) {
        return findFirstRecipe(itemInputHandler, chemicalInputHandler);
    }

    @Override
    public @NotNull CachedRecipe<SiliconSlicingRecipe> createNewCachedRecipe(@NotNull SiliconSlicingRecipe recipe, int cacheIndex) {
        return new SiliconSlicingCachedRecipe(recipe, recheckAllRecipeErrors, itemInputHandler, chemicalInputHandler, outputHandler, chemicalTank)
                .setErrorsChanged(this::onErrorsChanged)
                .setCanHolderFunction(this::canFunction)
                .setActive(this::setActive)
                .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
                .setRequiredTicks(this::getTicksRequired)
                .setOnFinish(this::markForSave)
                .setOperatingTicksChanged(this::setOperatingTicks)
                .setBaselineMaxOperations(this::getOperationsPerTick);
    }

    public MachineEnergyContainer<TileEntitySiliconSlicer> getEnergyContainer() {
        return energyContainer;
    }

    public TileComponentUpgrade getUpgradeComponent() {
        return upgradeComponent;
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putDouble("nitrogenLossBuffer", nitrogenLossBuffer);
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("nitrogenLossBuffer")) {
            nitrogenLossBuffer = tag.getDouble("nitrogenLossBuffer");
        }
    }
}
