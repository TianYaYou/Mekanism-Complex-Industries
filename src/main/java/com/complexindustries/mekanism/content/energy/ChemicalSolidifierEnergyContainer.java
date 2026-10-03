package com.complexindustries.mekanism.content.energy;

import com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifier;
import mekanism.api.IContentsListener;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import org.jetbrains.annotations.Nullable;

public class ChemicalSolidifierEnergyContainer extends MachineEnergyContainer<TileEntityChemicalSolidifier> {

    public static final long MAX_ENERGY = 100_000L;
    public static final long BASE_ENERGY_PER_TICK = 200L; // 50 FE/t

    public ChemicalSolidifierEnergyContainer(TileEntityChemicalSolidifier tile, @Nullable IContentsListener listener) {
        super(MAX_ENERGY, BASE_ENERGY_PER_TICK, notExternal, ConstantPredicates.alwaysTrue(), tile, listener);
    }
}
