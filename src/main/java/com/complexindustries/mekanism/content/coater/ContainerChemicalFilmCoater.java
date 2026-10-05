package com.complexindustries.mekanism.content.coater;

import com.complexindustries.mekanism.registration.MCIContainerTypes;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.world.entity.player.Inventory;

public class ContainerChemicalFilmCoater extends MekanismTileContainer<TileEntityChemicalFilmCoater> {

    public ContainerChemicalFilmCoater(int id, Inventory inv, TileEntityChemicalFilmCoater tile) {
        super(MCIContainerTypes.CHEMICAL_FILM_COATER, id, inv, tile);
    }

    @Override
    protected int getInventoryYOffset() {
        return super.getInventoryYOffset() + 2;
    }
}
