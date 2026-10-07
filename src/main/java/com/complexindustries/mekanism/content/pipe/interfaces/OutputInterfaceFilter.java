package com.complexindustries.mekanism.content.pipe.interfaces;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import java.util.ArrayList;
import java.util.List;

public class OutputInterfaceFilter {

    public static final int FILTER_SLOTS = 9;

    private final NonNullList<ItemStack> rawFilterStacks = NonNullList.withSize(FILTER_SLOTS, ItemStack.EMPTY);

    // Cached parsed filters
    private final List<ItemStack> itemFilters = new ArrayList<>();
    private final List<FluidStack> fluidFilters = new ArrayList<>();
    private final List<ChemicalStack> chemicalFilters = new ArrayList<>();

    public OutputInterfaceFilter() {
    }

    public NonNullList<ItemStack> getRawFilterStacks() {
        return rawFilterStacks;
    }

    public ItemStack getStack(int index) {
        if (index >= 0 && index < rawFilterStacks.size()) {
            return rawFilterStacks.get(index);
        }
        return ItemStack.EMPTY;
    }

    public List<ItemStack> getItemFilters() {
        return itemFilters;
    }

    public List<FluidStack> getFluidFilters() {
        return fluidFilters;
    }

    public List<ChemicalStack> getChemicalFilters() {
        return chemicalFilters;
    }

    public void setFilter(int index, ItemStack stack) {
        if (index < 0 || index >= FILTER_SLOTS) {
            return;
        }
        rawFilterStacks.set(index, stack.copy());
        rebuildFilters();
    }

    public void clearFilter(int index) {
        if (index < 0 || index >= FILTER_SLOTS) {
            return;
        }
        rawFilterStacks.set(index, ItemStack.EMPTY);
        rebuildFilters();
    }

    public void rebuildFilters() {
        itemFilters.clear();
        fluidFilters.clear();
        chemicalFilters.clear();

        for (ItemStack stack : rawFilterStacks) {
            if (stack.isEmpty()) {
                continue;
            }

            // Check if stack contains a chemical
            IChemicalHandler chemHandler = Capabilities.CHEMICAL.getCapability(stack);
            if (chemHandler != null && chemHandler.getChemicalTanks() > 0) {
                ChemicalStack inTank = chemHandler.getChemicalInTank(0);
                if (!inTank.isEmpty()) {
                    chemicalFilters.add(inTank.copy());
                    continue;
                }
            }

            // Check if stack contains a fluid
            IFluidHandlerItem fluidHandler = stack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM);
            if (fluidHandler != null && fluidHandler.getTanks() > 0) {
                FluidStack fluid = fluidHandler.getFluidInTank(0);
                if (!fluid.isEmpty()) {
                    fluidFilters.add(fluid.copy());
                    continue;
                }
            }

            // Otherwise treat as an item
            itemFilters.add(stack.copy());
        }
    }

    public boolean matchesItem(ItemStack stack) {
        if (itemFilters.isEmpty() || stack.isEmpty()) {
            return false;
        }
        for (ItemStack filter : itemFilters) {
            if (ItemStack.isSameItemSameComponents(filter, stack) || filter.is(stack.getItem())) {
                return true;
            }
        }
        return false;
    }

    public boolean matchesFluid(FluidStack stack) {
        if (fluidFilters.isEmpty() || stack.isEmpty()) {
            return false;
        }
        for (FluidStack filter : fluidFilters) {
            if (filter.is(stack.getFluid())) {
                return true;
            }
        }
        return false;
    }

    public boolean matchesChemical(ChemicalStack stack) {
        if (chemicalFilters.isEmpty() || stack.isEmpty()) {
            return false;
        }
        for (ChemicalStack filter : chemicalFilters) {
            if (ChemicalStack.isSameChemical(filter, stack)) {
                return true;
            }
        }
        return false;
    }

    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (int i = 0; i < FILTER_SLOTS; i++) {
            ItemStack stack = rawFilterStacks.get(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putByte("Slot", (byte) i);
                itemTag.put("Item", stack.save(registries));
                list.add(itemTag);
            }
        }
        tag.put("Filters", list);
        return tag;
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        for (int i = 0; i < FILTER_SLOTS; i++) {
            rawFilterStacks.set(i, ItemStack.EMPTY);
        }
        if (tag.contains("Filters", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Filters", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag itemTag = list.getCompound(i);
                int slot = itemTag.getByte("Slot") & 255;
                if (slot < FILTER_SLOTS && itemTag.contains("Item", Tag.TAG_COMPOUND)) {
                    ItemStack.parse(registries, itemTag.getCompound("Item"))
                            .ifPresent(stack -> rawFilterStacks.set(slot, stack));
                }
            }
        }
        rebuildFilters();
    }
}
