package com.complexindustries.mekanism.client.jei;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.client.gui.GuiAirCompressor;
import com.complexindustries.mekanism.content.freezer.GuiFreezerController;
import com.complexindustries.mekanism.registration.MCIBlocks;
import com.complexindustries.mekanism.registration.MCIFluids;
import com.complexindustries.mekanism.registration.MCIGases;
import java.util.ArrayList;
import java.util.List;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.MekanismJEIRecipeType;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class MCIJEIPlugin implements IModPlugin {

    public static final ResourceLocation PLUGIN_UID = new ResourceLocation(MCIConstants.MODID, "jei_plugin");

    public static final MekanismJEIRecipeType<FreezerJEIRecipe> FREEZER_RECIPE_TYPE =
            new MekanismJEIRecipeType<>(new ResourceLocation(MCIConstants.MODID, "freezer"), FreezerJEIRecipe.class);

    public static final RecipeType<FreezerJEIRecipe> FREEZER_JEI_TYPE =
            MekanismJEI.recipeType(FREEZER_RECIPE_TYPE);

    public static final MekanismJEIRecipeType<AirCompressorJEIRecipe> AIR_COMPRESSOR_RECIPE_TYPE =
            new MekanismJEIRecipeType<>(new ResourceLocation(MCIConstants.MODID, "air_compressor"), AirCompressorJEIRecipe.class);

    public static final RecipeType<AirCompressorJEIRecipe> AIR_COMPRESSOR_JEI_TYPE =
            MekanismJEI.recipeType(AIR_COMPRESSOR_RECIPE_TYPE);

    @NotNull
    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(new FreezerRecipeCategory(guiHelper, FREEZER_RECIPE_TYPE));
        registry.addRecipeCategories(new AirCompressorRecipeCategory(guiHelper, AIR_COMPRESSOR_RECIPE_TYPE));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry) {
        // 1. Freezer Recipes
        List<FreezerJEIRecipe> freezerRecipes = new ArrayList<>();

        // Process 1: 压缩空气 -> 氮气 (产物砍半: 1000 mB 空气 -> 500 mB 氮气)
        freezerRecipes.add(new FreezerJEIRecipe(
                null,
                MCIGases.COMPRESSED_AIR.getStack(1000),
                null,
                MCIGases.NITROGEN.getStack(500),
                Component.translatable("gui.mekanism_complex_industries.jei.process.nitrogen"),
                Component.translatable("gui.mekanism_complex_industries.jei.temp_requirement")
        ));

        // Process 2: 低温冷煤 + 压缩空气 -> 稀有气体(1/100 产出) + 水(1:1 融化回收)
        freezerRecipes.add(new FreezerJEIRecipe(
                new FluidStack(MCIFluids.SOURCE_CRYOGENIC_REFRIGERANT.get(), 1000),
                MCIGases.COMPRESSED_AIR.getStack(1000),
                new FluidStack(Fluids.WATER, 1000),
                MCIGases.NOBLE_GAS.getStack(10),
                Component.translatable("gui.mekanism_complex_industries.jei.process.noble_gas"),
                Component.translatable("gui.mekanism_complex_industries.jei.temp_requirement")
        ));

        // Process 3: 水 -> 低温冷煤 (产量效率减少至 1/10)
        freezerRecipes.add(new FreezerJEIRecipe(
                new FluidStack(Fluids.WATER, 1000),
                null,
                new FluidStack(MCIFluids.SOURCE_CRYOGENIC_REFRIGERANT.get(), 1000),
                null,
                Component.translatable("gui.mekanism_complex_industries.jei.process.refrigerant"),
                Component.translatable("gui.mekanism_complex_industries.jei.temp_requirement")
        ));

        registry.addRecipes(FREEZER_JEI_TYPE, freezerRecipes);

        // 2. Air Compressor Recipes
        List<AirCompressorJEIRecipe> airCompressorRecipes = new ArrayList<>();
        airCompressorRecipes.add(new AirCompressorJEIRecipe(
                MCIGases.COMPRESSED_AIR.getStack(1000),
                Component.translatable("gui.mekanism_complex_industries.jei.air_compressor.usage"),
                Component.translatable("gui.mekanism_complex_industries.jei.air_compressor.rate"),
                Component.translatable("gui.mekanism_complex_industries.jei.air_compressor.source")
        ));
        registry.addRecipes(AIR_COMPRESSOR_JEI_TYPE, airCompressorRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
        registry.addRecipeCatalyst(new ItemStack(MCIBlocks.FREEZER_CONTROLLER.get()), FREEZER_JEI_TYPE);
        registry.addRecipeCatalyst(new ItemStack(MCIBlocks.FREEZER_CASING.get()), FREEZER_JEI_TYPE);
        registry.addRecipeCatalyst(new ItemStack(MCIBlocks.FREEZER_VALVE.get()), FREEZER_JEI_TYPE);

        registry.addRecipeCatalyst(new ItemStack(MCIBlocks.AIR_COMPRESSOR.get()), AIR_COMPRESSOR_JEI_TYPE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registry) {
        registry.addRecipeClickArea(GuiFreezerController.class, 42, 18, 92, 44, FREEZER_JEI_TYPE);
        registry.addRecipeClickArea(GuiAirCompressor.class, 48, 23, 80, 42, AIR_COMPRESSOR_JEI_TYPE);
    }
}
