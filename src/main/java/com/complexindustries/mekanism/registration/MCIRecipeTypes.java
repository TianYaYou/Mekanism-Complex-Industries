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
