package com.complexindustries.mekanism.client.jei;

import com.complexindustries.mekanism.registration.MCIBlocks;
import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.List;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.jei.BaseRecipeCategory;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RefineryRecipeCategory extends BaseRecipeCategory<RefineryJEIRecipe> {

    private final GuiGauge<?> inputChemical;
    private final GuiGauge<?> outputBitumen;
    private final GuiGauge<?> outputHeavyOil;
    private final GuiGauge<?> outputRefinedFuel;
    private final GuiGauge<?> outputNaphtha;
    private final GuiGauge<?> outputPetroleumGas;
    private RefineryJEIRecipe currentRecipe;

    public RefineryRecipeCategory(IGuiHelper helper, RecipeType<RefineryJEIRecipe> recipeType) {
        super(helper, recipeType, Component.translatable("gui.mekanism_complex_industries.refinery.category"),
                helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(MCIBlocks.REFINERY_CONTROLLER.get())),
                0, 0, 206, 70);

        // 1. Left Input Chemical Gauge (Dense Crude Oil)
        inputChemical = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 5, 5));

        // 2. Center Terminal Screen (80x46)
        addElement(new GuiInnerScreen(this, 27, 5, 80, 46, () -> {
            Component titleComp = currentRecipe != null ? currentRecipe.title() : Component.translatable("gui.mekanism_complex_industries.jei.refinery.title");
            Component heatComp = currentRecipe != null ? currentRecipe.heatRequirement() : Component.translatable("gui.mekanism_complex_industries.jei.refinery.short_heat");
            Component deltaComp = currentRecipe != null ? currentRecipe.deltaRequirement() : Component.translatable("gui.mekanism_complex_industries.jei.refinery.short_delta");
            Component rateComp = currentRecipe != null ? currentRecipe.rateInfo() : Component.translatable("gui.mekanism_complex_industries.jei.refinery.short_rate");
            return List.of(titleComp, heatComp, deltaComp, rateComp);
        }).spacing(1).tooltip(() -> List.of(
                Component.translatable("gui.mekanism_complex_industries.jei.refinery.tooltip.title"),
                Component.translatable("gui.mekanism_complex_industries.jei.refinery.tooltip.dimensions"),
                Component.translatable("gui.mekanism_complex_industries.jei.refinery.tooltip.heat"),
                Component.translatable("gui.mekanism_complex_industries.jei.refinery.tooltip.delta"),
                Component.translatable("gui.mekanism_complex_industries.jei.refinery.tooltip.warning")
        )));

        // 3. Center Cracking Horizontal Rate Bar (80x8)
        addElement(new GuiHorizontalRateBar(this, RecipeViewerUtils.FULL_BAR, 27, 54));

        // 4. Right 5 Output Chemical Gauges (Layer 1 ~ Layer 5)
        outputBitumen = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 111, 5));
        outputHeavyOil = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 129, 5));
        outputRefinedFuel = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 147, 5));
        outputNaphtha = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 165, 5));
        outputPetroleumGas = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 183, 5));
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName(RefineryJEIRecipe recipe) {
        return null;
    }

    @Nullable
    @Override
    public Codec<RefineryJEIRecipe> getCodec(ICodecHelper codecHelper, IRecipeManager recipeManager) {
        return null;
    }

    @Override
    public void draw(@NotNull RefineryJEIRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        this.currentRecipe = recipe;
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, RefineryJEIRecipe recipe, @NotNull IFocusGroup focusGroup) {
        if (recipe.inputChemical() != null) {
            initChemical(builder, RecipeIngredientRole.INPUT, inputChemical, Collections.singletonList(recipe.inputChemical()));
        }
        if (recipe.bitumen() != null) {
            initChemical(builder, RecipeIngredientRole.OUTPUT, outputBitumen, Collections.singletonList(recipe.bitumen()));
        }
        if (recipe.heavyOil() != null) {
            initChemical(builder, RecipeIngredientRole.OUTPUT, outputHeavyOil, Collections.singletonList(recipe.heavyOil()));
        }
        if (recipe.refinedFuel() != null) {
            initChemical(builder, RecipeIngredientRole.OUTPUT, outputRefinedFuel, Collections.singletonList(recipe.refinedFuel()));
        }
        if (recipe.naphtha() != null) {
            initChemical(builder, RecipeIngredientRole.OUTPUT, outputNaphtha, Collections.singletonList(recipe.naphtha()));
        }
        if (recipe.petroleumGas() != null) {
            initChemical(builder, RecipeIngredientRole.OUTPUT, outputPetroleumGas, Collections.singletonList(recipe.petroleumGas()));
        }
    }
}
