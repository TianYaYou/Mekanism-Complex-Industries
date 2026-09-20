package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.MCIConstants;
import mekanism.api.text.ILangEntry;
import net.minecraft.Util;

public enum MCILang implements ILangEntry {
    CRUDE_OIL_EXTRACTOR("block", "crude_oil_extractor"),
    CRUDE_OIL_EXTRACTOR_DESCRIPTION("description", "crude_oil_extractor"),
    STATUS_EXTRACTING("status", "extracting"),
    STATUS_IDLE("status", "idle"),
    STATUS_NO_WATER("status", "no_water"),
    STATUS_NO_ENERGY("status", "no_energy"),
    STATUS_TANK_FULL("status", "tank_full"),
    STATUS_NO_OIL("status", "no_oil"),
    STATUS_DISABLED("status", "disabled");

    private final String key;

    MCILang(String type, String path) {
        this(Util.makeDescriptionId(type, MCIConstants.rl(path)));
    }

    MCILang(String key) {
        this.key = key;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}
