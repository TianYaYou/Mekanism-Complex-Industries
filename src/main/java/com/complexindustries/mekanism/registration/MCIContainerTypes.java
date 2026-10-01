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

    private MCIContainerTypes() {}
}
