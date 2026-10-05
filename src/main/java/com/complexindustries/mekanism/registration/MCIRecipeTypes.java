package com.complexindustries.mekanism.registration;

import java.lang.reflect.Constructor;
import java.util.function.Function;
import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.recipe.ChemicalSolidifierRecipe;
import mekanism.api.recipes.vanilla_input.SingleChemicalRecipeInput;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleChemical;
import mekanism.common.registration.impl.RecipeTypeDeferredRegister;
import mekanism.common.registration.impl.RecipeTypeRegistryObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeInput;

public final class MCIRecipeTypes {
    public static final RecipeTypeDeferredRegister RECIPE_TYPES =
            new RecipeTypeDeferredRegister(MCIConstants.MODID);

    public static final RecipeTypeRegistryObject<SingleChemicalRecipeInput, ChemicalSolidifierRecipe, SingleChemical<ChemicalSolidifierRecipe>> SOLIDIFYING =
            RECIPE_TYPES.registerMek("chemical_solidifying", name -> createRecipeType(name, recipeType -> new SingleChemical<>(recipeType, ChemicalSolidifierRecipe::getInput)));

    public static final RecipeTypeRegistryObject<mekanism.api.recipes.vanilla_input.SingleItemChemicalRecipeInput, com.complexindustries.mekanism.recipe.ChemicalSoakingRecipe, mekanism.common.recipe.lookup.cache.InputRecipeCache.ItemChemical<com.complexindustries.mekanism.recipe.ChemicalSoakingRecipe>> CHEMICAL_SOAKING =
            RECIPE_TYPES.registerMek("chemical_soaking", name -> createRecipeType(name, recipeType -> new mekanism.common.recipe.lookup.cache.InputRecipeCache.ItemChemical<>(recipeType, com.complexindustries.mekanism.recipe.ChemicalSoakingRecipe::getItemInput, com.complexindustries.mekanism.recipe.ChemicalSoakingRecipe::getChemicalInput)));

    public static final RecipeTypeRegistryObject<com.complexindustries.mekanism.recipe.input.ItemBiChemicalRecipeInput, com.complexindustries.mekanism.recipe.CrystalGrowthRecipe, com.complexindustries.mekanism.recipe.cache.ItemDoubleChemicalInputCache> CRYSTAL_GROWTH =
            RECIPE_TYPES.registerMek("crystal_growth", name -> createRecipeType(name, recipeType -> new com.complexindustries.mekanism.recipe.cache.ItemDoubleChemicalInputCache(recipeType,
                    com.complexindustries.mekanism.recipe.CrystalGrowthRecipe::getItemInput,
                    com.complexindustries.mekanism.recipe.CrystalGrowthRecipe::getChemicalInputA,
                    com.complexindustries.mekanism.recipe.CrystalGrowthRecipe::getChemicalInputB)));

    public static final RecipeTypeRegistryObject<mekanism.api.recipes.vanilla_input.SingleItemChemicalRecipeInput, com.complexindustries.mekanism.recipe.SiliconSlicingRecipe, mekanism.common.recipe.lookup.cache.InputRecipeCache.ItemChemical<com.complexindustries.mekanism.recipe.SiliconSlicingRecipe>> SILICON_SLICING =
            RECIPE_TYPES.registerMek("silicon_slicing", name -> createRecipeType(name, recipeType -> new mekanism.common.recipe.lookup.cache.InputRecipeCache.ItemChemical<>(recipeType, com.complexindustries.mekanism.recipe.SiliconSlicingRecipe::getItemInput, com.complexindustries.mekanism.recipe.SiliconSlicingRecipe::getChemicalInput)));

    public static final RecipeTypeRegistryObject<com.complexindustries.mekanism.recipe.input.PhotolithographyRecipeInput, com.complexindustries.mekanism.recipe.PhotolithographyRecipe, com.complexindustries.mekanism.recipe.cache.PhotolithographyInputCache> PHOTOLITHOGRAPHY =
            RECIPE_TYPES.registerMek("photolithography", name -> createRecipeType(name, recipeType -> new com.complexindustries.mekanism.recipe.cache.PhotolithographyInputCache(recipeType,
                    com.complexindustries.mekanism.recipe.PhotolithographyRecipe::getItemInput,
                    com.complexindustries.mekanism.recipe.PhotolithographyRecipe::getMaskInput,
                    com.complexindustries.mekanism.recipe.PhotolithographyRecipe::getChemicalInput)));

    @SuppressWarnings("unchecked")
    private static <VANILLA_INPUT extends RecipeInput, RECIPE extends mekanism.api.recipes.MekanismRecipe<VANILLA_INPUT>, INPUT_CACHE extends IInputRecipeCache>
    MekanismRecipeType<VANILLA_INPUT, RECIPE, INPUT_CACHE> createRecipeType(ResourceLocation name, Function<MekanismRecipeType<VANILLA_INPUT, RECIPE, INPUT_CACHE>, INPUT_CACHE> inputCacheCreator) {
        try {
            Constructor<MekanismRecipeType> constructor = MekanismRecipeType.class.getDeclaredConstructor(ResourceLocation.class, Function.class);
            constructor.setAccessible(true);
            return constructor.newInstance(name, inputCacheCreator);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate MekanismRecipeType for " + name, e);
        }
    }

    private MCIRecipeTypes() {}
}
