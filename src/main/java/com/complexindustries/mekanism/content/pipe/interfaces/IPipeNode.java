package com.complexindustries.mekanism.content.pipe.interfaces;

import com.complexindustries.mekanism.content.pipe.attachment.IPipeAttachment;
import com.complexindustries.mekanism.content.pipe.network.IndustrialPipeNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface IPipeNode {

    @Nullable
    Level getNodeLevel();

    BlockPos getNodePos();

    @Nullable
    IndustrialPipeNetwork getPipeNetwork();

    void setPipeNetwork(@Nullable IndustrialPipeNetwork network);

    boolean canConnectPipe(Direction direction);

    @Nullable
    default IPipeAttachment getAttachment(Direction face) {
        return null;
    }
}
