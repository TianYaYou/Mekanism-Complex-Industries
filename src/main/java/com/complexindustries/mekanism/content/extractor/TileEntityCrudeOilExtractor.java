package com.complexindustries.mekanism.content.extractor;

import com.complexindustries.mekanism.registration.MCIBlocks;
import com.complexindustries.mekanism.registration.MCIFluids;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.Upgrade;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.api.RelativeSide;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class TileEntityCrudeOilExtractor extends TileEntityConfigurableMachine {

    public static final int BASE_TICKS_REQUIRED = 40;
    public static final int WATER_CONSUMPTION = 2000;
    public static final int OIL_PRODUCED = 1000;
    public static final int SCAN_RADIUS = 16;

    public BasicFluidTank waterTank;
    public BasicFluidTank crudeOilTank;
    private MachineEnergyContainer<TileEntityCrudeOilExtractor> energyContainer;

    public FluidInventorySlot waterInputSlot;
    public OutputInventorySlot waterOutputSlot;
    public FluidInventorySlot oilInputSlot;
    public OutputInventorySlot oilOutputSlot;

    public int operatingTicks;
    public int ticksRequired = BASE_TICKS_REQUIRED;
    private ExtractorStatus status = ExtractorStatus.IDLE;

    private BlockPos currentTarget = null;
    private int scanDelay = 0;
    private int scanIndex = 0;
    private int scanY = 0;

    public TileEntityCrudeOilExtractor(BlockPos pos, BlockState state) {
        super(MCIBlocks.CRUDE_OIL_EXTRACTOR, pos, state);
        configComponent = new TileComponentConfig(this, TransmissionType.FLUID, TransmissionType.ENERGY);
        configComponent.setupIOConfig(TransmissionType.FLUID, waterTank, crudeOilTank, RelativeSide.RIGHT).setEjecting(true);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.FLUID);
    }

    @NotNull
    @Override
    protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
        FluidTankHelper builder = FluidTankHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addTank(waterTank = BasicFluidTank.input(16_000, fluid -> fluid.getFluid() == Fluids.WATER, fluid -> fluid.getFluid() == Fluids.WATER, listener));
        builder.addTank(crudeOilTank = BasicFluidTank.output(16_000, listener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, listener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(waterInputSlot = FluidInventorySlot.input(waterTank, listener, 8, 20));
        builder.addSlot(waterOutputSlot = OutputInventorySlot.at(listener, 8, 52));
        builder.addSlot(oilInputSlot = FluidInventorySlot.fill(crudeOilTank, listener, 150, 20));
        builder.addSlot(oilOutputSlot = OutputInventorySlot.at(listener, 150, 52));
        waterInputSlot.setSlotOverlay(SlotOverlay.PLUS);
        waterOutputSlot.setSlotOverlay(SlotOverlay.MINUS);
        oilInputSlot.setSlotOverlay(SlotOverlay.MINUS);
        oilOutputSlot.setSlotOverlay(SlotOverlay.PLUS);
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();

        // 1. Process container drain/fill: fill water tank from bucket, drain crude oil into empty bucket
        waterInputSlot.fillTank(waterOutputSlot);
        oilInputSlot.drainTank(oilOutputSlot);

        // 2. Check redstone signal and enabled state
        if (!MekanismUtils.canFunction(this)) {
            setActive(false);
            status = ExtractorStatus.DISABLED;
            return;
        }

        // 3. Check oil tank capacity
        if (crudeOilTank.getNeeded() < OIL_PRODUCED) {
            setActive(false);
            status = ExtractorStatus.TANK_FULL;
            return;
        }

        // 4. Check water availability
        if (waterTank.getFluidAmount() < WATER_CONSUMPTION) {
            setActive(false);
            status = ExtractorStatus.NO_WATER;
            return;
        }

        // 5. Check energy availability
        FloatingLong energyNeeded = energyContainer.getEnergyPerTick();
        if (energyContainer.extract(energyNeeded, Action.SIMULATE, AutomationType.INTERNAL).smallerThan(energyNeeded)) {
            setActive(false);
            status = ExtractorStatus.NO_ENERGY;
            return;
        }

        // 6. Find or validate crude oil source
        if (currentTarget == null || !isCrudeOilSource(level, currentTarget)) {
            operatingTicks = 0;
            if (scanDelay > 0) {
                scanDelay--;
                setActive(false);
                status = ExtractorStatus.NO_OIL;
                return;
            }
            currentTarget = scanForNextSource();
            if (currentTarget == null) {
                setActive(false);
                status = ExtractorStatus.NO_OIL;
                return;
            }
        }

        // 7. All conditions satisfied -> Run extraction
        status = ExtractorStatus.EXTRACTING;
        setActive(true);
        energyContainer.extract(energyNeeded, Action.EXECUTE, AutomationType.INTERNAL);
        operatingTicks++;

        if (operatingTicks >= ticksRequired) {
            operatingTicks = 0;
            completeExtraction();
        }
    }

    private void completeExtraction() {
        if (level == null || currentTarget == null) return;

        // Precondition check: Ensure resources are still available to prevent fluid duplication or voiding
        if (waterTank.getFluidAmount() < WATER_CONSUMPTION || crudeOilTank.getNeeded() < OIL_PRODUCED) {
            return;
        }

        // Drain water and produce crude oil
        waterTank.extract(WATER_CONSUMPTION, Action.EXECUTE, AutomationType.INTERNAL);
        crudeOilTank.insert(new FluidStack(MCIFluids.CRUDE_OIL_SOURCE.get(), OIL_PRODUCED), Action.EXECUTE, AutomationType.INTERNAL);

        // Replace or remove source block in world
        boolean stoneGen = getComponent().isUpgradeInstalled(Upgrade.STONE_GENERATOR);
        BlockState replacement = stoneGen ? Blocks.COBBLESTONE.defaultBlockState() : Blocks.AIR.defaultBlockState();
        level.setBlock(currentTarget, replacement, 3);

        // Heavy hydraulic extraction sound (attenuated by muffling upgrade)
        if (!isFullyMuffled()) {
            float volume = (float) Math.max(0.1, 1.0 - 0.25 * getComponent().getUpgrades(Upgrade.MUFFLING));
            level.playSound(null, getBlockPos(), MekanismSounds.HYDRAULIC.get(), SoundSource.BLOCKS, volume, 0.8F);
        }

        BlockPos previous = currentTarget;
        currentTarget = null;
        // Check immediate neighbors within scan boundaries for continuous fluid flow
        findNextFrom(previous);
    }

    private void findNextFrom(BlockPos pos) {
        if (pos == null || level == null) return;
        BlockPos center = getBlockPos();
        for (Direction dir : EnumUtils.DIRECTIONS) {
            BlockPos neighbor = pos.relative(dir);
            int dx = neighbor.getX() - center.getX();
            int dz = neighbor.getZ() - center.getZ();
            if (dx * dx + dz * dz <= SCAN_RADIUS * SCAN_RADIUS && neighbor.getY() < center.getY() && neighbor.getY() >= level.getMinBuildHeight()) {
                if (isCrudeOilSource(level, neighbor)) {
                    currentTarget = neighbor;
                    return;
                }
            }
        }
    }

    private BlockPos scanForNextSource() {
        if (level == null) return null;

        BlockPos center = getBlockPos();
        int minY = level.getMinBuildHeight();
        int maxY = center.getY() - 1;

        if (scanY < minY || scanY > maxY) {
            scanY = maxY;
            scanIndex = 0;
        }

        // Check up to 512 positions per tick to balance server performance and responsiveness
        int checked = 0;
        int diameter = SCAN_RADIUS * 2 + 1;
        int maxIndex = diameter * diameter;

        while (checked < 512) {
            int dx = (scanIndex % diameter) - SCAN_RADIUS;
            int dz = (scanIndex / diameter) - SCAN_RADIUS;

            if (dx * dx + dz * dz <= SCAN_RADIUS * SCAN_RADIUS) {
                BlockPos checkPos = new BlockPos(center.getX() + dx, scanY, center.getZ() + dz);
                if (isCrudeOilSource(level, checkPos)) {
                    return checkPos;
                }
            }

            scanIndex++;
            checked++;

            if (scanIndex >= maxIndex) {
                scanIndex = 0;
                scanY--;
                if (scanY < minY) {
                    scanY = maxY;
                    scanDelay = 40; // Entire volume scanned without finding any oil; wait 40 ticks before rescanning
                    return null;
                }
            }
        }
        return null;
    }

    public static boolean isCrudeOilSource(Level level, BlockPos pos) {
        if (level == null || !level.isLoaded(pos)) return false;
        BlockState state = level.getBlockState(pos);
        FluidState fluid = state.getFluidState();
        return !fluid.isEmpty() && fluid.isSource() &&
                (fluid.getType() == MCIFluids.CRUDE_OIL_SOURCE.get() || state.getBlock() == MCIBlocks.CRUDE_OIL_BLOCK.get());
    }

    @Override
    public void recalculateUpgrades(Upgrade upgradeType) {
        super.recalculateUpgrades(upgradeType);
        if (upgradeType == Upgrade.SPEED || upgradeType == Upgrade.ENERGY) {
            this.ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
            this.energyContainer.updateEnergyPerTick();
            this.energyContainer.updateMaxEnergy();
        }
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInt.create(() -> operatingTicks, val -> operatingTicks = val));
        container.track(SyncableInt.create(() -> ticksRequired, val -> ticksRequired = val));
        container.track(SyncableEnum.create(ExtractorStatus::byIndexStatic, ExtractorStatus.IDLE, () -> status, val -> status = val));
    }

    public double getScaledProgress() {
        return ticksRequired > 0 ? (double) operatingTicks / (double) ticksRequired : 0.0;
    }

    public MachineEnergyContainer<TileEntityCrudeOilExtractor> getEnergyContainer() {
        return energyContainer;
    }

    public ExtractorStatus getExtractorStatus() {
        return status;
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        operatingTicks = nbt.getInt("operatingTicks");
        if (nbt.contains("ticksRequired")) {
            ticksRequired = nbt.getInt("ticksRequired");
        }
        if (nbt.contains("targetPos")) {
            currentTarget = NbtUtils.readBlockPos(nbt.getCompound("targetPos"));
        }
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putInt("operatingTicks", operatingTicks);
        nbt.putInt("ticksRequired", ticksRequired);
        if (currentTarget != null) {
            nbt.put("targetPos", NbtUtils.writeBlockPos(currentTarget));
        }
    }
}
