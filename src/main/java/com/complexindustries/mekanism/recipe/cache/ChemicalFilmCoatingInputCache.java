package com.complexindustries.mekanism.recipe.cache;

import java.util.function.Function;
import com.complexindustries.mekanism.recipe.ChemicalFilmCoatingRecipe;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.TripleInputRecipeCache;
import mekanism.common.recipe.lookup.cache.type.ChemicalInputCache;
import mekanism.common.recipe.lookup.cache.type.ItemInputCache;
import net.minecraft.world.item.ItemStack;

public class ChemicalFilmCoatingInputCache extends TripleInputRecipeCache<
        ItemStack, ItemStackIngredient,
        ItemStack, ItemStackIngredient,
        ChemicalStack, ChemicalStackIngredient,
        ChemicalFilmCoatingRecipe,
        ItemInputCache<ChemicalFilmCoatingRecipe>,
        ItemInputCache<ChemicalFilmCoatingRecipe>,
        ChemicalInputCache<ChemicalFilmCoatingRecipe>> {

    public ChemicalFilmCoatingInputCache(MekanismRecipeType<?, ChemicalFilmCoatingRecipe, ?> recipeType,
                                        Function<ChemicalFilmCoatingRecipe, ItemStackIngredient> inputAlloy,
                                        Function<ChemicalFilmCoatingRecipe, ItemStackIngredient> inputChip,
                                        Function<ChemicalFilmCoatingRecipe, ChemicalStackIngredient> inputChemical) {
        super(recipeType, inputAlloy, new ItemInputCache<>(), inputChip, new ItemInputCache<>(), inputChemical, new ChemicalInputCache<>());
    }
}
