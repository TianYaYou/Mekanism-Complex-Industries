package com.complexindustries.mekanism.client.emi;

import java.util.List;
import com.complexindustries.mekanism.client.jei.SiliconSlicingJEIRecipe;
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
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.resources.ResourceLocation;

public class SiliconSlicingEmiRecipe extends MekanismEmiRecipe<SiliconSlicingJEIRecipe> {

    public SiliconSlicingEmiRecipe(EmiRecipeCategory category, ResourceLocation id, SiliconSlicingJEIRecipe recipe) {
        super(category, id, recipe, -28, -16, 146, 60);
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
        addSlot(widgetHolder, SlotType.INPUT, 54, 40, input(0));
        addSlot(widgetHolder, SlotType.OUTPUT, 116, 40, output(0)).recipeContext(this);
        addSlot(widgetHolder, SlotType.POWER, 141, 20).with(SlotOverlay.POWER);
        addElement(widgetHolder, new GuiVerticalPowerBar(this, RecipeViewerUtils.FULL_BAR, 164, 16));
        initTank(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 28, 16), input(1));
        addSimpleProgress(widgetHolder, ProgressType.RIGHT, 79, 43, 20);
    }
}
