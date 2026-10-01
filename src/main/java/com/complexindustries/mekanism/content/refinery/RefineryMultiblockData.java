package com.complexindustries.mekanism.content.refinery;

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
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RefineryMultiblockData extends MultiblockData implements IValveHandler {

    @ContainerSync
    public IChemicalTank inputChemicalTank;

    public final List<IChemicalTank> outputChemicalTanks = new ArrayList<>(5);

    @ContainerSync
    public VariableHeatCapacitor bottomHeatCapacitor;

    @ContainerSync
    public VariableHeatCapacitor topHeatCapacitor;

    // Partition floor heights (relative dy or absolute y)
    public int[] partitionFloors = new int[4];

    private double biomeAmbientTemp = HeatAPI.AMBIENT_TEMP;

    public RefineryMultiblockData(TileEntityMultiblock<?> tile) {
        super(tile);
        // 1 Chemical Input Tank (Layer 1)
        chemicalTanks.add(inputChemicalTank = VariableCapacityChemicalTank.input(this, this::getTankCapacityLong, ConstantPredicates.alwaysTrue(), this));

        // 5 Chemical Output Tanks (Layers 1..5)
        for (int i = 0; i < 5; i++) {
            IChemicalTank tank = VariableCapacityChemicalTank.output(this, this::getTankCapacityLong, ConstantPredicates.alwaysTrue(), this);
            outputChemicalTanks.add(tank);
            chemicalTanks.add(tank);
        }

        // Bottom Heat Input Capacitor (Bottom Heating)
        heatCapacitors.add(bottomHeatCapacitor = VariableHeatCapacitor.create(Math.max(64, getVolume()) * 40.0, () -> biomeAmbientTemp, this));
        // Top Cooling Capacitor (Top Cooling)
        heatCapacitors.add(topHeatCapacitor = VariableHeatCapacitor.create(Math.max(64, getVolume()) * 40.0, () -> biomeAmbientTemp, this));
    }

    public int getTankCapacity() {
        return Math.max(64_000, getVolume() * 16_000);
    }

    public long getTankCapacityLong() {
        return getTankCapacity();
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
    public boolean tick(Level world) {
        boolean needsPacket = super.tick(world);

        // Heat exchange and vertical gradient between bottom and top
        if (isFormed()) {
            double bottomTemp = bottomHeatCapacitor.getTemperature();
            double topTemp = topHeatCapacitor.getTemperature();
            double diff = bottomTemp - topTemp;

            // Internal thermal conduction between bottom and top
            if (Math.abs(diff) > 0.01) {
                double transfer = diff * 0.02;
                bottomHeatCapacitor.handleHeat(-transfer);
                topHeatCapacitor.handleHeat(transfer);
                needsPacket = true;
            }

            // Environmental dissipation towards ambient
            double bEnvDiff = bottomTemp - biomeAmbientTemp;
            if (Math.abs(bEnvDiff) > 0.05) {
                bottomHeatCapacitor.handleHeat(-bEnvDiff * 0.005);
            }
            double tEnvDiff = topTemp - biomeAmbientTemp;
            if (Math.abs(tEnvDiff) > 0.05) {
                topHeatCapacitor.handleHeat(-tEnvDiff * 0.005);
            }
        }

        return needsPacket;
    }

    @Override
    public void readUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.readUpdateTag(tag, provider);
        if (tag.contains("partitionFloors")) {
            partitionFloors = tag.getIntArray("partitionFloors");
        }
    }

    @Override
    public void writeUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.writeUpdateTag(tag, provider);
        tag.putIntArray("partitionFloors", partitionFloors);
    }
}
