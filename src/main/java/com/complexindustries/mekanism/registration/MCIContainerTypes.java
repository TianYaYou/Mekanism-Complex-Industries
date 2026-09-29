package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.content.tile.TileEntityResistiveCooler;
import com.complexindustries.mekanism.inventory.container.ContainerResistiveCooler;
import mekanism.common.registration.impl.ContainerTypeDeferredRegister;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;

public final class MCIContainerTypes {
    public static final ContainerTypeDeferredRegister CONTAINER_TYPES =
            new ContainerTypeDeferredRegister(MCIConstants.MODID);

    public static final ContainerTypeRegistryObject<ContainerResistiveCooler> RESISTIVE_COOLER =
            CONTAINER_TYPES.register("resistive_cooler", TileEntityResistiveCooler.class, ContainerResistiveCooler::new);

    public static final ContainerTypeRegistryObject<com.complexindustries.mekanism.content.freezer.ContainerFreezerController> FREEZER_CONTROLLER =
            CONTAINER_TYPES.register("freezer_controller", com.complexindustries.mekanism.content.freezer.TileEntityFreezerController.class, com.complexindustries.mekanism.content.freezer.ContainerFreezerController::new);

    public static final ContainerTypeRegistryObject<com.complexindustries.mekanism.inventory.container.ContainerAirCompressor> AIR_COMPRESSOR =
            CONTAINER_TYPES.register("air_compressor", com.complexindustries.mekanism.content.tile.TileEntityAirCompressor.class, com.complexindustries.mekanism.inventory.container.ContainerAirCompressor::new);

    private MCIContainerTypes() {}
}
