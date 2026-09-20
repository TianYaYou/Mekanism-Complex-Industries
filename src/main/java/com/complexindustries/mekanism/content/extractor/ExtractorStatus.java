package com.complexindustries.mekanism.content.extractor;

import com.complexindustries.mekanism.registration.MCILang;
import mekanism.api.text.ILangEntry;
import net.minecraft.network.chat.Component;

public enum ExtractorStatus {
    IDLE(MCILang.STATUS_IDLE),
    EXTRACTING(MCILang.STATUS_EXTRACTING),
    NO_WATER(MCILang.STATUS_NO_WATER),
    NO_ENERGY(MCILang.STATUS_NO_ENERGY),
    TANK_FULL(MCILang.STATUS_TANK_FULL),
    NO_OIL(MCILang.STATUS_NO_OIL),
    DISABLED(MCILang.STATUS_DISABLED);

    private static final ExtractorStatus[] VALUES = values();
    private final ILangEntry langEntry;

    ExtractorStatus(ILangEntry langEntry) {
        this.langEntry = langEntry;
    }

    public Component getComponent() {
        return langEntry.translate();
    }

    public static ExtractorStatus byIndexStatic(int index) {
        if (index < 0 || index >= VALUES.length) {
            return IDLE;
        }
        return VALUES[index];
    }
}
