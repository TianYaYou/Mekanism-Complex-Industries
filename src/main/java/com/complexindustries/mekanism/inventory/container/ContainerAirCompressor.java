package com.complexindustries.mekanism.inventory.container;

import com.complexindustries.mekanism.content.tile.TileEntityAirCompressor;
import com.complexindustries.mekanism.registration.MCIContainerTypes;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.util.WorldUtils;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class ContainerAirCompressor extends MekanismTileContainer<TileEntityAirCompressor> {

    public ContainerAirCompressor(int id, Inventory inv, TileEntityAirCompressor tile) {
        super(MCIContainerTypes.AIR_COMPRESSOR, id, inv, tile);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return !tile.isRemoved() && WorldUtils.isBlockLoaded(tile.getLevel(), tile.getBlockPos())
                && player.distanceToSqr(tile.getBlockPos().getX() + 0.5, tile.getBlockPos().getY() + 0.5, tile.getBlockPos().getZ() + 0.5) <= 64.0;
    }
}
