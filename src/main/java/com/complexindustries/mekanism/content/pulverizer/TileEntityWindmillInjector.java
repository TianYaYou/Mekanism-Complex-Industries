package com.complexindustries.mekanism.content.pulverizer;

import com.complexindustries.mekanism.registration.MCIBlocks;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityWindmillInjector extends TileEntityWindmillPulverizerCasing {

    public TileEntityWindmillInjector(BlockPos pos, BlockState state) {
        super(MCIBlocks.WINDMILL_INJECTOR, pos, state);
    }

    @NotNull
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        return side -> side == null
                ? getMultiblock().getInventorySlots(null)
                : (List<IInventorySlot>) (List) getMultiblock().inputSlots;
    }

    @NotNull
    @Override
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener) {
        return side -> getMultiblock().getChemicalTanks(side);
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        return side -> getMultiblock().getEnergyContainers(side);
    }

    @Override
    public boolean persists(ContainerType<?, ?, ?> type) {
        if (type == ContainerType.ITEM || type == ContainerType.CHEMICAL || type == ContainerType.ENERGY) {
            return false;
        }
        return super.persists(type);
    }
}
