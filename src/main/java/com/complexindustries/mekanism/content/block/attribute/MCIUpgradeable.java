package com.complexindustries.mekanism.content.block.attribute;

import java.util.function.Supplier;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.attribute.AttributeUpgradeable;
import mekanism.common.block.states.BlockStateHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class MCIUpgradeable extends AttributeUpgradeable {

    private final Supplier<? extends Holder<Block>> upgradeBlock;

    public MCIUpgradeable(Supplier<? extends Holder<Block>> upgradeBlock) {
        super(() -> null);
        this.upgradeBlock = upgradeBlock;
    }

    @NotNull
    @Override
    public BlockState upgradeResult(@NotNull BlockState current, @NotNull BaseTier tier) {
        return BlockStateHelper.copyStateData(current, upgradeBlock.get());
    }
}
