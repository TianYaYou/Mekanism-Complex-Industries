package com.complexindustries.mekanism.content.coater;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import com.complexindustries.mekanism.content.item.SemiFinishedChipItem;
import com.complexindustries.mekanism.recipe.ChemicalFilmCoatingRecipe;
import com.complexindustries.mekanism.recipe.cache.ChemicalFilmCoatingCachedRecipe;
import com.complexindustries.mekanism.recipe.input.ChemicalFilmCoatingRecipeInput;
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
import mekanism.common.recipe.lookup.IRecipeLookupHandler;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.prefab.TileEntityProgressMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityChemicalFilmCoater extends TileEntityProgressMachine<ChemicalFilmCoatingRecipe>
        implements IRecipeLookupHandler<ChemicalFilmCoatingRecipe>, mekanism.common.tile.interfaces.IHasDumpButton {

    private static final List<RecipeError> TRACKED_ERROR_TYPES = List.of(
            RecipeError.NOT_ENOUGH_ENERGY,
            RecipeError.NOT_ENOUGH_INPUT,
            RecipeError.NOT_ENOUGH_SECONDARY_INPUT,
            RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
            RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
    );

    public static final long MAX_CHEMICAL = 1_000L;
    public static final long MIN_OPERATING_CHEMICAL = 800L;
    public static final int BASE_TICKS_REQUIRED = 100; // 5 seconds per coating step

    public IChemicalTank chemicalTank;
    public InputInventorySlot alloySlot;
    public InputInventorySlot chipSlot;
    public OutputInventorySlot outputSlot;
    public ChemicalInventorySlot secondarySlot;
    public EnergyInventorySlot energySlot;

    private MachineEnergyContainer<TileEntityChemicalFilmCoater> energyContainer;
    private final IInputHandler<@NotNull ItemStack> alloyInputHandler;
    private final IInputHandler<@NotNull ItemStack> chipInputHandler;
    private final ILongInputHandler<@NotNull ChemicalStack> chemicalInputHandler;
    private final IOutputHandler<@NotNull ItemStack> outputHandler;

    private double nitrogenLossBuffer = 0.0;

    public TileEntityChemicalFilmCoater(BlockPos pos, BlockState state) {
        super(MCIBlocks.CHEMICAL_FILM_COATER, pos, state, TRACKED_ERROR_TYPES, BASE_TICKS_REQUIRED);
        if (this.upgradeComponent == null) {
            this.upgradeComponent = new TileComponentUpgrade(this);
        }
        configComponent.setupItemIOExtraConfig(chipSlot, outputSlot, secondarySlot, energySlot);
        configComponent.setupInputConfig(TransmissionType.CHEMICAL, chemicalTank);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);

        alloyInputHandler = InputHelper.getInputHandler(alloySlot, RecipeError.NOT_ENOUGH_INPUT);
        chipInputHandler = InputHelper.getInputHandler(chipSlot, RecipeError.NOT_ENOUGH_INPUT);
        chemicalInputHandler = InputHelper.getInputHandler(chemicalTank, RecipeError.NOT_ENOUGH_SECONDARY_INPUT);
        outputHandler = OutputHelper.getOutputHandler(outputSlot, RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
    }

    @NotNull
    @Override
    protected IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        ChemicalTankHelper builder = ChemicalTankHelper.forSideWithConfig(this);
        builder.addTank(chemicalTank = BasicChemicalTank.createModern(MAX_CHEMICAL, ConstantPredicates.alwaysTrueBi(),
                (chem, automationType) -> containsChemical(chem), this::containsChemical, recipeCacheListener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, recipeCacheListener));
        return builder.build();
    }

    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this);
        builder.addSlot(secondarySlot = ChemicalInventorySlot.fillOrConvert(chemicalTank, this::getLevel, listener, 6, 56));
        secondarySlot.setSlotOverlay(SlotOverlay.MINUS);

        builder.addSlot(alloySlot = InputInventorySlot.at(
                this::containsAlloy,
                this::containsAlloy,
                recipeCacheListener, 54, 18))
                .tracksWarnings(slot -> slot.warning(WarningType.NO_MATCHING_RECIPE, getWarningCheck(RecipeError.NOT_ENOUGH_INPUT)));

        builder.addSlot(chipSlot = InputInventorySlot.at(
                this::containsChip,
                this::containsChip,
                recipeCacheListener, 54, 42))
                .tracksWarnings(slot -> slot.warning(WarningType.NO_MATCHING_RECIPE, getWarningCheck(RecipeError.NOT_ENOUGH_INPUT)));

        builder.addSlot(outputSlot = OutputInventorySlot.at(recipeCacheUnpauseListener, 116, 42))
                .tracksWarnings(slot -> slot.warning(WarningType.NO_SPACE_IN_OUTPUT, getWarningCheck(RecipeError.NOT_ENOUGH_OUTPUT_SPACE)));

        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 141, 20));
        energySlot.setSlotOverlay(SlotOverlay.POWER);
        return builder.build();
    }

    private boolean containsAlloy(ItemStack stack) {
        if (stack.isEmpty() || level == null) return false;
        return level.getRecipeManager().getAllRecipesFor(MCIRecipeTypes.CHEMICAL_FILM_COATING.get())
                .stream().anyMatch(holder -> holder.value().testAlloy(stack));
    }

    private boolean containsChip(ItemStack stack) {
        if (stack.isEmpty() || level == null) return false;
        return level.getRecipeManager().getAllRecipesFor(MCIRecipeTypes.CHEMICAL_FILM_COATING.get())
                .stream().anyMatch(holder -> holder.value().testChip(stack));
    }

    private boolean containsChemical(ChemicalStack stack) {
        return stack.is(MCIChemicals.NITROGEN.get()) || isNitrogen(stack);
    }

    private boolean isNitrogen(ChemicalStack stack) {
        return stack.is(MCIChemicals.NITROGEN.get()) || stack.getChemical().getRegistryName().getPath().contains("nitrogen");
    }

    @Override
    public boolean canFunction() {
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

    @Override
    public void dump() {
        chemicalTank.setEmpty();
    }

    @Override
    public void setActive(boolean active) {
        super.setActive(active);
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            if (state.hasProperty(BlockChemicalFilmCoater.ACTIVE) && state.getValue(BlockChemicalFilmCoater.ACTIVE) != active) {
                level.setBlock(worldPosition, state.setValue(BlockChemicalFilmCoater.ACTIVE, active), 3);
            }
        }
    }

    @Override
    public boolean getActive() {
        if (isRemote()) {
            BlockState state = getBlockState();
            if (state.hasProperty(BlockChemicalFilmCoater.ACTIVE)) {
                return state.getValue(BlockChemicalFilmCoater.ACTIVE);
            }
        }
        return super.getActive();
    }

    @NotNull
    @Override
    public mekanism.common.recipe.IMekanismRecipeTypeProvider<ChemicalFilmCoatingRecipeInput, ChemicalFilmCoatingRecipe, com.complexindustries.mekanism.recipe.cache.ChemicalFilmCoatingInputCache> getRecipeType() {
        return MCIRecipeTypes.CHEMICAL_FILM_COATING;
    }

    @Override
    public @Nullable IRecipeViewerRecipeType<ChemicalFilmCoatingRecipe> recipeViewerType() {
        return null;
    }

    @Override
    public @Nullable ChemicalFilmCoatingRecipe getRecipe(int cacheIndex) {
        ItemStack alloy = alloyInputHandler.getInput();
        ItemStack chip = chipInputHandler.getInput();
        ChemicalStack chem = chemicalInputHandler.getInput();
        if (alloy.isEmpty() || chip.isEmpty() || chem.isEmpty() || level == null) {
            return null;
        }
        return getRecipeType().getInputCache().findFirstRecipe(level, alloy, chip, chem);
    }

    @NotNull
    @Override
    public CachedRecipe<ChemicalFilmCoatingRecipe> createNewCachedRecipe(@NotNull ChemicalFilmCoatingRecipe recipe, int cacheIndex) {
        return new ChemicalFilmCoatingCachedRecipe(recipe, recheckAllRecipeErrors,
                alloyInputHandler, chipInputHandler, chemicalInputHandler, outputHandler, chemicalTank)
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

    public MachineEnergyContainer<TileEntityChemicalFilmCoater> getEnergyContainer() {
        return energyContainer;
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag nbtTags, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(nbtTags, provider);
        nbtTags.putDouble("nitrogenLossBuffer", nitrogenLossBuffer);
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        nitrogenLossBuffer = nbt.getDouble("nitrogenLossBuffer");
    }
}
