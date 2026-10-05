package com.complexindustries.mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;
import com.complexindustries.mekanism.content.slicer.ContainerSiliconSlicer;
import com.complexindustries.mekanism.content.slicer.TileEntitySiliconSlicer;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiDumpButton;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiSiliconSlicer extends GuiConfigurableTile<TileEntitySiliconSlicer, ContainerSiliconSlicer> {

    public GuiSiliconSlicer(ContainerSiliconSlicer container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
        titleLabelY = 4;
        inventoryLabelY += 2;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();

        // 1. Right Power Bar
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 16))
                .warning(WarningType.NOT_ENOUGH_ENERGY, tile.getWarningCheck(RecipeError.NOT_ENOUGH_ENERGY));

        // 2. Energy Tab
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getActive));

        // 3. Left Chemical Tank Gauge (Nitrogen)
        GuiChemicalGauge nitrogenGauge = addRenderableWidget(new GuiChemicalGauge(() -> tile.chemicalTank, () -> tile.getChemicalTanks(null), GaugeType.STANDARD.with(mekanism.common.tile.component.config.DataType.INPUT), this, 28, 16));
        nitrogenGauge.warning(WarningType.NO_MATCHING_RECIPE, tile.getWarningCheck(RecipeError.NOT_ENOUGH_SECONDARY_INPUT));

        // 4. Dump Button (Clears chemical tank)
        addRenderableWidget(new GuiDumpButton<>(this, tile, 141, 46));

        // 5. Progress Arrow (Horizontal towards output)
        addRenderableWidget(new GuiProgress(tile::getScaledProgress, ProgressType.RIGHT, this, 79, 43))
                .warning(WarningType.INPUT_DOESNT_PRODUCE_OUTPUT, tile.getWarningCheck(RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT))
                .warning(WarningType.NO_SPACE_IN_OUTPUT, tile.getWarningCheck(RecipeError.NOT_ENOUGH_OUTPUT_SPACE));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}
