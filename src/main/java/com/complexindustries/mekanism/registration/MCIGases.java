package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.MCIConstants;
import mekanism.api.chemical.gas.Gas;
import mekanism.common.registration.impl.GasDeferredRegister;
import mekanism.common.registration.impl.GasRegistryObject;

public final class MCIGases {
    public static final GasDeferredRegister GASES = new GasDeferredRegister(MCIConstants.MODID);

    // Dark crude oil chemical gas: 0x211D1B
    public static final GasRegistryObject<Gas> DENSE_CRUDE_OIL = GASES.register("dense_crude_oil", 0x211D1B);
    // Compressed Air: 0xD8F0F8
    public static final GasRegistryObject<Gas> COMPRESSED_AIR = GASES.register("compressed_air", 0xD8F0F8);
    // Nitrogen: 0x7FB5FF
    public static final GasRegistryObject<Gas> NITROGEN = GASES.register("nitrogen", 0x7FB5FF);
    // Noble Gas: 0xC084FC
    public static final GasRegistryObject<Gas> NOBLE_GAS = GASES.register("noble_gas", 0xC084FC);

    private MCIGases() {}
}
