package com.complexindustries.mekanism.content.refinery;

import com.complexindustries.mekanism.content.block.RefineryControllerBlock;
import com.complexindustries.mekanism.registration.MCIBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityRefineryController extends TileEntityRefineryCasing {

    public TileEntityRefineryController(BlockPos pos, BlockState state) {
        super(MCIBlocks.REFINERY_CONTROLLER, pos, state);
        delaySupplier = NO_DELAY;
    }

    @Override
    protected boolean onUpdateServer(RefineryMultiblockData multiblock) {
        boolean needsPacket = super.onUpdateServer(multiblock);
        boolean formed = multiblock.isFormed();
        BlockState currentState = getBlockState();
        if (currentState.hasProperty(RefineryControllerBlock.ACTIVE) && currentState.getValue(RefineryControllerBlock.ACTIVE) != formed) {
            getLevel().setBlock(getBlockPos(), currentState.setValue(RefineryControllerBlock.ACTIVE, formed), 3);
        }
        return needsPacket;
    }

    @Override
    public boolean canBeMaster() {
        return true;
    }
}
