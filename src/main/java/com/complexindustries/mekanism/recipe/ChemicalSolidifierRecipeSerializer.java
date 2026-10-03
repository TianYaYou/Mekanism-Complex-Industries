package com.complexindustries.mekanism.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.SerializationConstants;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.recipe.serializer.MekanismRecipeSerializer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ChemicalSolidifierRecipeSerializer {

    public static RecipeSerializer<BasicChemicalSolidifierRecipe> create() {
        MapCodec<BasicChemicalSolidifierRecipe> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                IngredientCreatorAccess.chemicalStack().codec().fieldOf(SerializationConstants.INPUT).forGetter(BasicChemicalSolidifierRecipe::getInput),
                ItemStack.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(BasicChemicalSolidifierRecipe::getOutputRaw)
        ).apply(instance, BasicChemicalSolidifierRecipe::new));

        StreamCodec<RegistryFriendlyByteBuf, BasicChemicalSolidifierRecipe> streamCodec = StreamCodec.composite(
                IngredientCreatorAccess.chemicalStack().streamCodec(), BasicChemicalSolidifierRecipe::getInput,
                ItemStack.STREAM_CODEC, BasicChemicalSolidifierRecipe::getOutputRaw,
                BasicChemicalSolidifierRecipe::new
        );

        return new MekanismRecipeSerializer<>(codec, streamCodec);
    }
}
