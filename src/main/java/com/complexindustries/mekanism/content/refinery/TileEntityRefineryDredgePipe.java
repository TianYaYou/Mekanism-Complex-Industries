package com.complexindustries.mekanism.content.refinery;

import com.complexindustries.mekanism.registration.MCIBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityRefineryDredgePipe extends TileEntityRefineryCasing {

    public TileEntityRefineryDredgePipe(BlockPos pos, BlockState state) {
        super(MCIBlocks.REFINERY_DREDGE_PIPE, pos, state);
    }
}
