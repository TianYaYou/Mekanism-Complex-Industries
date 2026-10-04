package com.complexindustries.mekanism.recipe.input;

import mekanism.api.chemical.ChemicalStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record ItemBiChemicalRecipeInput(ItemStack item, ChemicalStack chemicalA, ChemicalStack chemicalB) implements RecipeInput {

    @Override
    public ItemStack getItem(int index) {
        return index == 0 ? item : ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return item.isEmpty() && chemicalA.isEmpty() && chemicalB.isEmpty();
    }
}
