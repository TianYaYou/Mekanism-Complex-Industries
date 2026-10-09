package com.complexindustries.mekanism.content.extractor;

import com.complexindustries.mekanism.registration.MCIBlocks;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import mekanism.common.MekanismLang;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.lib.multiblock.CuboidStructureValidator;
import mekanism.common.lib.multiblock.FormationProtocol;
import mekanism.common.lib.multiblock.FormationProtocol.CasingType;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class FluidExtractorValidator extends CuboidStructureValidator<FluidExtractorMultiblockData> {

    private static final VoxelCuboid MIN_CUBOID = new VoxelCuboid(3, 3, 3);
    private static final VoxelCuboid MAX_CUBOID = new VoxelCuboid(16, 16, 16);

    private int pumpCount = 0;
    private int portCount = 0;

    public FluidExtractorValidator() {
        super(MIN_CUBOID, MAX_CUBOID);
    }

    @Override
    public boolean precheck() {
        pumpCount = 0;
        portCount = 0;
        return super.precheck();
    }

    @Override
    protected FormationResult validateFrame(FormationProtocol<FluidExtractorMultiblockData> ctx, BlockPos pos, BlockState state, CasingType type, boolean needsFrame) {
        Block block = state.getBlock();
        boolean isFloor = pos.getY() == cuboid.getMinPos().getY();

        if (needsFrame) {
            // Frame edges must strictly be FLUID_EXTRACTOR_CASING
            if (block != MCIBlocks.FLUID_EXTRACTOR_CASING.get()) {
                return FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_FRAME, pos);
            }
            return super.validateFrame(ctx, pos, state, CasingType.FRAME, true);
        }

        if (isFloor) {
            // Floor (non-edge) can only be POWERED_PUMP or STRUCTURAL_GLASS
            if (block == MCIBlocks.POWERED_PUMP.get()) {
                pumpCount++;
                return super.validateFrame(ctx, pos, state, CasingType.OTHER, false);
            } else if (block == MekanismBlocks.STRUCTURAL_GLASS.get()) {
                return super.validateFrame(ctx, pos, state, CasingType.OTHER, false);
            }
            return FormationResult.fail(Component.translatable("gui.mekanism_complex_industries.extractor.invalid_floor"), true);
        } else {
            // Top and sides (non-edge) can only be CASING, PORT, or STRUCTURAL_GLASS
            if (block == MCIBlocks.FLUID_EXTRACTOR_PORT.get()) {
                portCount++;
                return super.validateFrame(ctx, pos, state, CasingType.VALVE, false);
            } else if (block == MCIBlocks.FLUID_EXTRACTOR_CASING.get()) {
                return super.validateFrame(ctx, pos, state, CasingType.OTHER, false);
            } else if (block == MekanismBlocks.STRUCTURAL_GLASS.get()) {
                return super.validateFrame(ctx, pos, state, CasingType.OTHER, false);
            } else if (block == MCIBlocks.POWERED_PUMP.get()) {
                return FormationResult.fail(Component.translatable("gui.mekanism_complex_industries.extractor.pump_must_be_floor"), true);
            }
            return FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_FRAME, pos);
        }
    }

    @Override
    protected CasingType getCasingType(BlockState state) {
        Block block = state.getBlock();
        if (block == MCIBlocks.FLUID_EXTRACTOR_CASING.get()) {
            return CasingType.FRAME;
        } else if (block == MCIBlocks.FLUID_EXTRACTOR_PORT.get()) {
            return CasingType.VALVE;
        } else if (block == MCIBlocks.POWERED_PUMP.get() || block == MekanismBlocks.STRUCTURAL_GLASS.get()) {
            return CasingType.OTHER;
        }
        return CasingType.INVALID;
    }

    @Override
    public FormationResult postcheck(FluidExtractorMultiblockData structure, Long2ObjectMap<ChunkAccess> chunkMap) {
        if (pumpCount <= 0) {
            return FormationResult.fail(Component.translatable("gui.mekanism_complex_industries.extractor.no_pump"));
        }
        if (portCount <= 0) {
            return FormationResult.fail(Component.translatable("gui.mekanism_complex_industries.extractor.no_port"));
        }
        return FormationResult.SUCCESS;
    }
}
