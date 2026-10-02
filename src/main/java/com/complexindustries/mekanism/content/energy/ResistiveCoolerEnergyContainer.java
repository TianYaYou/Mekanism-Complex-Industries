package com.complexindustries.mekanism.content.energy;

import com.complexindustries.mekanism.content.tile.TileEntityResistiveCooler;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ResistiveCoolerEnergyContainer extends MachineEnergyContainer<TileEntityResistiveCooler> {
    public static final long USAGE_MULTIPLIER = 400L;
    public static final long MIN_STORAGE = 100_000L;

    public static ResistiveCoolerEnergyContainer input(TileEntityResistiveCooler tile, @Nullable IContentsListener listener) {
        return new ResistiveCoolerEnergyContainer(
                100_000_000L,
                2_500L,
                notExternal,
                ConstantPredicates.alwaysTrue(),
                tile,
                listener
        );
    }

    public ResistiveCoolerEnergyContainer(long maxEnergy, long energyPerTick,
                                          Predicate<@NotNull AutomationType> canExtract, Predicate<@NotNull AutomationType> canInsert,
                                          TileEntityResistiveCooler tile, @Nullable IContentsListener listener) {
        super(maxEnergy, energyPerTick, canExtract, canInsert, tile, listener);
    }

    @Override
    public boolean adjustableRates() {
        return true;
    }

    public void updateEnergyUsage(long newUsage) {
        currentEnergyPerTick = newUsage;
        setMaxEnergy(Math.max(MIN_STORAGE, MathUtils.multiplyClamped(newUsage, USAGE_MULTIPLIER)));
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = super.serializeNBT(provider);
        tag.putLong("energyUsage", getEnergyPerTick());
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        NBTUtils.setLegacyEnergyIfPresent(nbt, "energyUsage", this::updateEnergyUsage);
        super.deserializeNBT(provider, nbt);
    }
}
