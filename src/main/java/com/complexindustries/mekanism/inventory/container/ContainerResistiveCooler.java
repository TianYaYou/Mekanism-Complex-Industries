package com.complexindustries.mekanism.inventory.container;

import com.complexindustries.mekanism.content.tile.TileEntityResistiveCooler;
import com.complexindustries.mekanism.registration.MCIContainerTypes;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.util.WorldUtils;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class ContainerResistiveCooler extends MekanismTileContainer<TileEntityResistiveCooler> {

    public ContainerResistiveCooler(int id, Inventory inv, TileEntityResistiveCooler tile) {
        super(MCIContainerTypes.RESISTIVE_COOLER, id, inv, tile);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return !tile.isRemoved() && WorldUtils.isBlockLoaded(tile.getLevel(), tile.getBlockPos())
                && player.distanceToSqr(tile.getBlockPos().getX() + 0.5, tile.getBlockPos().getY() + 0.5, tile.getBlockPos().getZ() + 0.5) <= 64.0;
    }
}
