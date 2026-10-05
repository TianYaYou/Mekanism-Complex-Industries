package com.complexindustries.mekanism.recipe.input;

import mekanism.api.chemical.ChemicalStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record ChemicalFilmCoatingRecipeInput(ItemStack alloy, ItemStack chip, ChemicalStack chemical) implements RecipeInput {

    @Override
    public ItemStack getItem(int index) {
        if (index == 0) return alloy;
        if (index == 1) return chip;
        return ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return alloy.isEmpty() && chip.isEmpty() && chemical.isEmpty();
    }
}
