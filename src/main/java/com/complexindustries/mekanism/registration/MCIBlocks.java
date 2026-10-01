package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.content.block.AirCompressorBlock;
import com.complexindustries.mekanism.content.block.CryogenicRefrigerantBlock;
import com.complexindustries.mekanism.content.block.FreezerCasingBlock;
import com.complexindustries.mekanism.content.block.FreezerControllerBlock;
import com.complexindustries.mekanism.content.block.FreezerValveBlock;
import com.complexindustries.mekanism.content.block.RefineryCasingBlock;
import com.complexindustries.mekanism.content.block.RefineryControllerBlock;
import com.complexindustries.mekanism.content.block.RefineryValveBlock;
import com.complexindustries.mekanism.content.block.ResistiveCoolerBlock;
import com.complexindustries.mekanism.content.block.SolidCrudeOilOreBlock;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MCIBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(MCIConstants.MODID);

    public static final DeferredBlock<LiquidBlock> CRUDE_OIL_BLOCK = BLOCKS.register("crude_oil",
            () -> new LiquidBlock(MCIFluids.SOURCE_CRUDE_OIL.get(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noCollission().strength(100.0F).noLootTable()));

    public static final DeferredBlock<CryogenicRefrigerantBlock> CRYOGENIC_REFRIGERANT_BLOCK = BLOCKS.register("cryogenic_refrigerant",
            () -> new CryogenicRefrigerantBlock(MCIFluids.SOURCE_CRYOGENIC_REFRIGERANT.get(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noCollission().strength(100.0F).noLootTable()));

    public static final DeferredBlock<SolidCrudeOilOreBlock> SOLID_CRUDE_OIL_ORE = BLOCKS.register("solid_crude_oil_ore",
            () -> new SolidCrudeOilOreBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.COAL_ORE)
                            .requiresCorrectToolForDrops()
                            .strength(3.0F, 3.0F),
                    UniformInt.of(1, 3)));

    public static final DeferredBlock<SolidCrudeOilOreBlock> DEEPSLATE_SOLID_CRUDE_OIL_ORE = BLOCKS.register("deepslate_solid_crude_oil_ore",
            () -> new SolidCrudeOilOreBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_COAL_ORE)
                            .requiresCorrectToolForDrops()
                            .strength(4.5F, 3.0F)
                            .sound(SoundType.DEEPSLATE),
                    UniformInt.of(1, 3)));

    public static final DeferredBlock<ResistiveCoolerBlock> RESISTIVE_COOLER = BLOCKS.register("resistive_cooler",
            () -> new ResistiveCoolerBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .requiresCorrectToolForDrops()
                            .strength(3.5F, 16.0F)
                            .lightLevel(state -> state.getValue(ResistiveCoolerBlock.ACTIVE) ? 15 : 0)
                            .noOcclusion()));

    public static final DeferredBlock<FreezerCasingBlock> FREEZER_CASING = BLOCKS.register("freezer_casing",
            () -> new FreezerCasingBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .requiresCorrectToolForDrops()
                            .strength(5.0F, 9.0F)));

    public static final DeferredBlock<FreezerValveBlock> FREEZER_VALVE = BLOCKS.register("freezer_valve",
            () -> new FreezerValveBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .requiresCorrectToolForDrops()
                            .strength(5.0F, 9.0F)));

    public static final DeferredBlock<FreezerControllerBlock> FREEZER_CONTROLLER = BLOCKS.register("freezer_controller",
            () -> new FreezerControllerBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .requiresCorrectToolForDrops()
                            .strength(5.0F, 9.0F)
                            .lightLevel(state -> state.getValue(FreezerControllerBlock.ACTIVE) ? 8 : 0)));

    public static final DeferredBlock<AirCompressorBlock> AIR_COMPRESSOR = BLOCKS.register("air_compressor",
            () -> new AirCompressorBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .requiresCorrectToolForDrops()
                            .strength(3.5F, 16.0F)
                            .lightLevel(state -> state.getValue(AirCompressorBlock.ACTIVE) ? 8 : 0)));

    public static final DeferredBlock<RefineryCasingBlock> REFINERY_CASING = BLOCKS.register("refinery_casing",
            () -> new RefineryCasingBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .requiresCorrectToolForDrops()
                            .strength(5.0F, 9.0F)));

    public static final DeferredBlock<RefineryValveBlock> REFINERY_VALVE = BLOCKS.register("refinery_valve",
            () -> new RefineryValveBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .requiresCorrectToolForDrops()
                            .strength(5.0F, 9.0F)));

    public static final DeferredBlock<RefineryControllerBlock> REFINERY_CONTROLLER = BLOCKS.register("refinery_controller",
            () -> new RefineryControllerBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .requiresCorrectToolForDrops()
                            .strength(5.0F, 9.0F)
                            .lightLevel(state -> state.getValue(RefineryControllerBlock.ACTIVE) ? 8 : 0)));

    public static final DeferredBlock<com.complexindustries.mekanism.content.block.RefineryDredgePipeBlock> REFINERY_DREDGE_PIPE = BLOCKS.register("refinery_dredge_pipe",
            () -> new com.complexindustries.mekanism.content.block.RefineryDredgePipeBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .requiresCorrectToolForDrops()
                            .strength(5.0F, 9.0F)));

    private MCIBlocks() {}
}
