package com.complexindustries.mekanism.client.gui;

import com.complexindustries.mekanism.client.gui.element.tab.GuiChemicalSoakingSortingTab;
import com.complexindustries.mekanism.content.soaker.ContainerChemicalSoakingFactory;
import com.complexindustries.mekanism.content.soaker.TileEntityChemicalSoakingFactory;
import mekanism.api.inventory.IInventorySlot;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiDumpButton;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.tier.FactoryTier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiChemicalSoakingFactory extends GuiConfigurableTile<TileEntityChemicalSoakingFactory, ContainerChemicalSoakingFactory> {

    @Nullable
    private GuiDumpButton<?> dumpButton;

    public GuiChemicalSoakingFactory(ContainerChemicalSoakingFactory container, Inventory inv, Component title) {
        super(container, inv, title);
        imageHeight += 11;
        inventoryLabelY = 85;
        if (tile.tier == FactoryTier.ULTIMATE) {
            imageWidth += 70;
            inventoryLabelX = 43;
        } else if (tile.tier == FactoryTier.ELITE) {
            imageWidth += 30;
            inventoryLabelX = 23;
        }
        titleLabelY = 4;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();

        // 1. Auto-sorting tab
        addRenderableWidget(new GuiChemicalSoakingSortingTab(this, tile));

        // 2. Right Power Bar
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), imageWidth - 12, 16, 52))
                .warning(WarningType.NOT_ENOUGH_ENERGY, () -> tile.getEnergyContainer().getEnergy() < 200L);

        // 3. Energy Tab
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getLastUsage));

        // 4. Left Large Chemical Tank Gauge (STANDARD 18x60)
        addRenderableWidget(new GuiChemicalGauge(() -> tile.chemicalTank, () -> tile.getChemicalTanks(null), GaugeType.STANDARD, this, 7, 14))
                .warning(WarningType.NO_MATCHING_RECIPE, () -> tile.chemicalTank.isEmpty());

        // 5. Dump Button under Chemical Gauge
        dumpButton = addRenderableWidget(new GuiDumpButton<>(this, tile, 6, 73));

        // 6. Process lanes: down progress arrows
        int baseX = tile.tier == FactoryTier.BASIC ? 55 : 49;
        int baseXMult = tile.tier == FactoryTier.BASIC ? 36 : tile.tier == FactoryTier.ADVANCED ? 22 : 20;
        for (int i = 0; i < tile.tier.processes; i++) {
            int cacheIndex = i;
            addRenderableWidget(new GuiProgress(() -> tile.getScaledProgress(1, cacheIndex), ProgressType.DOWN, this, 4 + baseX + (i * baseXMult), 33))
                    .warning(WarningType.NO_SPACE_IN_OUTPUT, () -> {
                        IInventorySlot slot = tile.outputSlots.get(cacheIndex);
                        return !slot.isEmpty() && slot.getStack().getCount() >= slot.getLimit(slot.getStack());
                    });
        }
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics, dumpButton == null ? getXSize() : dumpButton.getRelativeX());
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}
