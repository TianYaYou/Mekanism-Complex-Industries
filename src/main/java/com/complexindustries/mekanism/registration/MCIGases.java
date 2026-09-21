package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.MCIConstants;
import mekanism.api.chemical.gas.Gas;
import mekanism.common.registration.impl.GasDeferredRegister;
import mekanism.common.registration.impl.GasRegistryObject;

public final class MCIGases {
    public static final GasDeferredRegister GASES = new GasDeferredRegister(MCIConstants.MODID);

    // Dark crude oil chemical gas: 0x211D1B
    public static final GasRegistryObject<Gas> DENSE_CRUDE_OIL = GASES.register("dense_crude_oil", 0x211D1B);

    private MCIGases() {}
}
