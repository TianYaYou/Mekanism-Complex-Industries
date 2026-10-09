package com.complexindustries.mekanism.content.extractor;

import com.complexindustries.mekanism.registration.MCIBlocks;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityFluidExtractorCasing extends TileEntityMultiblock<FluidExtractorMultiblockData> {

    public TileEntityFluidExtractorCasing(BlockPos pos, BlockState state) {
        this(MCIBlocks.FLUID_EXTRACTOR_CASING, pos, state);
    }

    public TileEntityFluidExtractorCasing(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @NotNull
    @Override
    public FluidExtractorMultiblockData createMultiblock() {
        return new FluidExtractorMultiblockData(this);
    }

    @Override
    public MultiblockManager<FluidExtractorMultiblockData> getManager() {
        return MCIFluidExtractorMultiblock.EXTRACTOR_MANAGER;
    }

    @Override
    public boolean canBeMaster() {
        return true;
    }
}
