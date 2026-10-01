package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.MCIConstants;
import mekanism.api.chemical.Chemical;
import mekanism.common.registration.impl.ChemicalDeferredRegister;
import mekanism.common.registration.impl.DeferredChemical;

public final class MCIChemicals {
    public static final ChemicalDeferredRegister CHEMICALS = new ChemicalDeferredRegister(MCIConstants.MODID);

    // Dark crude oil chemical: 0x211D1B
    public static final DeferredChemical<Chemical> DENSE_CRUDE_OIL = CHEMICALS.register("dense_crude_oil", 0x211D1B);
    // Compressed Air: 0xD8F0F8
    public static final DeferredChemical<Chemical> COMPRESSED_AIR = CHEMICALS.register("compressed_air", 0xD8F0F8);
    // Nitrogen: 0x7FB5FF
    public static final DeferredChemical<Chemical> NITROGEN = CHEMICALS.register("nitrogen", 0x7FB5FF);
    // Noble Gas: 0xC084FC
    public static final DeferredChemical<Chemical> NOBLE_GAS = CHEMICALS.register("noble_gas", 0xC084FC);

    private MCIChemicals() {}
}
