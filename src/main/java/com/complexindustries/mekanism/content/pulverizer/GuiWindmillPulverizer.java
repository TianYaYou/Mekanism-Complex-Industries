package com.complexindustries.mekanism.content.pulverizer;

import java.util.List;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.gauge.GuiEnergyGauge.IEnergyInfoHandler;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiWindmillPulverizer extends GuiMekanismTile<TileEntityWindmillPulverizerCasing, ContainerWindmillPulverizer> {

    public GuiWindmillPulverizer(ContainerWindmillPulverizer container, Inventory inv, Component title) {
        super(container, inv, title);
        this.dynamicSlots = true;
        this.titleLabelY = 4;
        this.inventoryLabelY += 2;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();

        // 1. Left Nitrogen Chemical Tank Gauge (8, 17)
        addRenderableWidget(new GuiChemicalGauge(
                () -> tile.getMultiblock().chemicalTank,
                () -> tile.getMultiblock().getChemicalTanks(null),
                GaugeType.STANDARD,
                this,
                8,
                17
        ));

        // 2. Right Power / Energy Gauge (150, 17)
        addRenderableWidget(new GuiEnergyGauge(new IEnergyInfoHandler() {
            @Override
            public long getEnergy() {
                return tile.getMultiblock().isFormed() && tile.getMultiblock().energyContainer != null
                        ? tile.getMultiblock().energyContainer.getEnergy() : 0L;
            }

            @Override
            public long getMaxEnergy() {
                return tile.getMultiblock().isFormed() && tile.getMultiblock().energyContainer != null
                        ? tile.getMultiblock().energyContainer.getMaxEnergy() : 0L;
            }
        }, GaugeType.STANDARD, this, 150, 17));

        // 3. Side Energy Tab
        addRenderableWidget(new GuiEnergyTab(this, () -> List.of(
                Component.translatable("gui.mekanism_complex_industries.pulverizer.energy_usage",
                        EnergyDisplay.of(tile.getMultiblock().lastEnergyUsage).getTextComponent())
        )));

        // 4. 6 Parallel Down Progress Arrows for Groups 0..5
        for (int i = 0; i < WindmillPulverizerMultiblockData.GROUP_COUNT; i++) {
            final int groupIdx = i;
            addRenderableWidget(new GuiProgress(
                    () -> tile.getMultiblock().getScaledGroupProgress(groupIdx),
                    ProgressType.DOWN,
                    this,
                    30 + groupIdx * 20 + 5,
                    36
            ));
        }
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);

        WindmillPulverizerMultiblockData data = tile.getMultiblock();
        if (data != null && data.isFormed()) {
            Component statsComp = Component.translatable("gui.mekanism_complex_industries.pulverizer.turbine_stats",
                    String.format(java.util.Locale.ROOT, "%.1fx", data.speedMultiplier),
                    data.bladesCount,
                    data.coilsCount
            );
            guiGraphics.drawString(font, statsComp, 30, 74, 0x404040, false);
        }

        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}
