package com.complexindustries.mekanism.content.pipe.client;

import com.complexindustries.mekanism.content.pipe.container.ContainerInputInterface;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiInnerScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GuiInputInterface extends GuiMekanism<ContainerInputInterface> {

    public GuiInputInterface(ContainerInputInterface container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        this.imageHeight = 176;
        this.dynamicSlots = true;
        this.titleLabelY = 5;
        this.inventoryLabelY = 73;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();

        // 1. High-Tech CRT Monitor Screen (16, 17, 144, 48)
        addRenderableWidget(new GuiInnerScreen(this, 16, 17, 144, 48, () -> List.of(
                Component.literal("模式: 被动接收 (零缓冲即时路由)").withStyle(ChatFormatting.AQUA),
                Component.literal("当前网络优先级: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.valueOf(menu.getPriority())).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)),
                Component.literal("红石控制: ").withStyle(ChatFormatting.GRAY)
                        .append(menu.getRedstoneMode().getTextComponent()),
                Component.literal("状态: 运行就绪 (Standby)").withStyle(ChatFormatting.DARK_AQUA)
        )).spacing(2));

        // 2. Right-side Tabs (Redstone at y = 6, Priority at y = 34)
        addRenderableWidget(new GuiInterfaceRedstoneTab(this,
                menu.getTargetPos(), menu.isAttachment(), menu.getAttachedFace(),
                menu::getRedstoneMode,
                mode -> {
                    if (menu.getInterfaceInstance() != null) {
                        menu.getInterfaceInstance().setRedstoneMode(mode);
                    }
                }
        ));

        addRenderableWidget(new GuiInterfacePriorityTab(this,
                menu.getTargetPos(), menu.isAttachment(), menu.getAttachedFace(),
                menu::getPriority,
                val -> {
                    if (menu.getInterfaceInstance() != null) {
                        menu.getInterfaceInstance().setPriority(val);
                    }
                }
        ));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}
