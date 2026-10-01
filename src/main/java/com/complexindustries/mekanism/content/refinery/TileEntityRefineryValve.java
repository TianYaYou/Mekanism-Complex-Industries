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

import java.util.Collections;

public class TileEntityRefineryValve extends TileEntityRefineryCasing {

    public enum ValveMode {
        INPUT,
        OUTPUT,
        HEAT_INPUT,
        COOLING_INPUT
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
        setChanged();
        sendUpdatePacket();
    }

    public int getEffectiveLayer() {
        if (getMultiblock().isFormed()) {
            return getMultiblock().getLayerForPos(getBlockPos());
        }
        return 1;
    }

    public void cycleMode(Player player) {
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
        if (nextMode == ValveMode.INPUT) {
            modeDesc = Component.translatable("message.mekanism_complex_industries.refinery_valve.mode_input");
        } else if (nextMode == ValveMode.HEAT_INPUT) {
            modeDesc = Component.translatable("message.mekanism_complex_industries.refinery_valve.mode_heat_input");
        } else if (nextMode == ValveMode.COOLING_INPUT) {
            modeDesc = Component.translatable("message.mekanism_complex_industries.refinery_valve.mode_cooling_input");
        } else {
            modeDesc = Component.translatable("message.mekanism_complex_industries.refinery_valve.mode_output", layer);
        }

        player.displayClientMessage(
                Component.translatable("message.mekanism_complex_industries.refinery_valve.mode", modeDesc),
                true
        );
    }

    @NotNull
    public IChemicalTank getActiveChemicalTank() {
        if (!getMultiblock().isFormed()) {
            return null;
        }
        if (mode == ValveMode.INPUT) {
            return getMultiblock().getInputChemicalTank();
        } else if (mode == ValveMode.OUTPUT) {
            int layer = getEffectiveLayer();
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

    @NotNull
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        return side -> {
            if (!getMultiblock().isFormed()) {
                return Collections.emptyList();
            }
            if (mode == ValveMode.HEAT_INPUT) {
                return Collections.singletonList(getMultiblock().getBottomHeatCapacitor());
            } else if (mode == ValveMode.COOLING_INPUT) {
                return Collections.singletonList(getMultiblock().getTopHeatCapacitor());
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
