package com.complexindustries.mekanism.client.gui.element;

import com.complexindustries.mekanism.content.extractor.TileEntityCrudeOilExtractor;
import com.complexindustries.mekanism.network.MCIPacketHandler;
import com.complexindustries.mekanism.network.PacketExtractorGuiInteract;
import com.complexindustries.mekanism.network.PacketExtractorGuiInteract.Action;
import com.complexindustries.mekanism.registration.MCILang;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.common.inventory.container.SelectedWindowData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class MCIConfigWindow extends GuiWindow {

    private final TileEntityCrudeOilExtractor tile;

    public MCIConfigWindow(IGuiWrapper gui, int x, int y, TileEntityCrudeOilExtractor tile) {
        super(gui, x, y, 140, 90, SelectedWindowData.WindowType.UNSPECIFIED);
        this.tile = tile;
        this.interactionStrategy = InteractionStrategy.ALL;

        // Row 1: Radius stepper buttons (-, +)
        addChild(new MekanismButton(gui, relativeX + 85, relativeY + 18, 16, 14, Component.literal("-"),
                () -> sendInteract(Action.SET_RADIUS, Math.max(0, tile.getRadius() - 1)), null));
        addChild(new MekanismButton(gui, relativeX + 105, relativeY + 18, 16, 14, Component.literal("+"),
                () -> sendInteract(Action.SET_RADIUS, Math.min(32, tile.getRadius() + 1)), null));

        // Row 2: Min Y stepper buttons (-5, -1, +1, +5)
        addChild(new MekanismButton(gui, relativeX + 66, relativeY + 38, 16, 14, Component.literal("-5"),
                () -> sendInteract(Action.SET_MIN_Y, tile.getMinY() - 5), null));
        addChild(new MekanismButton(gui, relativeX + 84, relativeY + 38, 14, 14, Component.literal("-1"),
                () -> sendInteract(Action.SET_MIN_Y, tile.getMinY() - 1), null));
        addChild(new MekanismButton(gui, relativeX + 100, relativeY + 38, 14, 14, Component.literal("+1"),
                () -> sendInteract(Action.SET_MIN_Y, tile.getMinY() + 1), null));
        addChild(new MekanismButton(gui, relativeX + 116, relativeY + 38, 16, 14, Component.literal("+5"),
                () -> sendInteract(Action.SET_MIN_Y, tile.getMinY() + 5), null));

        // Row 3: Max Y stepper buttons (-5, -1, +1, +5)
        addChild(new MekanismButton(gui, relativeX + 66, relativeY + 58, 16, 14, Component.literal("-5"),
                () -> sendInteract(Action.SET_MAX_Y, tile.getMaxY() - 5), null));
        addChild(new MekanismButton(gui, relativeX + 84, relativeY + 58, 14, 14, Component.literal("-1"),
                () -> sendInteract(Action.SET_MAX_Y, tile.getMaxY() - 1), null));
        addChild(new MekanismButton(gui, relativeX + 100, relativeY + 58, 14, 14, Component.literal("+1"),
                () -> sendInteract(Action.SET_MAX_Y, tile.getMaxY() + 1), null));
        addChild(new MekanismButton(gui, relativeX + 116, relativeY + 58, 16, 14, Component.literal("+5"),
                () -> sendInteract(Action.SET_MAX_Y, tile.getMaxY() + 5), null));
    }

    private void sendInteract(Action action, int value) {
        MCIPacketHandler.sendToServer(new PacketExtractorGuiInteract(action, tile.getBlockPos(), value));
    }

    @Override
    public void renderForeground(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderForeground(graphics, mouseX, mouseY);
        drawTitleText(graphics, MCILang.GUI_RANGE_TITLE.translate(), 5.0F);

        // Current value labels
        drawString(graphics, MCILang.GUI_RADIUS.translate(tile.getRadius()), relativeX + 8, relativeY + 21, titleTextColor());
        drawString(graphics, MCILang.GUI_MIN_Y.translate(tile.getMinY()), relativeX + 8, relativeY + 41, titleTextColor());
        drawString(graphics, MCILang.GUI_MAX_Y.translate(tile.getMaxY()), relativeX + 8, relativeY + 61, titleTextColor());
    }
}
