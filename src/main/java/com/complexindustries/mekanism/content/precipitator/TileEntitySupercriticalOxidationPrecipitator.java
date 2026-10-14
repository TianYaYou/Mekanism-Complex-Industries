package com.complexindustries.mekanism.content.precipitator;

import com.complexindustries.mekanism.registration.MCIBlocks;
import com.complexindustries.mekanism.registration.MCIChemicals;
import com.complexindustries.mekanism.registration.MCIFluids;
import com.complexindustries.mekanism.registration.MCIItems;
import java.util.EnumSet;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.interfaces.IHasDumpButton;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class TileEntitySupercriticalOxidationPrecipitator extends TileEntityConfigurableMachine
        implements IHasDumpButton {

    public static final int MAX_FLUID = 10_000;
    public static final long MAX_CHEMICAL = 10_000L;
    public static final int LIQUID_OZONE_PER_OPERATION = 200; // 200 mB Liquid Ozone per powder
    public static final long AEROSOL_OUTPUT_PER_OPERATION = 2_000L; // 2,000 mB (2x) Cloudy X Complex Aerosol
    public static final int BASE_TICKS_REQUIRED = 100; // 5 seconds default
    public static final long BASE_ENERGY_PER_TICK = 200L; // 200 J/t default

    public BasicFluidTank fluidTank;
    public IChemicalTank outputChemicalTank;

    public FluidInventorySlot fluidSlot;
    public InputInventorySlot inputSlot;
    public ChemicalInventorySlot chemicalOutputSlot;
    public EnergyInventorySlot energySlot;

    private MachineEnergyContainer<TileEntitySupercriticalOxidationPrecipitator> energyContainer;

    private int operatingTicks = 0;
    private int ticksRequired = BASE_TICKS_REQUIRED;

    public TileEntitySupercriticalOxidationPrecipitator(BlockPos pos, BlockState state) {
        super(MCIBlocks.SUPERCRITICAL_OXIDATION_PRECIPITATOR, pos, state);
        if (this.upgradeComponent == null) {
            this.upgradeComponent = new TileComponentUpgrade(this);
        }
        configComponent.setupItemIOExtraConfig(inputSlot, chemicalOutputSlot, fluidSlot, energySlot);
        configComponent.setupInputConfig(TransmissionType.FLUID, fluidTank);
        configComponent.setupOutputConfig(TransmissionType.CHEMICAL, outputChemicalTank, RelativeSide.RIGHT);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM, TransmissionType.CHEMICAL);
    }

    public static boolean isLiquidOzone(FluidStack stack) {
        return !stack.isEmpty() && (stack.getFluid() == MCIFluids.SOURCE_LIQUID_OZONE.get()
                || stack.getFluid() == MCIFluids.FLOWING_LIQUID_OZONE.get());
    }

    public static boolean isValidPowder(ItemStack stack) {
        return !getAerosolOutput(stack).isEmpty();
    }

    /**
     * Maps 1x Cloudy Oxidized X Powder to 2,000 mB (2x) of the corresponding Cloudy X Complex Aerosol chemical.
     */
    @NotNull
    public static ChemicalStack getAerosolOutput(ItemStack stack) {
        if (stack.isEmpty()) {
            return ChemicalStack.EMPTY;
        }
        Item item = stack.getItem();
        if (item == MCIItems.CLOUDY_OXIDIZED_IRON_POWDER.get()) {
            return MCIChemicals.CLOUDY_IRON_COMPLEX_AEROSOL.asStack(AEROSOL_OUTPUT_PER_OPERATION);
        } else if (item == MCIItems.CLOUDY_OXIDIZED_GOLD_POWDER.get()) {
            return MCIChemicals.CLOUDY_GOLD_COMPLEX_AEROSOL.asStack(AEROSOL_OUTPUT_PER_OPERATION);
        } else if (item == MCIItems.CLOUDY_OXIDIZED_COPPER_POWDER.get()) {
            return MCIChemicals.CLOUDY_COPPER_COMPLEX_AEROSOL.asStack(AEROSOL_OUTPUT_PER_OPERATION);
        } else if (item == MCIItems.CLOUDY_OXIDIZED_OSMIUM_POWDER.get()) {
            return MCIChemicals.CLOUDY_OSMIUM_COMPLEX_AEROSOL.asStack(AEROSOL_OUTPUT_PER_OPERATION);
        } else if (item == MCIItems.CLOUDY_OXIDIZED_TIN_POWDER.get()) {
            return MCIChemicals.CLOUDY_TIN_COMPLEX_AEROSOL.asStack(AEROSOL_OUTPUT_PER_OPERATION);
        } else if (item == MCIItems.CLOUDY_OXIDIZED_LEAD_POWDER.get()) {
            return MCIChemicals.CLOUDY_LEAD_COMPLEX_AEROSOL.asStack(AEROSOL_OUTPUT_PER_OPERATION);
        } else if (item == MCIItems.CLOUDY_OXIDIZED_URANIUM_POWDER.get()) {
            return MCIChemicals.CLOUDY_URANIUM_COMPLEX_AEROSOL.asStack(AEROSOL_OUTPUT_PER_OPERATION);
        }

        // Fallback by registry path if needed
        ResourceLocation rl = BuiltInRegistries.ITEM.getKey(item);
        String path = rl.getPath();
        if (path.equals("cloudy_oxidized_iron_powder")) {
            return MCIChemicals.CLOUDY_IRON_COMPLEX_AEROSOL.asStack(AEROSOL_OUTPUT_PER_OPERATION);
        } else if (path.equals("cloudy_oxidized_gold_powder")) {
            return MCIChemicals.CLOUDY_GOLD_COMPLEX_AEROSOL.asStack(AEROSOL_OUTPUT_PER_OPERATION);
        } else if (path.equals("cloudy_oxidized_copper_powder")) {
            return MCIChemicals.CLOUDY_COPPER_COMPLEX_AEROSOL.asStack(AEROSOL_OUTPUT_PER_OPERATION);
        } else if (path.equals("cloudy_oxidized_osmium_powder")) {
            return MCIChemicals.CLOUDY_OSMIUM_COMPLEX_AEROSOL.asStack(AEROSOL_OUTPUT_PER_OPERATION);
        } else if (path.equals("cloudy_oxidized_tin_powder")) {
            return MCIChemicals.CLOUDY_TIN_COMPLEX_AEROSOL.asStack(AEROSOL_OUTPUT_PER_OPERATION);
        } else if (path.equals("cloudy_oxidized_lead_powder")) {
            return MCIChemicals.CLOUDY_LEAD_COMPLEX_AEROSOL.asStack(AEROSOL_OUTPUT_PER_OPERATION);
        } else if (path.equals("cloudy_oxidized_uranium_powder")) {
            return MCIChemicals.CLOUDY_URANIUM_COMPLEX_AEROSOL.asStack(AEROSOL_OUTPUT_PER_OPERATION);
        }

        return ChemicalStack.EMPTY;
    }

    @NotNull
    @Override
    protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
        FluidTankHelper builder = FluidTankHelper.forSideWithConfig(this);
        builder.addTank(fluidTank = BasicFluidTank.input(MAX_FLUID, TileEntitySupercriticalOxidationPrecipitator::isLiquidOzone, listener));
        return builder.build();
    }

    @NotNull
    @Override
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener) {
        ChemicalTankHelper builder = ChemicalTankHelper.forSideWithConfig(this);
        builder.addTank(outputChemicalTank = BasicChemicalTank.output(MAX_CHEMICAL, listener));
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
        builder.addSlot(fluidSlot = FluidInventorySlot.fill(fluidTank, listener, 8, 56));
        fluidSlot.setSlotOverlay(SlotOverlay.MINUS);

        builder.addSlot(inputSlot = InputInventorySlot.at(
                TileEntitySupercriticalOxidationPrecipitator::isValidPowder,
                TileEntitySupercriticalOxidationPrecipitator::isValidPowder,
                listener, 54, 35));

        builder.addSlot(chemicalOutputSlot = ChemicalInventorySlot.drain(outputChemicalTank, listener, 134, 56));
        chemicalOutputSlot.setSlotOverlay(SlotOverlay.PLUS);

        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 143, 18));
        energySlot.setSlotOverlay(SlotOverlay.POWER);
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        fluidSlot.fillTank();
        chemicalOutputSlot.drainTank();

        boolean active = false;
        if (canFunction()) {
            ItemStack inStack = inputSlot.getStack();
            ChemicalStack outAerosol = getAerosolOutput(inStack);

            if (!outAerosol.isEmpty()) {
                boolean hasOzone = fluidTank.getFluidAmount() >= LIQUID_OZONE_PER_OPERATION && isLiquidOzone(fluidTank.getFluid());
                boolean canFitOutput = outputChemicalTank.insert(outAerosol, Action.SIMULATE, AutomationType.INTERNAL).isEmpty();

                if (hasOzone && canFitOutput) {
                    long energyPerTick = MekanismUtils.getEnergyPerTick(this, BASE_ENERGY_PER_TICK);
                    if (energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL) >= energyPerTick) {
                        energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
                        operatingTicks++;
                        active = true;

                        if (operatingTicks >= ticksRequired) {
                            inputSlot.shrinkStack(1, Action.EXECUTE);
                            fluidTank.extract(LIQUID_OZONE_PER_OPERATION, Action.EXECUTE, AutomationType.INTERNAL);
                            outputChemicalTank.insert(outAerosol.copy(), Action.EXECUTE, AutomationType.INTERNAL);
                            operatingTicks = 0;
                            markForSave();
                        }
                    }
                }
            } else {
                operatingTicks = 0;
            }
        }

        setActive(active);
        return sendUpdatePacket;
    }

    @Override
    public void dump() {
        if (!outputChemicalTank.isEmpty()) {
            outputChemicalTank.setEmpty();
        } else if (!fluidTank.isEmpty()) {
            fluidTank.setEmpty();
        }
    }

    @Override
    public void setActive(boolean active) {
        super.setActive(active);
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            if (state.hasProperty(BlockSupercriticalOxidationPrecipitator.ACTIVE)
                    && state.getValue(BlockSupercriticalOxidationPrecipitator.ACTIVE) != active) {
                level.setBlock(worldPosition, state.setValue(BlockSupercriticalOxidationPrecipitator.ACTIVE, active), 3);
            }
        }
    }

    @Override
    public boolean getActive() {
        if (isRemote()) {
            BlockState state = getBlockState();
            if (state.hasProperty(BlockSupercriticalOxidationPrecipitator.ACTIVE)) {
                return state.getValue(BlockSupercriticalOxidationPrecipitator.ACTIVE);
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

    public int getOperatingTicks() {
        return operatingTicks;
    }

    public int getTicksRequired() {
        return ticksRequired;
    }

    public double getScaledProgress() {
        if (ticksRequired <= 0) {
            return 0.0;
        }
        return Math.min(1.0, (double) operatingTicks / ticksRequired);
    }

    public MachineEnergyContainer<TileEntitySupercriticalOxidationPrecipitator> getEnergyContainer() {
        return energyContainer;
    }

    public TileComponentUpgrade getUpgradeComponent() {
        return upgradeComponent;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInt.create(this::getOperatingTicks, val -> operatingTicks = val));
        container.track(SyncableInt.create(this::getTicksRequired, val -> ticksRequired = val));
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag nbtTags, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(nbtTags, provider);
        nbtTags.putInt("operatingTicks", operatingTicks);
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        if (nbt.contains("operatingTicks")) {
            operatingTicks = nbt.getInt("operatingTicks");
        }
    }
}
