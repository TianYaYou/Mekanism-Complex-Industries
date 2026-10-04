package com.complexindustries.mekanism.recipe.cache;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import com.complexindustries.mekanism.recipe.CrystalGrowthRecipe;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.ILongInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CrystalGrowthCachedRecipe extends CachedRecipe<CrystalGrowthRecipe> {

    private final IInputHandler<@NotNull ItemStack> itemInputHandler;
    private final ILongInputHandler<@NotNull ChemicalStack> chemicalInputHandlerA;
    private final ILongInputHandler<@NotNull ChemicalStack> chemicalInputHandlerB;
    private final IOutputHandler<@NotNull ItemStack> outputHandler;

    private ItemStack recipeItem = ItemStack.EMPTY;
    private ChemicalStack recipeChemicalA = ChemicalStack.EMPTY;
    private ChemicalStack recipeChemicalB = ChemicalStack.EMPTY;
    private boolean swappedChemicals = false;
    private ItemStack recipeOutput = ItemStack.EMPTY;

    public CrystalGrowthCachedRecipe(CrystalGrowthRecipe recipe, BooleanSupplier recheckAllRecipeErrors,
                                     IInputHandler<@NotNull ItemStack> itemInputHandler,
                                     ILongInputHandler<@NotNull ChemicalStack> chemicalInputHandlerA,
                                     ILongInputHandler<@NotNull ChemicalStack> chemicalInputHandlerB,
                                     IOutputHandler<@NotNull ItemStack> outputHandler) {
        super(recipe, recheckAllRecipeErrors);
        this.itemInputHandler = Objects.requireNonNull(itemInputHandler, "Item input handler cannot be null.");
        this.chemicalInputHandlerA = Objects.requireNonNull(chemicalInputHandlerA, "Chemical input handler A cannot be null.");
        this.chemicalInputHandlerB = Objects.requireNonNull(chemicalInputHandlerB, "Chemical input handler B cannot be null.");
        this.outputHandler = Objects.requireNonNull(outputHandler, "Output handler cannot be null.");
    }

    @Override
    protected void calculateOperationsThisTick(OperationTracker tracker) {
        super.calculateOperationsThisTick(tracker);
        if (tracker.shouldContinueChecking()) {
            recipeItem = itemInputHandler.getRecipeInput(recipe.getItemInput());
            if (recipeItem.isEmpty()) {
                tracker.addError(OperationTracker.RecipeError.NOT_ENOUGH_INPUT);
            } else {
                itemInputHandler.calculateOperationsCanSupport(tracker, recipeItem);
            }
        }
        if (tracker.shouldContinueChecking()) {
            ChemicalStack chemStackA = chemicalInputHandlerA.getInput();
            ChemicalStack chemStackB = chemicalInputHandlerB.getInput();

            if (recipe.getChemicalInputA().test(chemStackA) && recipe.getChemicalInputB().test(chemStackB)) {
                swappedChemicals = false;
                recipeChemicalA = chemStackA.copyWithAmount(recipe.getChemicalInputA().getNeededAmount(chemStackA));
                recipeChemicalB = chemStackB.copyWithAmount(recipe.getChemicalInputB().getNeededAmount(chemStackB));
                chemicalInputHandlerA.calculateOperationsCanSupport(tracker, recipeChemicalA);
                if (tracker.shouldContinueChecking()) {
                    chemicalInputHandlerB.calculateOperationsCanSupport(tracker, recipeChemicalB);
                }
            } else if (recipe.getChemicalInputA().test(chemStackB) && recipe.getChemicalInputB().test(chemStackA)) {
                swappedChemicals = true;
                recipeChemicalA = chemStackB.copyWithAmount(recipe.getChemicalInputA().getNeededAmount(chemStackB));
                recipeChemicalB = chemStackA.copyWithAmount(recipe.getChemicalInputB().getNeededAmount(chemStackA));
                chemicalInputHandlerB.calculateOperationsCanSupport(tracker, recipeChemicalA);
                if (tracker.shouldContinueChecking()) {
                    chemicalInputHandlerA.calculateOperationsCanSupport(tracker, recipeChemicalB);
                }
            } else {
                tracker.addError(OperationTracker.RecipeError.NOT_ENOUGH_SECONDARY_INPUT);
            }
        }
        if (tracker.shouldContinueChecking()) {
            recipeOutput = recipe.getOutput(recipeItem, recipeChemicalA, recipeChemicalB);
            outputHandler.calculateOperationsCanSupport(tracker, recipeOutput);
        }
    }

    @Override
    public boolean isInputValid() {
        ItemStack item = itemInputHandler.getInput();
        if (item.isEmpty()) return false;
        ChemicalStack chemA = chemicalInputHandlerA.getInput();
        ChemicalStack chemB = chemicalInputHandlerB.getInput();
        return recipe.test(item, chemA, chemB);
    }

    @Override
    protected void finishProcessing(int operations) {
        if (!recipeOutput.isEmpty() && !recipeItem.isEmpty() && !recipeChemicalA.isEmpty() && !recipeChemicalB.isEmpty()) {
            itemInputHandler.use(recipeItem, operations);
            if (!swappedChemicals) {
                chemicalInputHandlerA.use(recipeChemicalA, operations);
                chemicalInputHandlerB.use(recipeChemicalB, operations);
            } else {
                chemicalInputHandlerB.use(recipeChemicalA, operations);
                chemicalInputHandlerA.use(recipeChemicalB, operations);
            }
            outputHandler.handleOutput(recipeOutput, operations);
        }
    }
}
