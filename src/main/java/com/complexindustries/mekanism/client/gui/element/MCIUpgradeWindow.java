package com.complexindustries.mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.custom.GuiSupportedUpgrades;
import mekanism.client.gui.element.window.GuiUpgradeWindow;
import mekanism.common.tile.base.TileEntityMekanism;

public class MCIUpgradeWindow extends GuiUpgradeWindow {

    public MCIUpgradeWindow(IGuiWrapper gui, int x, int y, TileEntityMekanism tile) {
        super(gui, x, y, tile);
        this.height = 88;
        this.children.removeIf(child -> child instanceof GuiSupportedUpgrades);
        this.addChild(new MCISupportedUpgrades(gui, this.relativeX + 6, this.relativeY + 68, tile.getComponent().getSupportedTypes()));
    }
}
