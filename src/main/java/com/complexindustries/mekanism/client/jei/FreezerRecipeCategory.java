package com.complexindustries.mekanism.client.jei;

import com.complexindustries.mekanism.client.gui.element.bar.GuiCryoRateBar;
import com.complexindustries.mekanism.registration.MCIBlocks;
import java.util.Collections;
import java.util.List;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.jei.BaseRecipeCategory;
import mekanism.common.MekanismLang;
import mekanism.common.tile.component.config.DataType;
import com.mojang.serialization.Codec;
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

public class FreezerRecipeCategory extends BaseRecipeCategory<FreezerJEIRecipe> {

    private final GuiGauge<?> inputChemical;
    private final GuiGauge<?> inputFluid;
    private final GuiGauge<?> outputFluid;
    private final GuiGauge<?> outputChemical;
    private FreezerJEIRecipe currentRecipe;

    public FreezerRecipeCategory(IGuiHelper helper, RecipeType<FreezerJEIRecipe> recipeType) {
        super(helper, recipeType, Component.translatable("gui.mekanism_complex_industries.freezer.category"),
                helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(MCIBlocks.FREEZER_CONTROLLER.get())),
                0, 0, 170, 70);

        // Input Gauges on Left
        inputChemical = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT_1), this, 4, 5));
        inputFluid = addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT_2), this, 24, 5));

        // Center Terminal Screen
        addElement(new GuiInnerScreen(this, 44, 5, 82, 48, () -> {
            Component titleComp = currentRecipe != null ? currentRecipe.processName() : Component.translatable("gui.mekanism_complex_industries.jei.freezer.title");
            return List.of(
                    titleComp,
                    Component.translatable("gui.mekanism_complex_industries.jei.freezer.short_temp"),
                    Component.translatable("gui.mekanism_complex_industries.jei.freezer.short_efficiency"),
                    Component.translatable("gui.mekanism_complex_industries.jei.freezer.short_dimensions")
            );
        }).spacing(1).tooltip(() -> List.of(
                currentRecipe != null ? currentRecipe.processName() : Component.translatable("gui.mekanism_complex_industries.jei.freezer.title"),
                Component.translatable("gui.mekanism_complex_industries.jei.freezer.efficiency"),
                Component.translatable("gui.mekanism_complex_industries.jei.temp_requirement"),
                Component.translatable("gui.mekanism_complex_industries.jei.freezer.dimensions")
        )));

        // Cryo Rate Bar under screen (Blue -> Purple gradient)
        addElement(new GuiCryoRateBar(this, RecipeViewerUtils.FULL_BAR, 45, 55));

        // Output Gauges on Right
        outputFluid = addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT_1), this, 128, 5));
        outputChemical = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT_2), this, 148, 5));
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName(FreezerJEIRecipe recipe) {
        return null;
    }

    @Nullable
    @Override
    public Codec<FreezerJEIRecipe> getCodec(ICodecHelper codecHelper, IRecipeManager recipeManager) {
        return null;
    }

    @Override
    public void draw(@NotNull FreezerJEIRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        this.currentRecipe = recipe;
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, FreezerJEIRecipe recipe, @NotNull IFocusGroup focusGroup) {
        if (recipe.inputChemical() != null) {
            initChemical(builder, RecipeIngredientRole.INPUT, inputChemical, Collections.singletonList(recipe.inputChemical()));
        }
        if (recipe.inputFluid() != null) {
            initFluid(builder, RecipeIngredientRole.INPUT, inputFluid, Collections.singletonList(recipe.inputFluid()));
        }
        if (recipe.outputFluid() != null) {
            initFluid(builder, RecipeIngredientRole.OUTPUT, outputFluid, Collections.singletonList(recipe.outputFluid()));
        }
        if (recipe.outputChemical() != null) {
            initChemical(builder, RecipeIngredientRole.OUTPUT, outputChemical, Collections.singletonList(recipe.outputChemical()));
        }
    }
}
