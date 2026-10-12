package com.complexindustries.mekanism.content.pulverizer;

import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public abstract class TileEntityWindmillPulverizerCasing extends TileEntityMultiblock<WindmillPulverizerMultiblockData> {

    public TileEntityWindmillPulverizerCasing(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @NotNull
    @Override
    public WindmillPulverizerMultiblockData createMultiblock() {
        return new WindmillPulverizerMultiblockData(this);
    }

    @Override
    public MultiblockManager<WindmillPulverizerMultiblockData> getManager() {
        return MCIWindmillPulverizerMultiblock.PULVERIZER_MANAGER;
    }

    @Override
    public boolean canBeMaster() {
        return true;
    }
}
