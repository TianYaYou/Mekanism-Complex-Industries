package com.complexindustries.mekanism.client.jei;

import mekanism.api.chemical.ChemicalStack;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public record FreezerJEIRecipe(
        @Nullable FluidStack inputFluid,
        @Nullable ChemicalStack inputChemical,
        @Nullable FluidStack outputFluid,
        @Nullable ChemicalStack outputChemical,
        Component processName,
        Component tempCondition
) {}
