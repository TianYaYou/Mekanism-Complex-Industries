package com.complexindustries.mekanism.client.jei;

import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.world.item.ItemStack;

public record PhotolithographyJEIRecipe(
        List<ItemStack> inputItems,
        List<ItemStack> maskItems,
        List<ChemicalStack> inputChemicals,
        ItemStack outputItem
) {}
