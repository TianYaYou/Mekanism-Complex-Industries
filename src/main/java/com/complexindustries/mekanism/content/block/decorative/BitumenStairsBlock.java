package com.complexindustries.mekanism.content.block.decorative;

import mekanism.api.text.ILangEntry;
import mekanism.common.block.interfaces.IHasDescription;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class BitumenStairsBlock extends StairBlock implements IHasDescription {

    public BitumenStairsBlock(BlockState baseState, BlockBehaviour.Properties properties) {
        super(baseState, properties);
    }

    @NotNull
    @Override
    public ILangEntry getDescription() {
        return () -> "description.mekanism_complex_industries.bitumen_stairs";
    }
}
