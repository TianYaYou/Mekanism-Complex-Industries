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

public class PhotolithographyRecipeSerializer {

    public static RecipeSerializer<PhotolithographyRecipe> create() {
        MapCodec<PhotolithographyRecipe> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                IngredientCreatorAccess.item().codec().fieldOf(SerializationConstants.ITEM_INPUT).forGetter(PhotolithographyRecipe::getItemInput),
                IngredientCreatorAccess.item().codec().fieldOf("mask").forGetter(PhotolithographyRecipe::getMaskInput),
                IngredientCreatorAccess.chemicalStack().codec().fieldOf(SerializationConstants.CHEMICAL_INPUT).forGetter(PhotolithographyRecipe::getChemicalInput),
                ItemStack.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(PhotolithographyRecipe::getOutputRaw),
                Codec.INT.optionalFieldOf(SerializationConstants.DURATION, 1100).forGetter(PhotolithographyRecipe::getDuration)
        ).apply(instance, PhotolithographyRecipe::new));

        StreamCodec<RegistryFriendlyByteBuf, PhotolithographyRecipe> streamCodec = StreamCodec.composite(
                IngredientCreatorAccess.item().streamCodec(), PhotolithographyRecipe::getItemInput,
                IngredientCreatorAccess.item().streamCodec(), PhotolithographyRecipe::getMaskInput,
                IngredientCreatorAccess.chemicalStack().streamCodec(), PhotolithographyRecipe::getChemicalInput,
                ItemStack.STREAM_CODEC, PhotolithographyRecipe::getOutputRaw,
                ByteBufCodecs.VAR_INT, PhotolithographyRecipe::getDuration,
                PhotolithographyRecipe::new
        );

        return new MekanismRecipeSerializer<>(codec, streamCodec);
    }
}
