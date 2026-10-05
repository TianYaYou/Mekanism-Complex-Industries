package com.complexindustries.mekanism.content.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ColoredItem extends Item {

    private final ChatFormatting formatting;

    public ColoredItem(Properties properties, ChatFormatting formatting) {
        super(properties);
        this.formatting = formatting;
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(formatting);
    }
}
