package com.complexindustries.mekanism.client.jei;

import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.world.item.ItemStack;

public record CrystalGrowthJEIRecipe(
        List<ItemStack> inputItems,
        List<ChemicalStack> inputChemicalsA,
        List<ChemicalStack> inputChemicalsB,
        ItemStack outputItem
) {}
