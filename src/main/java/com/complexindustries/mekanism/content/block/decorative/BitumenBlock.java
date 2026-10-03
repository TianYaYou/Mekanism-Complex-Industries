package com.complexindustries.mekanism.content.block.decorative;

import mekanism.api.text.ILangEntry;
import mekanism.common.block.interfaces.IHasDescription;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.NotNull;

public class BitumenBlock extends Block implements IHasDescription {

    public BitumenBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @NotNull
    @Override
    public ILangEntry getDescription() {
        return () -> "description.mekanism_complex_industries.bitumen_block";
    }
}
