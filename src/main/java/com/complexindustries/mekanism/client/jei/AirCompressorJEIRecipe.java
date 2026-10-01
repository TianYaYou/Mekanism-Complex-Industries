package com.complexindustries.mekanism.client.jei;

import mekanism.api.chemical.ChemicalStack;
import net.minecraft.network.chat.Component;

public record AirCompressorJEIRecipe(
        ChemicalStack outputChemical,
        Component energyUsage,
        Component productionRate,
        Component sourceDesc
) {}
