package com.complexindustries.mekanism.content.extractor;

import com.complexindustries.mekanism.content.pulverizer.MCIWindmillPulverizerMultiblock;
import com.complexindustries.mekanism.registration.MCIBlocks;
import java.util.UUID;
import mekanism.common.lib.multiblock.FormationProtocol;
import mekanism.common.lib.multiblock.IMultiblock;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.tile.prefab.TileEntityStructuralMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TileEntityFluidExtractorCasing extends TileEntityStructuralMultiblock implements IMultiblock<MultiblockData> {

    public TileEntityFluidExtractorCasing(BlockPos pos, BlockState state) {
        super(MCIBlocks.FLUID_EXTRACTOR_CASING, pos, state);
    }

    @Override
    public boolean canInterface(MultiblockManager<?> manager) {
        return manager == MCIFluidExtractorMultiblock.EXTRACTOR_MANAGER ||
               manager == MCIWindmillPulverizerMultiblock.PULVERIZER_MANAGER;
    }

    public FluidExtractorMultiblockData getMultiblock() {
        var structure = getStructure(MCIFluidExtractorMultiblock.EXTRACTOR_MANAGER);
        return structure != null ? (FluidExtractorMultiblockData) structure.getMultiblockData() : null;
    }

    @Override
    public MultiblockData createMultiblock() {
        return null;
    }

    @Override
    public MultiblockData getDefaultData() {
        return null;
    }

    @Override
    public MultiblockManager<MultiblockData> getManager() {
        for (var entry : getStructureMap().entrySet()) {
            if (entry.getValue().isValid() && entry.getValue().getMultiblockData() != null && entry.getValue().getMultiblockData().isFormed()) {
                return (MultiblockManager<MultiblockData>) entry.getKey();
            }
        }
        return null;
    }

    @Nullable
    @Override
    public UUID getCacheID() {
        return null;
    }

    @Override
    public void resetCache() {
    }

    @Override
    public boolean isMaster() {
        return false;
    }

    @Override
    public boolean canBeMaster() {
        return false;
    }

    @Override
    public Structure getStructure() {
        for (Structure s : getStructureMap().values()) {
            if (s.isValid()) {
                return s;
            }
        }
        return Structure.INVALID;
    }

    @Override
    public void setStructure(Structure structure) {
        if (structure != null && structure.getManager() != null) {
            setStructure(structure.getManager(), structure);
        }
    }

    @Override
    public FormationProtocol<MultiblockData> createFormationProtocol() {
        return null;
    }
}
