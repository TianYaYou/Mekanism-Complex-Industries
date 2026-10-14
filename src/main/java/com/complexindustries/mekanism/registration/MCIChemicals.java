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
    // Cracking Products:
    // Layer 5 (Top): Petroleum Gas: 0xF5C542
    public static final DeferredChemical<Chemical> PETROLEUM_GAS = CHEMICALS.register("petroleum_gas", 0xF5C542);
    // Layer 4: Naphtha: 0xE0C870
    public static final DeferredChemical<Chemical> NAPHTHA = CHEMICALS.register("naphtha", 0xE0C870);
    // Layer 3: Refined Fuel: 0xFFA000
    public static final DeferredChemical<Chemical> REFINED_FUEL = CHEMICALS.register("refined_fuel", 0xFFA000);
    // Layer 2: Heavy Oil: 0x3D2E24
    public static final DeferredChemical<Chemical> HEAVY_OIL = CHEMICALS.register("heavy_oil", 0x3D2E24);
    // Layer 1 (Bottom): Bitumen: 0x1A1A1A
    public static final DeferredChemical<Chemical> BITUMEN = CHEMICALS.register("bitumen", 0x1A1A1A);

    // Petrochemical Products:
    // Propylene: 0xD8E866 (lime-yellow alkene)
    public static final DeferredChemical<Chemical> PROPYLENE = CHEMICALS.register("propylene", 0xD8E866);
    // Benzene: 0x7EC8E3 (sky-blue aromatic)
    public static final DeferredChemical<Chemical> BENZENE = CHEMICALS.register("benzene", 0x7EC8E3);
    // Styrene: 0xE6A8D7 (orchid/pink monomer)
    public static final DeferredChemical<Chemical> STYRENE = CHEMICALS.register("styrene", 0xE6A8D7);

    // Ozone & Supercritical Oxidation Aerosol Chain:
    // Ozone: 0x55A8FF (vivid ozone blue)
    public static final DeferredChemical<Chemical> OZONE = CHEMICALS.register("ozone", 0x55A8FF);

    // Cloudy X Complex Aerosols (Supercritical Oxidation Precipitator outputs):
    public static final DeferredChemical<Chemical> CLOUDY_IRON_COMPLEX_AEROSOL =
            CHEMICALS.register("cloudy_iron_complex_aerosol", 0xD4B098);
    public static final DeferredChemical<Chemical> CLOUDY_GOLD_COMPLEX_AEROSOL =
            CHEMICALS.register("cloudy_gold_complex_aerosol", 0xF5D142);
    public static final DeferredChemical<Chemical> CLOUDY_COPPER_COMPLEX_AEROSOL =
            CHEMICALS.register("cloudy_copper_complex_aerosol", 0xE07A48);
    public static final DeferredChemical<Chemical> CLOUDY_OSMIUM_COMPLEX_AEROSOL =
            CHEMICALS.register("cloudy_osmium_complex_aerosol", 0x8CB8D6);
    public static final DeferredChemical<Chemical> CLOUDY_TIN_COMPLEX_AEROSOL =
            CHEMICALS.register("cloudy_tin_complex_aerosol", 0xC8D6DE);
    public static final DeferredChemical<Chemical> CLOUDY_LEAD_COMPLEX_AEROSOL =
            CHEMICALS.register("cloudy_lead_complex_aerosol", 0x6E788C);
    public static final DeferredChemical<Chemical> CLOUDY_URANIUM_COMPLEX_AEROSOL =
            CHEMICALS.register("cloudy_uranium_complex_aerosol", 0x68D85B);

    private MCIChemicals() {}
}
