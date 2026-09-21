package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.MCIConstants;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class MCIItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MCIConstants.MODID);

    private MCIItems() {}
}
