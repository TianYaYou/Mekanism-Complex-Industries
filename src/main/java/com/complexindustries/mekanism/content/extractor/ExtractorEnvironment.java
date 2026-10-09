package com.complexindustries.mekanism.content.extractor;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum ExtractorEnvironment implements StringRepresentable {
    NONE("none"),
    AIR("air"),
    WATER("water"),
    LAVA_INSUFFICIENT("lava_insufficient"),
    LAVA("lava");

    private final String name;

    ExtractorEnvironment(String name) {
        this.name = name;
    }

    @NotNull
    @Override
    public String getSerializedName() {
        return name;
    }
}
