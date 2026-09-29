package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.content.tile.TileEntityResistiveCooler;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class MCITileEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MCIConstants.MODID);

    public static final RegistryObject<BlockEntityType<TileEntityResistiveCooler>> RESISTIVE_COOLER_BE =
            TILE_ENTITY_TYPES.register("resistive_cooler", () ->
                    BlockEntityType.Builder.of(TileEntityResistiveCooler::new, MCIBlocks.RESISTIVE_COOLER.get()).build(null));

    public static final TileEntityTypeRegistryObject<TileEntityResistiveCooler> RESISTIVE_COOLER =
            new TileEntityTypeRegistryObject<>(RESISTIVE_COOLER_BE);

    public static final RegistryObject<BlockEntityType<com.complexindustries.mekanism.content.freezer.TileEntityFreezerCasing>> FREEZER_CASING_BE =
            TILE_ENTITY_TYPES.register("freezer_casing", () ->
                    BlockEntityType.Builder.of(com.complexindustries.mekanism.content.freezer.TileEntityFreezerCasing::new, MCIBlocks.FREEZER_CASING.get()).build(null));

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.freezer.TileEntityFreezerCasing> FREEZER_CASING =
            new TileEntityTypeRegistryObject<>(FREEZER_CASING_BE);

    public static final RegistryObject<BlockEntityType<com.complexindustries.mekanism.content.freezer.TileEntityFreezerValve>> FREEZER_VALVE_BE =
            TILE_ENTITY_TYPES.register("freezer_valve", () ->
                    BlockEntityType.Builder.of(com.complexindustries.mekanism.content.freezer.TileEntityFreezerValve::new, MCIBlocks.FREEZER_VALVE.get()).build(null));

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.freezer.TileEntityFreezerValve> FREEZER_VALVE =
            new TileEntityTypeRegistryObject<>(FREEZER_VALVE_BE);

    public static final RegistryObject<BlockEntityType<com.complexindustries.mekanism.content.freezer.TileEntityFreezerController>> FREEZER_CONTROLLER_BE =
            TILE_ENTITY_TYPES.register("freezer_controller", () ->
                    BlockEntityType.Builder.of(com.complexindustries.mekanism.content.freezer.TileEntityFreezerController::new, MCIBlocks.FREEZER_CONTROLLER.get()).build(null));

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.freezer.TileEntityFreezerController> FREEZER_CONTROLLER =
            new TileEntityTypeRegistryObject<>(FREEZER_CONTROLLER_BE);

    public static final RegistryObject<BlockEntityType<com.complexindustries.mekanism.content.tile.TileEntityAirCompressor>> AIR_COMPRESSOR_BE =
            TILE_ENTITY_TYPES.register("air_compressor", () ->
                    BlockEntityType.Builder.of(com.complexindustries.mekanism.content.tile.TileEntityAirCompressor::new, MCIBlocks.AIR_COMPRESSOR.get()).build(null));

    public static final TileEntityTypeRegistryObject<com.complexindustries.mekanism.content.tile.TileEntityAirCompressor> AIR_COMPRESSOR =
            new TileEntityTypeRegistryObject<>(AIR_COMPRESSOR_BE);

    private MCITileEntityTypes() {}
}
