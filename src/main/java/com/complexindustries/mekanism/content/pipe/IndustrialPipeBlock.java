package com.complexindustries.mekanism.content.pipe;

import com.complexindustries.mekanism.content.pipe.attachment.IPipeAttachment;
import com.complexindustries.mekanism.content.pipe.attachment.InputInterfaceAttachment;
import com.complexindustries.mekanism.content.pipe.attachment.OutputInterfaceAttachment;
import com.complexindustries.mekanism.content.pipe.attachment.PowerInterfaceAttachment;
import com.complexindustries.mekanism.content.pipe.interfaces.IPipeNode;
import com.complexindustries.mekanism.registration.MCIItems;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class IndustrialPipeBlock extends Block implements EntityBlock {

    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;

    public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = Map.of(
            Direction.DOWN, DOWN,
            Direction.UP, UP,
            Direction.NORTH, NORTH,
            Direction.SOUTH, SOUTH,
            Direction.WEST, WEST,
            Direction.EAST, EAST
    );

    // Bounding boxes
    private static final VoxelShape CORE_SHAPE = Block.box(5, 5, 5, 11, 11, 11);
    private static final Map<Direction, VoxelShape> ARM_SHAPES = Map.of(
            Direction.DOWN, Block.box(5, 0, 5, 11, 5, 11),
            Direction.UP, Block.box(5, 11, 5, 11, 16, 11),
            Direction.NORTH, Block.box(5, 5, 0, 11, 11, 5),
            Direction.SOUTH, Block.box(5, 5, 11, 11, 11, 16),
            Direction.WEST, Block.box(0, 5, 5, 5, 11, 11),
            Direction.EAST, Block.box(11, 5, 5, 16, 11, 11)
    );

    private static final Map<Direction, VoxelShape> PLATE_SHAPES = Map.of(
            Direction.DOWN, Block.box(2, 0, 2, 14, 2, 14),
            Direction.UP, Block.box(2, 14, 2, 14, 16, 14),
            Direction.NORTH, Block.box(2, 2, 0, 14, 14, 2),
            Direction.SOUTH, Block.box(2, 2, 14, 14, 14, 16),
            Direction.WEST, Block.box(0, 2, 2, 2, 14, 14),
            Direction.EAST, Block.box(14, 2, 2, 16, 14, 14)
    );

    public IndustrialPipeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(DOWN, false)
                .setValue(UP, false)
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(EAST, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DOWN, UP, NORTH, SOUTH, WEST, EAST);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new TileEntityIndustrialPipe(pos, state);
    }

    @NotNull
    @Override
    public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        VoxelShape shape = CORE_SHAPE;
        BlockEntity be = level.getBlockEntity(pos);
        TileEntityIndustrialPipe pipe = be instanceof TileEntityIndustrialPipe p ? p : null;

        for (Direction dir : Direction.values()) {
            boolean hasAtt = pipe != null && pipe.getAttachment(dir) != null;
            if (hasAtt) {
                shape = Shapes.or(shape, PLATE_SHAPES.get(dir));
            } else if (state.getValue(PROPERTY_BY_DIRECTION.get(dir))) {
                shape = Shapes.or(shape, ARM_SHAPES.get(dir));
            }
        }
        return shape;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return updateConnections(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @NotNull
    @Override
    public BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState,
                                  @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        return updateConnections(state, level, pos);
    }

    public BlockState updateConnections(BlockState state, LevelAccessor level, BlockPos pos) {
        BlockState result = state;
        BlockEntity be = WorldUtils.getTileEntity(level, pos);
        TileEntityIndustrialPipe pipe = be instanceof TileEntityIndustrialPipe p ? p : null;

        for (Direction dir : Direction.values()) {
            boolean connects = false;
            if (pipe != null && pipe.getAttachment(dir) != null) {
                // If attachment is present, arm connection is suppressed in favor of attachment plate
                connects = false;
            } else {
                BlockPos neighborPos = pos.relative(dir);
                if (WorldUtils.isBlockLoaded(level, neighborPos)) {
                    BlockState neighborState = level.getBlockState(neighborPos);
                    BlockEntity neighbor = WorldUtils.getTileEntity(level, neighborPos);
                    if (neighbor instanceof IPipeNode node) {
                        connects = node.canConnectPipe(dir.getOpposite());
                    } else if (neighborState.getBlock() instanceof IndustrialPipeBlock ||
                               neighborState.getBlock() instanceof BlockInputInterface ||
                               neighborState.getBlock() instanceof BlockOutputInterface ||
                               neighborState.getBlock() instanceof BlockPowerInterface) {
                        connects = true;
                    }
                }
            }
            result = result.setValue(PROPERTY_BY_DIRECTION.get(dir), connects);
        }
        return result;
    }

    @Override
    protected void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                   @NotNull Block neighborBlock, @NotNull BlockPos neighborPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, isMoving);
        BlockState updated = updateConnections(state, level, pos);
        if (updated != state) {
            level.setBlock(pos, updated, 3);
        }
    }

    @Override
    protected void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                           @NotNull BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        level.updateNeighborsAt(pos, this);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof IPipeNode node) {
            com.complexindustries.mekanism.content.pipe.network.PipeNetworkManager.onNodeAdded(level, pos, node);
        }
    }

    @Nullable
    public static IPipeAttachment getAttachmentAt(TileEntityIndustrialPipe pipe, BlockHitResult hitResult) {
        Direction hitFace = hitResult.getDirection();
        if (pipe.getAttachment(hitFace) != null) {
            return pipe.getAttachment(hitFace);
        }

        BlockPos pos = pipe.getNodePos();
        double rx = hitResult.getLocation().x - pos.getX();
        double ry = hitResult.getLocation().y - pos.getY();
        double rz = hitResult.getLocation().z - pos.getZ();

        IPipeAttachment best = null;
        double bestDistSq = Double.MAX_VALUE;
        int attachCount = 0;
        IPipeAttachment singleAttachment = null;

        for (Direction dir : Direction.values()) {
            IPipeAttachment att = pipe.getAttachment(dir);
            if (att != null) {
                attachCount++;
                singleAttachment = att;
                VoxelShape shape = PLATE_SHAPES.get(dir);
                if (shape != null) {
                    AABB box = shape.bounds().inflate(0.0625);
                    if (box.contains(rx, ry, rz)) {
                        return att;
                    }
                    double distSq = box.distanceToSqr(new Vec3(rx, ry, rz));
                    if (distSq < bestDistSq) {
                        bestDistSq = distSq;
                        best = att;
                    }
                }
            }
        }

        if (attachCount == 1 && bestDistSq < 0.5) {
            return singleAttachment;
        }

        return bestDistSq < 0.1 ? best : null;
    }

    @NotNull
    @Override
    protected ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level,
                                              @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand,
                                              @NotNull BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof TileEntityIndustrialPipe pipe)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        Direction hitFace = hitResult.getDirection();
        IPipeAttachment targetAttachment = getAttachmentAt(pipe, hitResult);

        boolean isPartItem = stack.is(MCIItems.INPUT_INTERFACE_PART.get())
                || stack.is(MCIItems.OUTPUT_INTERFACE_PART.get())
                || stack.is(MCIItems.POWER_INTERFACE_PART.get());

        // Check if player is holding an interface part item to attach
        if (isPartItem && targetAttachment == null && pipe.getAttachment(hitFace) == null) {
            IPipeAttachment newAtt = null;
            if (stack.is(MCIItems.INPUT_INTERFACE_PART.get())) {
                newAtt = new InputInterfaceAttachment(pipe, hitFace);
            } else if (stack.is(MCIItems.OUTPUT_INTERFACE_PART.get())) {
                newAtt = new OutputInterfaceAttachment(pipe, hitFace);
            } else if (stack.is(MCIItems.POWER_INTERFACE_PART.get())) {
                newAtt = new PowerInterfaceAttachment(pipe, hitFace);
            }

            if (newAtt != null) {
                if (!level.isClientSide()) {
                    pipe.addAttachment(hitFace, newAtt);
                    if (!player.isCreative()) {
                        stack.shrink(1);
                    }
                    level.playSound(null, pos, SoundEvents.METAL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.setBlock(pos, updateConnections(pipe.getBlockState(), level, pos), 3);
                }
                return ItemInteractionResult.SUCCESS;
            }
        }

        // If player clicked on an existing attachment
        if (targetAttachment != null) {
            // Sneak + empty hand = detach
            if (player.isShiftKeyDown() && stack.isEmpty()) {
                if (!level.isClientSide()) {
                    Direction attFace = targetAttachment.getFace();
                    ItemStack drop = targetAttachment.getDropItem();
                    pipe.removeAttachment(attFace);
                    Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
                    level.playSound(null, pos, SoundEvents.METAL_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.setBlock(pos, updateConnections(pipe.getBlockState(), level, pos), 3);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            }

            // Normal click = open menu
            targetAttachment.openMenu(player);
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @NotNull
    @Override
    protected InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                               @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof TileEntityIndustrialPipe pipe)) {
            return InteractionResult.PASS;
        }

        IPipeAttachment attachment = getAttachmentAt(pipe, hitResult);

        if (attachment != null) {
            // Sneak = detach
            if (player.isShiftKeyDown()) {
                if (!level.isClientSide()) {
                    Direction attFace = attachment.getFace();
                    ItemStack drop = attachment.getDropItem();
                    pipe.removeAttachment(attFace);
                    Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
                    level.playSound(null, pos, SoundEvents.METAL_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.setBlock(pos, updateConnections(pipe.getBlockState(), level, pos), 3);
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }

            // Normal click = open menu
            return attachment.openMenu(player);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                         @NotNull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = WorldUtils.getTileEntity(level, pos);
            if (be instanceof TileEntityIndustrialPipe pipe) {
                if (!level.isClientSide()) {
                    com.complexindustries.mekanism.content.pipe.network.PipeNetworkManager.onNodeRemoved(level, pos, pipe);
                }
                for (IPipeAttachment att : pipe.getAllAttachments()) {
                    if (att != null) {
                        Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, att.getDropItem());
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
