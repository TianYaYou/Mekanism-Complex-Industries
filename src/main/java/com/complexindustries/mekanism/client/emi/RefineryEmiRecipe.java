package com.complexindustries.mekanism.client.emi;

import com.complexindustries.mekanism.client.jei.RefineryJEIRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.List;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.recipe.MekanismEmiRecipe;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class RefineryEmiRecipe extends MekanismEmiRecipe<RefineryJEIRecipe> {

    public RefineryEmiRecipe(EmiRecipeCategory category, ResourceLocation id, RefineryJEIRecipe recipe) {
        super(category, id, recipe, 0, 0, 206, 70);
        if (recipe.inputChemical() != null) {
            addInputDefinition(IngredientCreatorAccess.chemicalStack().from(recipe.inputChemical()));
        }
        if (recipe.bitumen() != null) {
            addChemicalOutputDefinition(List.of(recipe.bitumen()));
        }
        if (recipe.heavyOil() != null) {
            addChemicalOutputDefinition(List.of(recipe.heavyOil()));
        }
        if (recipe.refinedFuel() != null) {
            addChemicalOutputDefinition(List.of(recipe.refinedFuel()));
        }
        if (recipe.naphtha() != null) {
            addChemicalOutputDefinition(List.of(recipe.naphtha()));
        }
        if (recipe.petroleumGas() != null) {
            addChemicalOutputDefinition(List.of(recipe.petroleumGas()));
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        // 1. Left Input Chemical Gauge (Dense Crude Oil)
        GuiGauge<?> inputGauge = GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 5, 5);
        initTank(widgetHolder, inputGauge, input(0));

        // 2. Center Terminal Screen
        addElement(widgetHolder, new GuiInnerScreen(this, 27, 5, 80, 46, () -> List.of(
                recipe.title() != null ? recipe.title() : Component.translatable("gui.mekanism_complex_industries.jei.refinery.title"),
                recipe.heatRequirement() != null ? recipe.heatRequirement() : Component.translatable("gui.mekanism_complex_industries.jei.refinery.short_heat"),
                recipe.deltaRequirement() != null ? recipe.deltaRequirement() : Component.translatable("gui.mekanism_complex_industries.jei.refinery.short_delta"),
                recipe.rateInfo() != null ? recipe.rateInfo() : Component.translatable("gui.mekanism_complex_industries.jei.refinery.short_rate")
        )).spacing(1).tooltip(() -> List.of(
                Component.translatable("gui.mekanism_complex_industries.jei.refinery.tooltip.title"),
                Component.translatable("gui.mekanism_complex_industries.jei.refinery.tooltip.dimensions"),
                Component.translatable("gui.mekanism_complex_industries.jei.refinery.tooltip.heat"),
                Component.translatable("gui.mekanism_complex_industries.jei.refinery.tooltip.delta"),
                Component.translatable("gui.mekanism_complex_industries.jei.refinery.tooltip.warning")
        )));

        // 3. Center Cracking Horizontal Rate Bar (80x8)
        addElement(widgetHolder, new GuiHorizontalRateBar(this, RecipeViewerUtils.FULL_BAR, 27, 54));

        // 4. Right 5 Output Chemical Gauges (Layer 1 ~ Layer 5)
        initTank(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 111, 5), output(0)).recipeContext(this);
        initTank(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 129, 5), output(1)).recipeContext(this);
        initTank(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 147, 5), output(2)).recipeContext(this);
        initTank(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 165, 5), output(3)).recipeContext(this);
        initTank(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 183, 5), output(4)).recipeContext(this);
    }
}
