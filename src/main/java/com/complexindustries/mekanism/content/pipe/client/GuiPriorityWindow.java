package com.complexindustries.mekanism.content.pipe.client;

import com.complexindustries.mekanism.network.MCIPacketHandler;
import com.complexindustries.mekanism.network.PacketSetInterfacePriority;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class GuiPriorityWindow extends GuiWindow {

    private final BlockPos pos;
    private final boolean isAttachment;
    private final Direction face;
    private final IntSupplier prioritySupplier;
    private final IntConsumer priorityConsumer;

    public GuiPriorityWindow(IGuiWrapper gui, BlockPos pos, boolean isAttachment, @Nullable Direction face,
                             IntSupplier prioritySupplier, IntConsumer priorityConsumer) {
        super(gui, (gui.getXSize() - 146) / 2, 40, 146, 72, WindowType.UNSPECIFIED);
        this.pos = pos;
        this.isAttachment = isAttachment;
        this.face = face;
        this.prioritySupplier = prioritySupplier;
        this.priorityConsumer = priorityConsumer;

        // Inner screen showing current priority
        addChild(new GuiInnerScreen(gui, relativeX + 16, relativeY + 18, 114, 20, () -> List.of(
                Component.literal("网络路由优先级: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.valueOf(this.prioritySupplier.getAsInt()))
                                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
        )).spacing(1));

        // Buttons row
        addChild(new MekanismButton(gui, relativeX + 16, relativeY + 44, 24, 18, Component.literal("-10"),
                (element, mouseX, mouseY) -> { adjust(-10); return true; }));
        addChild(new MekanismButton(gui, relativeX + 44, relativeY + 44, 20, 18, Component.literal("-1"),
                (element, mouseX, mouseY) -> { adjust(-1); return true; }));
        addChild(new MekanismButton(gui, relativeX + 68, relativeY + 44, 20, 18, Component.literal("+1"),
                (element, mouseX, mouseY) -> { adjust(1); return true; }));
        addChild(new MekanismButton(gui, relativeX + 92, relativeY + 44, 24, 18, Component.literal("+10"),
                (element, mouseX, mouseY) -> { adjust(10); return true; }));
        addChild(new MekanismButton(gui, relativeX + 120, relativeY + 44, 16, 18, Component.literal("0"),
                (element, mouseX, mouseY) -> {
                    int current = this.prioritySupplier.getAsInt();
                    adjust(-current);
                    return true;
                }));
    }

    private void adjust(int delta) {
        int current = prioritySupplier.getAsInt();
        priorityConsumer.accept(current + delta);
        MCIPacketHandler.sendToServer(new PacketSetInterfacePriority(pos, isAttachment, face, delta));
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        drawTitleText(guiGraphics, Component.literal("优先级配置"), 6);
    }
}
