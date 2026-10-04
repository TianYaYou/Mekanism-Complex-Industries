package com.complexindustries.mekanism.client.jei;

import java.util.Collections;
import com.complexindustries.mekanism.registration.MCIBlocks;
import com.mojang.serialization.Codec;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.jei.BaseRecipeCategory;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrystalGrowthRecipeCategory extends BaseRecipeCategory<CrystalGrowthJEIRecipe> {

    private final GuiGauge<?> inputChemicalA;
    private final GuiGauge<?> inputChemicalB;
    private final GuiSlot input;
    private final GuiSlot output;

    public CrystalGrowthRecipeCategory(IGuiHelper helper, RecipeType<CrystalGrowthJEIRecipe> recipeType) {
        super(helper, recipeType, Component.translatable("gui.mekanism_complex_industries.crystal_growth.category"),
                helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(MCIBlocks.CRYSTAL_GROWTH_CHAMBER.get())),
                -4, -13, 172, 62);

        inputChemicalA = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT_1), this, 6, 15));
        inputChemicalB = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT_2), this, 27, 15));
        input = addSlot(SlotType.INPUT, 54, 40);
        addSimpleProgress(ProgressType.RIGHT, 79, 43);
        output = addSlot(SlotType.OUTPUT, 116, 40);
        addElement(new GuiVerticalPowerBar(this, RecipeViewerUtils.FULL_BAR, 164, 15));
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName(CrystalGrowthJEIRecipe recipe) {
        return null;
    }

    @Nullable
    @Override
    public Codec<CrystalGrowthJEIRecipe> getCodec(ICodecHelper codecHelper, IRecipeManager recipeManager) {
        return null;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, CrystalGrowthJEIRecipe recipe, @NotNull IFocusGroup focusGroup) {
        initChemical(builder, RecipeIngredientRole.INPUT, inputChemicalA, recipe.inputChemicalsA());
        initItem(builder, RecipeIngredientRole.INPUT, input, recipe.inputItems());
        initItem(builder, RecipeIngredientRole.OUTPUT, output, Collections.singletonList(recipe.outputItem()));
        initChemical(builder, RecipeIngredientRole.INPUT, inputChemicalB, recipe.inputChemicalsB());
    }
}
