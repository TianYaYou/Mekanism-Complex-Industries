package com.complexindustries.mekanism.content.energy;

import com.complexindustries.mekanism.content.tile.TileEntityAirCompressor;
import mekanism.api.IContentsListener;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.energy.MachineEnergyContainer;

public class AirCompressorEnergyContainer extends MachineEnergyContainer<TileEntityAirCompressor> {

    public static final FloatingLong MAX_ENERGY = FloatingLong.createConst(100_000);

    public AirCompressorEnergyContainer(TileEntityAirCompressor tile, IContentsListener listener) {
        super(MAX_ENERGY, TileEntityAirCompressor.BASE_ENERGY_PER_TICK, notExternal, alwaysTrue, tile, listener);
    }
}
