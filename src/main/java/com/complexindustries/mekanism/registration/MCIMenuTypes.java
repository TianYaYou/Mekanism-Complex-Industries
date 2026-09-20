package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.content.extractor.CrudeOilExtractorContainer;
import com.complexindustries.mekanism.content.extractor.TileEntityCrudeOilExtractor;
import mekanism.common.registration.impl.ContainerTypeDeferredRegister;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;

public final class MCIMenuTypes {
    public static final ContainerTypeDeferredRegister CONTAINER_TYPES =
            new ContainerTypeDeferredRegister(MCIConstants.MODID);

    public static final ContainerTypeRegistryObject<CrudeOilExtractorContainer> CRUDE_OIL_EXTRACTOR =
            CONTAINER_TYPES.register("crude_oil_extractor", TileEntityCrudeOilExtractor.class, CrudeOilExtractorContainer::new);

    private MCIMenuTypes() {}
}
