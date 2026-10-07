package com.complexindustries.mekanism.content.pipe.interfaces;

import com.complexindustries.mekanism.content.pipe.network.IndustrialPipeNetwork;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IOutputInterface {

    int getPriority();

    void setPriority(int priority);

    @Nullable
    IndustrialPipeNetwork getPipeNetwork();

    @Nullable
    Level getInterfaceLevel();

    BlockPos getInterfacePos();

    @Nullable
    Direction getAttachedFace();

    // --- Filters ---
    List<ItemStack> getItemFilters();

    List<FluidStack> getFluidFilters();

    List<ChemicalStack> getChemicalFilters();

    void setFilter(int index, ItemStack rawStack);

    void clearFilter(int index);

    /**
     * Strict whitelist: If empty filter list, returns false.
     */
    boolean matchesItem(ItemStack stack);

    boolean matchesFluid(FluidStack stack);

    boolean matchesChemical(ChemicalStack stack);

    // --- Pushes to targets ---
    ItemStack insertItemIntoTarget(ItemStack stack, boolean simulate);

    FluidStack insertFluidIntoTarget(FluidStack stack, boolean simulate);

    ChemicalStack insertChemicalIntoTarget(ChemicalStack stack, boolean simulate);
}
