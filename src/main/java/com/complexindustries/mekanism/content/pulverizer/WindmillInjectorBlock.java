package com.complexindustries.mekanism.content.pulverizer;

import com.complexindustries.mekanism.registration.MCIContainerTypes;
import com.complexindustries.mekanism.registration.MCITileEntityTypes;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class WindmillInjectorBlock extends Block implements IHasTileEntity<TileEntityWindmillInjector> {

    public WindmillInjectorBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public TileEntityTypeRegistryObject<TileEntityWindmillInjector> getTileType() {
        return MCITileEntityTypes.WINDMILL_INJECTOR;
    }

    @NotNull
    @Override
    protected ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player,
            @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof TileEntityWindmillInjector tile) {
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
                            MCIContainerTypes.WINDMILL_PULVERIZER.getProvider(tile.getDisplayName(), tile, false),
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
        if (level.getBlockEntity(pos) instanceof TileEntityWindmillInjector tile) {
            if (player.isShiftKeyDown()) {
                return net.minecraft.world.InteractionResult.PASS;
            }
            if (tile.getMultiblock().isFormed()) {
                if (!level.isClientSide) {
                    player.openMenu(
                            MCIContainerTypes.WINDMILL_PULVERIZER.getProvider(tile.getDisplayName(), tile, false),
                            buf -> buf.writeBlockPos(pos)
                    );
                }
                return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return net.minecraft.world.InteractionResult.PASS;
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block neighborBlock, @NotNull BlockPos neighborPos, boolean isMoving) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof TileEntityWindmillInjector tile) {
            if (!tile.getMultiblock().isFormed()) {
                FormationResult result = tile.getStructure().runUpdate(tile);
                if (!result.isFormed() && result.getResultText() != null) {
                    Player player = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 8.0, false);
                    if (player != null) {
                        player.displayClientMessage(result.getResultText(), true);
                    }
                }
            }
        }
    }
}
