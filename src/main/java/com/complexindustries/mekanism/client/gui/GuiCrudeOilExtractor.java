package com.complexindustries.mekanism.client.gui;

import com.complexindustries.mekanism.client.gui.element.MCIUpgradeWindowTab;
import com.complexindustries.mekanism.content.extractor.CrudeOilExtractorContainer;
import com.complexindustries.mekanism.content.extractor.TileEntityCrudeOilExtractor;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.common.MekanismLang;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GuiCrudeOilExtractor extends GuiConfigurableTile<TileEntityCrudeOilExtractor, CrudeOilExtractorContainer> {

    private MCIUpgradeWindowTab upgradeWindowTab;

    public GuiCrudeOilExtractor(CrudeOilExtractorContainer container, Inventory inv, Component title) {
        super(container, inv, title);
        this.dynamicSlots = true;
        this.inventoryLabelY = 72;
    }

    @Override
    protected void addGenericTabs() {
        if (tile.supportsUpgrades()) {
            upgradeWindowTab = addRenderableWidget(new MCIUpgradeWindowTab(this, tile, () -> upgradeWindowTab));
        }
        if (tile.supportsRedstone()) {
            addRenderableWidget(new GuiRedstoneControlTab(this, tile));
        }
        if (tile.hasSecurity()) {
            addSecurityTab();
        }
    }

    @Override
    protected void addGuiElements() {
        // 1. Mount generic and configurable side tabs
        super.addGuiElements();

        // 2. Water Section (Left)
        addRenderableWidget(new GuiFluidGauge(() -> tile.waterTank, () -> tile.getFluidTanks(null), GaugeType.STANDARD, this, 28, 18));

        // 3. Energy Section
        addRenderableWidget(new GuiEnergyGauge(tile.getEnergyContainer(), GaugeType.STANDARD, this, 48, 18));

        // 4. Center Diagnostic HUD & Pumping Progress
        addRenderableWidget(new GuiInnerScreen(this, 68, 18, 58, 36, () -> List.of(
                MekanismLang.STATUS.translate(""),
                tile.getExtractorStatus().getComponent(),
                Component.translatable("gui.mekanism_complex_industries.extractor.cycle_summary")
        )).spacing(2).tooltip(() -> List.of(
                Component.translatable("gui.mekanism_complex_industries.extractor.tooltip.details")
        )));
        addRenderableWidget(new GuiProgress(tile::getScaledProgress, ProgressType.LARGE_RIGHT, this, 81, 58));

        // 5. Crude Oil Section (Right)
        addRenderableWidget(new GuiFluidGauge(() -> tile.crudeOilTank, () -> tile.getFluidTanks(null), GaugeType.STANDARD, this, 128, 18));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        renderTitleText(graphics);
        drawString(graphics, playerInventoryTitle, inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(graphics, mouseX, mouseY);
    }
}
