package com.complexindustries.mekanism.content.pulverizer;

import com.complexindustries.mekanism.registration.MCIBlocks;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import mekanism.common.MekanismLang;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.lib.multiblock.CuboidStructureValidator;
import mekanism.common.lib.multiblock.FormationProtocol;
import mekanism.common.lib.multiblock.FormationProtocol.CasingType;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.tile.turbine.TileEntityElectromagneticCoil;
import mekanism.generators.common.tile.turbine.TileEntityRotationalComplex;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class WindmillPulverizerValidator extends CuboidStructureValidator<WindmillPulverizerMultiblockData> {

    private static final VoxelCuboid MIN_CUBOID = new VoxelCuboid(5, 5, 5);
    private static final VoxelCuboid MAX_CUBOID = new VoxelCuboid(17, 18, 17);

    private int injectorCount = 0;
    private int outputPortCount = 0;

    public WindmillPulverizerValidator() {
        super(MIN_CUBOID, MAX_CUBOID);
    }

    @Override
    public boolean precheck() {
        injectorCount = 0;
        outputPortCount = 0;
        return super.precheck();
    }

    @Override
    protected FormationResult validateFrame(FormationProtocol<WindmillPulverizerMultiblockData> ctx, BlockPos pos, BlockState state, CasingType type, boolean needsFrame) {
        Block block = state.getBlock();
        boolean isFloor = pos.getY() == cuboid.getMinPos().getY();

        if (needsFrame) {
            // Frame edges must strictly be FLUID_EXTRACTOR_CASING (保压外壳)
            if (block != MCIBlocks.FLUID_EXTRACTOR_CASING.get()) {
                return FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_FRAME, pos);
            }
            return super.validateFrame(ctx, pos, state, CasingType.FRAME, true);
        }

        if (isFloor) {
            // Floor can be Pressure Casing or Structural Glass
            if (block == MCIBlocks.FLUID_EXTRACTOR_CASING.get() || block == MekanismBlocks.STRUCTURAL_GLASS.get()) {
                return super.validateFrame(ctx, pos, state, CasingType.OTHER, false);
            }
            return FormationResult.fail(Component.translatable("gui.mekanism_complex_industries.pulverizer.invalid_floor"), true);
        } else {
            // Top and sides can be Pressure Casing, Structural Glass, Injector, or Output Port
            if (block == MCIBlocks.WINDMILL_INJECTOR.get()) {
                injectorCount++;
                return super.validateFrame(ctx, pos, state, CasingType.VALVE, false);
            } else if (block == MCIBlocks.WINDMILL_OUTPUT_PORT.get()) {
                outputPortCount++;
                return super.validateFrame(ctx, pos, state, CasingType.VALVE, false);
            } else if (block == MCIBlocks.FLUID_EXTRACTOR_CASING.get() || block == MekanismBlocks.STRUCTURAL_GLASS.get()) {
                return super.validateFrame(ctx, pos, state, CasingType.OTHER, false);
            }
            return FormationResult.fail(Component.translatable("gui.mekanism_complex_industries.pulverizer.invalid_wall"), true);
        }
    }

    @Override
    protected CasingType getCasingType(BlockState state) {
        Block block = state.getBlock();
        if (block == MCIBlocks.FLUID_EXTRACTOR_CASING.get()) {
            return CasingType.FRAME;
        } else if (block == MCIBlocks.WINDMILL_INJECTOR.get() || block == MCIBlocks.WINDMILL_OUTPUT_PORT.get()) {
            return CasingType.VALVE;
        } else if (block == MekanismBlocks.STRUCTURAL_GLASS.get()) {
            return CasingType.OTHER;
        }
        return CasingType.INVALID;
    }

    @Override
    protected boolean validateInner(BlockState state, Long2ObjectMap<ChunkAccess> chunkMap, BlockPos pos) {
        return super.validateInner(state, chunkMap, pos) ||
               state.is(MekanismBlocks.PRESSURE_DISPERSER.get()) ||
               state.is(mekanism.generators.common.registries.GeneratorsBlocks.TURBINE_ROTOR.get()) ||
               state.is(mekanism.generators.common.registries.GeneratorsBlocks.ROTATIONAL_COMPLEX.get()) ||
               state.is(mekanism.generators.common.registries.GeneratorsBlocks.ELECTROMAGNETIC_COIL.get());
    }

    @Override
    public FormationResult postcheck(WindmillPulverizerMultiblockData data, Long2ObjectMap<ChunkAccess> chunkMap) {
        int lengthX = cuboid.length();
        int lengthZ = cuboid.width();
        int height = cuboid.height();

        // 1. Dimensions check: odd width & length
        if (lengthX % 2 == 0 || lengthZ % 2 == 0) {
            return FormationResult.fail(Component.translatable("gui.mekanism_complex_industries.pulverizer.even_dimensions"), true);
        }

        if (injectorCount < 1) {
            return FormationResult.fail(Component.translatable("gui.mekanism_complex_industries.pulverizer.missing_injector"), true);
        }
        if (outputPortCount < 1) {
            return FormationResult.fail(Component.translatable("gui.mekanism_complex_industries.pulverizer.missing_output_port"), true);
        }

        BlockPos minPos = cuboid.getMinPos();
        BlockPos maxPos = cuboid.getMaxPos();
        int centerX = minPos.getX() + lengthX / 2;
        int centerZ = minPos.getZ() + lengthZ / 2;

        // 2. Find Rotational Complex along central axis
        int complexY = -1;
        for (int y = minPos.getY() + 2; y <= maxPos.getY() - 2; y++) {
            BlockPos p = new BlockPos(centerX, y, centerZ);
            BlockEntity tile = WorldUtils.getTileEntity(world, chunkMap, p);
            if (tile instanceof TileEntityRotationalComplex) {
                complexY = y;
                break;
            }
        }

        if (complexY == -1) {
            return FormationResult.fail(Component.translatable("gui.mekanism_complex_industries.pulverizer.missing_complex"), true);
        }

        // 3. Validate Lower Section: Turbine Rotors below Rotational Complex
        int totalBlades = 0;
        int rotorHeight = 0;
        for (int y = minPos.getY() + 1; y < complexY; y++) {
            BlockPos p = new BlockPos(centerX, y, centerZ);
            BlockEntity tile = WorldUtils.getTileEntity(world, chunkMap, p);
            if (!(tile instanceof TileEntityTurbineRotor rotorTile)) {
                return FormationResult.fail(Component.translatable("gui.mekanism_complex_industries.pulverizer.invalid_rotor"), true);
            }
            totalBlades += rotorTile.getHousedBlades();
            rotorHeight++;
        }

        if (rotorHeight == 0) {
            return FormationResult.fail(Component.translatable("gui.mekanism_complex_industries.pulverizer.missing_rotor"), true);
        }

        // Check that all other blocks in lower section are air
        for (int x = minPos.getX() + 1; x < maxPos.getX(); x++) {
            for (int z = minPos.getZ() + 1; z < maxPos.getZ(); z++) {
                if (x == centerX && z == centerZ) {
                    continue;
                }
                for (int y = minPos.getY() + 1; y < complexY; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState s = world.getBlockState(p);
                    if (!s.isAir()) {
                        return FormationResult.fail(Component.translatable("gui.mekanism_complex_industries.pulverizer.lower_not_air"), true);
                    }
                }
            }
        }

        // 4. Validate Complex Layer: Pressure Dispersers horizontal plane
        for (int x = minPos.getX() + 1; x < maxPos.getX(); x++) {
            for (int z = minPos.getZ() + 1; z < maxPos.getZ(); z++) {
                if (x == centerX && z == centerZ) {
                    continue; // Rotational complex is here
                }
                BlockPos p = new BlockPos(x, complexY, z);
                BlockState s = world.getBlockState(p);
                if (!s.is(MekanismBlocks.PRESSURE_DISPERSER.get())) {
                    return FormationResult.fail(Component.translatable("gui.mekanism_complex_industries.pulverizer.missing_disperser"), true);
                }
            }
        }

        // 5. Validate Upper Section: Electromagnetic Coils and Air
        // At least 1 coil must be directly above the complex
        BlockPos coilBase = new BlockPos(centerX, complexY + 1, centerZ);
        BlockEntity baseCoil = WorldUtils.getTileEntity(world, chunkMap, coilBase);
        if (!(baseCoil instanceof TileEntityElectromagneticCoil)) {
            return FormationResult.fail(Component.translatable("gui.mekanism_complex_industries.pulverizer.missing_coil"), true);
        }

        // Traverse all contiguous coils
        Set<BlockPos> coils = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        coils.add(coilBase);
        queue.add(coilBase);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir);
                if (neighbor.getY() > complexY && neighbor.getY() < maxPos.getY() &&
                    neighbor.getX() > minPos.getX() && neighbor.getX() < maxPos.getX() &&
                    neighbor.getZ() > minPos.getZ() && neighbor.getZ() < maxPos.getZ() &&
                    !coils.contains(neighbor)) {
                    BlockEntity te = WorldUtils.getTileEntity(world, chunkMap, neighbor);
                    if (te instanceof TileEntityElectromagneticCoil) {
                        coils.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        // Verify remaining space in upper section is air
        for (int x = minPos.getX() + 1; x < maxPos.getX(); x++) {
            for (int z = minPos.getZ() + 1; z < maxPos.getZ(); z++) {
                for (int y = complexY + 1; y < maxPos.getY(); y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (coils.contains(p)) {
                        continue;
                    }
                    BlockState s = world.getBlockState(p);
                    if (!s.isAir()) {
                        return FormationResult.fail(Component.translatable("gui.mekanism_complex_industries.pulverizer.upper_not_air"), true);
                    }
                }
            }
        }

        // Structure is fully valid! Update multiblock data with turbine metrics
        data.updateTurbineStats(totalBlades, coils.size(), rotorHeight);
        return FormationResult.SUCCESS;
    }
}
