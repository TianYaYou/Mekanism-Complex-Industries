package com.complexindustries.mekanism.client.emi;

import java.util.List;
import com.complexindustries.mekanism.client.jei.ChemicalFilmCoatingJEIRecipe;
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

public class ChemicalFilmCoatingEmiRecipe extends MekanismEmiRecipe<ChemicalFilmCoatingJEIRecipe> {

    public ChemicalFilmCoatingEmiRecipe(EmiRecipeCategory category, ResourceLocation id, ChemicalFilmCoatingJEIRecipe recipe) {
        super(category, id, recipe, -28, -16, 146, 60);
        if (recipe.alloyItems() != null && !recipe.alloyItems().isEmpty()) {
            addInputDefinition(IngredientCreatorAccess.item().from(recipe.alloyItems().get(0)));
        }
        if (recipe.chipItems() != null && !recipe.chipItems().isEmpty()) {
            addInputDefinition(IngredientCreatorAccess.item().from(recipe.chipItems().get(0)));
        }
        if (recipe.inputChemicals() != null && !recipe.inputChemicals().isEmpty()) {
            ChemicalStack chem = recipe.inputChemicals().get(0);
            ChemicalStackIngredient ingredient = IngredientCreatorAccess.chemicalStack().from(chem);
            addInputDefinition(ingredient);
        }
        if (recipe.outputItem() != null) {
            addItemOutputDefinition(List.of(recipe.outputItem()));
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        addSlot(widgetHolder, SlotType.INPUT, 54, 18, input(0));
        addSlot(widgetHolder, SlotType.INPUT, 54, 42, input(1));
        addSlot(widgetHolder, SlotType.OUTPUT, 116, 42, output(0)).recipeContext(this);
        addSlot(widgetHolder, SlotType.POWER, 141, 20).with(SlotOverlay.POWER);
        addElement(widgetHolder, new GuiVerticalPowerBar(this, RecipeViewerUtils.FULL_BAR, 164, 16));
        initTank(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 28, 16), input(2));
        addSimpleProgress(widgetHolder, ProgressType.RIGHT, 78, 47, 20);
        addElement(widgetHolder, new mekanism.client.gui.element.GuiElement(this, 141, 44, 18, 18) {
            @Override
            public void drawBackground(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
                super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
                int renderX = getButtonX();
                int renderY = getButtonY();
                guiGraphics.fill(renderX, renderY, renderX + 18, renderY + 18, 0xFF373737);
                guiGraphics.fill(renderX + 1, renderY + 1, renderX + 17, renderY + 17, 0xFF222222);
                guiGraphics.fill(renderX + 3, renderY + 3, renderX + 15, renderY + 15, 0xFF3399FF);
                guiGraphics.fill(renderX + 5, renderY + 5, renderX + 13, renderY + 13, 0xFF66CCFF);
            }

            @Override
            public void updateTooltip(int mouseX, int mouseY) {
                setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                        net.minecraft.network.chat.Component.translatable("gui.mekanism_complex_industries.chemical_film_coating.jei_desc")
                ));
            }
        });
    }
}
