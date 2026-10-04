package com.complexindustries.mekanism.recipe;

import java.util.Collections;
import java.util.List;
import com.complexindustries.mekanism.recipe.input.ItemBiChemicalRecipeInput;
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
public class CrystalGrowthRecipe extends MekanismRecipe<ItemBiChemicalRecipeInput> implements TriPredicate<ItemStack, ChemicalStack, ChemicalStack> {

    private final ItemStackIngredient itemInput;
    private final ChemicalStackIngredient chemicalInputA;
    private final ChemicalStackIngredient chemicalInputB;
    private final ItemStack output;
    private final int duration;

    public CrystalGrowthRecipe(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInputA, ChemicalStackIngredient chemicalInputB,
                               ItemStack output, int duration) {
        this.itemInput = itemInput;
        this.chemicalInputA = chemicalInputA;
        this.chemicalInputB = chemicalInputB;
        this.output = output.copy();
        this.duration = Math.max(1, duration);
    }

    public ItemStackIngredient getItemInput() {
        return itemInput;
    }

    public ChemicalStackIngredient getChemicalInputA() {
        return chemicalInputA;
    }

    public ChemicalStackIngredient getChemicalInputB() {
        return chemicalInputB;
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

    public boolean testChemicalA(ChemicalStack stack) {
        return chemicalInputA.test(stack);
    }

    public boolean testChemicalB(ChemicalStack stack) {
        return chemicalInputB.test(stack);
    }

    public boolean testChemicalAny(ChemicalStack stack) {
        return chemicalInputA.test(stack) || chemicalInputB.test(stack);
    }

    @Override
    public boolean test(ItemStack item, ChemicalStack chemA, ChemicalStack chemB) {
        if (!itemInput.test(item)) {
            return false;
        }
        return (chemicalInputA.test(chemA) && chemicalInputB.test(chemB))
                || (chemicalInputA.test(chemB) && chemicalInputB.test(chemA));
    }

    public ItemStack getOutput(ItemStack item, ChemicalStack chemA, ChemicalStack chemB) {
        return output.copy();
    }

    public List<ItemStack> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    public boolean matches(ItemBiChemicalRecipeInput input, Level level) {
        return test(input.item(), input.chemicalA(), input.chemicalB());
    }

    @Override
    public ItemStack assemble(ItemBiChemicalRecipeInput input, HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public boolean isIncomplete() {
        return itemInput.hasNoMatchingInstances() || chemicalInputA.hasNoMatchingInstances() || chemicalInputB.hasNoMatchingInstances();
    }

    @Override
    public RecipeType<?> getType() {
        return MCIRecipeTypes.CRYSTAL_GROWTH.get();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MCIRecipeSerializers.CRYSTAL_GROWTH.get();
    }
}
