package com.complexindustries.mekanism.content.pulverizer;

import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.lib.multiblock.MultiblockManager;

public final class MCIWindmillPulverizerMultiblock {

    public static final MultiblockManager<WindmillPulverizerMultiblockData> PULVERIZER_MANAGER =
            new MultiblockManager<>("windmillPulverizer", MultiblockCache::new, WindmillPulverizerValidator::new);

    private MCIWindmillPulverizerMultiblock() {}

    public static void init() {
        // Initialized via class loading
    }
}
