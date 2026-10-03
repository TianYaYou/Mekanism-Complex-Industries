package com.complexindustries.mekanism.client.gui;

import java.util.Collections;
import com.complexindustries.mekanism.content.solidifier.ContainerChemicalSolidifier;
import com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifier;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiElement;
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

public class GuiChemicalSolidifier extends GuiConfigurableTile<TileEntityChemicalSolidifier, ContainerChemicalSolidifier> {

    private GuiElement inputGauge;

    public GuiChemicalSolidifier(ContainerChemicalSolidifier container, Inventory inv, Component title) {
        super(container, inv, title);
        this.dynamicSlots = true;
        this.titleLabelY = 4;
        this.inventoryLabelY += 2;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();

        // 1. Right Power Bar
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15))
                .warning(WarningType.NOT_ENOUGH_ENERGY, tile.getWarningCheck(RecipeError.NOT_ENOUGH_ENERGY));

        // 2. Energy Tab
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getActive));

        // 3. Chemical Input Gauge (SMALL_MED at 25, 14, neatly aligned above slot at 6, 56)
        inputGauge = addRenderableWidget(new GuiChemicalGauge(() -> tile.inputTank, () -> Collections.singletonList(tile.inputTank), GaugeType.SMALL_MED, this, 25, 14));

        // 4. Center Solidification Progress Arrow (shorter, centered with output slot at 116, 35)
        addRenderableWidget(new GuiProgress(tile::getScaledProgress, ProgressType.RIGHT, this, 68, 40))
                .warning(WarningType.INPUT_DOESNT_PRODUCE_OUTPUT, tile.getWarningCheck(RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT))
                .warning(WarningType.NO_SPACE_IN_OUTPUT, () -> (!tile.outputSlot.isEmpty() && tile.outputSlot.getStack().getCount() >= tile.outputSlot.getLimit(tile.outputSlot.getStack())) || tile.getWarningCheck(RecipeError.NOT_ENOUGH_OUTPUT_SPACE).getAsBoolean());
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleTextWithOffset(guiGraphics, inputGauge != null ? inputGauge.getRelativeRight() : 45, tile.getEnergySlotX());
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}
