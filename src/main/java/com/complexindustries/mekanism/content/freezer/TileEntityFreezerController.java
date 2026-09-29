package com.complexindustries.mekanism.content.freezer;

import com.complexindustries.mekanism.content.block.FreezerControllerBlock;
import com.complexindustries.mekanism.registration.MCIBlocks;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.dynamic.SyncMapper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityFreezerController extends TileEntityFreezerCasing {

    public TileEntityFreezerController(BlockPos pos, BlockState state) {
        super(MCIBlocks.FREEZER_CONTROLLER_PROVIDER, pos, state);
        delaySupplier = NO_DELAY;
    }

    @Override
    protected boolean onUpdateServer(FreezerMultiblockData multiblock) {
        boolean needsPacket = super.onUpdateServer(multiblock);
        boolean formed = multiblock.isFormed();
        BlockState currentState = getBlockState();
        if (currentState.hasProperty(FreezerControllerBlock.ACTIVE) && currentState.getValue(FreezerControllerBlock.ACTIVE) != formed) {
            getLevel().setBlock(getBlockPos(), currentState.setValue(FreezerControllerBlock.ACTIVE, formed), 3);
        }
        return needsPacket;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        SyncMapper.INSTANCE.setup(container, FreezerMultiblockData.class, this::getMultiblock);
    }

    @Override
    public boolean canBeMaster() {
        return true;
    }
}
