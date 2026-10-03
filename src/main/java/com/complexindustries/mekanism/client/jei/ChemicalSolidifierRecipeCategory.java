package com.complexindustries.mekanism.client.jei;

import java.util.Collections;
import java.util.List;
import com.complexindustries.mekanism.registration.MCIBlocks;
import com.mojang.serialization.Codec;
import mekanism.client.gui.element.GuiInnerScreen;
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

public class ChemicalSolidifierRecipeCategory extends BaseRecipeCategory<ChemicalSolidifierJEIRecipe> {

    private final GuiGauge<?> inputChemical;
    private final GuiSlot outputSlot;

    public ChemicalSolidifierRecipeCategory(IGuiHelper helper, RecipeType<ChemicalSolidifierJEIRecipe> recipeType) {
        super(helper, recipeType, Component.translatable("gui.mekanism_complex_industries.chemical_solidifier.category"),
                helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(MCIBlocks.CHEMICAL_SOLIDIFIER.get())),
                0, 0, 170, 70);

        // Power vertical bar on left
        addElement(new GuiVerticalPowerBar(this, RecipeViewerUtils.FULL_BAR, 6, 5));

        // Input chemical gauge
        inputChemical = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 20, 5));

        // Center monitor screen
        addElement(new GuiInnerScreen(this, 44, 6, 62, 56, () -> List.of(
                Component.translatable("gui.mekanism_complex_industries.jei.chemical_solidifier.title"),
                Component.translatable("gui.mekanism_complex_industries.jei.chemical_solidifier.usage"),
                Component.translatable("gui.mekanism_complex_industries.jei.chemical_solidifier.duration"),
                Component.translatable("gui.mekanism_complex_industries.jei.chemical_solidifier.muffler")
        )).spacing(2));

        // Animated progress arrow
        addSimpleProgress(ProgressType.LARGE_RIGHT, 110, 25);

        // Output item slot
        outputSlot = addSlot(SlotType.OUTPUT, 142, 25);
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName(ChemicalSolidifierJEIRecipe recipe) {
        return null;
    }

    @Nullable
    @Override
    public Codec<ChemicalSolidifierJEIRecipe> getCodec(ICodecHelper codecHelper, IRecipeManager recipeManager) {
        return null;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, ChemicalSolidifierJEIRecipe recipe, @NotNull IFocusGroup focusGroup) {
        initChemical(builder, RecipeIngredientRole.INPUT, inputChemical, Collections.singletonList(recipe.inputChemical()));
        initItem(builder, RecipeIngredientRole.OUTPUT, outputSlot, Collections.singletonList(recipe.outputItem()));
    }
}
