package com.complexindustries.mekanism.content.miner;

import com.complexindustries.mekanism.registration.MCIBlocks;
import com.complexindustries.mekanism.util.MCIUpgradeHelper;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.miner.MinerFilter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

public class PetroleumMinerFilter extends MinerFilter<PetroleumMinerFilter> {

    public static final PetroleumMinerFilter INSTANCE = new PetroleumMinerFilter();

    public PetroleumMinerFilter() {
        this.replaceTarget = Items.AIR;
        this.requiresReplacement = false;
        this.setEnabled(true);
    }

    @Override
    public boolean canFilter(BlockState state) {
        return state.getBlock() == MCIBlocks.CRUDE_OIL_BLOCK.get() && MCIUpgradeHelper.isCrudeOilSource(state);
    }

    @Override
    public boolean hasBlacklistedElement() {
        return false;
    }

    @Override
    public boolean hasFilter() {
        return true;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.MINER_ITEMSTACK_FILTER;
    }

    @Override
    public PetroleumMinerFilter clone() {
        return new PetroleumMinerFilter();
    }

    @Override
    public CompoundTag write(CompoundTag nbt) {
        super.write(nbt);
        return nbt;
    }

    @Override
    public void read(CompoundTag nbt) {
        super.read(nbt);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        super.write(buffer);
    }

    @Override
    public void read(FriendlyByteBuf buffer) {
        super.read(buffer);
    }
}
