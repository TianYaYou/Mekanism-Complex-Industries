package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.MCIConstants;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class MCIBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MCIConstants.MODID);

    private MCIBlockEntityTypes() {}
}
