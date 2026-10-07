package com.complexindustries.mekanism.content.pipe.attachment;

import com.complexindustries.mekanism.content.pipe.network.IndustrialPipeNetwork;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public interface IPipeAttachment {

    AttachmentType getType();

    Direction getFace();

    void onAttachedToNetwork(IndustrialPipeNetwork network);

    void onDetachedFromNetwork(IndustrialPipeNetwork network);

    CompoundTag save(HolderLookup.Provider registries);

    void load(CompoundTag tag, HolderLookup.Provider registries);

    ItemStack getDropItem();

    InteractionResult openMenu(Player player);

    @Nullable
    default IItemHandler getItemHandler() {
        return null;
    }

    @Nullable
    default IFluidHandler getFluidHandler() {
        return null;
    }

    @Nullable
    default IChemicalHandler getChemicalHandler() {
        return null;
    }

    @Nullable
    default IEnergyStorage getEnergyHandler() {
        return null;
    }

    @Nullable
    default IStrictEnergyHandler getStrictEnergyHandler() {
        return null;
    }
}
