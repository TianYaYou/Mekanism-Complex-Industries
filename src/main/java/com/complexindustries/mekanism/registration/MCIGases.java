package com.complexindustries.mekanism.registration;

import mekanism.api.chemical.Chemical;
import mekanism.common.registration.impl.DeferredChemical;

@Deprecated
public final class MCIGases {
    public static final DeferredChemical<Chemical> DENSE_CRUDE_OIL = MCIChemicals.DENSE_CRUDE_OIL;
    public static final DeferredChemical<Chemical> COMPRESSED_AIR = MCIChemicals.COMPRESSED_AIR;
    public static final DeferredChemical<Chemical> NITROGEN = MCIChemicals.NITROGEN;
    public static final DeferredChemical<Chemical> NOBLE_GAS = MCIChemicals.NOBLE_GAS;

    private MCIGases() {}
}
