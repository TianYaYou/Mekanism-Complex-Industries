package com.complexindustries.mekanism.content.pipe.client;

import com.complexindustries.mekanism.content.pipe.container.ContainerInputInterface;
import com.complexindustries.mekanism.content.pipe.interfaces.IInputInterface;
import com.complexindustries.mekanism.network.MCIPacketHandler;
import com.complexindustries.mekanism.network.PacketSetInterfacePriority;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MekanismButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GuiInputInterface extends GuiMekanism<ContainerInputInterface> {

    public GuiInputInterface(ContainerInputInterface container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        this.dynamicSlots = true;
        this.titleLabelY = 5;
        this.inventoryLabelY = 73;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();

        // 1. High-Tech CRT Monitor Screen (16, 17, 144, 32)
        addRenderableWidget(new GuiInnerScreen(this, 16, 17, 144, 32, () -> List.of(
                Component.literal("模式: 被动接收 (零缓冲即时路由)").withStyle(ChatFormatting.AQUA),
                Component.literal("当前网络优先级: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.valueOf(menu.getPriority())).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
        )).spacing(2));

        // 2. Priority Adjustment Buttons below the screen (y = 52)
        addRenderableWidget(new MekanismButton(this, 18, 52, 32, 18, Component.literal("-10"),
                (element, mouseX, mouseY) -> { adjustPriority(-10); return true; }));
        addRenderableWidget(new MekanismButton(this, 54, 52, 28, 18, Component.literal("-1"),
                (element, mouseX, mouseY) -> { adjustPriority(-1); return true; }));
        addRenderableWidget(new MekanismButton(this, 94, 52, 28, 18, Component.literal("+1"),
                (element, mouseX, mouseY) -> { adjustPriority(1); return true; }));
        addRenderableWidget(new MekanismButton(this, 126, 52, 32, 18, Component.literal("+10"),
                (element, mouseX, mouseY) -> { adjustPriority(10); return true; }));
    }

    private void adjustPriority(int delta) {
        IInputInterface in = menu.getInterfaceInstance();
        if (in != null) {
            boolean isAtt = in.getAttachedFace() != null;
            MCIPacketHandler.sendToServer(new PacketSetInterfacePriority(
                    in.getInterfacePos(), isAtt, in.getAttachedFace(), delta));
        } else {
            MCIPacketHandler.sendToServer(new PacketSetInterfacePriority(
                    menu.getTargetPos(), menu.isAttachment(), menu.getAttachedFace(), delta));
        }
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}
