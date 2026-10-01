package com.complexindustries.mekanism.content.block;

import com.complexindustries.mekanism.content.refinery.TileEntityRefineryController;
import com.complexindustries.mekanism.registration.MCITileEntityTypes;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RefineryControllerBlock extends Block implements IHasTileEntity<TileEntityRefineryController> {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public RefineryControllerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVE, false));
    }

    @Override
    public TileEntityTypeRegistryObject<TileEntityRefineryController> getTileType() {
        return MCITileEntityTypes.REFINERY_CONTROLLER;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(ACTIVE, false);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    @NotNull
    @Override
    protected ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player,
            @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof TileEntityRefineryController tile) {
            if (MekanismUtils.canUseAsWrench(stack)) {
                if (!level.isClientSide) {
                    WrenchResult result = tile.tryWrench(state, player, stack);
                    if (result != WrenchResult.PASS) {
                        return ItemInteractionResult.SUCCESS;
                    }
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
            if (!player.isShiftKeyDown() && tile.getMultiblock().isFormed()) {
                if (!level.isClientSide) {
                    player.openMenu(
                            com.complexindustries.mekanism.registration.MCIContainerTypes.REFINERY_CONTROLLER.getProvider(tile.getDisplayName(), tile, false),
                            buf -> buf.writeBlockPos(pos)
                    );
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @NotNull
    @Override
    protected net.minecraft.world.InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof TileEntityRefineryController tile) {
            if (player.isShiftKeyDown()) {
                return net.minecraft.world.InteractionResult.PASS;
            }
            if (!tile.getMultiblock().isFormed()) {
                if (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()) {
                    return net.minecraft.world.InteractionResult.PASS;
                }
                if (!level.isClientSide) {
                    mekanism.common.lib.multiblock.FormationProtocol.FormationResult result = tile.getStructure().runUpdate(tile);
                    if (!result.isFormed() && result.getResultText() != null) {
                        player.sendSystemMessage(result.getResultText());
                    }
                }
                return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (!level.isClientSide) {
                player.openMenu(
                        com.complexindustries.mekanism.registration.MCIContainerTypes.REFINERY_CONTROLLER.getProvider(tile.getDisplayName(), tile, false),
                        buf -> buf.writeBlockPos(pos)
                );
            }
            return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
        }
        return net.minecraft.world.InteractionResult.PASS;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        net.minecraft.world.level.block.entity.BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityRefineryController tile) {
            tile.onNeighborChange(block, fromPos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            net.minecraft.world.level.block.entity.BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof mekanism.common.tile.base.TileEntityMekanism tile) {
                tile.blockRemoved();
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
