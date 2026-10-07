package com.complexindustries.mekanism.content.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockPowerInterface extends Block implements EntityBlock {

    public BlockPowerInterface(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new TileEntityPowerInterface(pos, state);
    }

    @NotNull
    @Override
    protected ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level,
                                              @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand,
                                              @NotNull BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof TileEntityPowerInterface tile) {
            tile.openMenu(player);
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @NotNull
    @Override
    protected InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                               @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof TileEntityPowerInterface tile) {
            return tile.openMenu(player);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                           @NotNull BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        level.updateNeighborsAt(pos, this);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof com.complexindustries.mekanism.content.pipe.interfaces.IPipeNode node) {
            com.complexindustries.mekanism.content.pipe.network.PipeNetworkManager.onNodeAdded(level, pos, node);
        }
    }

    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                            @NotNull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide() && level.getBlockEntity(pos) instanceof com.complexindustries.mekanism.content.pipe.interfaces.IPipeNode node) {
                com.complexindustries.mekanism.content.pipe.network.PipeNetworkManager.onNodeRemoved(level, pos, node);
            }
            level.updateNeighborsAt(pos, this);
            super.onRemove(state, level, pos, newState, isMoving);
        } else {
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
