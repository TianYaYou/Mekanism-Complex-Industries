package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.MCIConstants;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class MCIBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MCIConstants.MODID);

    private MCIBlocks() {}
}
