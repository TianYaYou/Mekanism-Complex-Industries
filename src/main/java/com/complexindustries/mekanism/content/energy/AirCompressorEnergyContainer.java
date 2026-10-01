package com.complexindustries.mekanism.content.energy;

import com.complexindustries.mekanism.content.tile.TileEntityAirCompressor;
import mekanism.api.IContentsListener;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import org.jetbrains.annotations.Nullable;

public class AirCompressorEnergyContainer extends MachineEnergyContainer<TileEntityAirCompressor> {

    public static final long MAX_ENERGY = 100_000L;

    public AirCompressorEnergyContainer(TileEntityAirCompressor tile, @Nullable IContentsListener listener) {
        super(MAX_ENERGY, TileEntityAirCompressor.BASE_ENERGY_PER_TICK, notExternal, ConstantPredicates.alwaysTrue(), tile, listener);
    }
}
