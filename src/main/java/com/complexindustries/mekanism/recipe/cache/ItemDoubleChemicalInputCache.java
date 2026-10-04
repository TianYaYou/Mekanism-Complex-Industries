package com.complexindustries.mekanism.recipe.cache;

import java.util.function.Function;
import com.complexindustries.mekanism.recipe.CrystalGrowthRecipe;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.TripleInputRecipeCache;
import mekanism.common.recipe.lookup.cache.type.ChemicalInputCache;
import mekanism.common.recipe.lookup.cache.type.ItemInputCache;
import net.minecraft.world.item.ItemStack;

public class ItemDoubleChemicalInputCache extends TripleInputRecipeCache<
        ItemStack, ItemStackIngredient,
        ChemicalStack, ChemicalStackIngredient,
        ChemicalStack, ChemicalStackIngredient,
        CrystalGrowthRecipe,
        ItemInputCache<CrystalGrowthRecipe>,
        ChemicalInputCache<CrystalGrowthRecipe>,
        ChemicalInputCache<CrystalGrowthRecipe>> {

    public ItemDoubleChemicalInputCache(MekanismRecipeType<?, CrystalGrowthRecipe, ?> recipeType,
                                        Function<CrystalGrowthRecipe, ItemStackIngredient> inputA,
                                        Function<CrystalGrowthRecipe, ChemicalStackIngredient> inputB,
                                        Function<CrystalGrowthRecipe, ChemicalStackIngredient> inputC) {
        super(recipeType, inputA, new ItemInputCache<>(), inputB, new ChemicalInputCache<>(), inputC, new ChemicalInputCache<>());
    }
}
