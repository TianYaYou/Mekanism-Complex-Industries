package com.complexindustries.mekanism.content.pipe.interfaces;

import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OutputInterfaceFilter {

    public static final int FILTER_SLOTS = 9;

    public record FilterCandidate(FilterType type, String id, String displayName, ItemStack previewStack) {}

    public enum FilterType {
        ITEM("物品"),
        FLUID("流体"),
        CHEMICAL("化学品");

        private final String displayName;

        FilterType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static FilterType byOrdinal(int ord) {
            if (ord >= 0 && ord < values().length) {
                return values()[ord];
            }
            return ITEM;
        }
    }

    public static class FilterEntry {
        private FilterType type;
        private String filterId;
        private ItemStack iconStack;

        public FilterEntry() {
            this(FilterType.ITEM, "", ItemStack.EMPTY);
        }

        public FilterEntry(FilterType type, String filterId, ItemStack iconStack) {
            this.type = type != null ? type : FilterType.ITEM;
            this.filterId = filterId != null ? filterId.trim() : "";
            this.iconStack = iconStack != null ? iconStack.copy() : ItemStack.EMPTY;
        }

        public FilterType getType() {
            return type;
        }

        public void setType(FilterType type) {
            this.type = type != null ? type : FilterType.ITEM;
        }

        public String getFilterId() {
            return filterId;
        }

        public void setFilterId(String filterId) {
            this.filterId = filterId != null ? filterId.trim() : "";
        }

        public ItemStack getIconStack() {
            return iconStack;
        }

        public void setIconStack(ItemStack iconStack) {
            this.iconStack = iconStack != null ? iconStack.copy() : ItemStack.EMPTY;
        }

        public boolean isEmpty() {
            return (filterId == null || filterId.isEmpty()) && (iconStack == null || iconStack.isEmpty());
        }

        public FilterEntry copy() {
            return new FilterEntry(type, filterId, iconStack.copy());
        }
    }

    private final NonNullList<FilterEntry> entries;
    @SuppressWarnings("unchecked")
    private final List<ItemStack>[] cachedMatchingStacks = new List[FILTER_SLOTS];

    // Cached parsed stacks for legacy getters
    private final List<ItemStack> legacyItemFilters = new ArrayList<>();
    private final List<FluidStack> legacyFluidFilters = new ArrayList<>();
    private final List<ChemicalStack> legacyChemicalFilters = new ArrayList<>();

    public OutputInterfaceFilter() {
        this.entries = NonNullList.withSize(FILTER_SLOTS, new FilterEntry());
        for (int i = 0; i < FILTER_SLOTS; i++) {
            this.entries.set(i, new FilterEntry());
            this.cachedMatchingStacks[i] = Collections.emptyList();
        }
    }

    public NonNullList<FilterEntry> getEntries() {
        return entries;
    }

    public FilterEntry getEntry(int index) {
        if (index >= 0 && index < FILTER_SLOTS) {
            return entries.get(index);
        }
        return new FilterEntry();
    }

    public void setFilter(int index, FilterType type, String filterId, ItemStack iconStack) {
        if (index < 0 || index >= FILTER_SLOTS) {
            return;
        }
        entries.set(index, new FilterEntry(type, filterId, iconStack));
        rebuildFilters();
    }

    public void setFilter(int index, ItemStack stack) {
        if (index < 0 || index >= FILTER_SLOTS) {
            return;
        }
        if (stack.isEmpty()) {
            clearFilter(index);
            return;
        }

        // 1. Check if stack contains a chemical
        IChemicalHandler chemHandler = Capabilities.CHEMICAL.getCapability(stack);
        if (chemHandler != null && chemHandler.getChemicalTanks() > 0) {
            ChemicalStack inTank = chemHandler.getChemicalInTank(0);
            if (!inTank.isEmpty()) {
                setFilter(index, FilterType.CHEMICAL, MekanismAPI.CHEMICAL_REGISTRY.getKey(inTank.getChemical()).toString(), stack);
                return;
            }
        }

        // 2. Check if stack contains a fluid
        IFluidHandlerItem fluidHandler = stack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM);
        if (fluidHandler != null && fluidHandler.getTanks() > 0) {
            FluidStack fluid = fluidHandler.getFluidInTank(0);
            if (!fluid.isEmpty()) {
                setFilter(index, FilterType.FLUID, BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString(), stack);
                return;
            }
        }

        // 3. Otherwise treat as an item
        setFilter(index, FilterType.ITEM, BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), stack);
    }

    public void clearFilter(int index) {
        if (index < 0 || index >= FILTER_SLOTS) {
            return;
        }
        entries.set(index, new FilterEntry());
        rebuildFilters();
    }

    public ItemStack getDisplayStack(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= FILTER_SLOTS) {
            return ItemStack.EMPTY;
        }
        FilterEntry entry = entries.get(slotIndex);
        if (entry.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (!entry.getIconStack().isEmpty()) {
            return entry.getIconStack();
        }

        String id = entry.getFilterId().trim();
        if (id.isEmpty() || id.startsWith("#")) {
            return ItemStack.EMPTY;
        }

        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null && !id.contains(":")) {
            rl = ResourceLocation.tryParse("minecraft:" + id);
        }

        if (rl != null) {
            switch (entry.getType()) {
                case ITEM -> {
                    if (BuiltInRegistries.ITEM.containsKey(rl)) {
                        return new ItemStack(BuiltInRegistries.ITEM.get(rl));
                    }
                }
                case FLUID -> {
                    if (BuiltInRegistries.FLUID.containsKey(rl)) {
                        Fluid fluid = BuiltInRegistries.FLUID.get(rl);
                        Item bucket = fluid.getBucket();
                        if (bucket != null) {
                            return bucket.getDefaultInstance();
                        }
                    }
                }
                case CHEMICAL -> {
                    // Chemicals don't inherently have an item, return empty for text badge display
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public ItemStack getStack(int index) {
        return getDisplayStack(index);
    }

    public List<ItemStack> getItemFilters() {
        return legacyItemFilters;
    }

    public List<FluidStack> getFluidFilters() {
        return legacyFluidFilters;
    }

    public List<ChemicalStack> getChemicalFilters() {
        return legacyChemicalFilters;
    }

    public void rebuildFilters() {
        legacyItemFilters.clear();
        legacyFluidFilters.clear();
        legacyChemicalFilters.clear();

        for (int i = 0; i < FILTER_SLOTS; i++) {
            FilterEntry entry = entries.get(i);
            if (entry.isEmpty()) {
                cachedMatchingStacks[i] = Collections.emptyList();
                continue;
            }
            if (entry.getType() == FilterType.ITEM && !entry.getIconStack().isEmpty()) {
                legacyItemFilters.add(entry.getIconStack().copy());
            }
            cachedMatchingStacks[i] = getMatchingItemStacks(entry.getType(), entry.getFilterId(), entry.getIconStack());
        }
    }

    public List<ItemStack> getMatchingItemStacks(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < FILTER_SLOTS && cachedMatchingStacks[slotIndex] != null) {
            return cachedMatchingStacks[slotIndex];
        }
        return Collections.emptyList();
    }

    public ItemStack getDisplayStack(int slotIndex, long ticks) {
        List<ItemStack> list = getMatchingItemStacks(slotIndex);
        if (list.isEmpty()) {
            return getDisplayStack(slotIndex);
        }
        int idx = (int) ((ticks / 20) % list.size());
        return list.get(idx);
    }

    public static List<FilterCandidate> extractCandidates(ItemStack stack) {
        if (stack.isEmpty()) {
            return Collections.emptyList();
        }
        List<FilterCandidate> list = new ArrayList<>();

        // 1. Chemicals
        IChemicalHandler chemHandler = Capabilities.CHEMICAL.getCapability(stack);
        if (chemHandler != null) {
            for (int t = 0; t < chemHandler.getChemicalTanks(); t++) {
                ChemicalStack chem = chemHandler.getChemicalInTank(t);
                if (!chem.isEmpty()) {
                    ResourceLocation chemRl = MekanismAPI.CHEMICAL_REGISTRY.getKey(chem.getChemical());
                    if (chemRl != null) {
                        String name = chem.getTextComponent().getString();
                        list.add(new FilterCandidate(FilterType.CHEMICAL, chemRl.toString(), "化学品: " + name, stack.copy()));
                    }
                }
            }
        }

        // 2. Fluids
        IFluidHandlerItem fluidHandler = stack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM);
        if (fluidHandler != null) {
            for (int t = 0; t < fluidHandler.getTanks(); t++) {
                FluidStack fl = fluidHandler.getFluidInTank(t);
                if (!fl.isEmpty()) {
                    ResourceLocation fluidRl = BuiltInRegistries.FLUID.getKey(fl.getFluid());
                    String name = fl.getHoverName().getString();
                    list.add(new FilterCandidate(FilterType.FLUID, fluidRl.toString(), "流体: " + name, stack.copy()));
                }
            }
        }

        // 3. Item
        ResourceLocation itemRl = BuiltInRegistries.ITEM.getKey(stack.getItem());
        list.add(new FilterCandidate(FilterType.ITEM, itemRl.toString(), "物品: " + stack.getHoverName().getString(), stack.copy()));

        // 4. Tags of item
        stack.getTags().forEach(tagKey -> {
            String tagId = "#" + tagKey.location().toString();
            list.add(new FilterCandidate(FilterType.ITEM, tagId, "标签: " + tagId, stack.copy()));
        });

        return list;
    }

    public static List<ItemStack> getMatchingItemStacks(FilterType type, String filterId, ItemStack iconStack) {
        if (type == FilterType.ITEM) {
            if (filterId != null && filterId.startsWith("#")) {
                String tagStr = filterId.substring(1).trim();
                ResourceLocation tagRl = ResourceLocation.tryParse(tagStr);
                if (tagRl != null) {
                    TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagRl);
                    List<ItemStack> matching = new ArrayList<>();
                    BuiltInRegistries.ITEM.getTag(tagKey).ifPresent(tag -> {
                        for (var holder : tag) {
                            matching.add(new ItemStack(holder.value()));
                        }
                    });
                    if (!matching.isEmpty()) {
                        return matching;
                    }
                }
            } else if (filterId != null && !filterId.isEmpty()) {
                ResourceLocation rl = ResourceLocation.tryParse(filterId);
                if (rl != null && BuiltInRegistries.ITEM.containsKey(rl)) {
                    return List.of(new ItemStack(BuiltInRegistries.ITEM.get(rl)));
                }
            }
            if (iconStack != null && !iconStack.isEmpty()) {
                return List.of(iconStack.copy());
            }
        } else if (type == FilterType.FLUID) {
            if (filterId != null && filterId.startsWith("#")) {
                String tagStr = filterId.substring(1).trim();
                ResourceLocation tagRl = ResourceLocation.tryParse(tagStr);
                if (tagRl != null) {
                    TagKey<Fluid> tagKey = TagKey.create(Registries.FLUID, tagRl);
                    List<ItemStack> matching = new ArrayList<>();
                    BuiltInRegistries.FLUID.getTag(tagKey).ifPresent(tag -> {
                        for (var holder : tag) {
                            Item b = holder.value().getBucket();
                            if (b != null && b != net.minecraft.world.item.Items.AIR) {
                                matching.add(new ItemStack(b));
                            }
                        }
                    });
                    if (!matching.isEmpty()) {
                        return matching;
                    }
                }
            } else if (filterId != null && !filterId.isEmpty()) {
                ResourceLocation rl = ResourceLocation.tryParse(filterId);
                if (rl != null && BuiltInRegistries.FLUID.containsKey(rl)) {
                    Fluid fl = BuiltInRegistries.FLUID.get(rl);
                    Item b = fl.getBucket();
                    if (b != null && b != net.minecraft.world.item.Items.AIR) {
                        return List.of(new ItemStack(b));
                    }
                }
            }
            if (iconStack != null && !iconStack.isEmpty()) {
                return List.of(iconStack.copy());
            }
        } else if (type == FilterType.CHEMICAL) {
            if (iconStack != null && !iconStack.isEmpty()) {
                return List.of(iconStack.copy());
            }
        }
        return Collections.emptyList();
    }

    public boolean matchesItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (FilterEntry entry : entries) {
            if (entry.isEmpty() || entry.getType() != FilterType.ITEM) {
                continue;
            }
            String filterId = entry.getFilterId().trim();
            if (filterId.startsWith("#")) {
                String tagStr = filterId.substring(1).trim();
                ResourceLocation tagRl = ResourceLocation.tryParse(tagStr);
                if (tagRl != null) {
                    TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagRl);
                    if (stack.is(tagKey)) {
                        return true;
                    }
                }
            } else if (!filterId.isEmpty()) {
                ResourceLocation itemRl = BuiltInRegistries.ITEM.getKey(stack.getItem());
                if (itemRl.toString().equalsIgnoreCase(filterId) || itemRl.getPath().equalsIgnoreCase(filterId)) {
                    return true;
                }
            }
            ItemStack icon = entry.getIconStack();
            if (!icon.isEmpty()) {
                if (ItemStack.isSameItemSameComponents(icon, stack) || icon.is(stack.getItem())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean matchesFluid(FluidStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (FilterEntry entry : entries) {
            if (entry.isEmpty() || entry.getType() != FilterType.FLUID) {
                continue;
            }
            String filterId = entry.getFilterId().trim();
            if (filterId.startsWith("#")) {
                String tagStr = filterId.substring(1).trim();
                ResourceLocation tagRl = ResourceLocation.tryParse(tagStr);
                if (tagRl != null) {
                    TagKey<Fluid> tagKey = TagKey.create(Registries.FLUID, tagRl);
                    if (stack.getFluid().is(tagKey)) {
                        return true;
                    }
                }
            } else if (!filterId.isEmpty()) {
                ResourceLocation fluidRl = BuiltInRegistries.FLUID.getKey(stack.getFluid());
                if (fluidRl.toString().equalsIgnoreCase(filterId) || fluidRl.getPath().equalsIgnoreCase(filterId)) {
                    return true;
                }
            }
            ItemStack icon = entry.getIconStack();
            if (!icon.isEmpty()) {
                IFluidHandlerItem fh = icon.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM);
                if (fh != null && fh.getTanks() > 0) {
                    FluidStack contained = fh.getFluidInTank(0);
                    if (!contained.isEmpty() && contained.is(stack.getFluid())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean matchesChemical(ChemicalStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation chemRl = MekanismAPI.CHEMICAL_REGISTRY.getKey(stack.getChemical());
        for (FilterEntry entry : entries) {
            if (entry.isEmpty() || entry.getType() != FilterType.CHEMICAL) {
                continue;
            }
            String filterId = entry.getFilterId().trim();
            if (filterId.startsWith("#")) {
                String tagStr = filterId.substring(1).trim();
                ResourceLocation tagRl = ResourceLocation.tryParse(tagStr);
                if (tagRl != null) {
                    TagKey<Chemical> tagKey = TagKey.create(MekanismAPI.CHEMICAL_REGISTRY_NAME, tagRl);
                    if (stack.is(tagKey)) {
                        return true;
                    }
                }
            } else if (!filterId.isEmpty()) {
                if (chemRl != null && (chemRl.toString().equalsIgnoreCase(filterId) || chemRl.getPath().equalsIgnoreCase(filterId))) {
                    return true;
                }
            }
            ItemStack icon = entry.getIconStack();
            if (!icon.isEmpty()) {
                IChemicalHandler ch = Capabilities.CHEMICAL.getCapability(icon);
                if (ch != null && ch.getChemicalTanks() > 0) {
                    ChemicalStack contained = ch.getChemicalInTank(0);
                    if (!contained.isEmpty() && ChemicalStack.isSameChemical(contained, stack)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (int i = 0; i < FILTER_SLOTS; i++) {
            FilterEntry entry = entries.get(i);
            if (!entry.isEmpty()) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.putByte("Slot", (byte) i);
                entryTag.putByte("Type", (byte) entry.getType().ordinal());
                entryTag.putString("FilterId", entry.getFilterId());
                if (!entry.getIconStack().isEmpty()) {
                    entryTag.put("Icon", entry.getIconStack().save(registries));
                }
                list.add(entryTag);
            }
        }
        tag.put("Filters", list);
        return tag;
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        for (int i = 0; i < FILTER_SLOTS; i++) {
            entries.set(i, new FilterEntry());
        }
        if (tag.contains("Filters", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Filters", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entryTag = list.getCompound(i);
                int slot = entryTag.getByte("Slot") & 255;
                if (slot < FILTER_SLOTS) {
                    FilterType type = FilterType.byOrdinal(entryTag.getByte("Type"));
                    String filterId = entryTag.getString("FilterId");
                    ItemStack icon = ItemStack.EMPTY;
                    if (entryTag.contains("Icon", Tag.TAG_COMPOUND)) {
                        icon = ItemStack.parse(registries, entryTag.getCompound("Icon")).orElse(ItemStack.EMPTY);
                    }
                    if (filterId.isEmpty() && entryTag.contains("Item", Tag.TAG_COMPOUND)) {
                        ItemStack legacy = ItemStack.parse(registries, entryTag.getCompound("Item")).orElse(ItemStack.EMPTY);
                        setFilter(slot, legacy);
                        continue;
                    }
                    entries.set(slot, new FilterEntry(type, filterId, icon));
                }
            }
        }
        rebuildFilters();
    }

    public void writeToBuf(RegistryFriendlyByteBuf buf) {
        for (int i = 0; i < FILTER_SLOTS; i++) {
            FilterEntry entry = entries.get(i);
            buf.writeBoolean(!entry.isEmpty());
            if (!entry.isEmpty()) {
                buf.writeByte(entry.getType().ordinal());
                buf.writeUtf(entry.getFilterId(), 128);
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, entry.getIconStack());
            }
        }
    }

    public void readFromBuf(RegistryFriendlyByteBuf buf) {
        for (int i = 0; i < FILTER_SLOTS; i++) {
            boolean present = buf.readBoolean();
            if (present) {
                FilterType type = FilterType.byOrdinal(buf.readByte() & 255);
                String filterId = buf.readUtf(128);
                ItemStack icon = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
                entries.set(i, new FilterEntry(type, filterId, icon));
            } else {
                entries.set(i, new FilterEntry());
            }
        }
        rebuildFilters();
    }
}
