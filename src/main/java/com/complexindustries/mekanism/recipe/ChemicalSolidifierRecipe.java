package com.complexindustries.mekanism.recipe;

import java.util.List;
import java.util.function.Predicate;
import com.complexindustries.mekanism.registration.MCIBlocks;
import com.complexindustries.mekanism.registration.MCIRecipeTypes;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.vanilla_input.SingleChemicalRecipeInput;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public abstract class ChemicalSolidifierRecipe extends MekanismRecipe<SingleChemicalRecipeInput> implements Predicate<@NotNull ChemicalStack> {

    @Contract(value = "_ -> new", pure = true)
    public abstract ItemStack getOutput(ChemicalStack input);

    public abstract List<ItemStack> getOutputDefinition();

    @NotNull
    @Override
    public ItemStack assemble(SingleChemicalRecipeInput input, HolderLookup.Provider provider) {
        if (!isIncomplete() && test(input.chemical())) {
            return getOutput(input.chemical());
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean matches(SingleChemicalRecipeInput input, Level level) {
        return !isIncomplete() && test(input.chemical());
    }

    @Override
    public abstract boolean test(ChemicalStack stack);

    public abstract boolean testType(ChemicalStack stack);

    public abstract ChemicalStackIngredient getInput();

    public abstract ItemStack getOutputRaw();

    @Override
    public boolean isIncomplete() {
        return getInput().hasNoMatchingInstances();
    }

    @Override
    public void logMissingTags() {
        getInput().logMissingTags();
    }

    @Override
    public final RecipeType<ChemicalSolidifierRecipe> getType() {
        return MCIRecipeTypes.SOLIDIFYING.value();
    }

    @Override
    public String getGroup() {
        return "chemical_solidifier";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(MCIBlocks.CHEMICAL_SOLIDIFIER.get());
    }
}
