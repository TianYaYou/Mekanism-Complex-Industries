package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.content.item.SolidCrudeOilItem;
import com.complexindustries.mekanism.content.upgrade.ItemPetroleumUpgrade;
import net.minecraft.world.item.BlockItem;
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

    public static final RegistryObject<ItemPetroleumUpgrade> PETROLEUM_UPGRADE = ITEMS.register("petroleum_upgrade",
            () -> new ItemPetroleumUpgrade(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final RegistryObject<SolidCrudeOilItem> SOLID_CRUDE_OIL = ITEMS.register("solid_crude_oil",
            () -> new SolidCrudeOilItem(new Item.Properties()));

    public static final RegistryObject<BucketItem> CRUDE_OIL_BUCKET = ITEMS.register("crude_oil_bucket",
            () -> new BucketItem(MCIFluids.SOURCE_CRUDE_OIL, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final RegistryObject<BlockItem> SOLID_CRUDE_OIL_ORE = ITEMS.register("solid_crude_oil_ore",
            () -> new BlockItem(MCIBlocks.SOLID_CRUDE_OIL_ORE.get(), new Item.Properties()));

    public static final RegistryObject<BlockItem> DEEPSLATE_SOLID_CRUDE_OIL_ORE = ITEMS.register("deepslate_solid_crude_oil_ore",
            () -> new BlockItem(MCIBlocks.DEEPSLATE_SOLID_CRUDE_OIL_ORE.get(), new Item.Properties()));

    private MCIItems() {}
}
