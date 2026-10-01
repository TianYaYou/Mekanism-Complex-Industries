package com.complexindustries.mekanism;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public final class MCIConstants {
    public static final String MODID = "mekanism_complex_industries";
    public static final String MOD_NAME = "Mekanism Complex Industries";
    public static final Logger LOGGER = LogUtils.getLogger();

    private MCIConstants() {}

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
