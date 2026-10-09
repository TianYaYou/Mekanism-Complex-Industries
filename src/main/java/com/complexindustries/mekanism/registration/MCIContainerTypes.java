package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.content.freezer.ContainerFreezerController;
import com.complexindustries.mekanism.content.freezer.TileEntityFreezerController;
import com.complexindustries.mekanism.content.tile.TileEntityAirCompressor;
import com.complexindustries.mekanism.content.tile.TileEntityResistiveCooler;
import com.complexindustries.mekanism.inventory.container.ContainerAirCompressor;
import com.complexindustries.mekanism.inventory.container.ContainerResistiveCooler;
import mekanism.common.registration.impl.ContainerTypeDeferredRegister;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;

public final class MCIContainerTypes {
    public static final ContainerTypeDeferredRegister CONTAINER_TYPES =
            new ContainerTypeDeferredRegister(MCIConstants.MODID);

    public static final ContainerTypeRegistryObject<ContainerResistiveCooler> RESISTIVE_COOLER =
            CONTAINER_TYPES.register("resistive_cooler", TileEntityResistiveCooler.class, ContainerResistiveCooler::new);

    public static final ContainerTypeRegistryObject<ContainerFreezerController> FREEZER_CONTROLLER =
            CONTAINER_TYPES.register("freezer_controller", TileEntityFreezerController.class, ContainerFreezerController::new);

    public static final ContainerTypeRegistryObject<ContainerAirCompressor> AIR_COMPRESSOR =
            CONTAINER_TYPES.register("air_compressor", TileEntityAirCompressor.class, ContainerAirCompressor::new);

    public static final ContainerTypeRegistryObject<com.complexindustries.mekanism.content.refinery.ContainerRefineryController> REFINERY_CONTROLLER =
            CONTAINER_TYPES.register("refinery_controller", com.complexindustries.mekanism.content.refinery.TileEntityRefineryController.class, com.complexindustries.mekanism.content.refinery.ContainerRefineryController::new);

    public static final ContainerTypeRegistryObject<com.complexindustries.mekanism.content.solidifier.ContainerChemicalSolidifier> CHEMICAL_SOLIDIFIER =
            CONTAINER_TYPES.register("chemical_solidifier", com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifier.class, com.complexindustries.mekanism.content.solidifier.ContainerChemicalSolidifier::new);

    public static final ContainerTypeRegistryObject<com.complexindustries.mekanism.content.solidifier.ContainerChemicalSolidifierFactory> CHEMICAL_SOLIDIFIER_FACTORY =
            CONTAINER_TYPES.register("chemical_solidifier_factory", com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifierFactory.class, com.complexindustries.mekanism.content.solidifier.ContainerChemicalSolidifierFactory::new);

    public static final ContainerTypeRegistryObject<com.complexindustries.mekanism.content.flowregulator.ContainerFlowRegulator> FLOW_REGULATOR =
            CONTAINER_TYPES.register("flow_regulator", com.complexindustries.mekanism.content.flowregulator.TileEntityFlowRegulator.class, com.complexindustries.mekanism.content.flowregulator.ContainerFlowRegulator::new);

    public static final ContainerTypeRegistryObject<com.complexindustries.mekanism.content.soaker.ContainerChemicalSoaker> CHEMICAL_SOAKER =
            CONTAINER_TYPES.register("chemical_soaker", com.complexindustries.mekanism.content.soaker.TileEntityChemicalSoaker.class, com.complexindustries.mekanism.content.soaker.ContainerChemicalSoaker::new);

    public static final ContainerTypeRegistryObject<com.complexindustries.mekanism.content.soaker.ContainerChemicalSoakingFactory> CHEMICAL_SOAKING_FACTORY =
            CONTAINER_TYPES.register("chemical_soaking_factory", com.complexindustries.mekanism.content.soaker.TileEntityChemicalSoakingFactory.class, com.complexindustries.mekanism.content.soaker.ContainerChemicalSoakingFactory::new);

    public static final ContainerTypeRegistryObject<com.complexindustries.mekanism.content.chamber.ContainerCrystalGrowthChamber> CRYSTAL_GROWTH_CHAMBER =
            CONTAINER_TYPES.register("crystal_growth_chamber", com.complexindustries.mekanism.content.chamber.TileEntityCrystalGrowthChamber.class, com.complexindustries.mekanism.content.chamber.ContainerCrystalGrowthChamber::new);

    public static final ContainerTypeRegistryObject<com.complexindustries.mekanism.content.slicer.ContainerSiliconSlicer> SILICON_SLICER =
            CONTAINER_TYPES.register("silicon_slicer", com.complexindustries.mekanism.content.slicer.TileEntitySiliconSlicer.class, com.complexindustries.mekanism.content.slicer.ContainerSiliconSlicer::new);

    public static final ContainerTypeRegistryObject<com.complexindustries.mekanism.content.lithography.ContainerPhotolithographyMachine> PHOTOLITHOGRAPHY_MACHINE =
            CONTAINER_TYPES.register("photolithography_machine", com.complexindustries.mekanism.content.lithography.TileEntityPhotolithographyMachine.class, com.complexindustries.mekanism.content.lithography.ContainerPhotolithographyMachine::new);

    public static final ContainerTypeRegistryObject<com.complexindustries.mekanism.content.coater.ContainerChemicalFilmCoater> CHEMICAL_FILM_COATER =
            CONTAINER_TYPES.register("chemical_film_coater", com.complexindustries.mekanism.content.coater.TileEntityChemicalFilmCoater.class, com.complexindustries.mekanism.content.coater.ContainerChemicalFilmCoater::new);

    public static final ContainerTypeRegistryObject<com.complexindustries.mekanism.content.pipe.container.ContainerInputInterface> INPUT_INTERFACE =
            CONTAINER_TYPES.register("input_interface", (net.neoforged.neoforge.network.IContainerFactory<com.complexindustries.mekanism.content.pipe.container.ContainerInputInterface>) com.complexindustries.mekanism.content.pipe.container.ContainerInputInterface::create);

    public static final ContainerTypeRegistryObject<com.complexindustries.mekanism.content.pipe.container.ContainerOutputInterface> OUTPUT_INTERFACE =
            CONTAINER_TYPES.register("output_interface", (net.neoforged.neoforge.network.IContainerFactory<com.complexindustries.mekanism.content.pipe.container.ContainerOutputInterface>) com.complexindustries.mekanism.content.pipe.container.ContainerOutputInterface::create);

    public static final ContainerTypeRegistryObject<com.complexindustries.mekanism.content.pipe.container.ContainerPowerInterface> POWER_INTERFACE =
            CONTAINER_TYPES.register("power_interface", (net.neoforged.neoforge.network.IContainerFactory<com.complexindustries.mekanism.content.pipe.container.ContainerPowerInterface>) com.complexindustries.mekanism.content.pipe.container.ContainerPowerInterface::create);

    public static final ContainerTypeRegistryObject<com.complexindustries.mekanism.content.extractor.ContainerFluidExtractor> FLUID_EXTRACTOR =
            CONTAINER_TYPES.register("fluid_extractor", com.complexindustries.mekanism.content.extractor.TileEntityFluidExtractorCasing.class, com.complexindustries.mekanism.content.extractor.ContainerFluidExtractor::new);

    private MCIContainerTypes() {}
}
