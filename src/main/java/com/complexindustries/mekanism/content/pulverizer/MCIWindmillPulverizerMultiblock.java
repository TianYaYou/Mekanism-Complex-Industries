package com.complexindustries.mekanism.content.pulverizer;

import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.lib.multiblock.MultiblockManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class MCIWindmillPulverizerMultiblock {

    public static class WindmillPulverizerMultiblockCache extends MultiblockCache<WindmillPulverizerMultiblockData> {

        private final int[] groupProgress = new int[WindmillPulverizerMultiblockData.GROUP_COUNT];
        private final int[] groupMaxProgress = new int[WindmillPulverizerMultiblockData.GROUP_COUNT];
        private final boolean[] groupActive = new boolean[WindmillPulverizerMultiblockData.GROUP_COUNT];
        private final ItemStack[] groupProcessingType = new ItemStack[WindmillPulverizerMultiblockData.GROUP_COUNT];

        public WindmillPulverizerMultiblockCache() {
            for (int i = 0; i < WindmillPulverizerMultiblockData.GROUP_COUNT; i++) {
                groupMaxProgress[i] = WindmillPulverizerMultiblockData.BASE_TICKS;
                groupProcessingType[i] = ItemStack.EMPTY;
            }
        }

        @Override
        public void sync(WindmillPulverizerMultiblockData data) {
            super.sync(data);
            for (int i = 0; i < WindmillPulverizerMultiblockData.GROUP_COUNT; i++) {
                groupProgress[i] = data.getGroupProgress(i);
                groupMaxProgress[i] = data.getGroupMaxProgress(i);
                groupActive[i] = data.isGroupActive(i);
                groupProcessingType[i] = data.groupProcessingType[i] != null ? data.groupProcessingType[i].copy() : ItemStack.EMPTY;
            }
        }

        @Override
        public void apply(HolderLookup.Provider provider, WindmillPulverizerMultiblockData data) {
            super.apply(provider, data);
            for (int i = 0; i < WindmillPulverizerMultiblockData.GROUP_COUNT; i++) {
                data.setGroupProgress(i, groupProgress[i]);
                data.setGroupMaxProgress(i, Math.max(1, groupMaxProgress[i]));
                data.setGroupActive(i, groupActive[i]);
                data.groupProcessingType[i] = groupProcessingType[i] != null ? groupProcessingType[i].copy() : ItemStack.EMPTY;
            }
        }

        @Override
        public void load(HolderLookup.Provider provider, CompoundTag nbtTags) {
            super.load(provider, nbtTags);
            for (int i = 0; i < WindmillPulverizerMultiblockData.GROUP_COUNT; i++) {
                if (nbtTags.contains("prog_" + i)) {
                    groupProgress[i] = nbtTags.getInt("prog_" + i);
                }
                if (nbtTags.contains("maxP_" + i)) {
                    groupMaxProgress[i] = Math.max(1, nbtTags.getInt("maxP_" + i));
                }
                if (nbtTags.contains("act_" + i)) {
                    groupActive[i] = nbtTags.getBoolean("act_" + i);
                }
                if (nbtTags.contains("item_" + i)) {
                    groupProcessingType[i] = ItemStack.parseOptional(provider, nbtTags.getCompound("item_" + i));
                } else {
                    groupProcessingType[i] = ItemStack.EMPTY;
                }
            }
        }

        @Override
        public void save(HolderLookup.Provider provider, CompoundTag nbtTags) {
            super.save(provider, nbtTags);
            for (int i = 0; i < WindmillPulverizerMultiblockData.GROUP_COUNT; i++) {
                nbtTags.putInt("prog_" + i, groupProgress[i]);
                nbtTags.putInt("maxP_" + i, groupMaxProgress[i]);
                nbtTags.putBoolean("act_" + i, groupActive[i]);
                if (groupProcessingType[i] != null && !groupProcessingType[i].isEmpty()) {
                    nbtTags.put("item_" + i, groupProcessingType[i].saveOptional(provider));
                }
            }
        }
    }

    public static final MultiblockManager<WindmillPulverizerMultiblockData> PULVERIZER_MANAGER =
            new MultiblockManager<>("windmillPulverizer", WindmillPulverizerMultiblockCache::new, WindmillPulverizerValidator::new);

    private MCIWindmillPulverizerMultiblock() {}

    public static void init() {
        // Initialized via class loading
    }
}
