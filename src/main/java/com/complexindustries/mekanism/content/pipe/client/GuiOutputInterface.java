package com.complexindustries.mekanism.content.pipe.client;

import com.complexindustries.mekanism.content.pipe.container.ContainerOutputInterface;
import com.complexindustries.mekanism.content.pipe.interfaces.IOutputInterface;
import com.complexindustries.mekanism.content.pipe.interfaces.OutputInterfaceFilter;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.slot.GuiSequencedSlotDisplay;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GuiOutputInterface extends GuiMekanism<ContainerOutputInterface> {

    private final GuiSequencedSlotDisplay[] sequencedDisplays = new GuiSequencedSlotDisplay[OutputInterfaceFilter.FILTER_SLOTS];
    private final GuiSlot[] filterSlots = new GuiSlot[OutputInterfaceFilter.FILTER_SLOTS];

    public GuiOutputInterface(ContainerOutputInterface container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        this.imageHeight = 176;
        this.dynamicSlots = true;
        this.titleLabelY = 5;
        this.inventoryLabelY = 73;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();

        // 1. High-Tech CRT Screen (16, 17, 144, 26)
        addRenderableWidget(new GuiInnerScreen(this, 16, 17, 144, 26, () -> List.of(
                Component.literal("模式: 白名单分流路由 (零缓冲即时传输)").withStyle(ChatFormatting.GOLD),
                Component.literal("点击下方槽位配置各路输出过滤规则").withStyle(ChatFormatting.GRAY)
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

        // 3. Filter Slots Row (9 slots at y = 48)
        OutputInterfaceFilter filter = menu.getFilter();
        for (int i = 0; i < OutputInterfaceFilter.FILTER_SLOTS; i++) {
            final int slotIndex = i;
            GuiSlot slot = addRenderableWidget(new GuiSlot(SlotType.NORMAL, this, 7 + i * 18, 47)
                    .click((element, mouseX, mouseY) -> {
                        openFilterWindow(slotIndex);
                        return true;
                    })
                    .hover(guiSlot -> getSlotTooltip(slotIndex)));
            filterSlots[i] = slot;

            sequencedDisplays[i] = addRenderableWidget(new GuiSequencedSlotDisplay(this, 8 + i * 18, 48,
                    () -> {
                        if (filter != null) {
                            return filter.getMatchingItemStacks(slotIndex);
                        }
                        return List.of();
                    }));
        }

        refreshSlotDisplays();
    }

    private void openFilterWindow(int slotIndex) {
        OutputInterfaceFilter filter = menu.getFilter();
        if (filter == null) {
            return;
        }
        addWindow(new GuiOutputFilterWindow(this, menu.getTargetPos(), menu.isAttachment(), menu.getAttachedFace(),
                slotIndex, filter, this::refreshSlotDisplays));
    }

    private void refreshSlotDisplays() {
        for (GuiSequencedSlotDisplay display : sequencedDisplays) {
            if (display != null) {
                display.updateStackList();
            }
        }
    }

    private List<Component> getSlotTooltip(int slotIndex) {
        List<Component> tooltip = new ArrayList<>();
        OutputInterfaceFilter filter = menu.getFilter();
        if (filter == null) {
            return tooltip;
        }

        OutputInterfaceFilter.FilterEntry entry = filter.getEntry(slotIndex);
        if (entry.isEmpty()) {
            tooltip.add(Component.literal("槽位 #" + (slotIndex + 1) + ": (未配置)").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("点击打开过滤配置窗口").withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.add(Component.literal("槽位 #" + (slotIndex + 1) + " [" + entry.getType().getDisplayName() + "]").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            if (!entry.getFilterId().isEmpty()) {
                tooltip.add(Component.literal("规则: " + entry.getFilterId()).withStyle(ChatFormatting.AQUA));
            }
            List<ItemStack> matching = filter.getMatchingItemStacks(slotIndex);
            if (!matching.isEmpty()) {
                tooltip.add(Component.literal("匹配数量: " + matching.size() + " 种").withStyle(ChatFormatting.GREEN));
            }
            tooltip.add(Component.literal("点击编辑此槽位").withStyle(ChatFormatting.YELLOW));
        }
        return tooltip;
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);

        // For Chemical filters without an item model, draw chemical text badge
        OutputInterfaceFilter filter = menu.getFilter();
        if (filter != null) {
            for (int i = 0; i < OutputInterfaceFilter.FILTER_SLOTS; i++) {
                OutputInterfaceFilter.FilterEntry entry = filter.getEntry(i);
                if (!entry.isEmpty() && entry.getType() == OutputInterfaceFilter.FilterType.CHEMICAL && entry.getIconStack().isEmpty()) {
                    guiGraphics.drawString(font, "化", 12 + i * 18, 52, 0xFF55FF, true);
                }
            }
        }
    }
}
