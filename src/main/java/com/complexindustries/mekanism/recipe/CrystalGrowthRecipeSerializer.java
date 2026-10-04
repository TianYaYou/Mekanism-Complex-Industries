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

public class CrystalGrowthRecipeSerializer {

    public static RecipeSerializer<CrystalGrowthRecipe> create() {
        MapCodec<CrystalGrowthRecipe> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                IngredientCreatorAccess.item().codec().fieldOf(SerializationConstants.ITEM_INPUT).forGetter(CrystalGrowthRecipe::getItemInput),
                IngredientCreatorAccess.chemicalStack().codec().fieldOf("chemical_input_a").forGetter(CrystalGrowthRecipe::getChemicalInputA),
                IngredientCreatorAccess.chemicalStack().codec().fieldOf("chemical_input_b").forGetter(CrystalGrowthRecipe::getChemicalInputB),
                ItemStack.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(CrystalGrowthRecipe::getOutputRaw),
                Codec.INT.optionalFieldOf(SerializationConstants.DURATION, 200).forGetter(CrystalGrowthRecipe::getDuration)
        ).apply(instance, CrystalGrowthRecipe::new));

        StreamCodec<RegistryFriendlyByteBuf, CrystalGrowthRecipe> streamCodec = StreamCodec.composite(
                IngredientCreatorAccess.item().streamCodec(), CrystalGrowthRecipe::getItemInput,
                IngredientCreatorAccess.chemicalStack().streamCodec(), CrystalGrowthRecipe::getChemicalInputA,
                IngredientCreatorAccess.chemicalStack().streamCodec(), CrystalGrowthRecipe::getChemicalInputB,
                ItemStack.STREAM_CODEC, CrystalGrowthRecipe::getOutputRaw,
                ByteBufCodecs.VAR_INT, CrystalGrowthRecipe::getDuration,
                CrystalGrowthRecipe::new
        );

        return new MekanismRecipeSerializer<>(codec, streamCodec);
    }
}
