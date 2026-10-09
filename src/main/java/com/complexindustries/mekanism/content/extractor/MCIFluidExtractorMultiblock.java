package com.complexindustries.mekanism.content.extractor;

import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.lib.multiblock.MultiblockManager;

public final class MCIFluidExtractorMultiblock {
    public static final MultiblockManager<FluidExtractorMultiblockData> EXTRACTOR_MANAGER =
            new MultiblockManager<>("fluidExtractor", MultiblockCache::new, FluidExtractorValidator::new);

    private MCIFluidExtractorMultiblock() {}
}
