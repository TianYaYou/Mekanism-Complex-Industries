package com.complexindustries.mekanism.content.refinery;

import com.complexindustries.mekanism.registration.MCIChemicals;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.heat.HeatAPI;
import mekanism.common.capabilities.chemical.VariableCapacityChemicalTank;
import mekanism.common.capabilities.heat.VariableHeatCapacitor;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RefineryMultiblockData extends MultiblockData implements IValveHandler {

    public static final Set<RefineryMultiblockData> FORMED_REFINERIES = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @ContainerSync
    public IChemicalTank inputChemicalTank;

    @ContainerSync
    public IChemicalTank outputTank1; // Layer 1 (Bottom): Bitumen (ratio 0.15)
    @ContainerSync
    public IChemicalTank outputTank2; // Layer 2: Heavy Oil (ratio 0.10)
    @ContainerSync
    public IChemicalTank outputTank3; // Layer 3: Refined Fuel (ratio 0.10)
    @ContainerSync
    public IChemicalTank outputTank4; // Layer 4: Naphtha (ratio 0.20)
    @ContainerSync
    public IChemicalTank outputTank5; // Layer 5 (Top): Petroleum Gas (ratio 2.00)

    public final List<IChemicalTank> outputChemicalTanks = new ArrayList<>(5);

    @ContainerSync
    public VariableHeatCapacitor bottomHeatCapacitor;

    @ContainerSync
    public VariableHeatCapacitor topHeatCapacitor;

    @ContainerSync
    public double lastCrackingRate = 0.0; // Effective cracking rate in mB/s (0.0 .. 160.0)

    @ContainerSync
    public int operatingStatus = 0; // 0=Idle/No Input, 1=Temp < 500K, 2=DeltaT < 100K, 3=Active, 4=Outputs Full

    // Partition floor heights (relative dy or absolute y)
    public int[] partitionFloors = new int[4];

    private double biomeAmbientTemp = HeatAPI.AMBIENT_TEMP;
    private double inputBuffer = 0.0;
    private double gasBuffer = 0.0;
    private double naphthaBuffer = 0.0;
    private double fuelBuffer = 0.0;
    private double heavyOilBuffer = 0.0;
    private double bitumenBuffer = 0.0;

    public RefineryMultiblockData(TileEntityMultiblock<?> tile) {
        super(tile);
        // 1 Chemical Input Tank (Layer 1) - Capacity scaled to 1/100 of original
        chemicalTanks.add(inputChemicalTank = VariableCapacityChemicalTank.input(this, this::getInputTankCapacityLong, ConstantPredicates.alwaysTrue(), this));

        // 5 Chemical Output Tanks (Layers 1..5) - Capacity scaled to 1/1000 of original (1/10 of input capacity)
        outputTank1 = VariableCapacityChemicalTank.output(this, this::getOutputTankCapacityLong, ConstantPredicates.alwaysTrue(), this);
        outputTank2 = VariableCapacityChemicalTank.output(this, this::getOutputTankCapacityLong, ConstantPredicates.alwaysTrue(), this);
        outputTank3 = VariableCapacityChemicalTank.output(this, this::getOutputTankCapacityLong, ConstantPredicates.alwaysTrue(), this);
        outputTank4 = VariableCapacityChemicalTank.output(this, this::getOutputTankCapacityLong, ConstantPredicates.alwaysTrue(), this);
        outputTank5 = VariableCapacityChemicalTank.output(this, this::getOutputTankCapacityLong, ConstantPredicates.alwaysTrue(), this);

        outputChemicalTanks.add(outputTank1);
        outputChemicalTanks.add(outputTank2);
        outputChemicalTanks.add(outputTank3);
        outputChemicalTanks.add(outputTank4);
        outputChemicalTanks.add(outputTank5);

        for (IChemicalTank tank : outputChemicalTanks) {
            chemicalTanks.add(tank);
        }

        // Bottom Heat Input Capacitor (Bottom Heating)
        heatCapacitors.add(bottomHeatCapacitor = VariableHeatCapacitor.create(Math.max(64, getVolume()) * 40.0, () -> biomeAmbientTemp, this));
        // Top Cooling Capacitor (Top Cooling)
        heatCapacitors.add(topHeatCapacitor = VariableHeatCapacitor.create(Math.max(64, getVolume()) * 40.0, () -> biomeAmbientTemp, this));
    }

    public int getInputTankCapacity() {
        return Math.max(640, getVolume() * 160);
    }

    public long getInputTankCapacityLong() {
        return getInputTankCapacity();
    }

    public int getOutputTankCapacity() {
        return Math.max(64, getVolume() * 16);
    }

    public long getOutputTankCapacityLong() {
        return getOutputTankCapacity();
    }

    public int getTankCapacity() {
        return getInputTankCapacity();
    }

    public long getTankCapacityLong() {
        return getInputTankCapacityLong();
    }

    public IChemicalTank getInputChemicalTank() {
        return inputChemicalTank;
    }

    public IChemicalTank getOutputChemicalTank(int layerIndex) {
        if (layerIndex >= 0 && layerIndex < outputChemicalTanks.size()) {
            return outputChemicalTanks.get(layerIndex);
        }
        return outputChemicalTanks.get(0);
    }

    public VariableHeatCapacitor getBottomHeatCapacitor() {
        return bottomHeatCapacitor;
    }

    public VariableHeatCapacitor getTopHeatCapacitor() {
        return topHeatCapacitor;
    }

    public void setPartitionFloors(List<Integer> partitions) {
        if (partitions != null && partitions.size() >= 4) {
            for (int i = 0; i < 4; i++) {
                partitionFloors[i] = partitions.get(i);
            }
        }
    }

    /**
     * Determine which layer (1 to 5) a block position belongs to.
     */
    public int getLayerForPos(BlockPos pos) {
        if (getBounds() == null) {
            return 1;
        }
        int dy = pos.getY() - getBounds().getMinPos().getY();
        if (dy <= partitionFloors[0]) {
            return 1;
        } else if (dy <= partitionFloors[1]) {
            return 2;
        } else if (dy <= partitionFloors[2]) {
            return 3;
        } else if (dy <= partitionFloors[3]) {
            return 4;
        } else {
            return 5;
        }
    }

    @Override
    public void onCreated(Level world) {
        super.onCreated(world);
        FORMED_REFINERIES.add(this);
        biomeAmbientTemp = calculateAverageAmbientTemperature(world);
        double cap = Math.max(10_000.0, Math.max(64, getVolume()) * 40.0);
        bottomHeatCapacitor.setHeatCapacity(cap, true);
        topHeatCapacitor.setHeatCapacity(cap, true);
    }

    @Override
    public void remove(Level world, Structure structure) {
        FORMED_REFINERIES.remove(this);
        super.remove(world, structure);
    }

    @Override
    public void setVolume(int volume) {
        super.setVolume(volume);
        double cap = Math.max(10_000.0, Math.max(64, volume) * 40.0);
        if (bottomHeatCapacitor != null) {
            bottomHeatCapacitor.setHeatCapacity(cap, false);
        }
        if (topHeatCapacitor != null) {
            topHeatCapacitor.setHeatCapacity(cap, false);
        }
    }

    @Override
    public boolean tick(Level world) {
        boolean needsPacket = super.tick(world);

        // Heat exchange and vertical gradient between bottom and top
        if (isFormed()) {
            if (!FORMED_REFINERIES.contains(this)) {
                FORMED_REFINERIES.add(this);
            }
            double bottomTemp = bottomHeatCapacitor.getTemperature();
            double topTemp = topHeatCapacitor.getTemperature();

            // Sanity clamp for legacy saves where temperature ran away up to tens of thousands of degrees
            if (bottomTemp > 2500.0) {
                bottomHeatCapacitor.setHeat(1200.0 * bottomHeatCapacitor.getHeatCapacity());
                bottomTemp = 1200.0;
                needsPacket = true;
            }
            if (topTemp > 2000.0) {
                topHeatCapacitor.setHeat(350.0 * topHeatCapacitor.getHeatCapacity());
                topTemp = 350.0;
                needsPacket = true;
            }

            double diff = bottomTemp - topTemp;

            // Internal thermal conduction between bottom and top (upward heat rise)
            if (Math.abs(diff) > 0.05) {
                double transfer = diff * 0.0001 * Math.min(bottomHeatCapacitor.getHeatCapacity(), topHeatCapacitor.getHeatCapacity());
                bottomHeatCapacitor.handleHeat(-transfer);
                topHeatCapacitor.handleHeat(transfer);
                needsPacket = true;
            }

            // Environmental dissipation towards ambient (proportional to heat capacity using Mekanism sqrt formula)
            double bEnvDiff = bottomTemp - biomeAmbientTemp;
            if (Math.abs(bEnvDiff) > 0.05) {
                double dissipation = 0.005 * Math.sqrt(Math.abs(bEnvDiff)) * bottomHeatCapacitor.getHeatCapacity();
                bottomHeatCapacitor.handleHeat(bEnvDiff > 0 ? -dissipation : dissipation);
            }
            double tEnvDiff = topTemp - biomeAmbientTemp;
            if (Math.abs(tEnvDiff) > 0.05) {
                double dissipation = 0.005 * Math.sqrt(Math.abs(tEnvDiff)) * topHeatCapacitor.getHeatCapacity();
                topHeatCapacitor.handleHeat(tEnvDiff > 0 ? -dissipation : dissipation);
            }

            // Flush heat buffer updates
            updateHeatCapacitors(null);

            // Cracking process
            needsPacket |= updateCrackingProcess(bottomTemp, topTemp, diff);
        } else {
            FORMED_REFINERIES.remove(this);
            if (lastCrackingRate != 0.0 || operatingStatus != 0) {
                lastCrackingRate = 0.0;
                operatingStatus = 0;
                needsPacket = true;
            }
        }

        return needsPacket;
    }

    private boolean updateCrackingProcess(double bottomTemp, double topTemp, double deltaT) {
        boolean changed = false;

        // Check input chemical
        if (inputChemicalTank.isEmpty() || !inputChemicalTank.getStack().is(MCIChemicals.DENSE_CRUDE_OIL.get())) {
            if (lastCrackingRate != 0.0 || operatingStatus != 0) {
                lastCrackingRate = 0.0;
                operatingStatus = 0; // Idle
                changed = true;
            }
            inputBuffer = 0.0;
            return changed;
        }

        // Check bottom heating condition (>= 500 K)
        if (bottomTemp < 500.0) {
            if (lastCrackingRate != 0.0 || operatingStatus != 1) {
                lastCrackingRate = 0.0;
                operatingStatus = 1; // Temperature too low (< 500 K)
                changed = true;
            }
            inputBuffer = 0.0;
            return changed;
        }

        // Calculate R_heat (mB/s)
        // 500 K: 10 mB/s; 1200 K: 80 mB/s
        double rHeat = 10.0 + 70.0 * (Math.min(bottomTemp, 1200.0) - 500.0) / 700.0;

        // Calculate R_deltaT (mB/s)
        // deltaT < 100 K: penalty down to -10 mB/s (when deltaT <= 0)
        // deltaT == 100 K: 0 mB/s
        // deltaT > 100 K: up to +80 mB/s at 600 K
        double rDeltaT;
        if (deltaT < 100.0) {
            double ratio = (100.0 - Math.max(0.0, deltaT)) / 100.0;
            rDeltaT = -10.0 * ratio;
        } else {
            rDeltaT = 80.0 * (Math.min(deltaT, 600.0) - 100.0) / 500.0;
        }

        double rEffective = Math.max(0.0, rHeat + rDeltaT);
        if (Math.abs(lastCrackingRate - rEffective) > 0.05) {
            lastCrackingRate = rEffective;
            changed = true;
        }

        int newStatus;
        if (deltaT < 100.0) {
            newStatus = 2; // Delta T too low (< 100K)
        } else if (rEffective <= 0.0) {
            newStatus = 2;
        } else {
            newStatus = 3; // Active
        }

        if (operatingStatus != newStatus) {
            operatingStatus = newStatus;
            changed = true;
        }

        if (rEffective <= 0.0) {
            inputBuffer = 0.0;
            return changed;
        }

        // Continuous processing accumulation
        double targetPerTick = rEffective / 20.0;
        inputBuffer += targetPerTick;
        long toProcess = (long) inputBuffer;

        if (toProcess > 0) {
            long available = Math.min(toProcess, inputChemicalTank.getStored());
            if (available <= 0) {
                return changed;
            }

            ChemicalStack gasStack = MCIChemicals.PETROLEUM_GAS.asStack(1);
            ChemicalStack naphthaStack = MCIChemicals.NAPHTHA.asStack(1);
            ChemicalStack fuelStack = MCIChemicals.REFINED_FUEL.asStack(1);
            ChemicalStack heavyOilStack = MCIChemicals.HEAVY_OIL.asStack(1);
            ChemicalStack bitumenStack = MCIChemicals.BITUMEN.asStack(1);

            long actual = available;
            actual = Math.min(actual, getAcceptableInputForTank(outputTank5, gasStack, 2.0));
            actual = Math.min(actual, getAcceptableInputForTank(outputTank4, naphthaStack, 0.2));
            actual = Math.min(actual, getAcceptableInputForTank(outputTank3, fuelStack, 0.1));
            actual = Math.min(actual, getAcceptableInputForTank(outputTank2, heavyOilStack, 0.1));
            actual = Math.min(actual, getAcceptableInputForTank(outputTank1, bitumenStack, 0.15));

            if (actual > 0) {
                inputChemicalTank.shrinkStack(actual, Action.EXECUTE);
                inputBuffer -= actual;

                gasBuffer += actual * 2.0;
                naphthaBuffer += actual * 0.2;
                fuelBuffer += actual * 0.1;
                heavyOilBuffer += actual * 0.1;
                bitumenBuffer += actual * 0.15;

                long g = (long) gasBuffer;
                if (g > 0) {
                    outputTank5.insert(MCIChemicals.PETROLEUM_GAS.asStack(g), Action.EXECUTE, AutomationType.INTERNAL);
                    gasBuffer -= g;
                }
                long n = (long) naphthaBuffer;
                if (n > 0) {
                    outputTank4.insert(MCIChemicals.NAPHTHA.asStack(n), Action.EXECUTE, AutomationType.INTERNAL);
                    naphthaBuffer -= n;
                }
                long f = (long) fuelBuffer;
                if (f > 0) {
                    outputTank3.insert(MCIChemicals.REFINED_FUEL.asStack(f), Action.EXECUTE, AutomationType.INTERNAL);
                    fuelBuffer -= f;
                }
                long h = (long) heavyOilBuffer;
                if (h > 0) {
                    outputTank2.insert(MCIChemicals.HEAVY_OIL.asStack(h), Action.EXECUTE, AutomationType.INTERNAL);
                    heavyOilBuffer -= h;
                }
                long b = (long) bitumenBuffer;
                if (b > 0) {
                    outputTank1.insert(MCIChemicals.BITUMEN.asStack(b), Action.EXECUTE, AutomationType.INTERNAL);
                    bitumenBuffer -= b;
                }

                // Heat consumption: ~50 J per mB processed
                bottomHeatCapacitor.handleHeat(-actual * 50.0);
                changed = true;
            } else {
                if (operatingStatus != 4) {
                    operatingStatus = 4; // Outputs full
                    changed = true;
                }
            }
        }

        return changed;
    }

    private long getAcceptableInputForTank(IChemicalTank tank, ChemicalStack outputType, double ratio) {
        if (ratio <= 0.0) {
            return Long.MAX_VALUE;
        }
        long neededCapacity;
        if (tank.isEmpty()) {
            neededCapacity = tank.getCapacity();
        } else if (tank.getStack().is(outputType.getChemical())) {
            neededCapacity = tank.getNeeded();
        } else {
            return 0;
        }
        return (long) (neededCapacity / ratio);
    }

    @Override
    public void readUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.readUpdateTag(tag, provider);
        if (tag.contains("partitionFloors")) {
            partitionFloors = tag.getIntArray("partitionFloors");
        }
        if (tag.contains("lastCrackingRate")) {
            lastCrackingRate = tag.getDouble("lastCrackingRate");
        }
        if (tag.contains("operatingStatus")) {
            operatingStatus = tag.getInt("operatingStatus");
        }
    }

    @Override
    public void writeUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.writeUpdateTag(tag, provider);
        tag.putIntArray("partitionFloors", partitionFloors);
        tag.putDouble("lastCrackingRate", lastCrackingRate);
        tag.putInt("operatingStatus", operatingStatus);
    }

    /**
     * Checks if a block position is physically inside the tower multiblock bounds (any interior or casing cell).
     */
    public boolean isInsideTower(BlockPos pos) {
        if (!isFormed() || getBounds() == null) {
            return false;
        }
        BlockPos min = getBounds().getMinPos();
        BlockPos max = getBounds().getMaxPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        if (y < min.getY() || y > max.getY()) {
            return false;
        }
        int dx = x - min.getX();
        int dz = z - min.getZ();
        if (dx >= 0 && dx < 7 && dz >= 0 && dz < 7) {
            return RefineryValidator.GRID_TEMPLATE[dz][dx] != RefineryValidator.TYPE_IGNORED;
        }
        return false;
    }

    /**
     * Checks if a block position is near the tower multiblock within specified horizontal and vertical distances.
     */
    public boolean isNearTower(BlockPos pos, int horizontalRadius, int verticalRadius) {
        if (!isFormed() || getBounds() == null) {
            return false;
        }
        BlockPos min = getBounds().getMinPos();
        BlockPos max = getBounds().getMaxPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        if (y < min.getY() - verticalRadius || y > max.getY() + verticalRadius) {
            return false;
        }
        if (x < min.getX() - horizontalRadius || x > max.getX() + horizontalRadius) {
            return false;
        }
        if (z < min.getZ() - horizontalRadius || z > max.getZ() + horizontalRadius) {
            return false;
        }
        return true;
    }

    /**
     * Determine whether mob spawning should be suppressed around or inside any active refinery multiblock.
     * - Inside tower: suppress ALL mob spawns.
     * - Around tower (16 blocks horizontal, 8 blocks vertical): suppress hostile / monster spawns.
     */
    public static boolean shouldSuppressSpawn(Level level, BlockPos pos, MobCategory category, Class<?> entityClass, MobSpawnType spawnType) {
        if (spawnType == MobSpawnType.SPAWN_EGG || spawnType == MobSpawnType.COMMAND) {
            return false;
        }
        boolean isHostile = (category == MobCategory.MONSTER) || (entityClass != null && Enemy.class.isAssignableFrom(entityClass));

        Iterator<RefineryMultiblockData> iterator = FORMED_REFINERIES.iterator();
        while (iterator.hasNext()) {
            RefineryMultiblockData refinery = iterator.next();
            if (!refinery.isFormed() || refinery.getBounds() == null) {
                iterator.remove();
                continue;
            }
            Level refineryLevel = refinery.getLevel();
            if (refineryLevel != null && level != null && !refineryLevel.dimension().equals(level.dimension())) {
                continue;
            }

            // 1. Inside tower: suppress all mob spawns
            if (refinery.isInsideTower(pos)) {
                return true;
            }

            // 2. Around tower: suppress hostile mobs within 16 blocks horizontal, 8 blocks vertical
            if (isHostile && refinery.isNearTower(pos, 16, 8)) {
                return true;
            }
        }
        return false;
    }

    public static boolean shouldSuppressSpawn(Level level, BlockPos pos, Mob mob, MobSpawnType spawnType) {
        if (mob == null) {
            return false;
        }
        return shouldSuppressSpawn(level, pos, mob.getType().getCategory(), mob.getClass(), spawnType);
    }
}
