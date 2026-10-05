package com.complexindustries.mekanism.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.SerializationConstants;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.recipe.serializer.MekanismRecipeSerializer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ChemicalFilmCoatingRecipeSerializer {

    public static RecipeSerializer<ChemicalFilmCoatingRecipe> create() {
        MapCodec<ChemicalFilmCoatingRecipe> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                IngredientCreatorAccess.item().codec().fieldOf("alloy_input").forGetter(ChemicalFilmCoatingRecipe::getAlloyInput),
                IngredientCreatorAccess.item().codec().fieldOf("chip_input").forGetter(ChemicalFilmCoatingRecipe::getChipInput),
                IngredientCreatorAccess.chemicalStack().codec().fieldOf(SerializationConstants.CHEMICAL_INPUT).forGetter(ChemicalFilmCoatingRecipe::getChemicalInput),
                ItemStack.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(ChemicalFilmCoatingRecipe::getOutputRaw),
                Codec.INT.optionalFieldOf(SerializationConstants.DURATION, 100).forGetter(ChemicalFilmCoatingRecipe::getDuration)
        ).apply(instance, ChemicalFilmCoatingRecipe::new));

        StreamCodec<RegistryFriendlyByteBuf, ChemicalFilmCoatingRecipe> streamCodec = StreamCodec.composite(
                IngredientCreatorAccess.item().streamCodec(), ChemicalFilmCoatingRecipe::getAlloyInput,
                IngredientCreatorAccess.item().streamCodec(), ChemicalFilmCoatingRecipe::getChipInput,
                IngredientCreatorAccess.chemicalStack().streamCodec(), ChemicalFilmCoatingRecipe::getChemicalInput,
                ItemStack.STREAM_CODEC, ChemicalFilmCoatingRecipe::getOutputRaw,
                ByteBufCodecs.VAR_INT, ChemicalFilmCoatingRecipe::getDuration,
                ChemicalFilmCoatingRecipe::new
        );

        return new MekanismRecipeSerializer<>(codec, streamCodec);
    }
}
