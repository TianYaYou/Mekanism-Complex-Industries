package com.complexindustries.mekanism.content.freezer;

import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class GuiFreezerController extends GuiMekanismTile<TileEntityFreezerController, ContainerFreezerController> {

    public GuiFreezerController(ContainerFreezerController container, Inventory inv, Component title) {
        super(container, inv, title);
        inventoryLabelY += 2;
        titleLabelY = 4;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();

        // 1. Center Terminal Screen (42, 18, 92, 44)
        addRenderableWidget(new GuiInnerScreen(this, 42, 18, 92, 44, () -> {
            FreezerMultiblockData multiblock = tile.getMultiblock();
            double temp = multiblock.getTemperature();
            boolean coldEnough = temp <= FreezerMultiblockData.THRESHOLD_TEMP;
            int efficiencyPercent = (int) Math.round(multiblock.getLastEfficiency() * 100);

            Component statusComp = coldEnough
                    ? Component.translatable("gui.mekanism_complex_industries.freezer.status_active", efficiencyPercent)
                    : Component.translatable("gui.mekanism_complex_industries.freezer.status_overheated");

            return List.of(
                    Component.translatable("gui.mekanism_complex_industries.freezer.formed"),
                    Component.translatable("gui.mekanism_complex_industries.freezer.dimensions",
                            multiblock.length(), multiblock.width(), multiblock.height()),
                    Component.translatable("gui.mekanism_complex_industries.freezer.temp",
                            MekanismUtils.getTemperatureDisplay(temp, TemperatureUnit.KELVIN, true)),
                    statusComp
            );
        }).spacing(1));

        // 2. Horizontal Cryo Rate Bar (blue to purple, colder = deeper freeze)
        addRenderableWidget(new com.complexindustries.mekanism.client.gui.element.bar.GuiCryoRateBar(this, new IBarInfoHandler() {
            @Override
            public Component getTooltip() {
                return MekanismUtils.getTemperatureDisplay(tile.getMultiblock().getTemperature(), TemperatureUnit.KELVIN, true);
            }

            @Override
            public double getLevel() {
                double temp = tile.getMultiblock().getTemperature();
                if (temp >= FreezerMultiblockData.THRESHOLD_TEMP) {
                    return 0.0;
                }
                return Math.min(1.0, Math.max(0.0, (FreezerMultiblockData.THRESHOLD_TEMP - temp) / 100.0));
            }
        }, 48, 64));

        // 3. Left Gauges: Input Gas Tank (6, 13) & Input Fluid Tank (24, 13)
        addRenderableWidget(new GuiGasGauge(() -> tile.getMultiblock().inputGasTank,
                () -> tile.getMultiblock().getGasTanks(null), GaugeType.STANDARD, this, 6, 13));
        addRenderableWidget(new GuiFluidGauge(() -> tile.getMultiblock().inputFluidTank,
                () -> tile.getMultiblock().getFluidTanks(null), GaugeType.STANDARD, this, 24, 13));

        // 4. Right Gauges: Output Fluid Tank (136, 13) & Output Gas Tank (154, 13)
        addRenderableWidget(new GuiFluidGauge(() -> tile.getMultiblock().outputFluidTank,
                () -> tile.getMultiblock().getFluidTanks(null), GaugeType.STANDARD, this, 136, 13));
        addRenderableWidget(new GuiGasGauge(() -> tile.getMultiblock().outputGasTank,
                () -> tile.getMultiblock().getGasTanks(null), GaugeType.STANDARD, this, 154, 13));

        // 5. Heat Tab on Right
        addRenderableWidget(new GuiHeatTab(this, () -> {
            Component env = MekanismUtils.getTemperatureDisplay(tile.getMultiblock().getLastEnvironmentLoss(), TemperatureUnit.KELVIN, false);
            return Collections.singletonList(MekanismLang.DISSIPATED_RATE.translate(env));
        }));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        drawString(guiGraphics, playerInventoryTitle, inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}
