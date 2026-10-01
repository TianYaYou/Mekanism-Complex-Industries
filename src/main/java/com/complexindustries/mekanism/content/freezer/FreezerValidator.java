package com.complexindustries.mekanism.content.freezer;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import mekanism.common.MekanismLang;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.lib.multiblock.CuboidStructureValidator;
import mekanism.common.lib.multiblock.FormationProtocol;
import mekanism.common.lib.multiblock.FormationProtocol.CasingType;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.registries.MekanismBlocks;
import com.complexindustries.mekanism.registration.MCIBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class FreezerValidator extends CuboidStructureValidator<FreezerMultiblockData> {

    private static final VoxelCuboid MIN_CUBOID = new VoxelCuboid(4, 4, 4);
    private static final VoxelCuboid MAX_CUBOID = new VoxelCuboid(8, 8, 8);

    private boolean foundController = false;

    public FreezerValidator() {
        super(MIN_CUBOID, MAX_CUBOID);
    }

    @Override
    protected FormationResult validateFrame(FormationProtocol<FreezerMultiblockData> ctx, BlockPos pos, BlockState state, CasingType type, boolean needsFrame) {
        boolean isController = structure.getTile(pos) instanceof TileEntityFreezerController;
        if (foundController && isController) {
            return FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_CONTROLLER_CONFLICT, pos, true);
        }
        foundController |= isController;
        return super.validateFrame(ctx, pos, state, type, needsFrame);
    }

    @Override
    protected CasingType getCasingType(BlockState state) {
        Block block = state.getBlock();
        if (block == MCIBlocks.FREEZER_CASING.get()) {
            return CasingType.FRAME;
        } else if (block == MCIBlocks.FREEZER_VALVE.get()) {
            return CasingType.VALVE;
        } else if (block == MCIBlocks.FREEZER_CONTROLLER.get()) {
            return CasingType.OTHER;
        } else if (block == MekanismBlocks.STRUCTURAL_GLASS.get()) {
            return CasingType.OTHER;
        }
        return CasingType.INVALID;
    }

    @Override
    public FormationResult postcheck(FreezerMultiblockData structure, Long2ObjectMap<ChunkAccess> chunkMap) {
        if (!foundController) {
            return FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_NO_CONTROLLER);
        }
        return FormationResult.SUCCESS;
    }
}
