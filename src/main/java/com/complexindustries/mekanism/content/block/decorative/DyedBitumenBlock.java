package com.complexindustries.mekanism.content.block.decorative;

import mekanism.api.text.ILangEntry;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.NotNull;

public class DyedBitumenBlock extends BitumenBlock {

    private final DyeColor color;

    public DyedBitumenBlock(DyeColor color, BlockBehaviour.Properties properties) {
        super(properties);
        this.color = color;
    }

    public DyeColor getColor() {
        return color;
    }

    @NotNull
    @Override
    public ILangEntry getDescription() {
        return () -> "description.mekanism_complex_industries.dyed_bitumen_block";
    }
}
