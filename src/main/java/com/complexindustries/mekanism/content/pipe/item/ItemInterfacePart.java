package com.complexindustries.mekanism.content.pipe.item;

import com.complexindustries.mekanism.content.pipe.attachment.AttachmentType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ItemInterfacePart extends Item {

    private final AttachmentType type;

    public ItemInterfacePart(Properties properties, AttachmentType type) {
        super(properties);
        this.type = type;
    }

    public AttachmentType getAttachmentType() {
        return type;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("description.mekanism_complex_industries." + type.getName() + "_interface_part")
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.mekanism_complex_industries.interface_part_usage")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
