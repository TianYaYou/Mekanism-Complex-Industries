package com.complexindustries.mekanism.content.refinery;

import com.complexindustries.mekanism.registration.MCIBlocks;
import mekanism.api.IContentsListener;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityRefineryCasing extends TileEntityMultiblock<RefineryMultiblockData> {

    public TileEntityRefineryCasing(BlockPos pos, BlockState state) {
        this(MCIBlocks.REFINERY_CASING, pos, state);
    }

    public TileEntityRefineryCasing(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
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
    public RefineryMultiblockData createMultiblock() {
        return new RefineryMultiblockData(this);
    }

    @Override
    public MultiblockManager<RefineryMultiblockData> getManager() {
        return MCIRefineryMultiblock.REFINERY_MANAGER;
    }

    @Override
    public boolean canBeMaster() {
        return false;
    }
}
