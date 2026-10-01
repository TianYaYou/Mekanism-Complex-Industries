package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.content.block.MCIBlockItem;
import com.complexindustries.mekanism.content.item.SolidCrudeOilItem;
import com.complexindustries.mekanism.content.upgrade.ItemPetroleumUpgrade;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MCIItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(MCIConstants.MODID);

    public static final DeferredItem<ItemPetroleumUpgrade> PETROLEUM_UPGRADE = ITEMS.register("petroleum_upgrade",
            () -> new ItemPetroleumUpgrade(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final DeferredItem<SolidCrudeOilItem> SOLID_CRUDE_OIL = ITEMS.register("solid_crude_oil",
            () -> new SolidCrudeOilItem(new Item.Properties()));

    public static final DeferredItem<BucketItem> CRUDE_OIL_BUCKET = ITEMS.register("crude_oil_bucket",
            () -> new BucketItem(MCIFluids.SOURCE_CRUDE_OIL.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final DeferredItem<BlockItem> SOLID_CRUDE_OIL_ORE = ITEMS.register("solid_crude_oil_ore",
            () -> new BlockItem(MCIBlocks.SOLID_CRUDE_OIL_ORE.get(), new Item.Properties()));

    public static final DeferredItem<BlockItem> DEEPSLATE_SOLID_CRUDE_OIL_ORE = ITEMS.register("deepslate_solid_crude_oil_ore",
            () -> new BlockItem(MCIBlocks.DEEPSLATE_SOLID_CRUDE_OIL_ORE.get(), new Item.Properties()));

    public static final DeferredItem<BlockItem> RESISTIVE_COOLER = ITEMS.register("resistive_cooler",
            () -> new MCIBlockItem(MCIBlocks.RESISTIVE_COOLER.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.resistive_cooler"));

    public static final DeferredItem<BlockItem> FREEZER_CASING = ITEMS.register("freezer_casing",
            () -> new MCIBlockItem(MCIBlocks.FREEZER_CASING.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.freezer_casing"));

    public static final DeferredItem<BlockItem> FREEZER_VALVE = ITEMS.register("freezer_valve",
            () -> new MCIBlockItem(MCIBlocks.FREEZER_VALVE.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.freezer_valve"));

    public static final DeferredItem<BlockItem> FREEZER_CONTROLLER = ITEMS.register("freezer_controller",
            () -> new MCIBlockItem(MCIBlocks.FREEZER_CONTROLLER.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.freezer_controller"));

    public static final DeferredItem<BucketItem> REFRIGERANT_BUCKET = ITEMS.register("refrigerant_bucket",
            () -> new BucketItem(MCIFluids.SOURCE_CRYOGENIC_REFRIGERANT.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final DeferredItem<BlockItem> AIR_COMPRESSOR = ITEMS.register("air_compressor",
            () -> new MCIBlockItem(MCIBlocks.AIR_COMPRESSOR.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.air_compressor"));

    public static final DeferredItem<BlockItem> REFINERY_CASING = ITEMS.register("refinery_casing",
            () -> new MCIBlockItem(MCIBlocks.REFINERY_CASING.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.refinery_casing"));

    public static final DeferredItem<BlockItem> REFINERY_VALVE = ITEMS.register("refinery_valve",
            () -> new MCIBlockItem(MCIBlocks.REFINERY_VALVE.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.refinery_valve"));

    public static final DeferredItem<BlockItem> REFINERY_CONTROLLER = ITEMS.register("refinery_controller",
            () -> new MCIBlockItem(MCIBlocks.REFINERY_CONTROLLER.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.refinery_controller"));

    public static final DeferredItem<BlockItem> REFINERY_DREDGE_PIPE = ITEMS.register("refinery_dredge_pipe",
            () -> new MCIBlockItem(MCIBlocks.REFINERY_DREDGE_PIPE.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.refinery_dredge_pipe"));

    private MCIItems() {}
}
