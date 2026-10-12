package com.complexindustries.mekanism.content.extractor;

import com.complexindustries.mekanism.content.pulverizer.MCIWindmillPulverizerMultiblock;
import com.complexindustries.mekanism.registration.MCIContainerTypes;
import com.complexindustries.mekanism.registration.MCITileEntityTypes;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
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
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class FluidExtractorCasingBlock extends Block implements IHasTileEntity<TileEntityFluidExtractorCasing> {

    public FluidExtractorCasingBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public TileEntityTypeRegistryObject<TileEntityFluidExtractorCasing> getTileType() {
        return MCITileEntityTypes.FLUID_EXTRACTOR_CASING;
    }

    @NotNull
    @Override
    protected ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player,
            @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof TileEntityFluidExtractorCasing tile) {
            if (MekanismUtils.canUseAsWrench(stack)) {
                if (!level.isClientSide) {
                    WrenchResult result = tile.tryWrench(state, player, stack);
                    if (result != WrenchResult.PASS) {
                        return ItemInteractionResult.SUCCESS;
                    }
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
            if (!player.isShiftKeyDown() && tile.hasFormedMultiblock()) {
                if (!level.isClientSide) {
                    var ext = tile.getStructure(MCIFluidExtractorMultiblock.EXTRACTOR_MANAGER);
                    if (ext != null && ext.getMultiblockData() != null && ext.getMultiblockData().isFormed()) {
                        BlockPos masterPos = ext.getMultiblockData().locations.iterator().next();
                        BlockEntity masterTile = level.getBlockEntity(masterPos);
                        if (masterTile instanceof TileEntityMekanism mekTile) {
                            player.openMenu(
                                    MCIContainerTypes.FLUID_EXTRACTOR.getProvider(tile.getDisplayName(), mekTile, false),
                                    buf -> buf.writeBlockPos(masterPos)
                            );
                            return ItemInteractionResult.sidedSuccess(level.isClientSide);
                        }
                    }
                    var pulv = tile.getStructure(MCIWindmillPulverizerMultiblock.PULVERIZER_MANAGER);
                    if (pulv != null && pulv.getMultiblockData() != null && pulv.getMultiblockData().isFormed()) {
                        for (var valve : pulv.getMultiblockData().valves) {
                            BlockEntity valveTile = level.getBlockEntity(valve.location);
                            if (valveTile instanceof com.complexindustries.mekanism.content.pulverizer.TileEntityWindmillPulverizerCasing pulvTile) {
                                player.openMenu(
                                        MCIContainerTypes.WINDMILL_PULVERIZER.getProvider(tile.getDisplayName(), pulvTile, false),
                                        buf -> buf.writeBlockPos(valve.location)
                                );
                                return ItemInteractionResult.sidedSuccess(level.isClientSide);
                            }
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
        if (level.getBlockEntity(pos) instanceof TileEntityFluidExtractorCasing tile) {
            if (player.isShiftKeyDown()) {
                return InteractionResult.PASS;
            }
            if (tile.hasFormedMultiblock()) {
                if (!level.isClientSide) {
                    var ext = tile.getStructure(MCIFluidExtractorMultiblock.EXTRACTOR_MANAGER);
                    if (ext != null && ext.getMultiblockData() != null && ext.getMultiblockData().isFormed()) {
                        BlockPos masterPos = ext.getMultiblockData().locations.iterator().next();
                        BlockEntity masterTile = level.getBlockEntity(masterPos);
                        if (masterTile instanceof TileEntityMekanism mekTile) {
                            player.openMenu(
                                    MCIContainerTypes.FLUID_EXTRACTOR.getProvider(tile.getDisplayName(), mekTile, false),
                                    buf -> buf.writeBlockPos(masterPos)
                            );
                            return InteractionResult.sidedSuccess(level.isClientSide);
                        }
                    }
                    var pulv = tile.getStructure(MCIWindmillPulverizerMultiblock.PULVERIZER_MANAGER);
                    if (pulv != null && pulv.getMultiblockData() != null && pulv.getMultiblockData().isFormed()) {
                        for (var valve : pulv.getMultiblockData().valves) {
                            BlockEntity valveTile = level.getBlockEntity(valve.location);
                            if (valveTile instanceof com.complexindustries.mekanism.content.pulverizer.TileEntityWindmillPulverizerCasing pulvTile) {
                                player.openMenu(
                                        MCIContainerTypes.WINDMILL_PULVERIZER.getProvider(tile.getDisplayName(), pulvTile, false),
                                        buf -> buf.writeBlockPos(valve.location)
                                );
                                return InteractionResult.sidedSuccess(level.isClientSide);
                            }
                        }
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }
}
