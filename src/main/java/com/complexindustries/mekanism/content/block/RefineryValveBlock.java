package com.complexindustries.mekanism.content.block;

import com.complexindustries.mekanism.content.refinery.TileEntityRefineryController;
import com.complexindustries.mekanism.content.refinery.TileEntityRefineryValve;
import com.complexindustries.mekanism.registration.MCIContainerTypes;
import com.complexindustries.mekanism.registration.MCITileEntityTypes;
import mekanism.api.MekanismItemAbilities;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class RefineryValveBlock extends Block implements IHasTileEntity<TileEntityRefineryValve> {

    public static final EnumProperty<TileEntityRefineryValve.ValveMode> MODE =
            EnumProperty.create("mode", TileEntityRefineryValve.ValveMode.class);

    public RefineryValveBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any().setValue(MODE, TileEntityRefineryValve.ValveMode.OUTPUT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(MODE);
    }

    @Override
    public TileEntityTypeRegistryObject<TileEntityRefineryValve> getTileType() {
        return MCITileEntityTypes.REFINERY_VALVE;
    }

    @NotNull
    @Override
    protected ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player,
            @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof TileEntityRefineryValve tile) {
            boolean isConfigTool = stack.is(MekanismTags.Items.CONFIGURATORS)
                    || stack.canPerformAction(MekanismItemAbilities.WRENCH_CONFIGURE)
                    || stack.canPerformAction(MekanismItemAbilities.WRENCH_DISMANTLE)
                    || MekanismUtils.canUseAsWrench(stack);

            if (isConfigTool) {
                if (!level.isClientSide) {
                    if (player.isShiftKeyDown() && MekanismUtils.canUseAsWrench(stack)) {
                        WrenchResult result = tile.tryWrench(state, player, stack);
                        if (result != WrenchResult.PASS) {
                            return ItemInteractionResult.SUCCESS;
                        }
                    } else {
                        tile.cycleMode(player);
                        return ItemInteractionResult.SUCCESS;
                    }
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }

            // Normal right-click with item on formed valve: open controller menu
            if (!player.isShiftKeyDown() && tile.getMultiblock().isFormed()) {
                if (!level.isClientSide) {
                    for (BlockPos location : tile.getMultiblock().locations) {
                        if (level.getBlockEntity(location) instanceof TileEntityRefineryController controller) {
                            player.openMenu(
                                    MCIContainerTypes.REFINERY_CONTROLLER.getProvider(controller.getDisplayName(), controller, false),
                                    buf -> buf.writeBlockPos(location)
                            );
                            break;
                        }
                    }
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @NotNull
    @Override
    protected InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof TileEntityRefineryValve tile) {
            if (player.isShiftKeyDown()) {
                if (tile.getMultiblock().isFormed()) {
                    if (!level.isClientSide) {
                        tile.cycleMode(player);
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
                return InteractionResult.PASS;
            }
            if (!tile.getMultiblock().isFormed()) {
                if (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()) {
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
                    if (level.getBlockEntity(location) instanceof TileEntityRefineryController controller) {
                        player.openMenu(
                                MCIContainerTypes.REFINERY_CONTROLLER.getProvider(controller.getDisplayName(), controller, false),
                                buf -> buf.writeBlockPos(location)
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
        if (blockEntity instanceof TileEntityRefineryValve tile) {
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
