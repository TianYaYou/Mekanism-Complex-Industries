package com.complexindustries.mekanism.recipe;

import java.util.function.BooleanSupplier;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.recipes.cache.OneInputCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SolidifyingCachedRecipe extends OneInputCachedRecipe<@NotNull ChemicalStack, @NotNull ItemStack, ChemicalSolidifierRecipe> {

    public SolidifyingCachedRecipe(ChemicalSolidifierRecipe recipe,
                                   BooleanSupplier recheckAllErrors,
                                   IInputHandler<@NotNull ChemicalStack> inputHandler,
                                   IOutputHandler<@NotNull ItemStack> outputHandler) {
        super(recipe, recheckAllErrors, inputHandler, outputHandler, recipe::getInput, recipe::getOutput,
                ConstantPredicates.CHEMICAL_EMPTY, ConstantPredicates.ITEM_EMPTY);
    }
}
