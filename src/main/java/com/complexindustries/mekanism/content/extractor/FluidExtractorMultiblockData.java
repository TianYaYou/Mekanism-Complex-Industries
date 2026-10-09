package com.complexindustries.mekanism.content.extractor;

import com.complexindustries.mekanism.registration.MCIBlocks;
import com.complexindustries.mekanism.registration.MCIFluids;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.capabilities.energy.VariableCapacityEnergyContainer;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

public class FluidExtractorMultiblockData extends MultiblockData implements IValveHandler {

    public static final int BASE_PUMP_RATE = 200; // 200 mB/tick per pump = 4000 mB/s
    public static final long BASE_ENERGY_PER_TICK = 200L; // 200 J/tick per pump (~80 FE/t)

    @ContainerSync
    public VariableCapacityFluidTank fluidTank;

    @ContainerSync
    public VariableCapacityEnergyContainer energyContainer;

    @ContainerSync
    public ExtractorEnvironment environment = ExtractorEnvironment.NONE;

    @ContainerSync
    public int lavaBlockCount = 0;

    @ContainerSync
    public int pumpCount = 0;

    @ContainerSync
    public int extractionRate = 0;

    @ContainerSync
    public long lastEnergyUsage = 0;

    @ContainerSync
    public boolean isOperating = false;

    public float prevScale;

    private int scanTimer = 0;
    private final List<BlockPos> pumpPositions = new ArrayList<>();

    public FluidExtractorMultiblockData(TileEntityMultiblock<?> tile) {
        super(tile);
        IContentsListener saveAndComparator = createSaveAndComparator();
        fluidTanks.add(fluidTank = VariableCapacityFluidTank.create(this, this::getTankCapacity, ConstantPredicates.alwaysTrue(), saveAndComparator));
        energyContainers.add(energyContainer = VariableCapacityEnergyContainer.input(this::getMaxEnergy, this));
    }

    public int getTankCapacity() {
        int innerW = Math.max(1, length() - 2);
        int innerL = Math.max(1, width() - 2);
        int innerH = Math.max(1, height() - 2);
        int volume = innerW * innerL * innerH;
        return Math.max(16_000, volume * 16_000);
    }

    public long getMaxEnergy() {
        return Math.max(100_000L, Math.max(1, pumpCount) * 50_000L);
    }

    public int getPumpCount() {
        return pumpCount;
    }

    @Override
    public void onCreated(Level world) {
        super.onCreated(world);
        scanEnvironment(world);
    }

    @Override
    public void remove(Level world, Structure structure) {
        if (inventoryID != null) {
            markDirty();
            MCIFluidExtractorMultiblock.EXTRACTOR_MANAGER.handleDirtyMultiblock(this);
        }
        super.remove(world, structure);
    }

    public void updatePumpPositions(Level world) {
        pumpPositions.clear();
        if (getBounds() == null) {
            pumpCount = 0;
            return;
        }
        int minY = getBounds().getMinPos().getY();
        for (BlockPos pos : locations) {
            if (pos.getY() == minY && world.getBlockState(pos).is(MCIBlocks.POWERED_PUMP.get())) {
                pumpPositions.add(pos.immutable());
            }
        }
        pumpCount = pumpPositions.size();
    }

    public void scanEnvironment(Level world) {
        updatePumpPositions(world);
        if (pumpPositions.isEmpty()) {
            environment = ExtractorEnvironment.NONE;
            lavaBlockCount = 0;
            return;
        }

        int waterFound = 0;
        int airFound = 0;
        int lavaFound = 0;

        List<BlockPos> lavaStarts = new ArrayList<>();

        for (BlockPos pumpPos : pumpPositions) {
            BlockPos below = pumpPos.below();
            FluidState fs = world.getFluidState(below);
            if (fs.is(FluidTags.LAVA)) {
                lavaFound++;
                lavaStarts.add(below);
            } else if (fs.is(FluidTags.WATER)) {
                waterFound++;
            } else if (world.getBlockState(below).isAir()) {
                airFound++;
            }
        }

        if (lavaFound > 0) {
            // Lava environment: scan connected lava blocks extending upward above extraction face
            lavaBlockCount = scanUpwardLava(world, lavaStarts);
            if (lavaBlockCount > 2048) {
                environment = ExtractorEnvironment.LAVA;
            } else {
                environment = ExtractorEnvironment.LAVA_INSUFFICIENT;
            }
        } else if (waterFound > 0) {
            environment = ExtractorEnvironment.WATER;
            lavaBlockCount = 0;
        } else if (airFound > 0) {
            environment = ExtractorEnvironment.AIR;
            lavaBlockCount = 0;
        } else {
            environment = ExtractorEnvironment.NONE;
            lavaBlockCount = 0;
        }
    }

    private int scanUpwardLava(Level world, List<BlockPos> starts) {
        if (getBounds() == null || starts.isEmpty()) {
            return 0;
        }
        int minFloorY = getBounds().getMinPos().getY() - 1;
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();

        for (BlockPos start : starts) {
            if (visited.add(start)) {
                queue.add(start);
            }
        }

        while (!queue.isEmpty() && visited.size() < 2049) {
            BlockPos current = queue.poll();
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir);
                // Scan upward & contiguous lava (at or above extraction surface)
                if (neighbor.getY() >= minFloorY && !visited.contains(neighbor)) {
                    FluidState nfs = world.getFluidState(neighbor);
                    if (nfs.is(FluidTags.LAVA)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                        if (visited.size() >= 2049) {
                            break;
                        }
                    }
                }
            }
        }
        return visited.size();
    }

    @Override
    public boolean tick(Level world) {
        boolean needsPacket = super.tick(world);

        if (world.isClientSide) {
            return needsPacket;
        }

        // Periodic environment scan every 40 ticks
        if (scanTimer++ % 40 == 0 || environment == ExtractorEnvironment.NONE) {
            scanEnvironment(world);
        }

        Fluid targetFluid = getTargetFluid();
        if (targetFluid != Fluids.EMPTY && pumpCount > 0) {
            int maxExtraction = pumpCount * BASE_PUMP_RATE;
            long energyNeeded = pumpCount * BASE_ENERGY_PER_TICK;

            // Check if tank accepts this fluid
            if (fluidTank.isEmpty() || fluidTank.getFluid().is(targetFluid)) {
                FluidStack testStack = new FluidStack(targetFluid, maxExtraction);
                FluidStack remainder = fluidTank.insert(testStack, Action.SIMULATE, AutomationType.INTERNAL);
                int accepted = maxExtraction - remainder.getAmount();

                if (accepted > 0 && energyContainer.getEnergy() >= energyNeeded) {
                    fluidTank.insert(new FluidStack(targetFluid, accepted), Action.EXECUTE, AutomationType.INTERNAL);
                    energyContainer.extract(energyNeeded, Action.EXECUTE, AutomationType.INTERNAL);
                    isOperating = true;
                    lastEnergyUsage = energyNeeded;
                    extractionRate = accepted;
                } else {
                    isOperating = false;
                    lastEnergyUsage = 0;
                    extractionRate = 0;
                }
            } else {
                isOperating = false;
                lastEnergyUsage = 0;
                extractionRate = 0;
            }
        } else {
            isOperating = false;
            lastEnergyUsage = 0;
            extractionRate = 0;
        }

        // Eject fluid from ports if any connected fluid handlers exist
        if (!fluidTank.isEmpty() && !valves.isEmpty()) {
            for (ValveData valve : valves) {
                BlockEntity target = WorldUtils.getTileEntity(world, valve.location.relative(valve.side));
                if (target != null && target.getLevel() != null) {
                    IFluidHandler handler = target.getLevel().getCapability(
                            Capabilities.FluidHandler.BLOCK,
                            valve.location.relative(valve.side),
                            valve.side.getOpposite()
                    );
                    if (handler != null) {
                        FluidStack toSend = fluidTank.extract(1000, Action.SIMULATE, AutomationType.INTERNAL);
                        if (!toSend.isEmpty()) {
                            int filled = handler.fill(toSend, IFluidHandler.FluidAction.EXECUTE);
                            if (filled > 0) {
                                fluidTank.extract(filled, Action.EXECUTE, AutomationType.INTERNAL);
                            }
                        }
                    }
                }
            }
        }

        // Sync visual rendering scale
        float scale = MekanismUtils.getScale(prevScale, fluidTank);
        if (MekanismUtils.scaleChanged(scale, prevScale)) {
            prevScale = scale;
            needsPacket = true;
        }

        return needsPacket;
    }

    public Fluid getTargetFluid() {
        return switch (environment) {
            case WATER -> Fluids.WATER;
            case AIR -> MCIFluids.SOURCE_LIQUID_AIR.get();
            case LAVA -> Fluids.LAVA;
            default -> Fluids.EMPTY;
        };
    }

    @Override
    public void readUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.readUpdateTag(tag, provider);
        NBTUtils.setFloatIfPresent(tag, SerializationConstants.SCALE, scale -> prevScale = scale);
        if (tag.contains("fluidStack")) {
            fluidTank.setStack(FluidStack.parseOptional(provider, tag.getCompound("fluidStack")));
        } else {
            fluidTank.setEmpty();
        }
        readValves(tag);
        if (tag.contains("env")) {
            environment = ExtractorEnvironment.values()[tag.getByte("env")];
        }
        lavaBlockCount = tag.getInt("lavaCount");
        pumpCount = tag.getInt("pumps");
        isOperating = tag.getBoolean("active");
    }

    @Override
    public void writeUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.writeUpdateTag(tag, provider);
        tag.putFloat(SerializationConstants.SCALE, prevScale);
        if (!fluidTank.isEmpty()) {
            tag.put("fluidStack", fluidTank.getFluid().saveOptional(provider));
        }
        writeValves(tag);
        tag.putByte("env", (byte) environment.ordinal());
        tag.putInt("lavaCount", lavaBlockCount);
        tag.putInt("pumps", pumpCount);
        tag.putBoolean("active", isOperating);
    }

    public boolean isEmpty() {
        return fluidTank.isEmpty();
    }
}
