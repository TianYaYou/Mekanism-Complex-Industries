package com.complexindustries.mekanism.client.gui;

import com.complexindustries.mekanism.content.extractor.CrudeOilExtractorContainer;
import com.complexindustries.mekanism.content.extractor.TileEntityCrudeOilExtractor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.slot.SlotOverlay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GuiCrudeOilExtractor extends GuiMekanismTile<TileEntityCrudeOilExtractor, CrudeOilExtractorContainer> {

    public GuiCrudeOilExtractor(CrudeOilExtractorContainer container, Inventory inv, Component title) {
        super(container, inv, title);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void addGuiElements() {
        // 1. Mount generic side tabs: UpgradeTab, RedstoneControl, SecurityTab
        super.addGuiElements();

        // 2. Water Section (Left)
        addRenderableWidget(new GuiSlot(SlotType.INPUT, this, 7, 19).with(SlotOverlay.PLUS));
        addRenderableWidget(new GuiSlot(SlotType.OUTPUT, this, 7, 51).with(SlotOverlay.MINUS));
        addRenderableWidget(new GuiFluidGauge(() -> tile.waterTank, () -> tile.getFluidTanks(null), GaugeType.STANDARD, this, 28, 18)
                .setLabel(Component.translatable("block.minecraft.water")));

        // 3. Energy Section
        addRenderableWidget(new GuiEnergyGauge(tile.getEnergyContainer(), GaugeType.STANDARD, this, 48, 18));

        // 4. Center Diagnostic HUD & Pumping Progress
        addRenderableWidget(new GuiInnerScreen(this, 68, 18, 58, 36, () -> List.of(
                MekanismLang.STATUS.translate(),
                tile.getExtractorStatus().getComponent()
        )));
        addRenderableWidget(new GuiProgress(tile::getScaledProgress, ProgressType.LARGE_RIGHT, this, 81, 58));

        // 5. Crude Oil Section (Right)
        addRenderableWidget(new GuiFluidGauge(() -> tile.crudeOilTank, () -> tile.getFluidTanks(null), GaugeType.STANDARD, this, 128, 18)
                .setLabel(Component.translatable("fluid.mekanism_complex_industries.crude_oil")));
        addRenderableWidget(new GuiSlot(SlotType.INPUT, this, 149, 19).with(SlotOverlay.MINUS));
        addRenderableWidget(new GuiSlot(SlotType.OUTPUT, this, 149, 51).with(SlotOverlay.PLUS));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        renderTitleText(graphics);
        drawString(graphics, playerInventoryTitle, inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(graphics, mouseX, mouseY);
    }
}
