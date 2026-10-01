package com.complexindustries.mekanism.content.refinery;

import com.complexindustries.mekanism.registration.MCIChemicals;
import mekanism.api.heat.HeatAPI;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GuiRefineryController extends GuiMekanismTile<TileEntityRefineryController, ContainerRefineryController> {

    public GuiRefineryController(ContainerRefineryController container, Inventory inv, Component title) {
        super(container, inv, title);
        imageWidth += 50;
        inventoryLabelX += 25;
        inventoryLabelY += 2;
        titleLabelY = 4;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();

        // 1. Center Terminal Screen (31, 13, 92, 44)
        addRenderableWidget(new GuiInnerScreen(this, 31, 13, 92, 44, () -> {
            RefineryMultiblockData multiblock = tile.getMultiblock();
            if (!multiblock.isFormed()) {
                return List.of(Component.translatable("gui.mekanism_complex_industries.refinery.unformed"));
            }

            double bottomTemp = multiblock.getBottomHeatCapacitor() != null
                    ? multiblock.getBottomHeatCapacitor().getTemperature()
                    : HeatAPI.AMBIENT_TEMP;
            double topTemp = multiblock.getTopHeatCapacitor() != null
                    ? multiblock.getTopHeatCapacitor().getTemperature()
                    : HeatAPI.AMBIENT_TEMP;

            Component statusComp;
            switch (multiblock.operatingStatus) {
                case 1 -> statusComp = Component.translatable("gui.mekanism_complex_industries.refinery.status_low_temp");
                case 2 -> statusComp = Component.translatable("gui.mekanism_complex_industries.refinery.status_low_delta_t");
                case 3 -> statusComp = Component.translatable("gui.mekanism_complex_industries.refinery.status_active",
                        String.format("%.1f", multiblock.lastCrackingRate));
                case 4 -> statusComp = Component.translatable("gui.mekanism_complex_industries.refinery.status_outputs_full");
                default -> statusComp = Component.translatable("gui.mekanism_complex_industries.refinery.status_idle");
            }

            return List.of(
                    Component.translatable("gui.mekanism_complex_industries.refinery.dimensions",
                            multiblock.length(), multiblock.width(), multiblock.height()),
                    Component.translatable("gui.mekanism_complex_industries.refinery.bottom_temp",
                            MekanismUtils.getTemperatureDisplay(bottomTemp, TemperatureUnit.KELVIN, true)),
                    Component.translatable("gui.mekanism_complex_industries.refinery.top_temp",
                            MekanismUtils.getTemperatureDisplay(topTemp, TemperatureUnit.KELVIN, true)),
                    statusComp
            );
        }).spacing(1));

        // 2. Horizontal Cracking Rate Bar (0 ~ 160 mB/s load)
        addRenderableWidget(new GuiHorizontalRateBar(this, new IBarInfoHandler() {
            @Override
            public Component getTooltip() {
                return Component.translatable("gui.mekanism_complex_industries.refinery.rate_tooltip",
                        String.format("%.1f", tile.getMultiblock().lastCrackingRate));
            }

            @Override
            public double getLevel() {
                return Math.min(1.0, Math.max(0.0, tile.getMultiblock().lastCrackingRate / 160.0));
            }
        }, 38, 60));

        // 3. Left Input Chemical Tank Gauge (Dense Crude Oil)
        GuiChemicalGauge inputGauge = addRenderableWidget(new GuiChemicalGauge(
                () -> tile.getMultiblock().inputChemicalTank,
                () -> tile.getMultiblock().getChemicalTanks(null),
                GaugeType.STANDARD, this, 7, 13
        ));
        inputGauge.setDummyType(MCIChemicals.DENSE_CRUDE_OIL.asStack(1));

        // 4. Right 5 Output Chemical Tank Gauges (Layers 1..5)
        // Layer 1: Bitumen
        GuiChemicalGauge out1 = addRenderableWidget(new GuiChemicalGauge(
                () -> tile.getMultiblock().outputTank1,
                () -> tile.getMultiblock().getChemicalTanks(null),
                GaugeType.STANDARD, this, 129, 13
        ));
        out1.setDummyType(MCIChemicals.BITUMEN.asStack(1));

        // Layer 2: Heavy Oil
        GuiChemicalGauge out2 = addRenderableWidget(new GuiChemicalGauge(
                () -> tile.getMultiblock().outputTank2,
                () -> tile.getMultiblock().getChemicalTanks(null),
                GaugeType.STANDARD, this, 147, 13
        ));
        out2.setDummyType(MCIChemicals.HEAVY_OIL.asStack(1));

        // Layer 3: Refined Fuel
        GuiChemicalGauge out3 = addRenderableWidget(new GuiChemicalGauge(
                () -> tile.getMultiblock().outputTank3,
                () -> tile.getMultiblock().getChemicalTanks(null),
                GaugeType.STANDARD, this, 165, 13
        ));
        out3.setDummyType(MCIChemicals.REFINED_FUEL.asStack(1));

        // Layer 4: Naphtha
        GuiChemicalGauge out4 = addRenderableWidget(new GuiChemicalGauge(
                () -> tile.getMultiblock().outputTank4,
                () -> tile.getMultiblock().getChemicalTanks(null),
                GaugeType.STANDARD, this, 183, 13
        ));
        out4.setDummyType(MCIChemicals.NAPHTHA.asStack(1));

        // Layer 5: Petroleum Gas
        GuiChemicalGauge out5 = addRenderableWidget(new GuiChemicalGauge(
                () -> tile.getMultiblock().outputTank5,
                () -> tile.getMultiblock().getChemicalTanks(null),
                GaugeType.STANDARD, this, 201, 13
        ));
        out5.setDummyType(MCIChemicals.PETROLEUM_GAS.asStack(1));

        // 5. Heat Tab on Right Side
        addRenderableWidget(new GuiHeatTab(this, () -> {
            RefineryMultiblockData multiblock = tile.getMultiblock();
            double bottomTemp = multiblock.getBottomHeatCapacitor() != null
                    ? multiblock.getBottomHeatCapacitor().getTemperature()
                    : HeatAPI.AMBIENT_TEMP;
            double topTemp = multiblock.getTopHeatCapacitor() != null
                    ? multiblock.getTopHeatCapacitor().getTemperature()
                    : HeatAPI.AMBIENT_TEMP;
            double deltaT = bottomTemp - topTemp;
            return List.of(
                    Component.translatable("gui.mekanism_complex_industries.refinery.heat_tab.bottom",
                            MekanismUtils.getTemperatureDisplay(bottomTemp, TemperatureUnit.KELVIN, true)),
                    Component.translatable("gui.mekanism_complex_industries.refinery.heat_tab.top",
                            MekanismUtils.getTemperatureDisplay(topTemp, TemperatureUnit.KELVIN, true)),
                    Component.translatable("gui.mekanism_complex_industries.refinery.heat_tab.delta",
                            MekanismUtils.getTemperatureDisplay(deltaT, TemperatureUnit.KELVIN, false))
            );
        }));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}
