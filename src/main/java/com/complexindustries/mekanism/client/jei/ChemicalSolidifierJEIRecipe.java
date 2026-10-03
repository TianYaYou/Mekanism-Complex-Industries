package com.complexindustries.mekanism.client.jei;

import mekanism.api.chemical.ChemicalStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public record ChemicalSolidifierJEIRecipe(
        ChemicalStack inputChemical,
        ItemStack outputItem,
        Component energyUsage,
        Component durationDesc
) {}
