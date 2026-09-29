package com.complexindustries.mekanism.client.jei;

import mekanism.api.chemical.gas.GasStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public record FreezerJEIRecipe(
        @Nullable FluidStack inputFluid,
        @Nullable GasStack inputGas,
        @Nullable FluidStack outputFluid,
        @Nullable GasStack outputGas,
        Component processName,
        Component tempCondition
) {}
