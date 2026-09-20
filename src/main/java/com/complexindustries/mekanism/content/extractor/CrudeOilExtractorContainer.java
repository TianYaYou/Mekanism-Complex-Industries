package com.complexindustries.mekanism.content.extractor;

import com.complexindustries.mekanism.registration.MCIMenuTypes;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.world.entity.player.Inventory;

public class CrudeOilExtractorContainer extends MekanismTileContainer<TileEntityCrudeOilExtractor> {

    public CrudeOilExtractorContainer(int id, Inventory inv, TileEntityCrudeOilExtractor tile) {
        super(MCIMenuTypes.CRUDE_OIL_EXTRACTOR, id, inv, tile);
    }

    @Override
    protected int getInventoryYOffset() {
        return 84;
    }
}
