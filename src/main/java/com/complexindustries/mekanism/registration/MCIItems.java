package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.MCIConstants;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class MCIItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MCIConstants.MODID);

    public static final RegistryObject<Item> COMPLEX_ALLOY = ITEMS.register("complex_alloy",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));

    public static final RegistryObject<Item> INDUSTRIAL_CIRCUIT = ITEMS.register("industrial_circuit",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    private MCIItems() {}
}
