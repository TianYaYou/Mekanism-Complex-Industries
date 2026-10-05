package com.complexindustries.mekanism.content.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

public class SemiFinishedChipItem extends Item {

    public static final int MAX_PROGRESS = 5;

    private final ChatFormatting formatting;
    private final int barColor;

    public SemiFinishedChipItem(Properties properties, ChatFormatting formatting, int barColor) {
        super(properties.stacksTo(1));
        this.formatting = formatting;
        this.barColor = barColor;
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(formatting);
    }

    public static int getCoatingProgress(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null && customData.contains("coating_progress")) {
            return Math.min(MAX_PROGRESS, Math.max(0, customData.copyTag().getInt("coating_progress")));
        }
        return 0;
    }

    public static ItemStack withProgress(ItemStack stack, int progress) {
        ItemStack copy = stack.copyWithCount(1);
        CustomData.update(DataComponents.CUSTOM_DATA, copy, tag -> tag.putInt("coating_progress", Math.min(MAX_PROGRESS, progress)));
        return copy;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int progress = getCoatingProgress(stack);
        return Math.round(13.0F * progress / (float) MAX_PROGRESS);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return barColor;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        int progress = getCoatingProgress(stack);
        tooltipComponents.add(Component.translatable("tooltip.mekanism_complex_industries.coating_progress", progress, MAX_PROGRESS)
                .withStyle(ChatFormatting.GRAY));
    }
}
