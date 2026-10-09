package com.complexindustries.mekanism.content.pipe.interfaces;

import com.complexindustries.mekanism.content.pipe.network.IndustrialPipeNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface IPowerInterface extends IRedstoneControllable {

    @Nullable
    IndustrialPipeNetwork getPipeNetwork();

    @Nullable
    Level getInterfaceLevel();

    BlockPos getInterfacePos();

    @Nullable
    Direction getAttachedFace();

    /**
     * Extracts energy on-demand from adjacent external power sources (generators, cables, energy cells).
     * @param maxAmount Max amount in FE.
     * @param simulate Whether to simulate extraction.
     * @return Actual amount extracted.
     */
    int extractEnergyFromExternal(int maxAmount, boolean simulate);

    /**
     * Receives energy pushed from external power sources, immediately passing it to network consumers.
     * @param amount Amount in FE.
     * @param simulate Whether to simulate.
     * @return Amount accepted.
     */
    int receiveEnergyFromExternal(int amount, boolean simulate);
}
