package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.content.extractor.TileEntityCrudeOilExtractor;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;

public final class MCIBlockEntityTypes {
    public static final TileEntityTypeDeferredRegister TILE_ENTITY_TYPES =
            new TileEntityTypeDeferredRegister(MCIConstants.MODID);

    public static final TileEntityTypeRegistryObject<TileEntityCrudeOilExtractor> CRUDE_OIL_EXTRACTOR =
            TILE_ENTITY_TYPES.register(MCIBlocks.CRUDE_OIL_EXTRACTOR, TileEntityCrudeOilExtractor::new,
                    TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);

    private MCIBlockEntityTypes() {}
}
