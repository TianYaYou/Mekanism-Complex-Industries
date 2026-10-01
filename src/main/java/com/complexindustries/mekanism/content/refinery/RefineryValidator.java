package com.complexindustries.mekanism.content.refinery;

import com.complexindustries.mekanism.registration.MCIBlocks;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import mekanism.common.MekanismLang;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.lib.multiblock.CuboidStructureValidator;
import mekanism.common.lib.multiblock.FormationProtocol;
import mekanism.common.lib.multiblock.FormationProtocol.CasingType;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.lib.multiblock.FormationProtocol.StructureRequirement;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RefineryValidator extends CuboidStructureValidator<RefineryMultiblockData> {

    private static final VoxelCuboid MIN_CUBOID = new VoxelCuboid(7, 16, 7);
    private static final VoxelCuboid MAX_CUBOID = new VoxelCuboid(7, 41, 7);

    public static final int TYPE_IGNORED = 0;
    public static final int TYPE_TIP = 1;          // [W]  - 4 Tip positions
    public static final int TYPE_DIAGONAL = 2;     // [W!] - 8 Diagonal wall positions
    public static final int TYPE_CENTER = 3;       // (M)  - Center pos (3,3)
    public static final int TYPE_INNER = 4;        // (I)  - Inner cavity positions

    public static final int[][] GRID_TEMPLATE = {
            {0, 0, 0, 1, 0, 0, 0}, // dz = 0
            {0, 0, 2, 4, 2, 0, 0}, // dz = 1
            {0, 2, 4, 4, 4, 2, 0}, // dz = 2
            {1, 4, 4, 3, 4, 4, 1}, // dz = 3
            {0, 2, 4, 4, 4, 2, 0}, // dz = 4
            {0, 0, 2, 4, 2, 0, 0}, // dz = 5
            {0, 0, 0, 1, 0, 0, 0}  // dz = 6
    };

    private boolean foundController = false;

    public RefineryValidator() {
        super(MIN_CUBOID, MAX_CUBOID);
    }

    private static int getMinCoord(Int2ObjectSortedMap<?> minor, Int2ObjectSortedMap<?> major) {
        if (minor.isEmpty()) {
            return major.firstIntKey();
        }
        if (major.isEmpty()) {
            return minor.firstIntKey();
        }
        return Math.min(minor.firstIntKey(), major.firstIntKey());
    }

    private static int getMaxCoord(Int2ObjectSortedMap<?> minor, Int2ObjectSortedMap<?> major) {
        if (minor.isEmpty()) {
            return major.lastIntKey();
        }
        if (major.isEmpty()) {
            return minor.lastIntKey();
        }
        return Math.max(minor.lastIntKey(), major.lastIntKey());
    }

    @Override
    public boolean precheck() {
        foundController = false;
        var minorX = structure.getMinorAxisMap(Structure.Axis.X);
        var majorX = structure.getMajorAxisMap(Structure.Axis.X);
        var minorY = structure.getMinorAxisMap(Structure.Axis.Y);
        var majorY = structure.getMajorAxisMap(Structure.Axis.Y);
        var minorZ = structure.getMinorAxisMap(Structure.Axis.Z);
        var majorZ = structure.getMajorAxisMap(Structure.Axis.Z);

        if ((minorX.isEmpty() && majorX.isEmpty()) ||
            (minorY.isEmpty() && majorY.isEmpty()) ||
            (minorZ.isEmpty() && majorZ.isEmpty())) {
            return false;
        }

        int minX = getMinCoord(minorX, majorX);
        int maxX = getMaxCoord(minorX, majorX);
        int minY = getMinCoord(minorY, majorY);
        int maxY = getMaxCoord(minorY, majorY);
        int minZ = getMinCoord(minorZ, majorZ);
        int maxZ = getMaxCoord(minorZ, majorZ);

        BlockPos minPos = new BlockPos(minX, minY, minZ);
        BlockPos maxPos = new BlockPos(maxX, maxY, maxZ);
        this.cuboid = new VoxelCuboid(minPos, maxPos);

        if (cuboid.length() != 7 || cuboid.width() != 7) {
            return false;
        }
        if (cuboid.height() < 16 || cuboid.height() > 41) {
            return false;
        }
        return true;
    }

    @Override
    protected StructureRequirement getStructureRequirement(BlockPos pos) {
        int dx = pos.getX() - cuboid.getMinPos().getX();
        int dy = pos.getY() - cuboid.getMinPos().getY();
        int dz = pos.getZ() - cuboid.getMinPos().getZ();

        if (dx < 0 || dx >= 7 || dz < 0 || dz >= 7) {
            return StructureRequirement.IGNORED;
        }

        int type = GRID_TEMPLATE[dz][dx];
        if (type == TYPE_IGNORED) {
            return StructureRequirement.IGNORED;
        }

        // Base layer is solid casing for all 25 blocks
        if (dy == 0) {
            return StructureRequirement.OTHER;
        }

        // Outer perimeter walls
        if (type == TYPE_TIP || type == TYPE_DIAGONAL) {
            return StructureRequirement.OTHER;
        }

        // Interior positions (Center (M) and Inner (I))
        return StructureRequirement.INNER;
    }

    @Override
    protected CasingType getCasingType(BlockState state) {
        Block block = state.getBlock();
        if (block == MCIBlocks.REFINERY_CASING.get()) {
            return CasingType.FRAME;
        } else if (block == MCIBlocks.REFINERY_VALVE.get()) {
            return CasingType.VALVE;
        } else if (block == MCIBlocks.REFINERY_CONTROLLER.get()) {
            return CasingType.OTHER;
        } else if (block == MekanismBlocks.STRUCTURAL_GLASS.get()) {
            return CasingType.OTHER;
        } else if (block == MCIBlocks.REFINERY_DREDGE_PIPE.get()) {
            return CasingType.OTHER;
        }
        return CasingType.INVALID;
    }

    @Override
    protected FormationResult validateFrame(FormationProtocol<RefineryMultiblockData> ctx, BlockPos pos, BlockState state, CasingType type, boolean needsFrame) {
        int dx = pos.getX() - cuboid.getMinPos().getX();
        int dy = pos.getY() - cuboid.getMinPos().getY();
        int dz = pos.getZ() - cuboid.getMinPos().getZ();
        int gridType = (dx >= 0 && dx < 7 && dz >= 0 && dz < 7) ? GRID_TEMPLATE[dz][dx] : TYPE_IGNORED;
        if (gridType == TYPE_IGNORED) {
            return FormationResult.SUCCESS;
        }

        boolean isController = structure.getTile(pos) instanceof TileEntityRefineryController;
        if (foundController && isController) {
            return FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_CONTROLLER_CONFLICT, pos, true);
        }
        foundController |= isController;

        // Base layer (dy == 0) must be 100% refinery casing
        if (dy == 0) {
            if (state.getBlock() != MCIBlocks.REFINERY_CASING.get()) {
                return FormationResult.fail(Component.literal("工业炼化塔底部必须全为炼化塔外壳！(" + pos.toShortString() + ")"), true);
            }
        }

        // Top rim (dy == H - 1) outer wall must be refinery casing only (no glass, no valve, no controller)
        if (dy == cuboid.height() - 1) {
            if (state.getBlock() != MCIBlocks.REFINERY_CASING.get()) {
                return FormationResult.fail(Component.literal("工业炼化塔顶部外壁必须全为炼化塔外壳！(" + pos.toShortString() + ")"), true);
            }
        }

        // Diagonal walls [W!] (TYPE_DIAGONAL) can ONLY be structural glass or refinery casing
        if (gridType == TYPE_DIAGONAL) {
            if (state.getBlock() == MCIBlocks.REFINERY_VALVE.get() || state.getBlock() == MCIBlocks.REFINERY_CONTROLLER.get()) {
                return FormationResult.fail(Component.literal("工业炼化塔斜边外壁不可放置接口或控制器！(" + pos.toShortString() + ")"), true);
            }
            if (state.getBlock() != MCIBlocks.REFINERY_CASING.get() && state.getBlock() != MekanismBlocks.STRUCTURAL_GLASS.get()) {
                return FormationResult.fail(Component.literal("工业炼化塔斜边外壁只能由结构玻璃或外壳构成！(" + pos.toShortString() + ")"), true);
            }
        }

        // Tip positions [W] (TYPE_TIP) can ONLY be refinery casing, valve, or controller (structural glass forbidden)
        if (gridType == TYPE_TIP) {
            if (state.getBlock() != MCIBlocks.REFINERY_CASING.get()
                    && state.getBlock() != MCIBlocks.REFINERY_VALVE.get()
                    && state.getBlock() != MCIBlocks.REFINERY_CONTROLLER.get()) {
                return FormationResult.fail(Component.literal("工业炼化塔棱形尖端外壁只能由外壳、接口或控制器构成！(" + pos.toShortString() + ")"), true);
            }
        }

        // Valves and Controller are ONLY permitted on the 4 Tip positions [W]
        if (state.getBlock() == MCIBlocks.REFINERY_VALVE.get() || state.getBlock() == MCIBlocks.REFINERY_CONTROLLER.get()) {
            if (gridType != TYPE_TIP) {
                return FormationResult.fail(Component.literal("工业炼化塔接口与控制器只能安装在正方向的4个尖端外延！(" + pos.toShortString() + ")"), true);
            }
        }

        return super.validateFrame(ctx, pos, state, type, needsFrame);
    }

    @Override
    protected boolean validateInner(BlockState state, Long2ObjectMap<ChunkAccess> chunkMap, BlockPos pos) {
        int dy = pos.getY() - cuboid.getMinPos().getY();

        // Top layer (dy == H - 1) must be open air
        if (dy == cuboid.height() - 1) {
            return state.isAir();
        }

        // Interior can be air, or dredge pipe, or casing for partition floors
        if (state.isAir()) {
            return true;
        }
        return state.getBlock() == MCIBlocks.REFINERY_DREDGE_PIPE.get() || state.getBlock() == MCIBlocks.REFINERY_CASING.get();
    }

    @Override
    public FormationResult postcheck(RefineryMultiblockData structure, Long2ObjectMap<ChunkAccess> chunkMap) {
        if (!foundController) {
            return FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_NO_CONTROLLER);
        }

        BlockPos minPos = cuboid.getMinPos();
        int height = cuboid.height();
        List<Integer> partitionFloors = new ArrayList<>();

        // Step scan each interior layer from dy = 1 to dy = height - 2
        for (int dy = 1; dy < height - 1; dy++) {
            BlockPos centerPos = minPos.offset(3, dy, 3);
            Optional<BlockState> centerStateOpt = WorldUtils.getBlockState(world, chunkMap, centerPos);
            if (centerStateOpt.isEmpty()) {
                return FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_INNER, centerPos);
            }
            BlockState centerState = centerStateOpt.get();

            if (centerState.getBlock() == MCIBlocks.REFINERY_DREDGE_PIPE.get()) {
                // Partition floor candidate: check all 12 other inner positions are refinery casing
                for (int dz = 0; dz < 7; dz++) {
                    for (int dx = 0; dx < 7; dx++) {
                        if (dx == 3 && dz == 3) continue;
                        if (GRID_TEMPLATE[dz][dx] == TYPE_INNER) {
                            BlockPos innerPos = minPos.offset(dx, dy, dz);
                            Optional<BlockState> innerOpt = WorldUtils.getBlockState(world, chunkMap, innerPos);
                            if (innerOpt.isEmpty() || innerOpt.get().getBlock() != MCIBlocks.REFINERY_CASING.get()) {
                                return FormationResult.fail(Component.literal("隔断层除中心疏通管道外必须全部为炼化塔外壳！(" + innerPos.toShortString() + ")"));
                            }
                        }
                    }
                }
                partitionFloors.add(dy);
            } else if (centerState.isAir()) {
                // Working cavity layer: all 12 other inner positions must be air
                for (int dz = 0; dz < 7; dz++) {
                    for (int dx = 0; dx < 7; dx++) {
                        if (dx == 3 && dz == 3) continue;
                        if (GRID_TEMPLATE[dz][dx] == TYPE_INNER) {
                            BlockPos innerPos = minPos.offset(dx, dy, dz);
                            Optional<BlockState> innerOpt = WorldUtils.getBlockState(world, chunkMap, innerPos);
                            if (innerOpt.isEmpty() || !innerOpt.get().isAir()) {
                                return FormationResult.fail(Component.literal("工业炼化塔精馏腔室内必须完全中空！(" + innerPos.toShortString() + ")"));
                            }
                        }
                    }
                }
            } else {
                return FormationResult.fail(Component.literal("精馏腔室内存在非法方块！(" + centerPos.toShortString() + ")"));
            }
        }

        // Validate exactly 4 partition floors
        if (partitionFloors.size() != 4) {
            return FormationResult.fail(Component.literal("工业炼化塔必须恰好由4个疏通管道隔断层分为5层！(当前隔断层数: " + partitionFloors.size() + ")"));
        }

        // Validate each of the 5 layers has height between 3 and 8
        int p0 = partitionFloors.get(0);
        int p1 = partitionFloors.get(1);
        int p2 = partitionFloors.get(2);
        int p3 = partitionFloors.get(3);

        int h1 = p0;
        int h2 = p1 - p0;
        int h3 = p2 - p1;
        int h4 = p3 - p2;
        int h5 = (height - 1) - p3;

        int[] heights = {h1, h2, h3, h4, h5};
        for (int i = 0; i < 5; i++) {
            if (heights[i] < 3 || heights[i] > 8) {
                return FormationResult.fail(Component.literal("工业炼化塔第 " + (i + 1) + " 层高度为 " + heights[i] + "，必须在 3 到 8 格之间！"));
            }
        }

        // Validate outer walls on partition floors: all perimeter blocks (tips and diagonals) must be refinery casing
        for (int p : partitionFloors) {
            for (int dz = 0; dz < 7; dz++) {
                for (int dx = 0; dx < 7; dx++) {
                    int cellType = GRID_TEMPLATE[dz][dx];
                    if (cellType == TYPE_TIP || cellType == TYPE_DIAGONAL) {
                        BlockPos wallPos = minPos.offset(dx, p, dz);
                        Optional<BlockState> wallOpt = WorldUtils.getBlockState(world, chunkMap, wallPos);
                        if (wallOpt.isEmpty() || wallOpt.get().getBlock() != MCIBlocks.REFINERY_CASING.get()) {
                            return FormationResult.fail(Component.literal("隔断层所在平面的外壁必须全部为炼化塔外壳！(" + wallPos.toShortString() + ")"));
                        }
                    }
                }
            }
        }

        structure.setPartitionFloors(partitionFloors);
        return FormationResult.SUCCESS;
    }
}
