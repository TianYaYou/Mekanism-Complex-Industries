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
            () -> new MCIBlockItem(MCIBlocks.SOLID_CRUDE_OIL_ORE.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.solid_crude_oil_ore", false));

    public static final DeferredItem<BlockItem> DEEPSLATE_SOLID_CRUDE_OIL_ORE = ITEMS.register("deepslate_solid_crude_oil_ore",
            () -> new MCIBlockItem(MCIBlocks.DEEPSLATE_SOLID_CRUDE_OIL_ORE.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.deepslate_solid_crude_oil_ore", false));

    public static final DeferredItem<BlockItem> RESISTIVE_COOLER = ITEMS.register("resistive_cooler",
            () -> new MCIBlockItem(MCIBlocks.RESISTIVE_COOLER.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.resistive_cooler", true));

    public static final DeferredItem<BlockItem> FREEZER_CASING = ITEMS.register("freezer_casing",
            () -> new MCIBlockItem(MCIBlocks.FREEZER_CASING.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.freezer_casing", false));

    public static final DeferredItem<BlockItem> FREEZER_VALVE = ITEMS.register("freezer_valve",
            () -> new MCIBlockItem(MCIBlocks.FREEZER_VALVE.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.freezer_valve", false));

    public static final DeferredItem<BlockItem> FREEZER_CONTROLLER = ITEMS.register("freezer_controller",
            () -> new MCIBlockItem(MCIBlocks.FREEZER_CONTROLLER.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.freezer_controller", true));

    public static final DeferredItem<BucketItem> REFRIGERANT_BUCKET = ITEMS.register("refrigerant_bucket",
            () -> new BucketItem(MCIFluids.SOURCE_CRYOGENIC_REFRIGERANT.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final DeferredItem<BlockItem> AIR_COMPRESSOR = ITEMS.register("air_compressor",
            () -> new MCIBlockItem(MCIBlocks.AIR_COMPRESSOR.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.air_compressor", true));

    public static final DeferredItem<BlockItem> REFINERY_CASING = ITEMS.register("refinery_casing",
            () -> new MCIBlockItem(MCIBlocks.REFINERY_CASING.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.refinery_casing", false));

    public static final DeferredItem<BlockItem> REFINERY_VALVE = ITEMS.register("refinery_valve",
            () -> new MCIBlockItem(MCIBlocks.REFINERY_VALVE.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.refinery_valve", false));

    public static final DeferredItem<BlockItem> REFINERY_CONTROLLER = ITEMS.register("refinery_controller",
            () -> new MCIBlockItem(MCIBlocks.REFINERY_CONTROLLER.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.refinery_controller", true));

    public static final DeferredItem<BlockItem> REFINERY_DREDGE_PIPE = ITEMS.register("refinery_dredge_pipe",
            () -> new MCIBlockItem(MCIBlocks.REFINERY_DREDGE_PIPE.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.refinery_dredge_pipe", false));

    public static final DeferredItem<BlockItem> FLOW_REGULATOR = ITEMS.register("flow_regulator",
            () -> new MCIBlockItem(MCIBlocks.FLOW_REGULATOR.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.flow_regulator", true));

    public static final DeferredItem<BlockItem> BITUMEN_BLOCK = ITEMS.register("bitumen_block",
            () -> new MCIBlockItem(MCIBlocks.BITUMEN_BLOCK.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.bitumen_block", false));

    public static final java.util.Map<net.minecraft.world.item.DyeColor, DeferredItem<BlockItem>> DYED_BITUMEN_BLOCKS =
            java.util.Arrays.stream(net.minecraft.world.item.DyeColor.values()).collect(java.util.stream.Collectors.toMap(
                    color -> color,
                    color -> ITEMS.register(color.getName() + "_bitumen_block",
                            () -> new MCIBlockItem(MCIBlocks.DYED_BITUMEN_BLOCKS.get(color).get(), new Item.Properties(),
                                    "description.mekanism_complex_industries.dyed_bitumen_block", false)),
                    (a, b) -> a,
                    () -> new java.util.EnumMap<>(net.minecraft.world.item.DyeColor.class)
            ));

    public static final DeferredItem<BlockItem> BITUMEN_STAIRS = ITEMS.register("bitumen_stairs",
            () -> new MCIBlockItem(MCIBlocks.BITUMEN_STAIRS.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.bitumen_stairs", false));

    public static final java.util.Map<net.minecraft.world.item.DyeColor, DeferredItem<BlockItem>> DYED_BITUMEN_STAIRS =
            java.util.Arrays.stream(net.minecraft.world.item.DyeColor.values()).collect(java.util.stream.Collectors.toMap(
                    color -> color,
                    color -> ITEMS.register(color.getName() + "_bitumen_stairs",
                            () -> new MCIBlockItem(MCIBlocks.DYED_BITUMEN_STAIRS.get(color).get(), new Item.Properties(),
                                    "description.mekanism_complex_industries.dyed_bitumen_stairs", false)),
                    (a, b) -> a,
                    () -> new java.util.EnumMap<>(net.minecraft.world.item.DyeColor.class)
            ));

    public static final DeferredItem<BlockItem> BITUMEN_SLAB = ITEMS.register("bitumen_slab",
            () -> new MCIBlockItem(MCIBlocks.BITUMEN_SLAB.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.bitumen_slab", false));

    public static final java.util.Map<net.minecraft.world.item.DyeColor, DeferredItem<BlockItem>> DYED_BITUMEN_SLABS =
            java.util.Arrays.stream(net.minecraft.world.item.DyeColor.values()).collect(java.util.stream.Collectors.toMap(
                    color -> color,
                    color -> ITEMS.register(color.getName() + "_bitumen_slab",
                            () -> new MCIBlockItem(MCIBlocks.DYED_BITUMEN_SLABS.get(color).get(), new Item.Properties(),
                                    "description.mekanism_complex_industries.dyed_bitumen_slab", false)),
                    (a, b) -> a,
                    () -> new java.util.EnumMap<>(net.minecraft.world.item.DyeColor.class)
            ));

    public static final DeferredItem<BlockItem> CHEMICAL_SOLIDIFIER = ITEMS.register("chemical_solidifier",
            () -> new MCIBlockItem(MCIBlocks.CHEMICAL_SOLIDIFIER.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.chemical_solidifier", true));

    public static final DeferredItem<BlockItem> BASIC_CHEMICAL_SOLIDIFIER_FACTORY = ITEMS.register("basic_chemical_solidifier_factory",
            () -> new MCIBlockItem(MCIBlocks.BASIC_CHEMICAL_SOLIDIFIER_FACTORY.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.chemical_solidifier_factory", true));

    public static final DeferredItem<BlockItem> ADVANCED_CHEMICAL_SOLIDIFIER_FACTORY = ITEMS.register("advanced_chemical_solidifier_factory",
            () -> new MCIBlockItem(MCIBlocks.ADVANCED_CHEMICAL_SOLIDIFIER_FACTORY.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.chemical_solidifier_factory", true));

    public static final DeferredItem<BlockItem> ELITE_CHEMICAL_SOLIDIFIER_FACTORY = ITEMS.register("elite_chemical_solidifier_factory",
            () -> new MCIBlockItem(MCIBlocks.ELITE_CHEMICAL_SOLIDIFIER_FACTORY.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.chemical_solidifier_factory", true));

    public static final DeferredItem<BlockItem> ULTIMATE_CHEMICAL_SOLIDIFIER_FACTORY = ITEMS.register("ultimate_chemical_solidifier_factory",
            () -> new MCIBlockItem(MCIBlocks.ULTIMATE_CHEMICAL_SOLIDIFIER_FACTORY.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.chemical_solidifier_factory", true));

    public static final DeferredItem<BucketItem> LIQUID_PROPYLENE_BUCKET = ITEMS.register("liquid_propylene_bucket",
            () -> new BucketItem(MCIFluids.SOURCE_LIQUID_PROPYLENE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final DeferredItem<Item> POLYPROPYLENE_PELLET = ITEMS.registerSimpleItem("polypropylene_pellet");

    public static final DeferredItem<Item> SOAKING_ROD = ITEMS.registerSimpleItem("soaking_rod");

    public static final DeferredItem<com.complexindustries.mekanism.content.item.HeavyOilFuelItem> HEAVY_OIL_FUEL = ITEMS.register("heavy_oil_fuel",
            () -> new com.complexindustries.mekanism.content.item.HeavyOilFuelItem(new Item.Properties()));

    public static final DeferredItem<BlockItem> CHEMICAL_SOAKER = ITEMS.register("chemical_soaker",
            () -> new MCIBlockItem(MCIBlocks.CHEMICAL_SOAKER.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.chemical_soaker", true));

    public static final DeferredItem<BlockItem> BASIC_CHEMICAL_SOAKING_FACTORY = ITEMS.register("basic_chemical_soaking_factory",
            () -> new MCIBlockItem(MCIBlocks.BASIC_CHEMICAL_SOAKING_FACTORY.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.chemical_soaking_factory", true));

    public static final DeferredItem<BlockItem> ADVANCED_CHEMICAL_SOAKING_FACTORY = ITEMS.register("advanced_chemical_soaking_factory",
            () -> new MCIBlockItem(MCIBlocks.ADVANCED_CHEMICAL_SOAKING_FACTORY.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.chemical_soaking_factory", true));

    public static final DeferredItem<BlockItem> ELITE_CHEMICAL_SOAKING_FACTORY = ITEMS.register("elite_chemical_soaking_factory",
            () -> new MCIBlockItem(MCIBlocks.ELITE_CHEMICAL_SOAKING_FACTORY.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.chemical_soaking_factory", true));

    public static final DeferredItem<BlockItem> ULTIMATE_CHEMICAL_SOAKING_FACTORY = ITEMS.register("ultimate_chemical_soaking_factory",
            () -> new MCIBlockItem(MCIBlocks.ULTIMATE_CHEMICAL_SOAKING_FACTORY.get(), new Item.Properties(),
                    "description.mekanism_complex_industries.chemical_soaking_factory", true));

    private MCIItems() {}
}
