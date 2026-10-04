package com.complexindustries.mekanism.content.chamber;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import com.complexindustries.mekanism.content.block.CrystalGrowthChamberBlock;
import com.complexindustries.mekanism.recipe.CrystalGrowthRecipe;
import com.complexindustries.mekanism.recipe.cache.CrystalGrowthCachedRecipe;
import com.complexindustries.mekanism.recipe.cache.ItemDoubleChemicalInputCache;
import com.complexindustries.mekanism.recipe.input.ItemBiChemicalRecipeInput;
import com.complexindustries.mekanism.registration.MCIBlocks;
import com.complexindustries.mekanism.registration.MCIRecipeTypes;
import mekanism.api.Action;
import mekanism.api.AutomationType;
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
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.interfaces.IHasDumpButton;
import mekanism.common.tile.prefab.TileEntityProgressMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import mekanism.api.SerializationConstants;
import mekanism.api.RelativeSide;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo;
import mekanism.common.util.EnumUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityCrystalGrowthChamber extends TileEntityProgressMachine<CrystalGrowthRecipe>
        implements IHasDumpButton {

    private static final List<RecipeError> TRACKED_ERROR_TYPES = List.of(
            RecipeError.NOT_ENOUGH_ENERGY,
            RecipeError.NOT_ENOUGH_INPUT,
            RecipeError.NOT_ENOUGH_SECONDARY_INPUT,
            RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
            RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
    );

    public static final long MAX_CHEMICAL = 10_000L;
    public static final int BASE_TICKS_REQUIRED = 200;
    public static final long BASE_ENERGY_PER_TICK = 400L;

    public IChemicalTank chemicalTankA;
    public IChemicalTank chemicalTankB;
    public InputInventorySlot inputSlot;
    public OutputInventorySlot outputSlot;
    public EnergyInventorySlot energySlot;

    public float clientProgress = 0.0f;
    public float prevClientProgress = 0.0f;

    private MachineEnergyContainer<TileEntityCrystalGrowthChamber> energyContainer;
    private final IInputHandler<@NotNull ItemStack> itemInputHandler;
    private final ILongInputHandler<@NotNull ChemicalStack> chemicalInputHandlerA;
    private final ILongInputHandler<@NotNull ChemicalStack> chemicalInputHandlerB;
    private final IOutputHandler<@NotNull ItemStack> outputHandler;

    public TileEntityCrystalGrowthChamber(BlockPos pos, BlockState state) {
        super(MCIBlocks.CRYSTAL_GROWTH_CHAMBER, pos, state, TRACKED_ERROR_TYPES, BASE_TICKS_REQUIRED);
        if (this.upgradeComponent == null) {
            this.upgradeComponent = new TileComponentUpgrade(this);
        }
        configComponent.setupItemIOConfig(inputSlot, outputSlot, energySlot);
        ConfigInfo itemConfig = configComponent.getConfig(TransmissionType.ITEM);
        if (itemConfig != null) {
            for (RelativeSide side : EnumUtils.SIDES) {
                itemConfig.setDataType(DataType.INPUT, side);
            }
            itemConfig.setDataType(DataType.OUTPUT, RelativeSide.RIGHT);
            itemConfig.setDataType(DataType.ENERGY, RelativeSide.BACK);
        }

        ConfigInfo gasConfig = configComponent.getConfig(TransmissionType.CHEMICAL);
        if (gasConfig != null) {
            gasConfig.addSlotInfo(DataType.INPUT_1, new ChemicalSlotInfo(true, false, chemicalTankA));
            gasConfig.addSlotInfo(DataType.INPUT_2, new ChemicalSlotInfo(true, false, chemicalTankB));
            gasConfig.addSlotInfo(DataType.INPUT_OUTPUT, new ChemicalSlotInfo(true, false, chemicalTankA, chemicalTankB));
            gasConfig.setDataType(DataType.INPUT_1, RelativeSide.LEFT);
            gasConfig.setDataType(DataType.INPUT_2, RelativeSide.RIGHT);
            gasConfig.setCanEject(false);
        }

        ConfigInfo energyConfig = configComponent.getConfig(TransmissionType.ENERGY);
        if (energyConfig != null) {
            configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);
            for (RelativeSide side : EnumUtils.SIDES) {
                energyConfig.setDataType(DataType.INPUT, side);
            }
        }

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);

        itemInputHandler = InputHelper.getInputHandler(inputSlot, RecipeError.NOT_ENOUGH_INPUT);
        chemicalInputHandlerA = InputHelper.getInputHandler(chemicalTankA, RecipeError.NOT_ENOUGH_SECONDARY_INPUT);
        chemicalInputHandlerB = InputHelper.getInputHandler(chemicalTankB, RecipeError.NOT_ENOUGH_SECONDARY_INPUT);
        outputHandler = OutputHelper.getOutputHandler(outputSlot, RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
    }

    @NotNull
    @Override
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        ChemicalTankHelper builder = ChemicalTankHelper.forSideWithConfig(this);
        builder.addTank(chemicalTankA = BasicChemicalTank.inputModern(MAX_CHEMICAL,
                this::containsChemicalA, this::containsChemical, recipeCacheListener));
        builder.addTank(chemicalTankB = BasicChemicalTank.inputModern(MAX_CHEMICAL,
                this::containsChemicalB, this::containsChemical, recipeCacheListener));
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

        builder.addSlot(inputSlot = InputInventorySlot.at(
                this::containsItem,
                this::containsItem,
                recipeCacheListener, 54, 40))
                .tracksWarnings(slot -> slot.warning(WarningType.NO_MATCHING_RECIPE, getWarningCheck(RecipeError.NOT_ENOUGH_INPUT)));

        builder.addSlot(outputSlot = OutputInventorySlot.at(recipeCacheUnpauseListener, 116, 40))
                .tracksWarnings(slot -> slot.warning(WarningType.NO_SPACE_IN_OUTPUT, getWarningCheck(RecipeError.NOT_ENOUGH_OUTPUT_SPACE)));

        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 141, 20));
        energySlot.setSlotOverlay(SlotOverlay.POWER);
        return builder.build();
    }

    public boolean containsItem(ItemStack stack) {
        return level != null && getRecipeType().getInputCache().containsInputA(level, stack);
    }

    public boolean containsChemicalA(ChemicalStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ChemicalStack other = chemicalTankB != null ? chemicalTankB.getStack() : ChemicalStack.EMPTY;
        if (!other.isEmpty()) {
            return containsChemicalPair(stack, other);
        }
        return containsChemical(stack);
    }

    public boolean containsChemicalB(ChemicalStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ChemicalStack other = chemicalTankA != null ? chemicalTankA.getStack() : ChemicalStack.EMPTY;
        if (!other.isEmpty()) {
            return containsChemicalPair(stack, other);
        }
        return containsChemical(stack);
    }

    public boolean containsChemicalPair(ChemicalStack chemA, ChemicalStack chemB) {
        if (chemA.isEmpty() || chemB.isEmpty() || ChemicalStack.isSameChemical(chemA, chemB)) {
            return false;
        }
        if (level == null) {
            return true;
        }
        ItemStack item = inputSlot != null ? inputSlot.getStack() : ItemStack.EMPTY;
        for (var recipeHolder : getRecipeType().getRecipes(level)) {
            CrystalGrowthRecipe recipe = recipeHolder.value();
            if (!item.isEmpty() && !recipe.getItemInput().test(item)) {
                continue;
            }
            if ((recipe.getChemicalInputA().test(chemA) && recipe.getChemicalInputB().test(chemB))
                    || (recipe.getChemicalInputA().test(chemB) && recipe.getChemicalInputB().test(chemA))) {
                return true;
            }
        }
        return false;
    }

    public boolean containsChemical(ChemicalStack stack) {
        if (stack.isEmpty() || level == null) {
            return false;
        }
        ItemStack item = inputSlot != null ? inputSlot.getStack() : ItemStack.EMPTY;
        if (!item.isEmpty()) {
            for (var recipeHolder : getRecipeType().getRecipes(level)) {
                CrystalGrowthRecipe recipe = recipeHolder.value();
                if (recipe.getItemInput().test(item) && (recipe.getChemicalInputA().test(stack) || recipe.getChemicalInputB().test(stack))) {
                    return true;
                }
            }
            return false;
        }
        return getRecipeType().getInputCache().containsInputB(level, stack)
                || getRecipeType().getInputCache().containsInputC(level, stack);
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        recipeCacheLookupMonitor.updateAndProcess();
        return sendUpdatePacket;
    }

    @Override
    protected void onUpdateClient() {
        super.onUpdateClient();
        prevClientProgress = clientProgress;
        if (getActive()) {
            float target = (float) getScaledProgress();
            if (target > 0) {
                clientProgress = target;
            } else {
                clientProgress = (clientProgress + 0.005f) % 1.0f;
            }
            if (level != null && level.random.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.END_ROD,
                        worldPosition.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.25,
                        worldPosition.getY() + 0.45 + (level.random.nextDouble() - 0.5) * 0.2,
                        worldPosition.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.25,
                        0, 0.01, 0);
            }
        } else if (!outputSlot.isEmpty()) {
            clientProgress = 1.0f;
        } else {
            clientProgress = 0.0f;
        }
    }

    public float getRenderProgress(float partialTick) {
        if (!outputSlot.isEmpty()) {
            return 1.0f;
        }
        if (getActive()) {
            return Mth.lerp(partialTick, prevClientProgress, clientProgress);
        }
        return 0.0f;
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag(@NotNull HolderLookup.Provider provider) {
        CompoundTag updateTag = super.getReducedUpdateTag(provider);
        updateTag.putInt(SerializationConstants.PROGRESS, getOperatingTicks());
        updateTag.putInt("ticksRequired", ticksRequired);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(tag, lookupProvider);
        setOperatingTicks(tag.getInt(SerializationConstants.PROGRESS));
        if (tag.contains("ticksRequired")) {
            ticksRequired = tag.getInt("ticksRequired");
        }
    }

    @Override
    public void dump() {
        chemicalTankA.setEmpty();
        chemicalTankB.setEmpty();
    }

    public void dumpA() {
        chemicalTankA.setEmpty();
    }

    public void dumpB() {
        chemicalTankB.setEmpty();
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        migrateLegacyChemicalConfig();
    }

    @Override
    protected void applyImplicitComponents(@NotNull BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        migrateLegacyChemicalConfig();
    }

    private void migrateLegacyChemicalConfig() {
        ConfigInfo gasConfig = configComponent.getConfig(TransmissionType.CHEMICAL);
        if (gasConfig != null) {
            boolean allNone = true;
            for (RelativeSide side : EnumUtils.SIDES) {
                DataType current = gasConfig.getDataType(side);
                if (current == DataType.INPUT) {
                    if (side == RelativeSide.LEFT) {
                        gasConfig.setDataType(DataType.INPUT_1, side);
                    } else if (side == RelativeSide.RIGHT) {
                        gasConfig.setDataType(DataType.INPUT_2, side);
                    } else {
                        gasConfig.setDataType(DataType.INPUT_OUTPUT, side);
                    }
                    allNone = false;
                } else if (current != DataType.NONE) {
                    allNone = false;
                }
            }
            if (allNone) {
                gasConfig.setDataType(DataType.INPUT_1, RelativeSide.LEFT);
                gasConfig.setDataType(DataType.INPUT_2, RelativeSide.RIGHT);
            }
        }
    }

    @Override
    public void setActive(boolean active) {
        super.setActive(active);
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            if (state.hasProperty(CrystalGrowthChamberBlock.ACTIVE) && state.getValue(CrystalGrowthChamberBlock.ACTIVE) != active) {
                level.setBlock(worldPosition, state.setValue(CrystalGrowthChamberBlock.ACTIVE, active), 3);
            }
        }
    }

    @Override
    public boolean getActive() {
        if (isRemote()) {
            BlockState state = getBlockState();
            if (state.hasProperty(CrystalGrowthChamberBlock.ACTIVE)) {
                return state.getValue(CrystalGrowthChamberBlock.ACTIVE);
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
    public @NotNull IMekanismRecipeTypeProvider<ItemBiChemicalRecipeInput, CrystalGrowthRecipe, ItemDoubleChemicalInputCache> getRecipeType() {
        return MCIRecipeTypes.CRYSTAL_GROWTH;
    }

    @Override
    public @Nullable IRecipeViewerRecipeType<CrystalGrowthRecipe> recipeViewerType() {
        return null;
    }

    @Override
    public @Nullable CrystalGrowthRecipe getRecipe(int cacheIndex) {
        ItemStack item = inputSlot.getStack();
        ChemicalStack chemA = chemicalTankA.getStack();
        ChemicalStack chemB = chemicalTankB.getStack();
        if (item.isEmpty() || chemA.isEmpty() || chemB.isEmpty() || level == null) {
            return null;
        }
        return getRecipeType().getInputCache().findFirstRecipe(level, item, chemA, chemB);
    }

    @NotNull
    @Override
    public CachedRecipe<CrystalGrowthRecipe> createNewCachedRecipe(@NotNull CrystalGrowthRecipe recipe, int cacheIndex) {
        return new CrystalGrowthCachedRecipe(recipe, recheckAllRecipeErrors, itemInputHandler, chemicalInputHandlerA, chemicalInputHandlerB, outputHandler)
                .setErrorsChanged(this::onErrorsChanged)
                .setCanHolderFunction(this::canFunction)
                .setActive(this::setActive)
                .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
                .setRequiredTicks(this::getTicksRequired)
                .setOnFinish(this::markForSave)
                .setOperatingTicksChanged(this::setOperatingTicks)
                .setBaselineMaxOperations(this::getOperationsPerTick);
    }

    public MachineEnergyContainer<TileEntityCrystalGrowthChamber> getEnergyContainer() {
        return energyContainer;
    }

    public TileComponentUpgrade getUpgradeComponent() {
        return upgradeComponent;
    }
}
