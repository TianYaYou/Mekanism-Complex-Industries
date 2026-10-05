package com.complexindustries.mekanism.content.slicer;

import com.complexindustries.mekanism.registration.MCIContainerTypes;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class ContainerSiliconSlicer extends MekanismTileContainer<TileEntitySiliconSlicer> {

    public ContainerSiliconSlicer(int id, Inventory inv, TileEntitySiliconSlicer tile) {
        super(MCIContainerTypes.SILICON_SLICER, id, inv, tile);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return super.stillValid(player);
    }
}
