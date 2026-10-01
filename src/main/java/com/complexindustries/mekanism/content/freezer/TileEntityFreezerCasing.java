package com.complexindustries.mekanism.content.freezer;

import mekanism.api.IContentsListener;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import com.complexindustries.mekanism.registration.MCIBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;

public class TileEntityFreezerCasing extends TileEntityMultiblock<FreezerMultiblockData> {

    public TileEntityFreezerCasing(BlockPos pos, BlockState state) {
        this(MCIBlocks.FREEZER_CASING, pos, state);
    }

    public TileEntityFreezerCasing(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @NotNull
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        return side -> getMultiblock().getHeatCapacitors(side);
    }

    @Override
    public boolean persists(ContainerType<?, ?, ?> type) {
        if (type == ContainerType.HEAT) {
            return false;
        }
        return super.persists(type);
    }

    @NotNull
    @Override
    public FreezerMultiblockData createMultiblock() {
        return new FreezerMultiblockData(this);
    }

    @Override
    public MultiblockManager<FreezerMultiblockData> getManager() {
        return MCIFreezerMultiblock.FREEZER_MANAGER;
    }

    @Override
    public boolean canBeMaster() {
        return false;
    }
}
