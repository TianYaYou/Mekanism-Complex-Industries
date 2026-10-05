package com.complexindustries.mekanism.recipe.cache;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import com.complexindustries.mekanism.content.lithography.TileEntityPhotolithographyMachine;
import com.complexindustries.mekanism.recipe.PhotolithographyRecipe;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.ILongInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PhotolithographyCachedRecipe extends CachedRecipe<PhotolithographyRecipe> {

    private final IInputHandler<@NotNull ItemStack> itemInputHandler;
    private final IInputHandler<@NotNull ItemStack> maskInputHandler;
    private final ILongInputHandler<@NotNull ChemicalStack> chemicalInputHandler;
    private final IOutputHandler<@NotNull ItemStack> outputHandler;
    private final IChemicalTank chemicalTank;
    private final TileEntityPhotolithographyMachine machine;

    private ItemStack recipeItem = ItemStack.EMPTY;
    private ItemStack recipeMask = ItemStack.EMPTY;
    private ChemicalStack recipeChemical = ChemicalStack.EMPTY;
    private ItemStack recipeOutput = ItemStack.EMPTY;

    public PhotolithographyCachedRecipe(PhotolithographyRecipe recipe, BooleanSupplier recheckAllRecipeErrors,
                                        IInputHandler<@NotNull ItemStack> itemInputHandler,
                                        IInputHandler<@NotNull ItemStack> maskInputHandler,
                                        ILongInputHandler<@NotNull ChemicalStack> chemicalInputHandler,
                                        IOutputHandler<@NotNull ItemStack> outputHandler,
                                        IChemicalTank chemicalTank,
                                        TileEntityPhotolithographyMachine machine) {
        super(recipe, recheckAllRecipeErrors);
        this.itemInputHandler = Objects.requireNonNull(itemInputHandler, "Item input handler cannot be null.");
        this.maskInputHandler = Objects.requireNonNull(maskInputHandler, "Mask input handler cannot be null.");
        this.chemicalInputHandler = Objects.requireNonNull(chemicalInputHandler, "Chemical input handler cannot be null.");
        this.outputHandler = Objects.requireNonNull(outputHandler, "Output handler cannot be null.");
        this.chemicalTank = chemicalTank;
        this.machine = machine;
    }

    @Override
    protected void calculateOperationsThisTick(OperationTracker tracker) {
        super.calculateOperationsThisTick(tracker);

        // 1. Nitrogen must be at least 80% (800 mB) full
        if (chemicalTank == null || chemicalTank.getStored() < 800L) {
            tracker.addError(OperationTracker.RecipeError.NOT_ENOUGH_SECONDARY_INPUT);
            tracker.updateOperations(0);
            return;
        }

        // 2. Machine must be receiving UV laser from the right side
        if (machine == null || !machine.hasActiveLaser()) {
            tracker.addError(OperationTracker.RecipeError.NOT_ENOUGH_ENERGY);
            tracker.updateOperations(0);
            return;
        }

        // 3. Item input (Blank Silicon Wafer)
        if (tracker.shouldContinueChecking()) {
            recipeItem = itemInputHandler.getRecipeInput(recipe.getItemInput());
            if (recipeItem.isEmpty()) {
                tracker.mismatchedRecipe();
                return;
            }
            itemInputHandler.calculateOperationsCanSupport(tracker, recipeItem);
        }

        // 4. Photomask input (checked, durable, never consumed)
        if (tracker.shouldContinueChecking()) {
            recipeMask = maskInputHandler.getRecipeInput(recipe.getMaskInput());
            if (recipeMask.isEmpty()) {
                tracker.mismatchedRecipe();
                return;
            }
        }

        // 5. Chemical input (Nitrogen)
        if (tracker.shouldContinueChecking()) {
            ChemicalStack chemStack = chemicalInputHandler.getInput();
            if (chemStack.isEmpty() || !recipe.getChemicalInput().test(chemStack)) {
                tracker.mismatchedRecipe();
                return;
            }
            recipeChemical = chemStack.copyWithAmount(recipe.getChemicalInput().getNeededAmount(chemStack));
            chemicalInputHandler.calculateOperationsCanSupport(tracker, recipeChemical);
        }

        // 6. Output space check
        if (tracker.shouldContinueChecking()) {
            recipeOutput = recipe.getOutput(recipeItem, recipeMask, recipeChemical);
            outputHandler.calculateOperationsCanSupport(tracker, recipeOutput);
        }
    }

    @Override
    public boolean isInputValid() {
        ItemStack item = itemInputHandler.getInput();
        ItemStack mask = maskInputHandler.getInput();
        ChemicalStack chem = chemicalInputHandler.getInput();
        if (item.isEmpty() || mask.isEmpty() || chem.isEmpty()) {
            return false;
        }
        return recipe.test(item, mask, chem);
    }

    @Override
    protected void resetCache() {
        super.resetCache();
        recipeItem = ItemStack.EMPTY;
        recipeMask = ItemStack.EMPTY;
        recipeChemical = ChemicalStack.EMPTY;
        recipeOutput = ItemStack.EMPTY;
    }

    @Override
    protected void finishProcessing(int operations) {
        if (!recipeOutput.isEmpty() && !recipeItem.isEmpty() && !recipeMask.isEmpty() && !recipeChemical.isEmpty()) {
            // Wafer and nitrogen are consumed
            itemInputHandler.use(recipeItem, operations);
            chemicalInputHandler.use(recipeChemical, operations);
            // Photomask is durable / catalyst, so NOT consumed!
            outputHandler.handleOutput(recipeOutput, operations);
        }
    }
}
