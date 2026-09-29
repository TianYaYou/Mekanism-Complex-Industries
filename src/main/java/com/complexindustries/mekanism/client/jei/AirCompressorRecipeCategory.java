package com.complexindustries.mekanism.client.jei;

import com.complexindustries.mekanism.registration.MCIBlocks;
import java.util.Collections;
import java.util.List;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AirCompressorRecipeCategory extends BaseRecipeCategory<AirCompressorJEIRecipe> {

    private final GuiGauge<?> outputGas;

    public AirCompressorRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<AirCompressorJEIRecipe> recipeType) {
        super(helper, recipeType, Component.translatable("gui.mekanism_complex_industries.air_compressor.category"),
                helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(MCIBlocks.AIR_COMPRESSOR.get())),
                3, 12, 170, 64);

        // Power vertical bar on left
        addElement(new GuiVerticalPowerBar(this, FULL_BAR, 8, 13));
        // Power slot
        addSlot(SlotType.POWER, 18, 24).with(SlotOverlay.POWER);

        // Center monitor screen with 4 crisp, non-squished lines
        addElement(new GuiInnerScreen(this, 40, 14, 74, 46, () -> List.of(
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
        addSimpleProgress(ProgressType.SMALL_RIGHT, 118, 33);

        // Output gas gauge on right
        outputGas = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 148, 13));
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, AirCompressorJEIRecipe recipe, @NotNull IFocusGroup focusGroup) {
        initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.OUTPUT, outputGas, Collections.singletonList(recipe.outputGas()));
    }
}
