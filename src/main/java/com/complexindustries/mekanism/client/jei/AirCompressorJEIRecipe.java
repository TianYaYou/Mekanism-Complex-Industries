package com.complexindustries.mekanism.client.jei;

import mekanism.api.chemical.gas.GasStack;
import net.minecraft.network.chat.Component;

public record AirCompressorJEIRecipe(
        GasStack outputGas,
        Component energyUsage,
        Component productionRate,
        Component sourceDesc
) {}
