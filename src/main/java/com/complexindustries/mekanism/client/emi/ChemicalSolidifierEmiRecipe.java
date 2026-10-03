package com.complexindustries.mekanism.client.emi;

import com.complexindustries.mekanism.client.jei.ChemicalSolidifierJEIRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.List;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.recipe.MekanismEmiRecipe;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ChemicalSolidifierEmiRecipe extends MekanismEmiRecipe<ChemicalSolidifierJEIRecipe> {

    public ChemicalSolidifierEmiRecipe(EmiRecipeCategory category, ResourceLocation id, ChemicalSolidifierJEIRecipe recipe) {
        super(category, id, recipe, 0, 0, 170, 70);
        if (recipe.inputChemical() != null) {
            addInputDefinition(IngredientCreatorAccess.chemicalStack().from(recipe.inputChemical()));
        }
        if (recipe.outputItem() != null) {
            addItemOutputDefinition(List.of(recipe.outputItem()));
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        addElement(widgetHolder, new GuiVerticalPowerBar(this, RecipeViewerUtils.FULL_BAR, 6, 5));
        initTank(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 20, 5), input(0));

        addElement(widgetHolder, new GuiInnerScreen(this, 44, 6, 62, 56, () -> List.of(
                Component.translatable("gui.mekanism_complex_industries.jei.chemical_solidifier.title"),
                Component.translatable("gui.mekanism_complex_industries.jei.chemical_solidifier.usage"),
                Component.translatable("gui.mekanism_complex_industries.jei.chemical_solidifier.duration"),
                Component.translatable("gui.mekanism_complex_industries.jei.chemical_solidifier.muffler")
        )).spacing(2));

        addSimpleProgress(widgetHolder, ProgressType.LARGE_RIGHT, 110, 25, 20);

        GuiSlot slot = addSlot(widgetHolder, SlotType.OUTPUT, 142, 25);
        initItem(widgetHolder, slot, output(0)).recipeContext(this);
    }
}
