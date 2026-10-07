package com.complexindustries.mekanism.content.pipe.client;

import com.complexindustries.mekanism.content.pipe.container.ContainerOutputInterface;
import com.complexindustries.mekanism.content.pipe.interfaces.IOutputInterface;
import com.complexindustries.mekanism.content.pipe.interfaces.OutputInterfaceFilter;
import com.complexindustries.mekanism.network.MCIPacketHandler;
import com.complexindustries.mekanism.network.PacketSetInterfacePriority;
import com.complexindustries.mekanism.network.PacketSetOutputFilter;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GuiOutputInterface extends GuiMekanism<ContainerOutputInterface> {

    public GuiOutputInterface(ContainerOutputInterface container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        this.imageHeight = 186;
        this.dynamicSlots = true;
        this.titleLabelY = 5;
        this.inventoryLabelY = 93;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();

        // 1. High-Tech CRT Monitor Screen (16, 17, 144, 25)
        addRenderableWidget(new GuiInnerScreen(this, 16, 17, 144, 25, () -> List.of(
                Component.literal("模式: 白名单轮询分流弹出 (零缓冲)").withStyle(ChatFormatting.GOLD),
                Component.literal("当前网络优先级: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.valueOf(menu.getPriority())).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
        )).spacing(1));

        // 2. Priority Adjustment Buttons (y = 44)
        addRenderableWidget(new MekanismButton(this, 18, 44, 32, 18, Component.literal("-10"),
                (element, mouseX, mouseY) -> { adjustPriority(-10); return true; }));
        addRenderableWidget(new MekanismButton(this, 54, 44, 28, 18, Component.literal("-1"),
                (element, mouseX, mouseY) -> { adjustPriority(-1); return true; }));
        addRenderableWidget(new MekanismButton(this, 94, 44, 28, 18, Component.literal("+1"),
                (element, mouseX, mouseY) -> { adjustPriority(1); return true; }));
        addRenderableWidget(new MekanismButton(this, 126, 44, 32, 18, Component.literal("+10"),
                (element, mouseX, mouseY) -> { adjustPriority(10); return true; }));

        // 3. 9 Whitelist Filter Slots (y = 69)
        for (int i = 0; i < OutputInterfaceFilter.FILTER_SLOTS; i++) {
            final int slotIndex = i;
            addRenderableWidget(new GuiSlot(SlotType.NORMAL, this, 7 + i * 18, 69))
                    .stored(() -> {
                        OutputInterfaceFilter filter = menu.getFilter();
                        return filter != null ? filter.getStack(slotIndex) : ItemStack.EMPTY;
                    })
                    .hover(slot -> {
                        OutputInterfaceFilter filter = menu.getFilter();
                        ItemStack stack = filter != null ? filter.getStack(slotIndex) : ItemStack.EMPTY;
                        if (!stack.isEmpty()) {
                            return List.of(
                                    stack.getHoverName(),
                                    Component.literal("右键单击清除过滤").withStyle(ChatFormatting.RED)
                            );
                        }
                        return List.of(
                                Component.literal("空过滤槽").withStyle(ChatFormatting.GRAY),
                                Component.literal("持物点击设置物品/流体/化学品白名单").withStyle(ChatFormatting.DARK_GRAY)
                        );
                    });
        }
    }

    private void adjustPriority(int delta) {
        IOutputInterface out = menu.getInterfaceInstance();
        BlockPos pos = out != null ? out.getInterfacePos() : menu.getTargetPos();
        boolean isAtt = out != null ? (out.getAttachedFace() != null) : menu.isAttachment();
        Direction face = out != null ? out.getAttachedFace() : menu.getAttachedFace();
        MCIPacketHandler.sendToServer(new PacketSetInterfacePriority(pos, isAtt, face, delta));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = this.leftPos;
        int y = this.topPos;

        for (int i = 0; i < OutputInterfaceFilter.FILTER_SLOTS; i++) {
            int slotX = x + 7 + i * 18;
            int slotY = y + 69;
            if (mouseX >= slotX && mouseX < slotX + 18 && mouseY >= slotY && mouseY < slotY + 18) {
                IOutputInterface out = menu.getInterfaceInstance();
                BlockPos pos = out != null ? out.getInterfacePos() : menu.getTargetPos();
                boolean isAtt = out != null ? (out.getAttachedFace() != null) : menu.isAttachment();
                Direction face = out != null ? out.getAttachedFace() : menu.getAttachedFace();

                if (button == 1) { // Right click = clear
                    MCIPacketHandler.sendToServer(new PacketSetOutputFilter(pos, isAtt, face, i, ItemStack.EMPTY));
                } else if (button == 0) { // Left click = set from cursor
                    ItemStack carried = menu.getCarried();
                    if (!carried.isEmpty()) {
                        ItemStack filterItem = carried.copyWithCount(1);
                        MCIPacketHandler.sendToServer(new PacketSetOutputFilter(pos, isAtt, face, i, filterItem));
                    }
                }
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}
