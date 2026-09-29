package com.complexindustries.mekanism.content.block;

import com.complexindustries.mekanism.content.freezer.TileEntityFreezerController;
import com.complexindustries.mekanism.content.freezer.TileEntityFreezerValve;
import com.complexindustries.mekanism.registration.MCIContainerTypes;
import com.complexindustries.mekanism.registration.MCITileEntityTypes;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class FreezerValveBlock extends Block implements IHasTileEntity<TileEntityFreezerValve> {

    public FreezerValveBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public TileEntityTypeRegistryObject<TileEntityFreezerValve> getTileType() {
        return MCITileEntityTypes.FREEZER_VALVE;
    }

    @Nullable
    @Override
    public TileEntityFreezerValve newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityFreezerValve(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return createTickerHelper(type, MCITileEntityTypes.FREEZER_VALVE_BE.get(), TileEntityMekanism::tickClient);
        }
        return createTickerHelper(type, MCITileEntityTypes.FREEZER_VALVE_BE.get(), TileEntityMekanism::tickServer);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
            BlockEntityType<A> givenType, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
        return expectedType == givenType ? (BlockEntityTicker<A>) ticker : null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof TileEntityFreezerValve tile) {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.isEmpty() && MekanismUtils.canUseAsWrench(stack)) {
                if (!level.isClientSide) {
                    WrenchResult result = tile.tryWrench(state, player, hand, hit);
                    if (result != WrenchResult.PASS) {
                        return InteractionResult.SUCCESS;
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (player.isShiftKeyDown()) {
                return InteractionResult.PASS;
            }
            if (!tile.getMultiblock().isFormed()) {
                if (!stack.isEmpty()) {
                    return InteractionResult.PASS;
                }
                if (!level.isClientSide) {
                    FormationResult result = tile.getStructure().runUpdate(tile);
                    if (!result.isFormed() && result.getResultText() != null) {
                        player.sendSystemMessage(result.getResultText());
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (!level.isClientSide) {
                for (BlockPos location : tile.getMultiblock().locations) {
                    if (level.getBlockEntity(location) instanceof TileEntityFreezerController controller) {
                        NetworkHooks.openScreen(
                                (ServerPlayer) player,
                                MCIContainerTypes.FREEZER_CONTROLLER.getProvider(controller.getDisplayName(), controller),
                                location
                        );
                        break;
                    }
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityFreezerValve tile) {
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
