package com.complexindustries.mekanism.content.pipe.client;

import com.complexindustries.mekanism.content.pipe.container.ContainerPowerInterface;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiInnerScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GuiPowerInterface extends GuiMekanism<ContainerPowerInterface> {

    public GuiPowerInterface(ContainerPowerInterface container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        this.dynamicSlots = true;
        this.titleLabelY = 5;
        this.inventoryLabelY = 73;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();

        // 1. High-Tech CRT Monitor Screen
        addRenderableWidget(new GuiInnerScreen(this, 16, 17, 144, 48, () -> List.of(
                Component.literal("模式: 外部电力注入 (按需提取)").withStyle(ChatFormatting.YELLOW),
                Component.literal("网络内部电力缓冲: 0 J / 0 FE").withStyle(ChatFormatting.GREEN),
                Component.literal("特性: 零储能无损即时透传中继").withStyle(ChatFormatting.GRAY),
                Component.literal("状态: 运行就绪 (Standby)").withStyle(ChatFormatting.AQUA)
        )).spacing(2));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}
