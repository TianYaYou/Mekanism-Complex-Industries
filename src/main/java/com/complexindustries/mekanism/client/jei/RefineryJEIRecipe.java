package com.complexindustries.mekanism.client.jei;

import mekanism.api.chemical.ChemicalStack;
import net.minecraft.network.chat.Component;

public record RefineryJEIRecipe(
        ChemicalStack inputChemical,
        ChemicalStack bitumen,
        ChemicalStack heavyOil,
        ChemicalStack refinedFuel,
        ChemicalStack naphtha,
        ChemicalStack petroleumGas,
        Component title,
        Component heatRequirement,
        Component deltaRequirement,
        Component rateInfo
) {}
