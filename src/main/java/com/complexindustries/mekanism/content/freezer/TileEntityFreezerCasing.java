package com.complexindustries.mekanism.content.freezer;

import mekanism.api.providers.IBlockProvider;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import com.complexindustries.mekanism.registration.MCIBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityFreezerCasing extends TileEntityMultiblock<FreezerMultiblockData> {

    public TileEntityFreezerCasing(BlockPos pos, BlockState state) {
        this(MCIBlocks.FREEZER_CASING_PROVIDER, pos, state);
    }

    public TileEntityFreezerCasing(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
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
