package com.complexindustries.mekanism.recipe.cache;

import java.util.function.BooleanSupplier;
import com.complexindustries.mekanism.recipe.SiliconSlicingRecipe;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.recipes.cache.TwoInputCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.ILongInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.world.item.ItemStack;

public class SiliconSlicingCachedRecipe extends TwoInputCachedRecipe<ItemStack, ChemicalStack, ItemStack, SiliconSlicingRecipe> {

    private final IChemicalTank chemicalTank;

    public SiliconSlicingCachedRecipe(SiliconSlicingRecipe recipe, BooleanSupplier recheckAllErrors,
                                      IInputHandler<ItemStack> itemInputHandler,
                                      ILongInputHandler<ChemicalStack> chemicalInputHandler,
                                      IOutputHandler<ItemStack> outputHandler,
                                      IChemicalTank chemicalTank) {
        super(recipe, recheckAllErrors, itemInputHandler, chemicalInputHandler, outputHandler,
                recipe::getItemInput, recipe::getChemicalInput, recipe::getOutput,
                ItemStack::isEmpty, ChemicalStack::isEmpty, ItemStack::isEmpty);
        this.chemicalTank = chemicalTank;
    }

    @Override
    protected void calculateOperationsThisTick(OperationTracker tracker) {
        // Condition: Nitrogen must be at least 80% (800 mB) full to work
        if (chemicalTank == null || chemicalTank.getStored() < 800L) {
            tracker.addError(OperationTracker.RecipeError.NOT_ENOUGH_SECONDARY_INPUT);
            tracker.updateOperations(0);
            return;
        }
        super.calculateOperationsThisTick(tracker);
    }
}
