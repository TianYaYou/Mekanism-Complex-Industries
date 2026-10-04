package com.complexindustries.mekanism.client.jei;

import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.world.item.ItemStack;

public record ChemicalSoakingJEIRecipe(
        List<ItemStack> inputItems,
        List<ChemicalStack> inputChemicals,
        ItemStack outputItem
) {}
