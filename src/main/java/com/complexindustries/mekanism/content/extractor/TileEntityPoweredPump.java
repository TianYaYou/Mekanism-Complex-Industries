package com.complexindustries.mekanism.content.extractor;

import com.complexindustries.mekanism.registration.MCIBlocks;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityPoweredPump extends TileEntityMultiblock<FluidExtractorMultiblockData> {

    public TileEntityPoweredPump(BlockPos pos, BlockState state) {
        super(MCIBlocks.POWERED_PUMP, pos, state);
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
        return false;
    }
}
