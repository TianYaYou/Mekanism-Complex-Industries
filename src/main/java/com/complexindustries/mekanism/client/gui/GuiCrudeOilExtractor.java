package com.complexindustries.mekanism.client.gui;

import com.complexindustries.mekanism.client.gui.element.MCIConfigWindow;
import com.complexindustries.mekanism.client.gui.element.MCIUpgradeWindowTab;
import com.complexindustries.mekanism.content.extractor.CrudeOilExtractorContainer;
import com.complexindustries.mekanism.content.extractor.TileEntityCrudeOilExtractor;
import com.complexindustries.mekanism.network.MCIPacketHandler;
import com.complexindustries.mekanism.network.PacketExtractorGuiInteract;
import com.complexindustries.mekanism.network.PacketExtractorGuiInteract.Action;
import com.complexindustries.mekanism.registration.MCILang;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.common.MekanismLang;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GuiCrudeOilExtractor extends GuiConfigurableTile<TileEntityCrudeOilExtractor, CrudeOilExtractorContainer> {

    private MCIUpgradeWindowTab upgradeWindowTab;
    private TranslationButton startButton;
    private TranslationButton stopButton;

    public GuiCrudeOilExtractor(CrudeOilExtractorContainer container, Inventory inv, Component title) {
        super(container, inv, title);
        this.dynamicSlots = true;
        this.inventoryLabelY = 72;
    }

    @Override
    protected void addGenericTabs() {
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getActive));
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

        // 3. Center Diagnostic HUD
        addRenderableWidget(new GuiInnerScreen(this, 47, 18, 50, 48, () -> List.of(
                MekanismLang.STATUS.translate(""),
                tile.getExtractorStatus().getComponent(),
                Component.translatable("gui.mekanism_complex_industries.extracted", tile.getExtractedCount()),
                Component.translatable("gui.mekanism_complex_industries.range_stat", tile.getRadius(), tile.getMinY(), tile.getMaxY())
        )).spacing(1).padding(2).textScale(0.8F).tooltip(() -> List.of(
                Component.translatable("gui.mekanism_complex_industries.extractor.tooltip.details")
        )));

        // 4. Control Buttons (Start, Stop, Config, Reset)
        startButton = addRenderableWidget(new TranslationButton(this, 99, 18, 28, 11, MCILang.GUI_START, () -> {
            MCIPacketHandler.sendToServer(new PacketExtractorGuiInteract(Action.START, tile.getBlockPos()));
        }));
        stopButton = addRenderableWidget(new TranslationButton(this, 99, 30, 28, 11, MCILang.GUI_STOP, () -> {
            MCIPacketHandler.sendToServer(new PacketExtractorGuiInteract(Action.STOP, tile.getBlockPos()));
        }));
        addRenderableWidget(new TranslationButton(this, 99, 42, 28, 11, MCILang.GUI_CONFIG, () -> {
            if (windows.stream().noneMatch(w -> w instanceof MCIConfigWindow)) {
                addWindow(new MCIConfigWindow(this, getXSize() / 2 - 70, 20, tile));
            }
        }));
        addRenderableWidget(new TranslationButton(this, 99, 54, 28, 11, MCILang.GUI_RESET, () -> {
            MCIPacketHandler.sendToServer(new PacketExtractorGuiInteract(Action.RESET, tile.getBlockPos()));
        }));

        updateButtonStates();

        // 5. Crude Oil Section (Right)
        addRenderableWidget(new GuiFluidGauge(() -> tile.crudeOilTank, () -> tile.getFluidTanks(null), GaugeType.STANDARD, this, 128, 18));
    }

    @Override
    public void containerTick() {
        super.containerTick();
        updateButtonStates();
    }

    private void updateButtonStates() {
        if (startButton != null) {
            startButton.active = !tile.isRunning();
        }
        if (stopButton != null) {
            stopButton.active = tile.isRunning();
        }
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        renderTitleText(graphics);
        drawString(graphics, playerInventoryTitle, inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(graphics, mouseX, mouseY);
    }
}
