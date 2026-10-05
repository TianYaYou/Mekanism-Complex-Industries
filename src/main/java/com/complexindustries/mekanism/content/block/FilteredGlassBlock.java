package com.complexindustries.mekanism.content.block;

import com.complexindustries.mekanism.content.glass.TileEntityFilteredGlass;
import com.complexindustries.mekanism.registration.MCITileEntityTypes;
import mekanism.api.text.ILangEntry;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ITypeBlock;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FilteredGlassBlock extends Block implements IHasTileEntity<TileEntityFilteredGlass>, ITypeBlock, IHasDescription {

    private final BlockTypeTile<TileEntityFilteredGlass> type;

    public FilteredGlassBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.type = BlockTypeTile.BlockTileBuilder.createBlock(
                () -> MCITileEntityTypes.FILTERED_GLASS,
                () -> "description.mekanism_complex_industries.filtered_glass"
        ).build();
    }

    @NotNull
    @Override
    public ILangEntry getDescription() {
        return this.type.getDescription();
    }

    @Override
    public BlockType getType() {
        return this.type;
    }

    @Override
    public TileEntityTypeRegistryObject<TileEntityFilteredGlass> getTileType() {
        return MCITileEntityTypes.FILTERED_GLASS;
    }

    @Override
    public void stepOn(@NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState state, Entity entity) {
        super.stepOn(world, pos, state, entity);
        if (!world.isClientSide && !entity.isSteppingCarefully() && !entity.fireImmune()) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof TileEntityFilteredGlass glassTile && glassTile.getTemperature() > 373.15) {
                entity.igniteForSeconds(4);
                entity.hurt(world.damageSources().hotFloor(), (float) Math.min(20.0, 1.0 + (glassTile.getTemperature() - 373.15) / 100.0));
            }
        }
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        return adjacentBlockState.is(this) || super.skipRendering(state, adjacentBlockState, side);
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }
}
