package com.complexindustries.mekanism.client.gui;

import com.complexindustries.mekanism.content.precipitator.ContainerSupercriticalOxidationPrecipitator;
import com.complexindustries.mekanism.content.precipitator.TileEntitySupercriticalOxidationPrecipitator;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiDumpButton;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiSupercriticalOxidationPrecipitator
        extends GuiConfigurableTile<TileEntitySupercriticalOxidationPrecipitator, ContainerSupercriticalOxidationPrecipitator> {

    public GuiSupercriticalOxidationPrecipitator(ContainerSupercriticalOxidationPrecipitator container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
        titleLabelY = 4;
        inventoryLabelY += 2;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();

        // 1. Right Power Bar
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 16));

        // 2. Energy Tab
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getActive));

        // 3. Left Fluid Input Tank Gauge (Liquid Ozone)
        addRenderableWidget(new GuiFluidGauge(
                () -> tile.fluidTank,
                () -> tile.getFluidTanks(null),
                GaugeType.STANDARD.with(DataType.INPUT),
                this, 28, 16));

        // 4. Right Chemical Output Tank Gauge (Cloudy X Complex Aerosol)
        addRenderableWidget(new GuiChemicalGauge(
                () -> tile.outputChemicalTank,
                () -> tile.getChemicalTanks(null),
                GaugeType.STANDARD.with(DataType.OUTPUT),
                this, 112, 16));

        // 5. Dump Button
        addRenderableWidget(new GuiDumpButton<>(this, tile, 134, 38));

        // 6. Progress Arrow (Horizontal towards output chemical tank)
        addRenderableWidget(new GuiProgress(tile::getScaledProgress, ProgressType.RIGHT, this, 77, 39));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}
