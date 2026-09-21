package com.complexindustries.mekanism.content.extractor;

import com.complexindustries.mekanism.registration.MCIBlocks;
import com.complexindustries.mekanism.registration.MCIFluids;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
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
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.chunkloading.IChunkLoader;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.tile.component.TileComponentChunkLoader;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class TileEntityCrudeOilExtractor extends TileEntityConfigurableMachine implements IChunkLoader {

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

    private final TileComponentChunkLoader<TileEntityCrudeOilExtractor> chunkLoader;

    public int operatingTicks;
    public int ticksRequired = BASE_TICKS_REQUIRED;
    private ExtractorStatus status = ExtractorStatus.IDLE;

    private boolean running = true;
    private int radius = 16;
    private int minY = -64;
    private int maxY = 64;
    private int extractedCount = 0;

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

        chunkLoader = new TileComponentChunkLoader<>(this);
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
    public TileComponentChunkLoader<TileEntityCrudeOilExtractor> getChunkLoader() {
        return chunkLoader;
    }

    @Override
    public Set<ChunkPos> getChunkSet() {
        Set<ChunkPos> set = new HashSet<>();
        set.add(new ChunkPos(getBlockPos()));
        int minChunkX = SectionPos.blockToSectionCoord(getBlockPos().getX() - radius);
        int maxChunkX = SectionPos.blockToSectionCoord(getBlockPos().getX() + radius);
        int minChunkZ = SectionPos.blockToSectionCoord(getBlockPos().getZ() - radius);
        int maxChunkZ = SectionPos.blockToSectionCoord(getBlockPos().getZ() + radius);
        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                set.add(new ChunkPos(cx, cz));
            }
        }
        return set;
    }

    public boolean isRunning() {
        return running;
    }

    public int getRadius() {
        return radius;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getExtractedCount() {
        return extractedCount;
    }

    public void start() {
        this.running = true;
        markForSave();
    }

    public void stop() {
        this.running = false;
        setActive(false);
        this.status = ExtractorStatus.STOPPED;
        markForSave();
    }

    public void reset() {
        this.scanY = maxY;
        this.scanIndex = 0;
        this.currentTarget = null;
        this.scanDelay = 0;
        markForSave();
    }

    public void setRadius(int r) {
        this.radius = Math.max(0, Math.min(32, r));
        if (chunkLoader != null && !isRemote()) {
            chunkLoader.refreshChunkTickets();
        }
        reset();
    }

    public void setMinY(int y) {
        int worldMin = level != null ? level.getMinBuildHeight() : -64;
        this.minY = Math.max(worldMin, Math.min(maxY, y));
        reset();
    }

    public void setMaxY(int y) {
        int worldMax = level != null ? level.getMaxBuildHeight() : 320;
        this.maxY = Math.min(worldMax, Math.max(minY, y));
        reset();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();

        // 1. Process container drain/fill: fill water tank from bucket, drain crude oil into empty bucket
        waterInputSlot.fillTank(waterOutputSlot);
        oilInputSlot.drainTank(oilOutputSlot);

        // 2. Running state check
        if (!running) {
            setActive(false);
            status = ExtractorStatus.STOPPED;
            return;
        }

        // 3. Check redstone signal and enabled state
        if (!MekanismUtils.canFunction(this)) {
            setActive(false);
            status = ExtractorStatus.DISABLED;
            return;
        }

        // 4. Check oil tank capacity
        if (crudeOilTank.getNeeded() < OIL_PRODUCED) {
            setActive(false);
            status = ExtractorStatus.TANK_FULL;
            return;
        }

        // 5. Check water availability
        if (waterTank.getFluidAmount() < WATER_CONSUMPTION) {
            setActive(false);
            status = ExtractorStatus.NO_WATER;
            return;
        }

        // 6. Check energy availability
        FloatingLong energyNeeded = energyContainer.getEnergyPerTick();
        if (energyContainer.extract(energyNeeded, Action.SIMULATE, AutomationType.INTERNAL).smallerThan(energyNeeded)) {
            setActive(false);
            status = ExtractorStatus.NO_ENERGY;
            return;
        }

        // 7. Find or validate crude oil source
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

        // 8. All conditions satisfied -> Run extraction
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
        extractedCount++;

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
        int effMinY = Math.max(level.getMinBuildHeight(), minY);
        int effMaxY = Math.min(level.getMaxBuildHeight(), maxY);
        for (Direction dir : EnumUtils.DIRECTIONS) {
            BlockPos neighbor = pos.relative(dir);
            int dx = neighbor.getX() - center.getX();
            int dz = neighbor.getZ() - center.getZ();
            if (dx * dx + dz * dz <= radius * radius && neighbor.getY() <= effMaxY && neighbor.getY() >= effMinY) {
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
        int effMinY = Math.max(level.getMinBuildHeight(), minY);
        int effMaxY = Math.min(level.getMaxBuildHeight(), maxY);

        if (scanY < effMinY || scanY > effMaxY) {
            scanY = effMaxY;
            scanIndex = 0;
        }

        // Check up to 512 positions per tick to balance server performance and responsiveness
        int checked = 0;
        int diameter = radius * 2 + 1;
        int maxIndex = diameter * diameter;

        while (checked < 512) {
            int dx = (scanIndex % diameter) - radius;
            int dz = (scanIndex / diameter) - radius;

            if (dx * dx + dz * dz <= radius * radius) {
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
                if (scanY < effMinY) {
                    scanY = effMaxY;
                    scanDelay = 40; // Entire volume scanned without finding any oil; wait 40 ticks before rescanning
                    status = ExtractorStatus.FINISHED;
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
        container.track(SyncableBoolean.create(() -> running, val -> running = val));
        container.track(SyncableInt.create(() -> radius, val -> radius = val));
        container.track(SyncableInt.create(() -> minY, val -> minY = val));
        container.track(SyncableInt.create(() -> maxY, val -> maxY = val));
        container.track(SyncableInt.create(() -> extractedCount, val -> extractedCount = val));
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
        if (nbt.contains("running")) {
            running = nbt.getBoolean("running");
        }
        if (nbt.contains("radius")) {
            radius = nbt.getInt("radius");
        }
        if (nbt.contains("minY")) {
            minY = nbt.getInt("minY");
        }
        if (nbt.contains("maxY")) {
            maxY = nbt.getInt("maxY");
        }
        if (nbt.contains("extractedCount")) {
            extractedCount = nbt.getInt("extractedCount");
        }
        if (nbt.contains("scanY")) {
            scanY = nbt.getInt("scanY");
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
        nbt.putBoolean("running", running);
        nbt.putInt("radius", radius);
        nbt.putInt("minY", minY);
        nbt.putInt("maxY", maxY);
        nbt.putInt("extractedCount", extractedCount);
        nbt.putInt("scanY", scanY);
    }
}
