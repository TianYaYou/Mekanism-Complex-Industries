package com.complexindustries.mekanism.recipe.cache;

import java.util.function.Function;
import com.complexindustries.mekanism.recipe.PhotolithographyRecipe;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.TripleInputRecipeCache;
import mekanism.common.recipe.lookup.cache.type.ChemicalInputCache;
import mekanism.common.recipe.lookup.cache.type.ItemInputCache;
import net.minecraft.world.item.ItemStack;

public class PhotolithographyInputCache extends TripleInputRecipeCache<
        ItemStack, ItemStackIngredient,
        ItemStack, ItemStackIngredient,
        ChemicalStack, ChemicalStackIngredient,
        PhotolithographyRecipe,
        ItemInputCache<PhotolithographyRecipe>,
        ItemInputCache<PhotolithographyRecipe>,
        ChemicalInputCache<PhotolithographyRecipe>> {

    public PhotolithographyInputCache(MekanismRecipeType<?, PhotolithographyRecipe, ?> recipeType,
                                      Function<PhotolithographyRecipe, ItemStackIngredient> inputItem,
                                      Function<PhotolithographyRecipe, ItemStackIngredient> inputMask,
                                      Function<PhotolithographyRecipe, ChemicalStackIngredient> inputChemical) {
        super(recipeType, inputItem, new ItemInputCache<>(), inputMask, new ItemInputCache<>(), inputChemical, new ChemicalInputCache<>());
    }
}
