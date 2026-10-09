package com.complexindustries.mekanism.content.pipe.interfaces;

import com.complexindustries.mekanism.content.pipe.network.IndustrialPipeNetwork;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public interface IInputInterface extends IRedstoneControllable {

    int getPriority();

    void setPriority(int priority);

    @Nullable
    IndustrialPipeNetwork getPipeNetwork();

    @Nullable
    Level getInterfaceLevel();

    BlockPos getInterfacePos();

    @Nullable
    Direction getAttachedFace();

    /**
     * Pushes an item into the network.
     * Returns the remainder stack that could not be accepted.
     */
    default ItemStack routeItem(ItemStack stack, boolean simulate) {
        IndustrialPipeNetwork network = getPipeNetwork();
        if (network == null || stack.isEmpty()) {
            return stack;
        }
        return network.routeItem(this, stack, simulate);
    }

    /**
     * Pushes fluid into the network.
     * Returns the remainder fluid stack that could not be accepted.
     */
    default FluidStack routeFluid(FluidStack stack, boolean simulate) {
        IndustrialPipeNetwork network = getPipeNetwork();
        if (network == null || stack.isEmpty()) {
            return stack;
        }
        return network.routeFluid(this, stack, simulate);
    }

    /**
     * Pushes chemical into the network.
     * Returns the remainder chemical stack that could not be accepted.
     */
    default ChemicalStack routeChemical(ChemicalStack stack, boolean simulate) {
        IndustrialPipeNetwork network = getPipeNetwork();
        if (network == null || stack.isEmpty()) {
            return stack;
        }
        return network.routeChemical(this, stack, simulate);
    }
}
