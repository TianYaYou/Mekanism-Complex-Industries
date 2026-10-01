package com.complexindustries.mekanism.client.jei;

import com.complexindustries.mekanism.registration.MCIBlocks;
import java.util.Collections;
import java.util.List;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.jei.BaseRecipeCategory;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.component.config.DataType;
import com.mojang.serialization.Codec;
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

public class AirCompressorRecipeCategory extends BaseRecipeCategory<AirCompressorJEIRecipe> {

    private final GuiGauge<?> outputChemical;

    public AirCompressorRecipeCategory(IGuiHelper helper, RecipeType<AirCompressorJEIRecipe> recipeType) {
        super(helper, recipeType, Component.translatable("gui.mekanism_complex_industries.air_compressor.category"),
                helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(MCIBlocks.AIR_COMPRESSOR.get())),
                0, 0, 170, 70);

        // Power vertical bar on left
        addElement(new GuiVerticalPowerBar(this, RecipeViewerUtils.FULL_BAR, 6, 5));
        // Power slot
        addSlot(SlotType.POWER, 18, 26).with(SlotOverlay.POWER);

        // Center monitor screen with 4 crisp, non-squished lines
        addElement(new GuiInnerScreen(this, 42, 6, 74, 56, () -> List.of(
                Component.translatable("gui.mekanism_complex_industries.jei.air_compressor.title"),
                Component.translatable("gui.mekanism_complex_industries.jei.air_compressor.short_usage"),
                Component.translatable("gui.mekanism_complex_industries.jei.air_compressor.short_rate"),
                Component.translatable("gui.mekanism_complex_industries.jei.air_compressor.short_source")
        )).spacing(2).tooltip(() -> List.of(
                Component.translatable("gui.mekanism_complex_industries.jei.air_compressor.title"),
                Component.translatable("gui.mekanism_complex_industries.jei.air_compressor.source"),
                Component.translatable("gui.mekanism_complex_industries.jei.air_compressor.upgrades")
        )));

        // Animated progress arrow (28px wide) cleanly pointing between screen and gauge
        addSimpleProgress(ProgressType.SMALL_RIGHT, 118, 25);

        // Output chemical gauge on right
        outputChemical = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 146, 5));
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName(AirCompressorJEIRecipe recipe) {
        return null;
    }

    @Nullable
    @Override
    public Codec<AirCompressorJEIRecipe> getCodec(ICodecHelper codecHelper, IRecipeManager recipeManager) {
        return null;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, AirCompressorJEIRecipe recipe, @NotNull IFocusGroup focusGroup) {
        initChemical(builder, RecipeIngredientRole.OUTPUT, outputChemical, Collections.singletonList(recipe.outputChemical()));
    }
}
