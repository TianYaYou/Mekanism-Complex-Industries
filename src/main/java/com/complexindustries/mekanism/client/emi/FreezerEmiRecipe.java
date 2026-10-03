package com.complexindustries.mekanism.client.emi;

import com.complexindustries.mekanism.client.gui.element.bar.GuiCryoRateBar;
import com.complexindustries.mekanism.client.jei.FreezerJEIRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.List;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.recipe.MekanismEmiRecipe;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class FreezerEmiRecipe extends MekanismEmiRecipe<FreezerJEIRecipe> {

    private final boolean hasInputChem;
    private final boolean hasInputFluid;
    private final boolean hasOutputFluid;
    private final boolean hasOutputChem;

    public FreezerEmiRecipe(EmiRecipeCategory category, ResourceLocation id, FreezerJEIRecipe recipe) {
        super(category, id, recipe, 0, 0, 170, 70);
        this.hasInputChem = recipe.inputChemical() != null;
        this.hasInputFluid = recipe.inputFluid() != null;
        this.hasOutputFluid = recipe.outputFluid() != null;
        this.hasOutputChem = recipe.outputChemical() != null;

        if (hasInputChem) {
            addInputDefinition(IngredientCreatorAccess.chemicalStack().from(recipe.inputChemical()));
        }
        if (hasInputFluid) {
            addInputDefinition(IngredientCreatorAccess.fluid().from(recipe.inputFluid()));
        }
        if (hasOutputFluid) {
            addFluidOutputDefinition(List.of(recipe.outputFluid()));
        }
        if (hasOutputChem) {
            addChemicalOutputDefinition(List.of(recipe.outputChemical()));
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        int inIdx = 0;
        if (hasInputChem) {
            initTank(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT_1), this, 4, 5), input(inIdx++));
        } else {
            addElement(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT_1), this, 4, 5));
        }
        if (hasInputFluid) {
            initTank(widgetHolder, GuiFluidGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT_2), this, 24, 5), input(inIdx++));
        } else {
            addElement(widgetHolder, GuiFluidGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT_2), this, 24, 5));
        }

        // Center Terminal Screen
        addElement(widgetHolder, new GuiInnerScreen(this, 44, 5, 82, 48, () -> List.of(
                recipe.processName() != null ? recipe.processName() : Component.translatable("gui.mekanism_complex_industries.jei.freezer.title"),
                Component.translatable("gui.mekanism_complex_industries.jei.freezer.short_temp"),
                Component.translatable("gui.mekanism_complex_industries.jei.freezer.short_efficiency"),
                Component.translatable("gui.mekanism_complex_industries.jei.freezer.short_dimensions")
        )).spacing(1).tooltip(() -> List.of(
                recipe.processName() != null ? recipe.processName() : Component.translatable("gui.mekanism_complex_industries.jei.freezer.title"),
                Component.translatable("gui.mekanism_complex_industries.jei.freezer.efficiency"),
                Component.translatable("gui.mekanism_complex_industries.jei.temp_requirement"),
                Component.translatable("gui.mekanism_complex_industries.jei.freezer.dimensions")
        )));

        // Cryo Rate Bar
        addElement(widgetHolder, new GuiCryoRateBar(this, RecipeViewerUtils.FULL_BAR, 45, 55));

        int outIdx = 0;
        if (hasOutputFluid) {
            initTank(widgetHolder, GuiFluidGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT_1), this, 128, 5), output(outIdx++)).recipeContext(this);
        } else {
            addElement(widgetHolder, GuiFluidGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT_1), this, 128, 5));
        }
        if (hasOutputChem) {
            initTank(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT_2), this, 148, 5), output(outIdx++)).recipeContext(this);
        } else {
            addElement(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT_2), this, 148, 5));
        }
    }
}
