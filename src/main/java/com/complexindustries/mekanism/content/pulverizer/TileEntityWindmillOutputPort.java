package com.complexindustries.mekanism.content.pulverizer;

import com.complexindustries.mekanism.registration.MCIBlocks;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityWindmillOutputPort extends TileEntityWindmillPulverizerCasing {

    public TileEntityWindmillOutputPort(BlockPos pos, BlockState state) {
        super(MCIBlocks.WINDMILL_OUTPUT_PORT, pos, state);
    }

    @NotNull
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        return side -> side == null
                ? getMultiblock().getInventorySlots(null)
                : (List<IInventorySlot>) (List) getMultiblock().outputSlots;
    }

    @Override
    public boolean persists(ContainerType<?, ?, ?> type) {
        if (type == ContainerType.ITEM) {
            return false;
        }
        return super.persists(type);
    }
}
