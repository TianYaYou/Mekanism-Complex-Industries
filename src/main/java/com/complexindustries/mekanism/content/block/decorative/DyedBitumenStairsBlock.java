package com.complexindustries.mekanism.content.block.decorative;

import mekanism.api.text.ILangEntry;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class DyedBitumenStairsBlock extends BitumenStairsBlock {

    private final DyeColor color;

    public DyedBitumenStairsBlock(BlockState baseState, DyeColor color, BlockBehaviour.Properties properties) {
        super(baseState, properties);
        this.color = color;
    }

    public DyeColor getColor() {
        return color;
    }

    @NotNull
    @Override
    public ILangEntry getDescription() {
        return () -> "description.mekanism_complex_industries.dyed_bitumen_stairs";
    }
}
