package com.complexindustries.mekanism.content.energy;

import com.complexindustries.mekanism.content.tile.TileEntityResistiveCooler;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;

import java.util.function.Predicate;

public class ResistiveCoolerEnergyContainer extends MachineEnergyContainer<TileEntityResistiveCooler> {
    public static ResistiveCoolerEnergyContainer input(TileEntityResistiveCooler tile, IContentsListener listener) {
        return new ResistiveCoolerEnergyContainer(
                FloatingLong.createConst(100_000_000),
                FloatingLong.createConst(250),
                notExternal,
                alwaysTrue,
                tile,
                listener
        );
    }

    public ResistiveCoolerEnergyContainer(FloatingLong maxEnergy, FloatingLong energyPerTick,
                                          Predicate<AutomationType> canExtract, Predicate<AutomationType> canInsert,
                                          TileEntityResistiveCooler tile, IContentsListener listener) {
        super(maxEnergy, energyPerTick, canExtract, canInsert, tile, listener);
    }

    @Override
    public boolean adjustableRates() {
        return true;
    }

    @Override
    public void setEnergyPerTick(FloatingLong energyPerTick) {
        super.setEnergyPerTick(energyPerTick);
        setMaxEnergy(energyPerTick.multiply(400L).max(FloatingLong.createConst(100_000)));
    }

    public void updateEnergyUsage(FloatingLong newUsage) {
        setEnergyPerTick(newUsage);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = super.serializeNBT();
        tag.putString("energyUsage", getEnergyPerTick().toString());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        NBTUtils.setFloatingLongIfPresent(nbt, "energyUsage", this::updateEnergyUsage);
    }
}
