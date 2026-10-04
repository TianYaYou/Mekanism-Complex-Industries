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
import mekanism.common.inventory.container.slot.SlotOverlay;
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

public class ChemicalSoakingRecipeCategory extends BaseRecipeCategory<ChemicalSoakingJEIRecipe> {

    private final GuiGauge<?> inputChemical;
    private final GuiSlot input;
    private final GuiSlot output;

    public ChemicalSoakingRecipeCategory(IGuiHelper helper, RecipeType<ChemicalSoakingJEIRecipe> recipeType) {
        super(helper, recipeType, Component.translatable("gui.mekanism_complex_industries.chemical_soaker.category"),
                helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(MCIBlocks.CHEMICAL_SOAKER.get())),
                -25, -13, 146, 60);

        input = addSlot(SlotType.INPUT, 45, 35);
        output = addSlot(SlotType.OUTPUT, 116, 35);
        addSlot(SlotType.POWER, 144, 35).with(SlotOverlay.POWER);
        addElement(new GuiVerticalPowerBar(this, RecipeViewerUtils.FULL_BAR, 164, 15));
        inputChemical = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 25, 13));
        addSimpleProgress(ProgressType.LARGE_RIGHT, 64, 39);
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName(ChemicalSoakingJEIRecipe recipe) {
        return null;
    }

    @Nullable
    @Override
    public Codec<ChemicalSoakingJEIRecipe> getCodec(ICodecHelper codecHelper, IRecipeManager recipeManager) {
        return null;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, ChemicalSoakingJEIRecipe recipe, @NotNull IFocusGroup focusGroup) {
        initItem(builder, RecipeIngredientRole.INPUT, input, recipe.inputItems());
        initChemical(builder, RecipeIngredientRole.INPUT, inputChemical, recipe.inputChemicals());
        initItem(builder, RecipeIngredientRole.OUTPUT, output, Collections.singletonList(recipe.outputItem()));
    }
}
