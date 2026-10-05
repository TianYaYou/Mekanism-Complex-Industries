package com.complexindustries.mekanism.recipe.input;

import mekanism.api.chemical.ChemicalStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record PhotolithographyRecipeInput(ItemStack item, ItemStack mask, ChemicalStack chemical) implements RecipeInput {

    @Override
    public ItemStack getItem(int index) {
        if (index == 0) return item;
        if (index == 1) return mask;
        return ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return item.isEmpty() && mask.isEmpty() && chemical.isEmpty();
    }
}
