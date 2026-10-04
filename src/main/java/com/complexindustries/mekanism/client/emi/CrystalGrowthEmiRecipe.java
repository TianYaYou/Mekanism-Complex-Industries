package com.complexindustries.mekanism.client.emi;

import java.util.List;
import com.complexindustries.mekanism.client.jei.CrystalGrowthJEIRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.widget.WidgetHolder;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.recipe.MekanismEmiRecipe;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.resources.ResourceLocation;

public class CrystalGrowthEmiRecipe extends MekanismEmiRecipe<CrystalGrowthJEIRecipe> {

    public CrystalGrowthEmiRecipe(EmiRecipeCategory category, ResourceLocation id, CrystalGrowthJEIRecipe recipe) {
        super(category, id, recipe, -4, -13, 172, 62);
        if (recipe.inputItems() != null && !recipe.inputItems().isEmpty()) {
            addInputDefinition(IngredientCreatorAccess.item().from(recipe.inputItems().get(0)));
        }
        if (recipe.inputChemicalsA() != null && !recipe.inputChemicalsA().isEmpty()) {
            ChemicalStack chemA = recipe.inputChemicalsA().get(0);
            ChemicalStackIngredient ingredientA = IngredientCreatorAccess.chemicalStack().from(chemA);
            addInputDefinition(ingredientA);
            addCatalsyst(ingredientA);
        }
        if (recipe.inputChemicalsB() != null && !recipe.inputChemicalsB().isEmpty()) {
            ChemicalStack chemB = recipe.inputChemicalsB().get(0);
            ChemicalStackIngredient ingredientB = IngredientCreatorAccess.chemicalStack().from(chemB);
            addInputDefinition(ingredientB);
            addCatalsyst(ingredientB);
        }
        if (recipe.outputItem() != null) {
            addItemOutputDefinition(List.of(recipe.outputItem()));
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        initTank(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT_1), this, 6, 15), input(1));
        initTank(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT_2), this, 27, 15), input(2));
        addSlot(widgetHolder, SlotType.INPUT, 54, 40, input(0));
        addSimpleProgress(widgetHolder, ProgressType.RIGHT, 79, 43, 20);
        addSlot(widgetHolder, SlotType.OUTPUT, 116, 40, output(0)).recipeContext(this);
        addElement(widgetHolder, new GuiVerticalPowerBar(this, RecipeViewerUtils.FULL_BAR, 164, 15));
    }
}
