package com.complexindustries.mekanism.content.upgrade;

import java.util.List;
import mekanism.api.Upgrade;
import mekanism.common.item.interfaces.IUpgradeItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

public class ItemPetroleumUpgrade extends Item implements IUpgradeItem {

    public ItemPetroleumUpgrade(Properties properties) {
        super(properties);
    }

    @Override
    public Upgrade getUpgradeType(ItemStack stack) {
        return MCIUpgrades.PETROLEUM;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag isAdvanced) {
        tooltip.add(Component.translatable("tooltip.mekanism_complex_industries.petroleum_upgrade.desc")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.mekanism_complex_industries.petroleum_upgrade.supported")
                .withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("tooltip.mekanism_complex_industries.petroleum_upgrade.max")
                .withStyle(ChatFormatting.DARK_AQUA));
    }
}
