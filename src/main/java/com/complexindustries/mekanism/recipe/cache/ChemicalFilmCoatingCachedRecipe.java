package com.complexindustries.mekanism.recipe.cache;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import com.complexindustries.mekanism.recipe.ChemicalFilmCoatingRecipe;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.ILongInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ChemicalFilmCoatingCachedRecipe extends CachedRecipe<ChemicalFilmCoatingRecipe> {

    private final IInputHandler<@NotNull ItemStack> alloyInputHandler;
    private final IInputHandler<@NotNull ItemStack> chipInputHandler;
    private final ILongInputHandler<@NotNull ChemicalStack> chemicalInputHandler;
    private final IOutputHandler<@NotNull ItemStack> outputHandler;
    private final IChemicalTank chemicalTank;

    private ItemStack recipeAlloy = ItemStack.EMPTY;
    private ItemStack recipeChip = ItemStack.EMPTY;
    private ChemicalStack recipeChemical = ChemicalStack.EMPTY;
    private ItemStack recipeOutput = ItemStack.EMPTY;

    public ChemicalFilmCoatingCachedRecipe(ChemicalFilmCoatingRecipe recipe, BooleanSupplier recheckAllRecipeErrors,
                                          IInputHandler<@NotNull ItemStack> alloyInputHandler,
                                          IInputHandler<@NotNull ItemStack> chipInputHandler,
                                          ILongInputHandler<@NotNull ChemicalStack> chemicalInputHandler,
                                          IOutputHandler<@NotNull ItemStack> outputHandler,
                                          IChemicalTank chemicalTank) {
        super(recipe, recheckAllRecipeErrors);
        this.alloyInputHandler = Objects.requireNonNull(alloyInputHandler, "Alloy input handler cannot be null.");
        this.chipInputHandler = Objects.requireNonNull(chipInputHandler, "Chip input handler cannot be null.");
        this.chemicalInputHandler = Objects.requireNonNull(chemicalInputHandler, "Chemical input handler cannot be null.");
        this.outputHandler = Objects.requireNonNull(outputHandler, "Output handler cannot be null.");
        this.chemicalTank = chemicalTank;
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

        // 2. Alloy input check
        if (tracker.shouldContinueChecking()) {
            recipeAlloy = alloyInputHandler.getRecipeInput(recipe.getAlloyInput());
            if (recipeAlloy.isEmpty()) {
                tracker.mismatchedRecipe();
                return;
            }
            alloyInputHandler.calculateOperationsCanSupport(tracker, recipeAlloy);
        }

        // 3. Chip input check
        if (tracker.shouldContinueChecking()) {
            recipeChip = chipInputHandler.getRecipeInput(recipe.getChipInput());
            if (recipeChip.isEmpty()) {
                tracker.mismatchedRecipe();
                return;
            }
            chipInputHandler.calculateOperationsCanSupport(tracker, recipeChip);
        }

        // 4. Chemical input (Nitrogen) check
        if (tracker.shouldContinueChecking()) {
            ChemicalStack chemStack = chemicalInputHandler.getInput();
            if (chemStack.isEmpty() || !recipe.getChemicalInput().test(chemStack)) {
                tracker.mismatchedRecipe();
                return;
            }
            recipeChemical = chemStack.copyWithAmount(recipe.getChemicalInput().getNeededAmount(chemStack));
            chemicalInputHandler.calculateOperationsCanSupport(tracker, recipeChemical);
        }

        // 5. Output space check
        if (tracker.shouldContinueChecking()) {
            recipeOutput = recipe.getOutput(recipeChip);
            outputHandler.calculateOperationsCanSupport(tracker, recipeOutput);
        }
    }

    @Override
    public boolean isInputValid() {
        ItemStack alloy = alloyInputHandler.getInput();
        ItemStack chip = chipInputHandler.getInput();
        ChemicalStack chem = chemicalInputHandler.getInput();
        if (alloy.isEmpty() || chip.isEmpty() || chem.isEmpty()) {
            return false;
        }
        return recipe.test(alloy, chip, chem);
    }

    @Override
    protected void resetCache() {
        super.resetCache();
        recipeAlloy = ItemStack.EMPTY;
        recipeChip = ItemStack.EMPTY;
        recipeChemical = ChemicalStack.EMPTY;
        recipeOutput = ItemStack.EMPTY;
    }

    @Override
    protected void finishProcessing(int operations) {
        if (!recipeOutput.isEmpty() && !recipeAlloy.isEmpty() && !recipeChip.isEmpty() && !recipeChemical.isEmpty()) {
            alloyInputHandler.use(recipeAlloy, operations);
            chipInputHandler.use(recipeChip, operations);
            chemicalInputHandler.use(recipeChemical, operations);
            outputHandler.handleOutput(recipeOutput, operations);
        }
    }
}
