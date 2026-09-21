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
    STATUS_DISABLED("status", "disabled"),
    STATUS_STOPPED("status", "stopped"),
    STATUS_FINISHED("status", "finished"),
    GUI_START("gui", "start"),
    GUI_STOP("gui", "stop"),
    GUI_CONFIG("gui", "config"),
    GUI_RESET("gui", "reset"),
    GUI_RADIUS("gui", "radius"),
    GUI_MIN_Y("gui", "min_y"),
    GUI_MAX_Y("gui", "max_y"),
    GUI_RANGE_TITLE("gui", "range_title"),
    GUI_EXTRACTED("gui", "extracted"),
    GUI_RANGE_STAT("gui", "range_stat");

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
