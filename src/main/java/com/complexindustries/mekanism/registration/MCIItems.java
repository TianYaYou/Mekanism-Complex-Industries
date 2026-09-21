package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.content.item.ItemDebugWand;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class MCIItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MCIConstants.MODID);



    public static final RegistryObject<BucketItem> CRUDE_OIL_BUCKET = ITEMS.register("crude_oil_bucket",
            () -> new BucketItem(MCIFluids.CRUDE_OIL_SOURCE,
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<ItemDebugWand> DEBUG_WAND = ITEMS.register("debug_wand",
            ItemDebugWand::new);

    private MCIItems() {}
}
