package com.complexindustries.mekanism.recipe;

import java.util.Collections;
import java.util.List;
import com.complexindustries.mekanism.recipe.input.PhotolithographyRecipeInput;
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
public class PhotolithographyRecipe extends MekanismRecipe<PhotolithographyRecipeInput> implements TriPredicate<ItemStack, ItemStack, ChemicalStack> {

    private final ItemStackIngredient itemInput;
    private final ItemStackIngredient maskInput;
    private final ChemicalStackIngredient chemicalInput;
    private final ItemStack output;
    private final int duration;

    public PhotolithographyRecipe(ItemStackIngredient itemInput, ItemStackIngredient maskInput, ChemicalStackIngredient chemicalInput,
                                  ItemStack output, int duration) {
        this.itemInput = itemInput;
        this.maskInput = maskInput;
        this.chemicalInput = chemicalInput;
        this.output = output.copy();
        this.duration = Math.max(1, duration);
    }

    public ItemStackIngredient getItemInput() {
        return itemInput;
    }

    public ItemStackIngredient getMaskInput() {
        return maskInput;
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

    public boolean testItem(ItemStack stack) {
        return itemInput.test(stack);
    }

    public boolean testMask(ItemStack stack) {
        return maskInput.test(stack);
    }

    public boolean testChemical(ChemicalStack stack) {
        return chemicalInput.test(stack);
    }

    public boolean test(ItemStack item, ItemStack mask, ChemicalStack chemical) {
        return itemInput.test(item) && maskInput.test(mask) && chemicalInput.test(chemical);
    }

    public ItemStack getOutput(ItemStack item, ItemStack mask, ChemicalStack chemical) {
        return output.copy();
    }

    public List<ItemStack> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    public boolean matches(PhotolithographyRecipeInput input, Level level) {
        return test(input.item(), input.mask(), input.chemical());
    }

    @Override
    public ItemStack assemble(PhotolithographyRecipeInput input, HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public boolean isIncomplete() {
        return itemInput.hasNoMatchingInstances() || maskInput.hasNoMatchingInstances() || chemicalInput.hasNoMatchingInstances();
    }

    @Override
    public RecipeType<?> getType() {
        return MCIRecipeTypes.PHOTOLITHOGRAPHY.get();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MCIRecipeSerializers.PHOTOLITHOGRAPHY.get();
    }
}
