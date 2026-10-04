package com.complexindustries.mekanism.recipe;

import com.complexindustries.mekanism.registration.MCIRecipeSerializers;
import com.complexindustries.mekanism.registration.MCIRecipeTypes;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.basic.BasicItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

@NothingNullByDefault
public class ChemicalSoakingRecipe extends BasicItemStackChemicalToItemStackRecipe {

    public ChemicalSoakingRecipe(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, ItemStack output, boolean perTickUsage) {
        super(itemInput, chemicalInput, output, perTickUsage, MCIRecipeTypes.CHEMICAL_SOAKING.get());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MCIRecipeSerializers.CHEMICAL_SOAKING.get();
    }
}
