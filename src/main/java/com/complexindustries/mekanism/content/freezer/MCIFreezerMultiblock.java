package com.complexindustries.mekanism.content.freezer;

import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.lib.multiblock.MultiblockManager;

public final class MCIFreezerMultiblock {
    public static final MultiblockManager<FreezerMultiblockData> FREEZER_MANAGER =
            new MultiblockManager<>("industrialFreezer", MultiblockCache::new, FreezerValidator::new);

    private MCIFreezerMultiblock() {}
}
