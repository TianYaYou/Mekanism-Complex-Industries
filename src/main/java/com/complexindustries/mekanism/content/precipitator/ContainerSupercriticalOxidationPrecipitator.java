package com.complexindustries.mekanism.content.precipitator;

import com.complexindustries.mekanism.registration.MCIContainerTypes;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.world.entity.player.Inventory;

public class ContainerSupercriticalOxidationPrecipitator extends MekanismTileContainer<TileEntitySupercriticalOxidationPrecipitator> {

    public ContainerSupercriticalOxidationPrecipitator(int id, Inventory inv, TileEntitySupercriticalOxidationPrecipitator tile) {
        super(MCIContainerTypes.SUPERCRITICAL_OXIDATION_PRECIPITATOR, id, inv, tile);
    }

    @Override
    protected int getInventoryYOffset() {
        return super.getInventoryYOffset() + 2;
    }
}
