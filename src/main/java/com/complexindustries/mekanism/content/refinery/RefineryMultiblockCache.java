package com.complexindustries.mekanism.content.refinery;

import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.IChemicalTank;
import mekanism.common.lib.multiblock.MultiblockCache;

import java.util.List;

public class RefineryMultiblockCache extends MultiblockCache<RefineryMultiblockData> {

    @Override
    public void sync(RefineryMultiblockData data) {
        // Ensure chemical tanks in cache are pre-allocated if loading from an older save with fewer tanks
        List<IChemicalTank> cacheTanks = getChemicalTanks(null);
        List<IChemicalTank> dataTanks = data != null ? data.getFormedChemicalTanks() : null;
        if (dataTanks != null) {
            while (cacheTanks.size() < dataTanks.size()) {
                cacheTanks.add(BasicChemicalTank.createAllValid(Long.MAX_VALUE, this));
            }
        }
        super.sync(data);
    }
}
