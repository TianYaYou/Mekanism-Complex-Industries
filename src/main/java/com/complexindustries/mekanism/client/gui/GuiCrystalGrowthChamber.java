package com.complexindustries.mekanism.client.gui;

import com.complexindustries.mekanism.content.chamber.ContainerCrystalGrowthChamber;
import com.complexindustries.mekanism.content.chamber.TileEntityCrystalGrowthChamber;
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

public class GuiCrystalGrowthChamber extends GuiConfigurableTile<TileEntityCrystalGrowthChamber, ContainerCrystalGrowthChamber> {

    public GuiCrystalGrowthChamber(ContainerCrystalGrowthChamber container, Inventory inv, Component title) {
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

        // 3. Left Chemical Tank A Gauge
        addRenderableWidget(new GuiChemicalGauge(() -> tile.chemicalTankA, () -> tile.getChemicalTanks(null), GaugeType.STANDARD.with(mekanism.common.tile.component.config.DataType.INPUT_1), this, 6, 16))
                .warning(WarningType.NO_MATCHING_RECIPE, tile.getWarningCheck(RecipeError.NOT_ENOUGH_SECONDARY_INPUT));

        // 4. Left Chemical Tank B Gauge (both on input side!)
        addRenderableWidget(new GuiChemicalGauge(() -> tile.chemicalTankB, () -> tile.getChemicalTanks(null), GaugeType.STANDARD.with(mekanism.common.tile.component.config.DataType.INPUT_2), this, 27, 16))
                .warning(WarningType.NO_MATCHING_RECIPE, tile.getWarningCheck(RecipeError.NOT_ENOUGH_SECONDARY_INPUT));

        // 5. Dump Button (Clears tanks, aligned under energy slot at 141, 46)
        addRenderableWidget(new GuiDumpButton<>(this, tile, 141, 46));

        // 6. Progress Arrow (Horizontal towards output, standard size)
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
