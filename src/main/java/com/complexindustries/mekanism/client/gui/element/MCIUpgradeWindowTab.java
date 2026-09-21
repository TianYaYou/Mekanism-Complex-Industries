package com.complexindustries.mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.tab.window.GuiUpgradeWindowTab;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.common.tile.base.TileEntityMekanism;

import java.util.function.Supplier;

public class MCIUpgradeWindowTab extends GuiUpgradeWindowTab {

    public MCIUpgradeWindowTab(IGuiWrapper gui, TileEntityMekanism tile, Supplier<MCIUpgradeWindowTab> elementSupplier) {
        super(gui, tile, elementSupplier::get);
    }

    @Override
    protected GuiWindow createWindow() {
        return new MCIUpgradeWindow(gui(), getGuiWidth() / 2 - 78, 15, this.dataSource);
    }
}
