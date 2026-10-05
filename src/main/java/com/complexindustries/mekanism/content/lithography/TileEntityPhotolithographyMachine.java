package com.complexindustries.mekanism.content.lithography;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import mekanism.common.tile.component.TileComponentUpgrade;
import com.complexindustries.mekanism.content.block.BlockPhotolithographyMachine;
import com.complexindustries.mekanism.recipe.PhotolithographyRecipe;
import com.complexindustries.mekanism.recipe.cache.PhotolithographyCachedRecipe;
import com.complexindustries.mekanism.recipe.input.PhotolithographyRecipeInput;
import com.complexindustries.mekanism.registration.MCIBlocks;
import com.complexindustries.mekanism.registration.MCIChemicals;
import com.complexindustries.mekanism.registration.MCIItems;
import com.complexindustries.mekanism.registration.MCIRecipeTypes;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.Upgrade;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.ILongInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.lookup.IRecipeLookupHandler;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.interfaces.IHasDumpButton;
import mekanism.common.tile.prefab.TileEntityProgressMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityPhotolithographyMachine extends TileEntityProgressMachine<PhotolithographyRecipe>
        implements IRecipeLookupHandler<PhotolithographyRecipe> {

    private static final List<RecipeError> TRACKED_ERROR_TYPES = List.of(
            RecipeError.NOT_ENOUGH_ENERGY,
            RecipeError.NOT_ENOUGH_INPUT,
            RecipeError.NOT_ENOUGH_SECONDARY_INPUT,
            RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
            RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
    );

    public static final long MAX_CHEMICAL = 1_000L;
    public static final long MIN_OPERATING_CHEMICAL = 800L;
    public static final int BASE_TICKS_REQUIRED = 1100; // 55 seconds default at lowest laser power
    public static final int MIN_TICKS_REQUIRED = 100;   // 5 seconds at highest laser power
    public static final int MAX_TICKS_REQUIRED = 1100;  // 55 seconds at lowest laser power

    public IChemicalTank chemicalTank;
    public InputInventorySlot inputSlot;
    public InputInventorySlot maskSlot;
    public OutputInventorySlot outputSlot;
    public ChemicalInventorySlot secondarySlot;
    public EnergyInventorySlot energySlot;

    private MachineEnergyContainer<TileEntityPhotolithographyMachine> energyContainer;
    private final IInputHandler<@NotNull ItemStack> itemInputHandler;
    private final IInputHandler<@NotNull ItemStack> maskInputHandler;
    private final ILongInputHandler<@NotNull ChemicalStack> chemicalInputHandler;
    private final IOutputHandler<@NotNull ItemStack> outputHandler;

    private double nitrogenLossBuffer = 0.0;
    private long laserEnergyAccumulator = 0L;
    private long currentLaserEnergy = 0L;
    private int laserActiveTicks = 0;

    private final ILaserReceptor laserReceptor = new ILaserReceptor() {
        @Override
        public void receiveLaserEnergy(long energy) {
            laserEnergyAccumulator += energy;
        }

        @Override
        public boolean canLasersDig() {
            return false;
        }
    };

    public TileEntityPhotolithographyMachine(BlockPos pos, BlockState state) {
        super(MCIBlocks.PHOTOLITHOGRAPHY_MACHINE, pos, state, TRACKED_ERROR_TYPES, BASE_TICKS_REQUIRED);
        if (this.upgradeComponent == null) {
            this.upgradeComponent = new TileComponentUpgrade(this);
        }
        configComponent.setupItemIOExtraConfig(inputSlot, outputSlot, secondarySlot, energySlot);
        configComponent.setupInputConfig(TransmissionType.CHEMICAL, chemicalTank);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);

        itemInputHandler = InputHelper.getInputHandler(inputSlot, RecipeError.NOT_ENOUGH_INPUT);
        maskInputHandler = InputHelper.getInputHandler(maskSlot, RecipeError.NOT_ENOUGH_INPUT);
        chemicalInputHandler = InputHelper.getInputHandler(chemicalTank, RecipeError.NOT_ENOUGH_SECONDARY_INPUT);
        outputHandler = OutputHelper.getOutputHandler(outputSlot, RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
    }

    @NotNull
    @Override
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        ChemicalTankHelper builder = ChemicalTankHelper.forSideWithConfig(this);
        builder.addTank(chemicalTank = BasicChemicalTank.createModern(MAX_CHEMICAL, ConstantPredicates.alwaysTrueBi(),
                (chem, automationType) -> containsChemical(chem), this::containsChemical, recipeCacheListener));
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
                item -> containsItem(item),
                this::containsItem,
                recipeCacheListener, 54, 42))
                .tracksWarnings(slot -> slot.warning(WarningType.NO_MATCHING_RECIPE, getWarningCheck(RecipeError.NOT_ENOUGH_INPUT)));

        builder.addSlot(maskSlot = InputInventorySlot.at(
                this::isPhotomask,
                this::isPhotomask,
                recipeCacheListener, 54, 18))
                .tracksWarnings(slot -> slot.warning(WarningType.NO_MATCHING_RECIPE, getWarningCheck(RecipeError.NOT_ENOUGH_INPUT)));

        builder.addSlot(outputSlot = OutputInventorySlot.at(recipeCacheUnpauseListener, 116, 42))
                .tracksWarnings(slot -> slot.warning(WarningType.NO_SPACE_IN_OUTPUT, getWarningCheck(RecipeError.NOT_ENOUGH_OUTPUT_SPACE)));

        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 141, 20));
        energySlot.setSlotOverlay(SlotOverlay.POWER);
        return builder.build();
    }

    private boolean isPhotomask(ItemStack stack) {
        return stack.is(MCIItems.CALCULATION_MASK.get()) || stack.is(MCIItems.LOGIC_MASK.get())
                || stack.getItem().getDescriptionId().contains("mask");
    }

    private boolean containsItem(ItemStack stack) {
        return stack.is(MCIItems.BLANK_SILICON_WAFER.get()) || containsRecipe(stack);
    }

    private boolean containsChemical(ChemicalStack stack) {
        return stack.is(MCIChemicals.NITROGEN.get()) || isNitrogen(stack);
    }

    private boolean isNitrogen(ChemicalStack stack) {
        return stack.is(MCIChemicals.NITROGEN.get()) || stack.getChemical().getRegistryName().getPath().contains("nitrogen");
    }

    public boolean isLaserInputSide(@Nullable Direction side) {
        // Only the designated optical input side (relative right side) can receive UV laser
        return side == null || side == getDirection().getClockWise();
    }

    public @Nullable ILaserReceptor getLaserReceptor(@Nullable Direction side) {
        return isLaserInputSide(side) ? laserReceptor : null;
    }

    public ILaserReceptor getLaserReceptor() {
        return laserReceptor;
    }

    public boolean hasActiveLaser() {
        return laserActiveTicks > 0 && currentLaserEnergy > 0;
    }

    public long getCurrentLaserEnergy() {
        return currentLaserEnergy;
    }

    public double getExposureDurationSeconds() {
        return ticksRequired / 20.0;
    }

    @Override
    public boolean canFunction() {
        return super.canFunction() && chemicalTank != null && chemicalTank.getStored() >= MIN_OPERATING_CHEMICAL && hasActiveLaser();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        secondarySlot.fillTankOrConvert();

        // 1. Process UV Laser input on right face
        long incomingLaser = laserEnergyAccumulator;
        laserEnergyAccumulator = 0;
        if (incomingLaser > 0) {
            currentLaserEnergy = incomingLaser;
            laserActiveTicks = 4;
        } else if (laserActiveTicks > 0) {
            laserActiveTicks--;
        } else {
            currentLaserEnergy = 0;
        }

        // 2. Adjust exposure speed based on laser power (5s = 100 ticks, 55s = 1100 ticks)
        if (hasActiveLaser()) {
            double ratio = Math.min(1.0, Math.max(0.0, (currentLaserEnergy - 100.0) / 9900.0));
            ticksRequired = (int) Math.round(MAX_TICKS_REQUIRED - (MAX_TICKS_REQUIRED - MIN_TICKS_REQUIRED) * ratio);
        } else {
            ticksRequired = MAX_TICKS_REQUIRED;
        }

        // 3. Environmental dissipation (0.1% per tick)
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


    @Override
    public void setActive(boolean active) {
        super.setActive(active);
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            if (state.hasProperty(BlockPhotolithographyMachine.ACTIVE) && state.getValue(BlockPhotolithographyMachine.ACTIVE) != active) {
                level.setBlock(worldPosition, state.setValue(BlockPhotolithographyMachine.ACTIVE, active), 3);
            }
        }
    }

    @Override
    public boolean getActive() {
        if (isRemote()) {
            BlockState state = getBlockState();
            if (state.hasProperty(BlockPhotolithographyMachine.ACTIVE)) {
                return state.getValue(BlockPhotolithographyMachine.ACTIVE);
            }
        }
        return super.getActive();
    }


    @NotNull
    @Override
    public mekanism.common.recipe.IMekanismRecipeTypeProvider<PhotolithographyRecipeInput, PhotolithographyRecipe, com.complexindustries.mekanism.recipe.cache.PhotolithographyInputCache> getRecipeType() {
        return MCIRecipeTypes.PHOTOLITHOGRAPHY;
    }

    @Override
    public @Nullable IRecipeViewerRecipeType<PhotolithographyRecipe> recipeViewerType() {
        return null;
    }

    @Override
    public @Nullable PhotolithographyRecipe getRecipe(int cacheIndex) {
        ItemStack item = itemInputHandler.getInput();
        ItemStack mask = maskInputHandler.getInput();
        ChemicalStack chem = chemicalInputHandler.getInput();
        if (item.isEmpty() || mask.isEmpty() || chem.isEmpty() || level == null) {
            return null;
        }
        return getRecipeType().getInputCache().findFirstRecipe(level, item, mask, chem);
    }

    public boolean containsRecipe(ItemStack item) {
        if (item.isEmpty() || level == null) {
            return false;
        }
        return level.getRecipeManager().getAllRecipesFor(MCIRecipeTypes.PHOTOLITHOGRAPHY.get())
                .stream().anyMatch(holder -> holder.value().testItem(item));
    }

    @NotNull
    @Override
    public CachedRecipe<PhotolithographyRecipe> createNewCachedRecipe(@NotNull PhotolithographyRecipe recipe, int cacheIndex) {
        return new PhotolithographyCachedRecipe(recipe, recheckAllRecipeErrors,
                itemInputHandler, maskInputHandler, chemicalInputHandler, outputHandler, chemicalTank, this)
                .setErrorsChanged(this::onErrorsChanged)
                .setCanHolderFunction(this::canFunction)
                .setActive(this::setActive)
                .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
                .setRequiredTicks(this::getTicksRequired)
                .setOnFinish(this::markForSave)
                .setOperatingTicksChanged(this::setOperatingTicks)
                .setBaselineMaxOperations(this::getOperationsPerTick);
    }

    @NotNull
    @Override
    public Set<Upgrade> getSupportedUpgrade() {
        return EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING);
    }

    public TileComponentUpgrade getUpgradeComponent() {
        return upgradeComponent;
    }

    public MachineEnergyContainer<TileEntityPhotolithographyMachine> getEnergyContainer() {
        return energyContainer;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableLong.create(this::getCurrentLaserEnergy, value -> currentLaserEnergy = value));
        container.track(SyncableBoolean.create(this::hasActiveLaser, value -> laserActiveTicks = value ? 4 : 0));
        container.track(SyncableInt.create(this::getTicksRequired, value -> ticksRequired = value));
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag nbtTags, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(nbtTags, provider);
        nbtTags.putDouble("nitrogenLossBuffer", nitrogenLossBuffer);
        nbtTags.putLong("currentLaserEnergy", currentLaserEnergy);
        nbtTags.putInt("laserActiveTicks", laserActiveTicks);
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        nitrogenLossBuffer = nbt.getDouble("nitrogenLossBuffer");
        currentLaserEnergy = nbt.getLong("currentLaserEnergy");
        laserActiveTicks = nbt.getInt("laserActiveTicks");
    }
}
