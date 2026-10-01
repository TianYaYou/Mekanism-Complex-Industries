package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.content.freezer.TileEntityFreezerCasing;
import com.complexindustries.mekanism.content.freezer.TileEntityFreezerController;
import com.complexindustries.mekanism.content.freezer.TileEntityFreezerValve;
import com.complexindustries.mekanism.content.tile.TileEntityAirCompressor;
import com.complexindustries.mekanism.content.tile.TileEntityResistiveCooler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;

public final class MCITileEntityTypes {
    public static final TileEntityTypeDeferredRegister TILE_ENTITY_TYPES =
            new TileEntityTypeDeferredRegister(MCIConstants.MODID);

    public static final TileEntityTypeRegistryObject<TileEntityResistiveCooler> RESISTIVE_COOLER =
            TILE_ENTITY_TYPES.mekBuilder(MCIBlocks.RESISTIVE_COOLER, TileEntityResistiveCooler::new)
                    .clientTicker(TileEntityMekanism::tickClient)
                    .serverTicker(TileEntityMekanism::tickServer)
                    .withSimple(Capabilities.CONFIGURABLE)
                    .build();

    public static final TileEntityTypeRegistryObject<TileEntityFreezerCasing> FREEZER_CASING =
            TILE_ENTITY_TYPES.mekBuilder(MCIBlocks.FREEZER_CASING, TileEntityFreezerCasing::new)
                    .clientTicker(TileEntityMekanism::tickClient)
                    .serverTicker(TileEntityMekanism::tickServer)
                    .withSimple(Capabilities.CONFIGURABLE)
                    .build();

    public static final TileEntityTypeRegistryObject<TileEntityFreezerValve> FREEZER_VALVE =
            TILE_ENTITY_TYPES.mekBuilder(MCIBlocks.FREEZER_VALVE, TileEntityFreezerValve::new)
                    .clientTicker(TileEntityMekanism::tickClient)
                    .serverTicker(TileEntityMekanism::tickServer)
                    .withSimple(Capabilities.CONFIGURABLE)
                    .build();

    public static final TileEntityTypeRegistryObject<TileEntityFreezerController> FREEZER_CONTROLLER =
            TILE_ENTITY_TYPES.mekBuilder(MCIBlocks.FREEZER_CONTROLLER, TileEntityFreezerController::new)
                    .clientTicker(TileEntityMekanism::tickClient)
                    .serverTicker(TileEntityMekanism::tickServer)
                    .withSimple(Capabilities.CONFIGURABLE)
                    .build();

    public static final TileEntityTypeRegistryObject<TileEntityAirCompressor> AIR_COMPRESSOR =
            TILE_ENTITY_TYPES.mekBuilder(MCIBlocks.AIR_COMPRESSOR, TileEntityAirCompressor::new)
                    .clientTicker(TileEntityMekanism::tickClient)
                    .serverTicker(TileEntityMekanism::tickServer)
                    .withSimple(Capabilities.CONFIGURABLE)
                    .build();

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.refinery.TileEntityRefineryCasing> REFINERY_CASING =
            TILE_ENTITY_TYPES.mekBuilder(MCIBlocks.REFINERY_CASING, com.complexindustries.mekanism.content.refinery.TileEntityRefineryCasing::new)
                    .clientTicker(TileEntityMekanism::tickClient)
                    .serverTicker(TileEntityMekanism::tickServer)
                    .withSimple(Capabilities.CONFIGURABLE)
                    .build();

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.refinery.TileEntityRefineryValve> REFINERY_VALVE =
            TILE_ENTITY_TYPES.mekBuilder(MCIBlocks.REFINERY_VALVE, com.complexindustries.mekanism.content.refinery.TileEntityRefineryValve::new)
                    .clientTicker(TileEntityMekanism::tickClient)
                    .serverTicker(TileEntityMekanism::tickServer)
                    .withSimple(Capabilities.CONFIGURABLE)
                    .build();

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.refinery.TileEntityRefineryController> REFINERY_CONTROLLER =
            TILE_ENTITY_TYPES.mekBuilder(MCIBlocks.REFINERY_CONTROLLER, com.complexindustries.mekanism.content.refinery.TileEntityRefineryController::new)
                    .clientTicker(TileEntityMekanism::tickClient)
                    .serverTicker(TileEntityMekanism::tickServer)
                    .withSimple(Capabilities.CONFIGURABLE)
                    .build();

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.refinery.TileEntityRefineryDredgePipe> REFINERY_DREDGE_PIPE =
            TILE_ENTITY_TYPES.mekBuilder(MCIBlocks.REFINERY_DREDGE_PIPE, com.complexindustries.mekanism.content.refinery.TileEntityRefineryDredgePipe::new)
                    .clientTicker(TileEntityMekanism::tickClient)
                    .serverTicker(TileEntityMekanism::tickServer)
                    .withSimple(Capabilities.CONFIGURABLE)
                    .build();

    private MCITileEntityTypes() {}
}
