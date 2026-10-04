package com.complexindustries.mekanism.content.flowregulator;

import com.complexindustries.mekanism.registration.MCIContainerTypes;
import com.complexindustries.mekanism.registration.MCITileEntityTypes;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ITypeBlock;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockFlowRegulator extends Block implements IHasTileEntity<TileEntityFlowRegulator>, ITypeBlock, IHasDescription {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    // Voxel shapes: 10x10 casing, 12x12 ring in middle
    private static final VoxelShape SHAPE_Z = Shapes.or(
            Block.box(3, 3, 0, 13, 13, 16),
            Block.box(2, 2, 7, 14, 14, 9)
    );
    private static final VoxelShape SHAPE_X = Shapes.or(
            Block.box(0, 3, 3, 16, 13, 13),
            Block.box(7, 2, 2, 9, 14, 14)
    );
    private static final VoxelShape SHAPE_Y = Shapes.or(
            Block.box(3, 0, 3, 13, 16, 13),
            Block.box(2, 7, 2, 14, 9, 14)
    );

    private final BlockTypeTile<TileEntityFlowRegulator> type;

    public BlockFlowRegulator(BlockBehaviour.Properties properties) {
        super(properties);
        this.type = BlockTypeTile.BlockTileBuilder.createBlock(
                () -> MCITileEntityTypes.FLOW_REGULATOR,
                () -> "description.mekanism_complex_industries.flow_regulator"
        )
        .with(Attributes.REDSTONE)
        .with(new AttributeStateFacing(BlockStateProperties.FACING))
        .build();
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockTypeTile<TileEntityFlowRegulator> getType() {
        return type;
    }

    @Override
    public TileEntityTypeRegistryObject<TileEntityFlowRegulator> getTileType() {
        return MCITileEntityTypes.FLOW_REGULATOR;
    }

    @NotNull
    @Override
    public mekanism.api.text.ILangEntry getDescription() {
        return this.type.getDescription();
    }

    @NotNull
    @Override
    public String getDescriptionId() {
        return "block.mekanism_complex_industries.flow_regulator";
    }

    @NotNull
    @Override
    public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        Direction facing = state.getValue(FACING);
        return switch (facing.getAxis()) {
            case X -> SHAPE_X;
            case Y -> SHAPE_Y;
            case Z -> SHAPE_Z;
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection());
    }

    @NotNull
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @NotNull
    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @NotNull
    @Override
    protected ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level,
            @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (stack.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.getBlockEntity(pos) instanceof TileEntityFlowRegulator tile) {
            // 1. Right-click with dye: dye the ring
            if (stack.getItem() instanceof DyeItem dyeItem) {
                if (tile.getRingColor() != dyeItem.getDyeColor()) {
                    if (!level.isClientSide) {
                        tile.setRingColor(dyeItem.getDyeColor());
                        if (!player.isCreative()) {
                            stack.shrink(1);
                        }
                        level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                }
                return ItemInteractionResult.CONSUME;
            }

            // 2. Wrench / configurator interaction
            if (MekanismUtils.canUseAsWrench(stack)) {
                if (!level.isClientSide) {
                    WrenchResult result = tile.tryWrench(state, player, stack);
                    if (result != WrenchResult.PASS) {
                        return ItemInteractionResult.SUCCESS;
                    }
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @NotNull
    @Override
    protected InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
            @NotNull Player player, @NotNull BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof TileEntityFlowRegulator tile) {
            if (player.isShiftKeyDown()) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide) {
                player.openMenu(
                        MCIContainerTypes.FLOW_REGULATOR.getProvider(tile.getDisplayName(), tile, false),
                        buf -> buf.writeBlockPos(pos)
                );
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityFlowRegulator tile) {
            tile.onNeighborChange(block, fromPos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TileEntityMekanism tile) {
                tile.blockRemoved();
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
