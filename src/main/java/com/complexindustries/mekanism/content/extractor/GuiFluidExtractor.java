package com.complexindustries.mekanism.content.extractor;

import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.gauge.GuiEnergyGauge.IEnergyInfoHandler;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiFluidExtractor extends GuiMekanismTile<TileEntityFluidExtractorCasing, ContainerFluidExtractor> {

    public GuiFluidExtractor(ContainerFluidExtractor container, Inventory inv, Component title) {
        super(container, inv, title);
        inventoryLabelY += 2;
        titleLabelY = 4;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();

        // 1. Center CRT Screen (38, 18, 98, 62)
        addRenderableWidget(new GuiInnerScreen(this, 38, 18, 98, 62, () -> {
            FluidExtractorMultiblockData multiblock = tile.getMultiblock();
            List<Component> lines = new ArrayList<>();

            if (!multiblock.isFormed()) {
                lines.add(Component.translatable("gui.mekanism_complex_industries.extractor.unformed"));
                return lines;
            }

            // Line 1: Dimensions
            lines.add(Component.translatable("gui.mekanism_complex_industries.extractor.dimensions",
                    multiblock.length(), multiblock.width(), multiblock.height()));

            // Line 2: Environment
            Component envComp = switch (multiblock.environment) {
                case WATER -> Component.translatable("gui.mekanism_complex_industries.extractor.env_water");
                case AIR -> Component.translatable("gui.mekanism_complex_industries.extractor.env_air");
                case LAVA -> Component.translatable("gui.mekanism_complex_industries.extractor.env_lava_infinite");
                case LAVA_INSUFFICIENT -> Component.translatable("gui.mekanism_complex_industries.extractor.env_lava_insufficient",
                        multiblock.lavaBlockCount, 2048);
                default -> Component.translatable("gui.mekanism_complex_industries.extractor.env_none");
            };
            lines.add(envComp);

            // Line 3: Powered Pumps count
            lines.add(Component.translatable("gui.mekanism_complex_industries.extractor.pumps", multiblock.pumpCount));

            // Line 4: Extraction Rate
            lines.add(Component.translatable("gui.mekanism_complex_industries.extractor.rate", multiblock.extractionRate));

            // Line 5: Power Usage
            Component energyComp = EnergyDisplay.of(multiblock.lastEnergyUsage).getTextComponent();
            lines.add(Component.translatable("gui.mekanism_complex_industries.extractor.power", energyComp));

            return lines;
        }).spacing(1));

        // 2. Left Gauge: Energy Gauge (14, 18)
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
        }, GaugeType.STANDARD, this, 14, 18));

        // 3. Right Gauge: Fluid Tank Gauge (140, 18)
        addRenderableWidget(new GuiFluidGauge(() -> tile.getMultiblock().fluidTank,
                () -> tile.getMultiblock().getFluidTanks(null), GaugeType.STANDARD, this, 140, 18));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}
