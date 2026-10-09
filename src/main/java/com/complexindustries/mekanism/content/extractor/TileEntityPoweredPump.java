package com.complexindustries.mekanism.content.extractor;

import com.complexindustries.mekanism.registration.MCIBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityPoweredPump extends TileEntityFluidExtractorCasing {

    public TileEntityPoweredPump(BlockPos pos, BlockState state) {
        super(MCIBlocks.POWERED_PUMP, pos, state);
    }
}
