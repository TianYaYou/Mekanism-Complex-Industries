package com.complexindustries.mekanism.util;

import com.complexindustries.mekanism.content.miner.PetroleumMinerFilter;
import com.complexindustries.mekanism.content.upgrade.ItemPetroleumUpgrade;
import com.complexindustries.mekanism.content.upgrade.MCIUpgrades;
import com.complexindustries.mekanism.registration.MCIBlocks;
import com.complexindustries.mekanism.registration.MCIItems;
import mekanism.common.content.filter.SortableFilterManager;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.miner.MinerItemStackFilter;
import mekanism.common.content.miner.MinerTagFilter;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class MCIUpgradeHelper {

    private MCIUpgradeHelper() {}

    public static boolean isPetroleumUpgrade(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemPetroleumUpgrade;
    }

    public static boolean hasPetroleumUpgrade(TileEntityDigitalMiner miner) {
        if (miner == null) {
            return false;
        }
        TileComponentUpgrade upgradeComponent = miner.getComponent();
        if (upgradeComponent != null) {
            return upgradeComponent.isUpgradeInstalled(MCIUpgrades.PETROLEUM) ||
                   isPetroleumUpgrade(upgradeComponent.getUpgradeSlot().getStack());
        }
        return false;
    }

    public static boolean hasOilOreFilter(TileEntityDigitalMiner miner) {
        if (miner == null) {
            return false;
        }
        SortableFilterManager<MinerFilter<?>> filterManager = miner.getFilterManager();
        if (filterManager == null) {
            return false;
        }
        for (MinerFilter<?> filter : filterManager.getEnabledFilters()) {
            if (filter instanceof MinerItemStackFilter isf) {
                Item item = isf.getItemStack().getItem();
                if (item == MCIItems.SOLID_CRUDE_OIL_ORE.get() ||
                    item == MCIItems.DEEPSLATE_SOLID_CRUDE_OIL_ORE.get() ||
                    item == MCIItems.SOLID_CRUDE_OIL.get()) {
                    return true;
                }
            } else if (filter instanceof MinerTagFilter tf) {
                String tag = tf.getTagName();
                if (tag != null) {
                    if (tag.equals("forge:ores/crude_oil") || tag.equals("forge:ores/oil") ||
                        tag.equals("forge:crude_oil") || tag.equals("forge:oil") ||
                        tag.contains("crude_oil") || tag.contains("ores/oil")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean canHarvestOil(TileEntityDigitalMiner miner) {
        // Automatic harvesting when Petroleum Upgrade is installed - no filters required!
        return hasPetroleumUpgrade(miner);
    }

    public static boolean isCrudeOilSource(BlockState state) {
        return state.getBlock() == MCIBlocks.CRUDE_OIL_BLOCK.get() &&
               state.hasProperty(LiquidBlock.LEVEL) &&
               state.getValue(LiquidBlock.LEVEL) == 0;
    }

    public static PetroleumMinerFilter getPetroleumMinerFilter() {
        return PetroleumMinerFilter.INSTANCE;
    }
}
