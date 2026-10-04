package com.complexindustries.mekanism.client.emi;

import com.complexindustries.mekanism.client.jei.ChemicalSoakingJEIRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.client.gui.element.bar.GuiEmptyBar;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.recipe.MekanismEmiRecipe;
import mekanism.common.inventory.container.slot.SlotOverlay;
import net.minecraft.resources.ResourceLocation;

public class ChemicalSoakingEmiRecipe extends MekanismEmiRecipe<ChemicalSoakingJEIRecipe> {

    public ChemicalSoakingEmiRecipe(EmiRecipeCategory category, ResourceLocation id, ChemicalSoakingJEIRecipe recipe) {
        super(category, id, recipe, 28, 16, 144, 54);
        if (recipe.inputItems() != null && !recipe.inputItems().isEmpty()) {
            addInputDefinition(IngredientCreatorAccess.item().from(recipe.inputItems().get(0)));
        }
        if (recipe.inputChemicals() != null && !recipe.inputChemicals().isEmpty()) {
            ChemicalStack chem = recipe.inputChemicals().get(0);
            ChemicalStackIngredient ingredient = IngredientCreatorAccess.chemicalStack().from(chem);
            addInputDefinition(ingredient);
            addCatalsyst(ingredient);
        }
        if (recipe.outputItem() != null) {
            addItemOutputDefinition(List.of(recipe.outputItem()));
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        addSlot(widgetHolder, SlotType.INPUT, 64, 17, input(0));
        if (!getCatalysts().isEmpty()) {
            addSlot(widgetHolder, SlotType.EXTRA, 64, 53, catalyst(0)).catalyst(true);
        } else {
            addSlot(widgetHolder, SlotType.EXTRA, 64, 53);
        }
        addSlot(widgetHolder, SlotType.OUTPUT, 116, 35, output(0)).recipeContext(this);
        addSlot(widgetHolder, SlotType.POWER, 39, 35).with(SlotOverlay.POWER);
        addElement(widgetHolder, new GuiVerticalPowerBar(this, RecipeViewerUtils.FULL_BAR, 164, 16));
        initTank(widgetHolder, new GuiEmptyBar(this, 68, 36, 6, 12), input(1));
        addSimpleProgress(widgetHolder, ProgressType.BAR, 86, 38, 20);
    }
}
