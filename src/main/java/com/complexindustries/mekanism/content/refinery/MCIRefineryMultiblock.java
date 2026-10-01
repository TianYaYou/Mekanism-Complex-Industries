package com.complexindustries.mekanism.content.refinery;

import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.lib.multiblock.MultiblockManager;

public final class MCIRefineryMultiblock {
    public static final MultiblockManager<RefineryMultiblockData> REFINERY_MANAGER =
            new MultiblockManager<>("industrialRefinery", MultiblockCache::new, RefineryValidator::new);

    private MCIRefineryMultiblock() {}
}
