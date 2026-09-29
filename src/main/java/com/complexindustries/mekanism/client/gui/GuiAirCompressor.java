package com.complexindustries.mekanism.client.gui;

import com.complexindustries.mekanism.content.tile.TileEntityAirCompressor;
import com.complexindustries.mekanism.inventory.container.ContainerAirCompressor;
import java.util.Collections;
import java.util.List;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiAirCompressor extends GuiMekanismTile<TileEntityAirCompressor, ContainerAirCompressor> {

    public GuiAirCompressor(ContainerAirCompressor container, Inventory inv, Component title) {
        super(container, inv, title);
        this.dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();

        // 1. Center Terminal Screen
        addRenderableWidget(new GuiInnerScreen(this, 48, 23, 80, 42, () -> List.of(
                Component.translatable("gui.mekanism_complex_industries.air_compressor.screen_title"),
                Component.translatable("gui.mekanism_complex_industries.air_compressor.production", tile.getProductionRate()),
                Component.translatable("gui.mekanism_complex_industries.air_compressor.usage", EnergyDisplay.of(tile.getEnergyUsed()).getTextComponent())
        )).spacing(2));

        // 2. Left Gas Gauge (Output Compressed Air)
        addRenderableWidget(new GuiGasGauge(() -> tile.outputTank, () -> Collections.singletonList(tile.outputTank), GaugeType.STANDARD, this, 24, 13));

        // 3. Right Vertical Power Bar
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15));

        // 4. Energy Tab
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getEnergyUsed));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        drawString(guiGraphics, playerInventoryTitle, inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}
