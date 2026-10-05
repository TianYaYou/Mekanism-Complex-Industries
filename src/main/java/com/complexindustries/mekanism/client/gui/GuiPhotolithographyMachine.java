package com.complexindustries.mekanism.client.gui;

import java.util.List;
import com.complexindustries.mekanism.content.lithography.ContainerPhotolithographyMachine;
import com.complexindustries.mekanism.content.lithography.TileEntityPhotolithographyMachine;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiPhotolithographyMachine extends GuiConfigurableTile<TileEntityPhotolithographyMachine, ContainerPhotolithographyMachine> {

    public GuiPhotolithographyMachine(ContainerPhotolithographyMachine container, Inventory inv, Component title) {
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

        // 4. Optical Laser Aperture Indicator (Located at 141, 44)
        addRenderableWidget(new GuiElement(this, 141, 44, 18, 18) {
            @Override
            public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
                super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
                int renderX = getButtonX();
                int renderY = getButtonY();
                // Outer frame
                guiGraphics.fill(renderX, renderY, renderX + 18, renderY + 18, 0xFF373737);
                guiGraphics.fill(renderX + 1, renderY + 1, renderX + 17, renderY + 17, 0xFF222222);
                if (tile.hasActiveLaser()) {
                    // Glowing vibrant purple aperture when receiving laser
                    guiGraphics.fill(renderX + 3, renderY + 3, renderX + 15, renderY + 15, 0xFF9933FF);
                    guiGraphics.fill(renderX + 5, renderY + 5, renderX + 13, renderY + 13, 0xFFCC66FF);
                    guiGraphics.fill(renderX + 7, renderY + 7, renderX + 11, renderY + 11, 0xFFFFFFFF);
                } else {
                    // Dark dim aperture when offline
                    guiGraphics.fill(renderX + 3, renderY + 3, renderX + 15, renderY + 15, 0xFF442255);
                    guiGraphics.fill(renderX + 6, renderY + 6, renderX + 12, renderY + 12, 0xFF221133);
                }
            }

            @Override
            public void updateTooltip(int mouseX, int mouseY) {
                if (tile.hasActiveLaser()) {
                    setTooltip(Tooltip.create(
                            Component.translatable("gui.mekanism_complex_industries.photolithography.laser_active")
                                    .append("\n")
                                    .append(Component.translatable("gui.mekanism_complex_industries.photolithography.speed",
                                            String.format("%.1fs", tile.getExposureDurationSeconds()),
                                            tile.getCurrentLaserEnergy()))
                    ));
                } else {
                    setTooltip(Tooltip.create(
                            Component.translatable("gui.mekanism_complex_industries.photolithography.laser_offline")
                                    .append("\n")
                                    .append(Component.translatable("gui.mekanism_complex_industries.photolithography.laser_required"))
                    ));
                }
            }
        });

        // 5. Progress Arrow - Horizontally centered on the exact same line as input and output slots (Y=47)
        addRenderableWidget(new GuiProgress(tile::getScaledProgress, ProgressType.RIGHT, this, 78, 47) {
            @Override
            public void updateTooltip(int mouseX, int mouseY) {
                if (tile.hasActiveLaser()) {
                    setTooltip(Tooltip.create(
                            Component.translatable("gui.mekanism_complex_industries.photolithography.laser_active")
                                    .append("\n")
                                    .append(Component.translatable("gui.mekanism_complex_industries.photolithography.speed",
                                            String.format("%.1fs", tile.getExposureDurationSeconds()),
                                            tile.getCurrentLaserEnergy()))
                    ));
                } else {
                    setTooltip(Tooltip.create(
                            Component.translatable("gui.mekanism_complex_industries.photolithography.laser_offline")
                                    .append("\n")
                                    .append(Component.translatable("gui.mekanism_complex_industries.photolithography.laser_required"))
                    ));
                }
            }
        })
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
