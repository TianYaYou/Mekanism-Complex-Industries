package com.complexindustries.mekanism.content.refinery;

import com.complexindustries.mekanism.registration.MCIBlocks;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.IChemicalTank;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.tile.base.WrenchResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import mekanism.api.IConfigurable;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IHeatHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.heat.VariableHeatCapacitor;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;

import java.util.Collections;

public class TileEntityRefineryValve extends TileEntityRefineryCasing implements IConfigurable {

    public enum ValveMode implements net.minecraft.util.StringRepresentable {
        INPUT("input"),
        OUTPUT("output"),
        HEAT_INPUT("heat_input"),
        COOLING_INPUT("cooling_input");

        private final String name;

        ValveMode(String name) {
            this.name = name;
        }

        @NotNull
        @Override
        public String getSerializedName() {
            return name;
        }
    }

    private ValveMode mode = ValveMode.OUTPUT;

    public TileEntityRefineryValve(BlockPos pos, BlockState state) {
        super(MCIBlocks.REFINERY_VALVE, pos, state);
    }

    public ValveMode getMode() {
        return mode;
    }

    public void setMode(ValveMode mode) {
        this.mode = mode;
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            if (state.hasProperty(com.complexindustries.mekanism.content.block.RefineryValveBlock.MODE)
                    && state.getValue(com.complexindustries.mekanism.content.block.RefineryValveBlock.MODE) != mode) {
                level.setBlock(worldPosition, state.setValue(com.complexindustries.mekanism.content.block.RefineryValveBlock.MODE, mode), 3);
            }
        }
        setChanged();
        sendUpdatePacket();
    }

    @Override
    protected boolean onUpdateServer(RefineryMultiblockData multiblock) {
        boolean needsPacket = super.onUpdateServer(multiblock);
        BlockState currentState = getBlockState();
        if (currentState.hasProperty(com.complexindustries.mekanism.content.block.RefineryValveBlock.MODE)
                && currentState.getValue(com.complexindustries.mekanism.content.block.RefineryValveBlock.MODE) != mode) {
            getLevel().setBlock(getBlockPos(), currentState.setValue(com.complexindustries.mekanism.content.block.RefineryValveBlock.MODE, mode), 3);
        }
        return needsPacket;
    }

    public int getEffectiveLayer() {
        if (getMultiblock().isFormed()) {
            return getMultiblock().getLayerForPos(getBlockPos());
        }
        return 1;
    }

    public void cycleMode(Player player) {
        if (!getMultiblock().isFormed()) {
            player.displayClientMessage(
                    Component.translatable("message.mekanism_complex_industries.refinery_valve.unformed"),
                    true
            );
            return;
        }

        int layer = getEffectiveLayer();
        ValveMode nextMode;

        if (layer == 1) {
            // Layer 1: INPUT -> OUTPUT -> HEAT_INPUT -> INPUT
            if (mode == ValveMode.INPUT) {
                nextMode = ValveMode.OUTPUT;
            } else if (mode == ValveMode.OUTPUT) {
                nextMode = ValveMode.HEAT_INPUT;
            } else {
                nextMode = ValveMode.INPUT;
            }
        } else if (layer == 5) {
            // Layer 5: OUTPUT -> COOLING_INPUT -> OUTPUT
            if (mode == ValveMode.OUTPUT) {
                nextMode = ValveMode.COOLING_INPUT;
            } else {
                nextMode = ValveMode.OUTPUT;
            }
        } else {
            // Layers 2..4: fixed to OUTPUT
            nextMode = ValveMode.OUTPUT;
        }

        setMode(nextMode);

        Component modeDesc;
        if (layer == 1) {
            if (nextMode == ValveMode.INPUT) {
                modeDesc = Component.translatable("message.mekanism_complex_industries.refinery_valve.mode_input");
            } else if (nextMode == ValveMode.HEAT_INPUT) {
                modeDesc = Component.translatable("message.mekanism_complex_industries.refinery_valve.mode_heat_input");
            } else {
                modeDesc = Component.translatable("message.mekanism_complex_industries.refinery_valve.mode_output_1");
            }
        } else if (layer == 5) {
            if (nextMode == ValveMode.COOLING_INPUT) {
                modeDesc = Component.translatable("message.mekanism_complex_industries.refinery_valve.mode_cooling_input");
            } else {
                modeDesc = Component.translatable("message.mekanism_complex_industries.refinery_valve.mode_output_5");
            }
        } else {
            modeDesc = Component.translatable("message.mekanism_complex_industries.refinery_valve.mode_output_" + layer);
        }

        player.displayClientMessage(
                Component.translatable("message.mekanism_complex_industries.refinery_valve.mode", modeDesc),
                true
        );
    }

    @Override
    public InteractionResult onRightClick(Player player) {
        if (getLevel() != null && !getLevel().isClientSide) {
            cycleMode(player);
        }
        return InteractionResult.sidedSuccess(getLevel() != null && getLevel().isClientSide);
    }

    @Override
    public InteractionResult onSneakRightClick(Player player) {
        if (getLevel() != null && !getLevel().isClientSide) {
            cycleMode(player);
        }
        return InteractionResult.sidedSuccess(getLevel() != null && getLevel().isClientSide);
    }

    @NotNull
    public IChemicalTank getActiveChemicalTank() {
        if (!getMultiblock().isFormed()) {
            return null;
        }
        int layer = getEffectiveLayer();
        if (layer == 1) {
            if (mode == ValveMode.INPUT) {
                return getMultiblock().getInputChemicalTank();
            } else if (mode == ValveMode.OUTPUT) {
                return getMultiblock().getOutputChemicalTank(0);
            }
        } else if (layer == 5) {
            if (mode == ValveMode.OUTPUT) {
                return getMultiblock().getOutputChemicalTank(4);
            }
        } else {
            return getMultiblock().getOutputChemicalTank(layer - 1);
        }
        return null;
    }

    @NotNull
    @Override
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener) {
        return side -> {
            if (!getMultiblock().isFormed()) {
                return Collections.emptyList();
            }
            IChemicalTank tank = getActiveChemicalTank();
            return tank == null ? Collections.emptyList() : Collections.singletonList(tank);
        };
    }

    private final IHeatCapacitor heatingValveCapacitor = new ClampedHeatCapacitor(true);
    private final IHeatCapacitor coolingValveCapacitor = new ClampedHeatCapacitor(false);

    public double getMaxAdjacentTemperature() {
        if (getLevel() == null) return 0.0;
        double maxTemp = 0.0;
        for (Direction dir : Direction.values()) {
            IHeatHandler adjacent = getAdjacent(dir);
            if (adjacent == null && getLevel() != null) {
                adjacent = getLevel().getCapability(Capabilities.HEAT, getBlockPos().relative(dir), dir.getOpposite());
            }
            if (adjacent != null) {
                maxTemp = Math.max(maxTemp, adjacent.getTotalTemperature());
            }
        }
        return maxTemp;
    }

    public double getMinAdjacentTemperature() {
        if (getLevel() == null) return Double.MAX_VALUE;
        double minTemp = Double.MAX_VALUE;
        for (Direction dir : Direction.values()) {
            IHeatHandler adjacent = getAdjacent(dir);
            if (adjacent == null && getLevel() != null) {
                adjacent = getLevel().getCapability(Capabilities.HEAT, getBlockPos().relative(dir), dir.getOpposite());
            }
            if (adjacent != null) {
                minTemp = Math.min(minTemp, adjacent.getTotalTemperature());
            }
        }
        return minTemp;
    }

    private class ClampedHeatCapacitor implements IHeatCapacitor {
        private final boolean isHeating;

        public ClampedHeatCapacitor(boolean isHeating) {
            this.isHeating = isHeating;
        }

        private VariableHeatCapacitor getTarget() {
            if (!getMultiblock().isFormed()) return null;
            return isHeating ? getMultiblock().getBottomHeatCapacitor() : getMultiblock().getTopHeatCapacitor();
        }

        @Override
        public double getTemperature() {
            VariableHeatCapacitor target = getTarget();
            return target != null ? target.getTemperature() : HeatAPI.AMBIENT_TEMP;
        }

        @Override
        public double getInverseConduction() {
            VariableHeatCapacitor target = getTarget();
            return target != null ? target.getInverseConduction() : HeatAPI.DEFAULT_INVERSE_CONDUCTION;
        }

        @Override
        public double getInverseInsulation() {
            VariableHeatCapacitor target = getTarget();
            return target != null ? target.getInverseInsulation() : HeatAPI.DEFAULT_INVERSE_INSULATION;
        }

        @Override
        public double getHeatCapacity() {
            VariableHeatCapacitor target = getTarget();
            return target != null ? target.getHeatCapacity() : HeatAPI.DEFAULT_HEAT_CAPACITY;
        }

        @Override
        public double getHeat() {
            VariableHeatCapacitor target = getTarget();
            return target != null ? target.getHeat() : 0.0;
        }

        @Override
        public void setHeat(double heat) {
            VariableHeatCapacitor target = getTarget();
            if (target != null) target.setHeat(heat);
        }

        @Override
        public void handleHeat(double heat) {
            VariableHeatCapacitor target = getTarget();
            if (target == null) return;

            if (isHeating) {
                // Bottom heating valve: only accepts heating (heat > 0)
                if (heat <= 0) return;
                double maxSourceTemp = getMaxAdjacentTemperature();
                if (maxSourceTemp > 0) {
                    double currentTemp = target.getTemperature();
                    if (currentTemp >= maxSourceTemp) {
                        return; // Cannot heat beyond source temperature!
                    }
                    double maxHeat = (maxSourceTemp - currentTemp) * target.getHeatCapacity();
                    heat = Math.min(heat, maxHeat);
                }
            } else {
                // Top cooling valve: only accepts cooling (heat < 0)
                if (heat >= 0) return;
                double minSourceTemp = getMinAdjacentTemperature();
                if (minSourceTemp > 0 && minSourceTemp < Double.MAX_VALUE) {
                    double currentTemp = target.getTemperature();
                    if (currentTemp <= minSourceTemp) {
                        return; // Cannot cool below source temperature!
                    }
                    double maxExtract = (currentTemp - minSourceTemp) * target.getHeatCapacity();
                    heat = -Math.min(Math.abs(heat), maxExtract);
                }
            }
            target.handleHeat(heat);
        }

        @Override
        public void onContentsChanged() {
            VariableHeatCapacitor target = getTarget();
            if (target != null) target.onContentsChanged();
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider provider) {
            VariableHeatCapacitor target = getTarget();
            return target != null ? target.serializeNBT(provider) : new CompoundTag();
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
            VariableHeatCapacitor target = getTarget();
            if (target != null) target.deserializeNBT(provider, nbt);
        }
    }

    @NotNull
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        return side -> {
            if (!getMultiblock().isFormed()) {
                return Collections.emptyList();
            }
            if (mode == ValveMode.HEAT_INPUT) {
                return Collections.singletonList(heatingValveCapacitor);
            } else if (mode == ValveMode.COOLING_INPUT) {
                return Collections.singletonList(coolingValveCapacitor);
            }
            return Collections.emptyList();
        };
    }

    @Override
    public boolean persists(ContainerType<?, ?, ?> type) {
        if (type == ContainerType.CHEMICAL || type == ContainerType.HEAT) {
            return false;
        }
        return super.persists(type);
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getReducedUpdateTag(provider);
        tag.putInt("valveMode", mode.ordinal());
        return tag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        if (tag.contains("valveMode")) {
            mode = ValveMode.values()[tag.getInt("valveMode") % ValveMode.values().length];
            if (level != null && level.isClientSide) {
                BlockState state = getBlockState();
                if (state.hasProperty(com.complexindustries.mekanism.content.block.RefineryValveBlock.MODE)
                        && state.getValue(com.complexindustries.mekanism.content.block.RefineryValveBlock.MODE) != mode) {
                    level.setBlock(worldPosition, state.setValue(com.complexindustries.mekanism.content.block.RefineryValveBlock.MODE, mode), 3);
                }
            }
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("valveMode")) {
            mode = ValveMode.values()[tag.getInt("valveMode") % ValveMode.values().length];
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("valveMode", mode.ordinal());
    }
}
