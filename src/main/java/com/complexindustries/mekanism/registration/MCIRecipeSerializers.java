package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.recipe.BasicChemicalSolidifierRecipe;
import com.complexindustries.mekanism.recipe.ChemicalSolidifierRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MCIRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MCIConstants.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BasicChemicalSolidifierRecipe>> SOLIDIFYING =
            RECIPE_SERIALIZERS.register("chemical_solidifying", ChemicalSolidifierRecipeSerializer::create);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<com.complexindustries.mekanism.recipe.ChemicalSoakingRecipe>> CHEMICAL_SOAKING =
            RECIPE_SERIALIZERS.register("chemical_soaking", () -> mekanism.common.recipe.serializer.MekanismRecipeSerializer.itemChemicalToItem(com.complexindustries.mekanism.recipe.ChemicalSoakingRecipe::new));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<com.complexindustries.mekanism.recipe.CrystalGrowthRecipe>> CRYSTAL_GROWTH =
            RECIPE_SERIALIZERS.register("crystal_growth", com.complexindustries.mekanism.recipe.CrystalGrowthRecipeSerializer::create);

    private MCIRecipeSerializers() {}
}
