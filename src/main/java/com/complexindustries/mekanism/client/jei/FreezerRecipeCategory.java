package com.complexindustries.mekanism.client.jei;

import com.complexindustries.mekanism.client.gui.element.bar.GuiCryoRateBar;
import com.complexindustries.mekanism.registration.MCIBlocks;
import java.util.Collections;
import java.util.List;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.MekanismLang;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FreezerRecipeCategory extends BaseRecipeCategory<FreezerJEIRecipe> {

    private final GuiGauge<?> inputGas;
    private final GuiGauge<?> inputFluid;
    private final GuiGauge<?> outputFluid;
    private final GuiGauge<?> outputGas;
    private FreezerJEIRecipe currentRecipe;

    public FreezerRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<FreezerJEIRecipe> recipeType) {
        super(helper, recipeType, Component.translatable("gui.mekanism_complex_industries.freezer.category"),
                helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(MCIBlocks.FREEZER_CONTROLLER.get())),
                3, 12, 170, 64);

        // Input Gauges on Left
        inputGas = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT_1), this, 6, 13));
        inputFluid = addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT_2), this, 26, 13));

        // Center Terminal Screen
        addElement(new GuiInnerScreen(this, 46, 15, 80, 46, () -> {
            Component titleComp = currentRecipe != null ? currentRecipe.processName() : Component.translatable("gui.mekanism_complex_industries.jei.freezer.title");
            return List.of(
                    titleComp,
                    MekanismLang.MULTIBLOCK_FORMED.translate(),
                    Component.translatable("gui.mekanism_complex_industries.jei.freezer.temp"),
                    Component.translatable("gui.mekanism_complex_industries.jei.freezer.efficiency")
            );
        }).spacing(1).tooltip(() -> List.of(
                currentRecipe != null ? currentRecipe.processName() : Component.translatable("gui.mekanism_complex_industries.jei.freezer.title"),
                Component.translatable("gui.mekanism_complex_industries.jei.temp_requirement"),
                Component.translatable("gui.mekanism_complex_industries.jei.freezer.dimensions")
        )));

        // Cryo Rate Bar under screen (Blue -> Purple gradient)
        addElement(new GuiCryoRateBar(this, FULL_BAR, 46, 63));

        // Output Gauges on Right
        outputFluid = addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT_1), this, 130, 13));
        outputGas = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT_2), this, 150, 13));
    }

    @Override
    public void draw(@NotNull FreezerJEIRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        this.currentRecipe = recipe;
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, FreezerJEIRecipe recipe, @NotNull IFocusGroup focusGroup) {
        if (recipe.inputGas() != null) {
            initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.INPUT, inputGas, Collections.singletonList(recipe.inputGas()));
        }
        if (recipe.inputFluid() != null) {
            initFluid(builder, RecipeIngredientRole.INPUT, inputFluid, Collections.singletonList(recipe.inputFluid()));
        }
        if (recipe.outputFluid() != null) {
            initFluid(builder, RecipeIngredientRole.OUTPUT, outputFluid, Collections.singletonList(recipe.outputFluid()));
        }
        if (recipe.outputGas() != null) {
            initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.OUTPUT, outputGas, Collections.singletonList(recipe.outputGas()));
        }
    }
}
