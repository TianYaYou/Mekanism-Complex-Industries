package com.complexindustries.mekanism.content.pipe.client;

import com.complexindustries.mekanism.content.pipe.interfaces.OutputInterfaceFilter;
import com.complexindustries.mekanism.content.pipe.interfaces.OutputInterfaceFilter.FilterCandidate;
import com.complexindustries.mekanism.content.pipe.interfaces.OutputInterfaceFilter.FilterType;
import com.complexindustries.mekanism.network.MCIPacketHandler;
import com.complexindustries.mekanism.network.PacketSetOutputFilter;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.slot.GuiSequencedSlotDisplay;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuiOutputFilterWindow extends GuiWindow {

    private final BlockPos pos;
    private final boolean isAttachment;
    private final Direction face;
    private final int slotIndex;
    private final OutputInterfaceFilter filter;
    private final Runnable onSaveCallback;

    private ItemStack inspectedStack = ItemStack.EMPTY;
    private List<FilterCandidate> candidates = Collections.emptyList();
    private int candidatePage = 0;

    private FilterType selectedType = FilterType.ITEM;
    private final MekanismButton typeButton;
    private final GuiTextField idField;

    private final MekanismButton[] candidateButtons = new MekanismButton[3];
    private final MekanismButton prevPageButton;
    private final MekanismButton nextPageButton;

    private final GuiSlot inspectSlot;
    private final GuiSlot previewSlot;
    private final GuiSequencedSlotDisplay previewSlotDisplay;
    private List<ItemStack> previewStacks = Collections.emptyList();

    public GuiOutputFilterWindow(IGuiWrapper gui, BlockPos pos, boolean isAttachment, @Nullable Direction face,
                                 int slotIndex, OutputInterfaceFilter filter, Runnable onSaveCallback) {
        super(gui, (gui.getXSize() - 168) / 2, 12, 168, 154, WindowType.UNSPECIFIED);
        this.pos = pos;
        this.isAttachment = isAttachment;
        this.face = face;
        this.slotIndex = slotIndex;
        this.filter = filter;
        this.onSaveCallback = onSaveCallback;

        OutputInterfaceFilter.FilterEntry currentEntry = filter.getEntry(slotIndex);
        if (!currentEntry.isEmpty()) {
            this.selectedType = currentEntry.getType();
            if (!currentEntry.getIconStack().isEmpty()) {
                this.inspectedStack = currentEntry.getIconStack().copy();
                this.candidates = OutputInterfaceFilter.extractCandidates(this.inspectedStack);
            }
        }

        // 1. Inspection Slot (x = 10, y = 18)
        this.inspectSlot = addChild(new GuiSlot(SlotType.NORMAL, gui, relativeX + 10, relativeY + 18));

        // Candidate buttons (3 rows)
        for (int i = 0; i < 3; i++) {
            final int btnIdx = i;
            candidateButtons[i] = addChild(new MekanismButton(gui, relativeX + 32, relativeY + 18 + i * 18, 108, 16,
                    Component.empty(), (element, mouseX, mouseY) -> {
                onCandidateClicked(btnIdx);
                return true;
            }));
            candidateButtons[i].visible = false;
        }

        prevPageButton = addChild(new MekanismButton(gui, relativeX + 142, relativeY + 18, 18, 24, Component.literal("▲"),
                (element, mouseX, mouseY) -> {
                    if (candidatePage > 0) {
                        candidatePage--;
                        refreshCandidateButtons();
                    }
                    return true;
                }));
        prevPageButton.visible = false;

        nextPageButton = addChild(new MekanismButton(gui, relativeX + 142, relativeY + 44, 18, 24, Component.literal("▼"),
                (element, mouseX, mouseY) -> {
                    if ((candidatePage + 1) * 3 < candidates.size()) {
                        candidatePage++;
                        refreshCandidateButtons();
                    }
                    return true;
                }));
        nextPageButton.visible = false;

        // 2. Manual Config Row (y = 74)
        typeButton = addChild(new MekanismButton(gui, relativeX + 10, relativeY + 74, 40, 16,
                Component.literal(selectedType.getDisplayName()),
                (element, mouseX, mouseY) -> {
                    selectedType = switch (selectedType) {
                        case ITEM -> FilterType.FLUID;
                        case FLUID -> FilterType.CHEMICAL;
                        case CHEMICAL -> FilterType.ITEM;
                    };
                    ((MekanismButton) element).setMessage(Component.literal(selectedType.getDisplayName()));
                    updatePreview();
                    return true;
                }));

        idField = addChild(new GuiTextField(gui, this, relativeX + 54, relativeY + 74, 104, 16));
        idField.setMaxLength(128);
        if (!currentEntry.isEmpty()) {
            idField.setText(currentEntry.getFilterId());
        }
        idField.setResponder(text -> updatePreview());

        // 3. Preview Area (y = 96)
        this.previewSlot = addChild(new GuiSlot(SlotType.NORMAL, gui, relativeX + 66, relativeY + 96));
        this.previewSlotDisplay = addChild(new GuiSequencedSlotDisplay(gui, relativeX + 67, relativeY + 97, () -> previewStacks));

        // 4. Action Buttons (y = 126)
        addChild(new MekanismButton(gui, relativeX + 10, relativeY + 126, 70, 20, Component.literal("保存配置"),
                (element, mouseX, mouseY) -> {
                    saveFilter();
                    return true;
                }));

        addChild(new MekanismButton(gui, relativeX + 88, relativeY + 126, 70, 20, Component.literal("清除过滤"),
                (element, mouseX, mouseY) -> {
                    clearFilter();
                    return true;
                }));

        refreshCandidateButtons();
        updatePreview();
    }

    private void setInspectedStack(ItemStack stack) {
        this.inspectedStack = stack.copy();
        if (!stack.isEmpty()) {
            this.candidates = OutputInterfaceFilter.extractCandidates(stack);
            this.candidatePage = 0;
            if (!candidates.isEmpty()) {
                FilterCandidate first = candidates.get(0);
                this.selectedType = first.type();
                this.typeButton.setMessage(Component.literal(selectedType.getDisplayName()));
                this.idField.setText(first.id());
            }
        } else {
            this.candidates = Collections.emptyList();
            this.candidatePage = 0;
        }
        refreshCandidateButtons();
        updatePreview();
    }

    private void refreshCandidateButtons() {
        if (candidates.isEmpty()) {
            for (MekanismButton btn : candidateButtons) {
                btn.visible = false;
            }
            prevPageButton.visible = false;
            nextPageButton.visible = false;
            return;
        }

        int totalPages = (candidates.size() + 2) / 3;
        prevPageButton.visible = totalPages > 1;
        prevPageButton.active = candidatePage > 0;
        nextPageButton.visible = totalPages > 1;
        nextPageButton.active = (candidatePage + 1) < totalPages;

        for (int i = 0; i < 3; i++) {
            int index = candidatePage * 3 + i;
            if (index < candidates.size()) {
                FilterCandidate c = candidates.get(index);
                candidateButtons[i].visible = true;
                String display = c.displayName();
                if (display.length() > 16) {
                    display = display.substring(0, 15) + "…";
                }
                candidateButtons[i].setMessage(Component.literal(display));
            } else {
                candidateButtons[i].visible = false;
            }
        }
    }

    private void onCandidateClicked(int buttonIndex) {
        int index = candidatePage * 3 + buttonIndex;
        if (index < candidates.size()) {
            FilterCandidate c = candidates.get(index);
            this.selectedType = c.type();
            this.typeButton.setMessage(Component.literal(selectedType.getDisplayName()));
            this.idField.setText(c.id());
            updatePreview();
        }
    }

    private void updatePreview() {
        String filterId = idField.getText().trim();
        this.previewStacks = OutputInterfaceFilter.getMatchingItemStacks(selectedType, filterId, inspectedStack);
        this.previewSlotDisplay.updateStackList();
    }

    private void saveFilter() {
        String filterId = idField.getText().trim();
        ItemStack icon = (!inspectedStack.isEmpty() && selectedType == FilterType.ITEM) ? inspectedStack : ItemStack.EMPTY;
        filter.setFilter(slotIndex, selectedType, filterId, icon);
        MCIPacketHandler.sendToServer(new PacketSetOutputFilter(pos, isAttachment, face, slotIndex, selectedType, filterId, icon));
        if (onSaveCallback != null) {
            onSaveCallback.run();
        }
        close();
    }

    private void clearFilter() {
        filter.clearFilter(slotIndex);
        MCIPacketHandler.sendToServer(new PacketSetOutputFilter(pos, isAttachment, face, slotIndex, FilterType.ITEM, "", ItemStack.EMPTY));
        if (onSaveCallback != null) {
            onSaveCallback.run();
        }
        close();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (inspectSlot.isMouseOver(mouseX, mouseY)) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                ItemStack carried = player.containerMenu.getCarried();
                if (!carried.isEmpty()) {
                    setInspectedStack(carried);
                } else if (!inspectedStack.isEmpty()) {
                    setInspectedStack(ItemStack.EMPTY);
                }
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);

        // Window Title
        drawTitleText(guiGraphics, Component.literal("槽位 #" + (slotIndex + 1) + " 过滤配置"), 5);

        // Render Inspected Item in slot
        if (!inspectedStack.isEmpty()) {
            gui().renderItem(guiGraphics, inspectedStack, relativeX + 11, relativeY + 19);
        }

        // Hint text if no candidates
        if (candidates.isEmpty()) {
            guiGraphics.drawString(font(), "放入罐/滴管/物品提取", relativeX + 32, relativeY + 22, 0x808080, false);
            guiGraphics.drawString(font(), "或在下方手动输入过滤", relativeX + 32, relativeY + 36, 0x808080, false);
        }

        // Preview label
        guiGraphics.drawString(font(), "匹配预览:", relativeX + 12, relativeY + 101, 0xA0A0A0, false);
        String countStr = previewStacks.isEmpty() ? "未匹配" : ("匹配 " + previewStacks.size() + " 项");
        int countColor = previewStacks.isEmpty() ? 0xFF5555 : 0x55FF55;
        guiGraphics.drawString(font(), countStr, relativeX + 88, relativeY + 101, countColor, false);
    }
}
