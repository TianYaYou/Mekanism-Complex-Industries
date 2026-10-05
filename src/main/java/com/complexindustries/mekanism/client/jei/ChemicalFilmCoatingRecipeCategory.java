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

public class ChemicalFilmCoatingRecipeCategory extends BaseRecipeCategory<ChemicalFilmCoatingJEIRecipe> {

    private final GuiGauge<?> inputChemical;
    private final GuiSlot alloy;
    private final GuiSlot chip;
    private final GuiSlot output;

    public ChemicalFilmCoatingRecipeCategory(IGuiHelper helper, RecipeType<ChemicalFilmCoatingJEIRecipe> recipeType) {
        super(helper, recipeType, Component.translatable("gui.mekanism_complex_industries.chemical_film_coating.category"),
                helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(MCIBlocks.CHEMICAL_FILM_COATER.get())),
                -28, -16, 146, 60);

        inputChemical = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 28, 16));
        alloy = addSlot(SlotType.INPUT, 54, 18);
        chip = addSlot(SlotType.INPUT, 54, 42);
        output = addSlot(SlotType.OUTPUT, 116, 42);
        addSlot(SlotType.POWER, 141, 20).with(SlotOverlay.POWER);
        addElement(new GuiVerticalPowerBar(this, RecipeViewerUtils.FULL_BAR, 164, 16));
        addSimpleProgress(ProgressType.RIGHT, 78, 47);

        addElement(new mekanism.client.gui.element.GuiElement(this, 141, 44, 18, 18) {
            @Override
            public void drawBackground(@NotNull net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
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
                        Component.translatable("gui.mekanism_complex_industries.chemical_film_coating.jei_desc")
                ));
            }
        });
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName(ChemicalFilmCoatingJEIRecipe recipe) {
        return null;
    }

    @Nullable
    @Override
    public Codec<ChemicalFilmCoatingJEIRecipe> getCodec(ICodecHelper codecHelper, IRecipeManager recipeManager) {
        return null;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, ChemicalFilmCoatingJEIRecipe recipe, @NotNull IFocusGroup focusGroup) {
        initItem(builder, RecipeIngredientRole.INPUT, alloy, recipe.alloyItems());
        initItem(builder, RecipeIngredientRole.INPUT, chip, recipe.chipItems());
        initChemical(builder, RecipeIngredientRole.INPUT, inputChemical, recipe.inputChemicals());
        initItem(builder, RecipeIngredientRole.OUTPUT, output, Collections.singletonList(recipe.outputItem()));
    }
}
