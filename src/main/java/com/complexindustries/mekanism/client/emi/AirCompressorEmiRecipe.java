package com.complexindustries.mekanism.client.emi;

import com.complexindustries.mekanism.client.jei.AirCompressorJEIRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.List;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.recipe.MekanismEmiRecipe;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class AirCompressorEmiRecipe extends MekanismEmiRecipe<AirCompressorJEIRecipe> {

    public AirCompressorEmiRecipe(EmiRecipeCategory category, ResourceLocation id, AirCompressorJEIRecipe recipe) {
        super(category, id, recipe, 0, 0, 170, 70);
        if (recipe.outputChemical() != null) {
            addChemicalOutputDefinition(List.of(recipe.outputChemical()));
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        addElement(widgetHolder, new GuiVerticalPowerBar(this, RecipeViewerUtils.FULL_BAR, 6, 5));
        addSlot(widgetHolder, SlotType.POWER, 18, 26).with(SlotOverlay.POWER);

        addElement(widgetHolder, new GuiInnerScreen(this, 42, 6, 74, 56, () -> List.of(
                Component.translatable("gui.mekanism_complex_industries.jei.air_compressor.title"),
                Component.translatable("gui.mekanism_complex_industries.jei.air_compressor.short_usage"),
                Component.translatable("gui.mekanism_complex_industries.jei.air_compressor.short_rate"),
                Component.translatable("gui.mekanism_complex_industries.jei.air_compressor.short_source")
        )).spacing(2).tooltip(() -> List.of(
                Component.translatable("gui.mekanism_complex_industries.jei.air_compressor.title"),
                Component.translatable("gui.mekanism_complex_industries.jei.air_compressor.source"),
                Component.translatable("gui.mekanism_complex_industries.jei.air_compressor.upgrades")
        )));

        addSimpleProgress(widgetHolder, ProgressType.SMALL_RIGHT, 118, 25, 20);

        initTank(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 146, 5), output(0)).recipeContext(this);
    }
}
