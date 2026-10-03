package com.complexindustries.mekanism.client.gui;

import com.complexindustries.mekanism.content.solidifier.ContainerChemicalSolidifierFactory;
import com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifierFactory;
import com.complexindustries.mekanism.client.gui.element.tab.GuiChemicalSolidifierSortingTab;
import mekanism.api.inventory.IInventorySlot;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiChemicalSolidifierFactory extends GuiConfigurableTile<TileEntityChemicalSolidifierFactory, ContainerChemicalSolidifierFactory> {

    public GuiChemicalSolidifierFactory(ContainerChemicalSolidifierFactory container, Inventory inv, Component title) {
        super(container, inv, title);
        imageHeight += 13;
        inventoryLabelY = 88;
        if (tile.tier == FactoryTier.ULTIMATE) {
            imageWidth += 34;
            inventoryLabelX = 26;
        }
        titleLabelY = 4;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();

        // 1. Right Power Bar (Height 65, neatly spanning machine area)
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), imageWidth - 12, 16, 65))
                .warning(WarningType.NOT_ENOUGH_ENERGY, () -> tile.getEnergyContainer().getEnergy() < 200L);

        // 2. Energy Info Tab
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getLastUsage));

        // 3. Sorting Tab (Toggle auto-distribution of chemical across lanes)
        addRenderableWidget(new GuiChemicalSolidifierSortingTab(this, tile));

        // 4. Process lanes: Top Chemical Gauge -> Down Progress Arrow -> Bottom Output Slot
        for (int i = 0; i < tile.tier.processes; i++) {
            int cacheIndex = i;
            addRenderableWidget(new GuiChemicalGauge(
                    () -> tile.inputTank[cacheIndex],
                    () -> tile.inputChemicalTanks,
                    GaugeType.SMALL.with(DataType.INPUT),
                    this,
                    tile.getXPos(cacheIndex) - 1,
                    13
            ));

            addRenderableWidget(new GuiProgress(
                    () -> tile.getScaledProgress(1, cacheIndex),
                    ProgressType.DOWN,
                    this,
                    4 + tile.getXPos(cacheIndex),
                    46
            )).warning(WarningType.NO_SPACE_IN_OUTPUT, () -> {
                IInventorySlot slot = tile.outputSlots.get(cacheIndex);
                return !slot.isEmpty() && slot.getStack().getCount() >= slot.getLimit(slot.getStack());
            });
        }
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleTextWithOffset(guiGraphics, 28, imageWidth - 14);
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}
