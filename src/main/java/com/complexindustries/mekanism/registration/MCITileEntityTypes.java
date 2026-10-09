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

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.flowregulator.TileEntityFlowRegulator> FLOW_REGULATOR =
            TILE_ENTITY_TYPES.mekBuilder(MCIBlocks.FLOW_REGULATOR, com.complexindustries.mekanism.content.flowregulator.TileEntityFlowRegulator::new)
                    .clientTicker(TileEntityMekanism::tickClient)
                    .serverTicker(TileEntityMekanism::tickServer)
                    .withSimple(Capabilities.CONFIGURABLE)
                    .build();

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifier> CHEMICAL_SOLIDIFIER =
            TILE_ENTITY_TYPES.mekBuilder(MCIBlocks.CHEMICAL_SOLIDIFIER, com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifier::new)
                    .clientTicker(TileEntityMekanism::tickClient)
                    .serverTicker(TileEntityMekanism::tickServer)
                    .withSimple(Capabilities.CONFIGURABLE)
                    .build();

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifierFactory> BASIC_CHEMICAL_SOLIDIFIER_FACTORY =
            TILE_ENTITY_TYPES.mekBuilder(MCIBlocks.BASIC_CHEMICAL_SOLIDIFIER_FACTORY, com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifierFactory::new)
                    .clientTicker(TileEntityMekanism::tickClient)
                    .serverTicker(TileEntityMekanism::tickServer)
                    .withSimple(Capabilities.CONFIGURABLE)
                    .build();

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifierFactory> ADVANCED_CHEMICAL_SOLIDIFIER_FACTORY =
            TILE_ENTITY_TYPES.mekBuilder(MCIBlocks.ADVANCED_CHEMICAL_SOLIDIFIER_FACTORY, com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifierFactory::new)
                    .clientTicker(TileEntityMekanism::tickClient)
                    .serverTicker(TileEntityMekanism::tickServer)
                    .withSimple(Capabilities.CONFIGURABLE)
                    .build();

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifierFactory> ELITE_CHEMICAL_SOLIDIFIER_FACTORY =
            TILE_ENTITY_TYPES.mekBuilder(MCIBlocks.ELITE_CHEMICAL_SOLIDIFIER_FACTORY, com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifierFactory::new)
                    .clientTicker(TileEntityMekanism::tickClient)
                    .serverTicker(TileEntityMekanism::tickServer)
                    .withSimple(Capabilities.CONFIGURABLE)
                    .build();

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifierFactory> ULTIMATE_CHEMICAL_SOLIDIFIER_FACTORY =
            TILE_ENTITY_TYPES.mekBuilder(MCIBlocks.ULTIMATE_CHEMICAL_SOLIDIFIER_FACTORY, com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifierFactory::new)
                    .clientTicker(TileEntityMekanism::tickClient)
                    .serverTicker(TileEntityMekanism::tickServer)
                    .withSimple(Capabilities.CONFIGURABLE)
                    .build();

    public static TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifierFactory> getFactoryTile(mekanism.common.tier.FactoryTier tier) {
        return switch (tier) {
            case BASIC -> BASIC_CHEMICAL_SOLIDIFIER_FACTORY;
            case ADVANCED -> ADVANCED_CHEMICAL_SOLIDIFIER_FACTORY;
            case ELITE -> ELITE_CHEMICAL_SOLIDIFIER_FACTORY;
            case ULTIMATE -> ULTIMATE_CHEMICAL_SOLIDIFIER_FACTORY;
        };
    }

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.soaker.TileEntityChemicalSoaker> CHEMICAL_SOAKER =
            TILE_ENTITY_TYPES.mekBuilder(MCIBlocks.CHEMICAL_SOAKER, com.complexindustries.mekanism.content.soaker.TileEntityChemicalSoaker::new)
                    .clientTicker(TileEntityMekanism::tickClient)
                    .serverTicker(TileEntityMekanism::tickServer)
                    .withSimple(Capabilities.CONFIGURABLE)
                    .build();

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.soaker.TileEntityChemicalSoakingFactory> BASIC_CHEMICAL_SOAKING_FACTORY =
            TILE_ENTITY_TYPES.mekBuilder(MCIBlocks.BASIC_CHEMICAL_SOAKING_FACTORY, com.complexindustries.mekanism.content.soaker.TileEntityChemicalSoakingFactory::new)
                    .clientTicker(TileEntityMekanism::tickClient)
                    .serverTicker(TileEntityMekanism::tickServer)
                    .withSimple(Capabilities.CONFIGURABLE)
                    .build();

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.soaker.TileEntityChemicalSoakingFactory> ADVANCED_CHEMICAL_SOAKING_FACTORY =
            TILE_ENTITY_TYPES.mekBuilder(MCIBlocks.ADVANCED_CHEMICAL_SOAKING_FACTORY, com.complexindustries.mekanism.content.soaker.TileEntityChemicalSoakingFactory::new)
                    .clientTicker(TileEntityMekanism::tickClient)
                    .serverTicker(TileEntityMekanism::tickServer)
                    .withSimple(Capabilities.CONFIGURABLE)
                    .build();

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.soaker.TileEntityChemicalSoakingFactory> ELITE_CHEMICAL_SOAKING_FACTORY =
            TILE_ENTITY_TYPES.mekBuilder(MCIBlocks.ELITE_CHEMICAL_SOAKING_FACTORY, com.complexindustries.mekanism.content.soaker.TileEntityChemicalSoakingFactory::new)
                    .clientTicker(TileEntityMekanism::tickClient)
                    .serverTicker(TileEntityMekanism::tickServer)
                    .withSimple(Capabilities.CONFIGURABLE)
                    .build();

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.soaker.TileEntityChemicalSoakingFactory> ULTIMATE_CHEMICAL_SOAKING_FACTORY =
            TILE_ENTITY_TYPES.mekBuilder(MCIBlocks.ULTIMATE_CHEMICAL_SOAKING_FACTORY, com.complexindustries.mekanism.content.soaker.TileEntityChemicalSoakingFactory::new)
                    .clientTicker(TileEntityMekanism::tickClient)
                    .serverTicker(TileEntityMekanism::tickServer)
                    .withSimple(Capabilities.CONFIGURABLE)
                    .build();

    public static TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.soaker.TileEntityChemicalSoakingFactory> getSoakingFactoryTile(mekanism.common.tier.FactoryTier tier) {
        return switch (tier) {
            case BASIC -> BASIC_CHEMICAL_SOAKING_FACTORY;
            case ADVANCED -> ADVANCED_CHEMICAL_SOAKING_FACTORY;
            case ELITE -> ELITE_CHEMICAL_SOAKING_FACTORY;
            case ULTIMATE -> ULTIMATE_CHEMICAL_SOAKING_FACTORY;
        };
    }

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.chamber.TileEntityCrystalGrowthChamber> CRYSTAL_GROWTH_CHAMBER =
            TILE_ENTITY_TYPES.mekBuilder(MCIBlocks.CRYSTAL_GROWTH_CHAMBER, com.complexindustries.mekanism.content.chamber.TileEntityCrystalGrowthChamber::new)
                    .clientTicker(TileEntityMekanism::tickClient)
                    .serverTicker(TileEntityMekanism::tickServer)
                    .withSimple(Capabilities.CONFIGURABLE)
                    .build();

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.slicer.TileEntitySiliconSlicer> SILICON_SLICER =
            TILE_ENTITY_TYPES.mekBuilder(MCIBlocks.SILICON_SLICER, com.complexindustries.mekanism.content.slicer.TileEntitySiliconSlicer::new)
                    .clientTicker(TileEntityMekanism::tickClient)
                    .serverTicker(TileEntityMekanism::tickServer)
                    .withSimple(Capabilities.CONFIGURABLE)
                    .withSimple(Capabilities.CONFIG_CARD)
                    .build();

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.glass.TileEntityFilteredGlass> FILTERED_GLASS =
            TILE_ENTITY_TYPES.mekBuilder(MCIBlocks.FILTERED_GLASS, com.complexindustries.mekanism.content.glass.TileEntityFilteredGlass::new)
                    .serverTicker(TileEntityMekanism::tickServer)
                    .with(Capabilities.LASER_RECEPTOR, (tile, side) -> tile.getLaserReceptor(side))
                    .build();

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.lithography.TileEntityPhotolithographyMachine> PHOTOLITHOGRAPHY_MACHINE =
            TILE_ENTITY_TYPES.mekBuilder(MCIBlocks.PHOTOLITHOGRAPHY_MACHINE, com.complexindustries.mekanism.content.lithography.TileEntityPhotolithographyMachine::new)
                    .clientTicker(TileEntityMekanism::tickClient)
                    .serverTicker(TileEntityMekanism::tickServer)
                    .withSimple(Capabilities.CONFIGURABLE)
                    .withSimple(Capabilities.CONFIG_CARD)
                    .with(Capabilities.LASER_RECEPTOR, (tile, side) -> tile.getLaserReceptor(side))
                    .build();

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.coater.TileEntityChemicalFilmCoater> CHEMICAL_FILM_COATER =
            TILE_ENTITY_TYPES.mekBuilder(MCIBlocks.CHEMICAL_FILM_COATER, com.complexindustries.mekanism.content.coater.TileEntityChemicalFilmCoater::new)
                    .clientTicker(TileEntityMekanism::tickClient)
                    .serverTicker(TileEntityMekanism::tickServer)
                    .withSimple(Capabilities.CONFIGURABLE)
                    .withSimple(Capabilities.CONFIG_CARD)
                    .build();

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.pipe.TileEntityIndustrialPipe> INDUSTRIAL_PIPE =
            TILE_ENTITY_TYPES.builder(MCIBlocks.INDUSTRIAL_PIPE, com.complexindustries.mekanism.content.pipe.TileEntityIndustrialPipe::new)
                    .with(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, (tile, side) -> tile.getItemHandler(side))
                    .with(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK, (tile, side) -> tile.getFluidHandler(side))
                    .with(Capabilities.CHEMICAL.block(), (tile, side) -> tile.getChemicalHandler(side))
                    .with(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK, (tile, side) -> tile.getEnergyHandler(side))
                    .with(Capabilities.STRICT_ENERGY.block(), (tile, side) -> tile.getStrictEnergyHandler(side))
                    .build();

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.pipe.TileEntityInputInterface> INPUT_INTERFACE =
            TILE_ENTITY_TYPES.builder(MCIBlocks.INPUT_INTERFACE, com.complexindustries.mekanism.content.pipe.TileEntityInputInterface::new)
                    .with(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, (tile, side) -> tile.getItemHandler(side))
                    .with(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK, (tile, side) -> tile.getFluidHandler(side))
                    .with(Capabilities.CHEMICAL.block(), (tile, side) -> tile.getChemicalHandler(side))
                    .with(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK, (tile, side) -> tile.getEnergyHandler(side))
                    .with(Capabilities.STRICT_ENERGY.block(), (tile, side) -> tile.getStrictEnergyHandler(side))
                    .build();

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.pipe.TileEntityOutputInterface> OUTPUT_INTERFACE =
            TILE_ENTITY_TYPES.builder(MCIBlocks.OUTPUT_INTERFACE, com.complexindustries.mekanism.content.pipe.TileEntityOutputInterface::new)
                    .with(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK, (tile, side) -> tile.getEnergyHandler(side))
                    .with(Capabilities.STRICT_ENERGY.block(), (tile, side) -> tile.getStrictEnergyHandler(side))
                    .build();

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.pipe.TileEntityPowerInterface> POWER_INTERFACE =
            TILE_ENTITY_TYPES.builder(MCIBlocks.POWER_INTERFACE, com.complexindustries.mekanism.content.pipe.TileEntityPowerInterface::new)
                    .with(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK, (tile, side) -> tile.getEnergyHandler(side))
                    .with(Capabilities.STRICT_ENERGY.block(), (tile, side) -> tile.getStrictEnergyHandler(side))
                    .build();

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.extractor.TileEntityFluidExtractorCasing> FLUID_EXTRACTOR_CASING =
            TILE_ENTITY_TYPES.mekBuilder(MCIBlocks.FLUID_EXTRACTOR_CASING, com.complexindustries.mekanism.content.extractor.TileEntityFluidExtractorCasing::new)
                    .clientTicker(TileEntityMekanism::tickClient)
                    .serverTicker(TileEntityMekanism::tickServer)
                    .withSimple(Capabilities.CONFIGURABLE)
                    .build();

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.extractor.TileEntityFluidExtractorPort> FLUID_EXTRACTOR_PORT =
            TILE_ENTITY_TYPES.mekBuilder(MCIBlocks.FLUID_EXTRACTOR_PORT, com.complexindustries.mekanism.content.extractor.TileEntityFluidExtractorPort::new)
                    .clientTicker(TileEntityMekanism::tickClient)
                    .serverTicker(TileEntityMekanism::tickServer)
                    .withSimple(Capabilities.CONFIGURABLE)
                    .build();

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.extractor.TileEntityPoweredPump> POWERED_PUMP =
            TILE_ENTITY_TYPES.mekBuilder(MCIBlocks.POWERED_PUMP, com.complexindustries.mekanism.content.extractor.TileEntityPoweredPump::new)
                    .clientTicker(TileEntityMekanism::tickClient)
                    .serverTicker(TileEntityMekanism::tickServer)
                    .withSimple(Capabilities.CONFIGURABLE)
                    .build();

    private MCITileEntityTypes() {}
}
