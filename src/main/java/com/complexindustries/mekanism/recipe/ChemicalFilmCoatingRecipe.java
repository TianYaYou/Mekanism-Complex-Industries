package com.complexindustries.mekanism.recipe;

import java.util.Collections;
import java.util.List;
import com.complexindustries.mekanism.content.item.SemiFinishedChipItem;
import com.complexindustries.mekanism.recipe.input.ChemicalFilmCoatingRecipeInput;
import com.complexindustries.mekanism.registration.MCIRecipeSerializers;
import com.complexindustries.mekanism.registration.MCIRecipeTypes;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.TriPredicate;

@NothingNullByDefault
public class ChemicalFilmCoatingRecipe extends MekanismRecipe<ChemicalFilmCoatingRecipeInput> implements TriPredicate<ItemStack, ItemStack, ChemicalStack> {

    private final ItemStackIngredient alloyInput;
    private final ItemStackIngredient chipInput;
    private final ChemicalStackIngredient chemicalInput;
    private final ItemStack output;
    private final int duration;

    public ChemicalFilmCoatingRecipe(ItemStackIngredient alloyInput, ItemStackIngredient chipInput, ChemicalStackIngredient chemicalInput,
                                     ItemStack output, int duration) {
        this.alloyInput = alloyInput;
        this.chipInput = chipInput;
        this.chemicalInput = chemicalInput;
        this.output = output.copy();
        this.duration = Math.max(1, duration);
    }

    public ItemStackIngredient getAlloyInput() {
        return alloyInput;
    }

    public ItemStackIngredient getChipInput() {
        return chipInput;
    }

    public ChemicalStackIngredient getChemicalInput() {
        return chemicalInput;
    }

    public ItemStack getOutputRaw() {
        return output.copy();
    }

    public int getDuration() {
        return duration;
    }

    public boolean testAlloy(ItemStack stack) {
        return alloyInput.test(stack);
    }

    public boolean testChip(ItemStack stack) {
        return chipInput.test(stack);
    }

    @Override
    public boolean test(ItemStack alloy, ItemStack chip, ChemicalStack chemical) {
        return alloyInput.test(alloy) && chipInput.test(chip) && !chemical.isEmpty() && chemicalInput.test(chemical);
    }

    public ItemStack getOutput(ItemStack inputChip) {
        if (inputChip.getItem() instanceof SemiFinishedChipItem) {
            int progress = SemiFinishedChipItem.getCoatingProgress(inputChip);
            if (progress < 4) {
                return SemiFinishedChipItem.withProgress(inputChip, progress + 1);
            }
        }
        return output.copy();
    }

    @Override
    public boolean matches(ChemicalFilmCoatingRecipeInput input, Level level) {
        return test(input.alloy(), input.chip(), input.chemical());
    }

    @Override
    public ItemStack assemble(ChemicalFilmCoatingRecipeInput input, HolderLookup.Provider registries) {
        return getOutput(input.chip());
    }

    @Override
    public boolean isIncomplete() {
        return alloyInput.hasNoMatchingInstances() || chipInput.hasNoMatchingInstances() || chemicalInput.hasNoMatchingInstances();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MCIRecipeSerializers.CHEMICAL_FILM_COATING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return MCIRecipeTypes.CHEMICAL_FILM_COATING.get();
    }

    public List<ItemStack> getAlloyRepresentations() {
        return alloyInput.getRepresentations();
    }

    public List<ItemStack> getChipRepresentations() {
        return chipInput.getRepresentations();
    }

    public List<ChemicalStack> getChemicalRepresentations() {
        return chemicalInput.getRepresentations();
    }
}
