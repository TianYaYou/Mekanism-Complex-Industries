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

    public static final DeferredBlock<com.complexindustries.mekanism.content.flowregulator.BlockFlowRegulator> FLOW_REGULATOR = BLOCKS.register("flow_regulator",
            () -> new com.complexindustries.mekanism.content.flowregulator.BlockFlowRegulator(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .requiresCorrectToolForDrops()
                            .strength(3.5F, 16.0F)
                            .noOcclusion()));

    public static final DeferredBlock<com.complexindustries.mekanism.content.block.decorative.BitumenBlock> BITUMEN_BLOCK = BLOCKS.register("bitumen_block",
            () -> new com.complexindustries.mekanism.content.block.decorative.BitumenBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.BLACKSTONE)
                            .requiresCorrectToolForDrops()
                            .strength(2.0F, 6.0F)
                            .sound(SoundType.STONE)));

    public static final java.util.Map<net.minecraft.world.item.DyeColor, DeferredBlock<com.complexindustries.mekanism.content.block.decorative.DyedBitumenBlock>> DYED_BITUMEN_BLOCKS =
            java.util.Arrays.stream(net.minecraft.world.item.DyeColor.values()).collect(java.util.stream.Collectors.toMap(
                    color -> color,
                    color -> BLOCKS.register(color.getName() + "_bitumen_block",
                            () -> new com.complexindustries.mekanism.content.block.decorative.DyedBitumenBlock(color,
                                     BlockBehaviour.Properties.ofFullCopy(Blocks.BLACKSTONE)
                                             .requiresCorrectToolForDrops()
                                             .strength(2.0F, 6.0F)
                                             .sound(SoundType.STONE))),
                    (a, b) -> a,
                    () -> new java.util.EnumMap<>(net.minecraft.world.item.DyeColor.class)
            ));

    public static final DeferredBlock<com.complexindustries.mekanism.content.block.decorative.BitumenStairsBlock> BITUMEN_STAIRS = BLOCKS.register("bitumen_stairs",
            () -> new com.complexindustries.mekanism.content.block.decorative.BitumenStairsBlock(
                    BITUMEN_BLOCK.get().defaultBlockState(),
                    BlockBehaviour.Properties.ofFullCopy(BITUMEN_BLOCK.get())));

    public static final java.util.Map<net.minecraft.world.item.DyeColor, DeferredBlock<com.complexindustries.mekanism.content.block.decorative.DyedBitumenStairsBlock>> DYED_BITUMEN_STAIRS =
            java.util.Arrays.stream(net.minecraft.world.item.DyeColor.values()).collect(java.util.stream.Collectors.toMap(
                    color -> color,
                    color -> BLOCKS.register(color.getName() + "_bitumen_stairs",
                            () -> new com.complexindustries.mekanism.content.block.decorative.DyedBitumenStairsBlock(
                                    DYED_BITUMEN_BLOCKS.get(color).get().defaultBlockState(),
                                    color,
                                    BlockBehaviour.Properties.ofFullCopy(DYED_BITUMEN_BLOCKS.get(color).get()))),
                    (a, b) -> a,
                    () -> new java.util.EnumMap<>(net.minecraft.world.item.DyeColor.class)
            ));

    public static final DeferredBlock<com.complexindustries.mekanism.content.block.decorative.BitumenSlabBlock> BITUMEN_SLAB = BLOCKS.register("bitumen_slab",
            () -> new com.complexindustries.mekanism.content.block.decorative.BitumenSlabBlock(
                    BlockBehaviour.Properties.ofFullCopy(BITUMEN_BLOCK.get())));

    public static final java.util.Map<net.minecraft.world.item.DyeColor, DeferredBlock<com.complexindustries.mekanism.content.block.decorative.DyedBitumenSlabBlock>> DYED_BITUMEN_SLABS =
            java.util.Arrays.stream(net.minecraft.world.item.DyeColor.values()).collect(java.util.stream.Collectors.toMap(
                    color -> color,
                    color -> BLOCKS.register(color.getName() + "_bitumen_slab",
                            () -> new com.complexindustries.mekanism.content.block.decorative.DyedBitumenSlabBlock(
                                    color,
                                    BlockBehaviour.Properties.ofFullCopy(DYED_BITUMEN_BLOCKS.get(color).get()))),
                    (a, b) -> a,
                    () -> new java.util.EnumMap<>(net.minecraft.world.item.DyeColor.class)
            ));

    public static final DeferredBlock<com.complexindustries.mekanism.content.block.ChemicalSolidifierBlock> CHEMICAL_SOLIDIFIER = BLOCKS.register("chemical_solidifier",
            () -> new com.complexindustries.mekanism.content.block.ChemicalSolidifierBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .requiresCorrectToolForDrops()
                            .strength(3.5F, 16.0F)
                            .lightLevel(state -> state.getValue(com.complexindustries.mekanism.content.block.ChemicalSolidifierBlock.ACTIVE) ? 8 : 0)
                            .noOcclusion()));

    public static final DeferredBlock<com.complexindustries.mekanism.content.block.ChemicalSolidifierFactoryBlock> BASIC_CHEMICAL_SOLIDIFIER_FACTORY = BLOCKS.register("basic_chemical_solidifier_factory",
            () -> new com.complexindustries.mekanism.content.block.ChemicalSolidifierFactoryBlock(
                    mekanism.common.tier.FactoryTier.BASIC,
                    () -> new mekanism.common.registration.impl.BlockRegistryObject<>(MCIBlocks.ADVANCED_CHEMICAL_SOLIDIFIER_FACTORY, MCIItems.ADVANCED_CHEMICAL_SOLIDIFIER_FACTORY),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .requiresCorrectToolForDrops()
                            .strength(3.5F, 16.0F)
                            .lightLevel(state -> state.getValue(com.complexindustries.mekanism.content.block.ChemicalSolidifierFactoryBlock.ACTIVE) ? 8 : 0)));

    public static final DeferredBlock<com.complexindustries.mekanism.content.block.ChemicalSolidifierFactoryBlock> ADVANCED_CHEMICAL_SOLIDIFIER_FACTORY = BLOCKS.register("advanced_chemical_solidifier_factory",
            () -> new com.complexindustries.mekanism.content.block.ChemicalSolidifierFactoryBlock(
                    mekanism.common.tier.FactoryTier.ADVANCED,
                    () -> new mekanism.common.registration.impl.BlockRegistryObject<>(MCIBlocks.ELITE_CHEMICAL_SOLIDIFIER_FACTORY, MCIItems.ELITE_CHEMICAL_SOLIDIFIER_FACTORY),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .requiresCorrectToolForDrops()
                            .strength(3.5F, 16.0F)
                            .lightLevel(state -> state.getValue(com.complexindustries.mekanism.content.block.ChemicalSolidifierFactoryBlock.ACTIVE) ? 8 : 0)));

    public static final DeferredBlock<com.complexindustries.mekanism.content.block.ChemicalSolidifierFactoryBlock> ELITE_CHEMICAL_SOLIDIFIER_FACTORY = BLOCKS.register("elite_chemical_solidifier_factory",
            () -> new com.complexindustries.mekanism.content.block.ChemicalSolidifierFactoryBlock(
                    mekanism.common.tier.FactoryTier.ELITE,
                    () -> new mekanism.common.registration.impl.BlockRegistryObject<>(MCIBlocks.ULTIMATE_CHEMICAL_SOLIDIFIER_FACTORY, MCIItems.ULTIMATE_CHEMICAL_SOLIDIFIER_FACTORY),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .requiresCorrectToolForDrops()
                            .strength(3.5F, 16.0F)
                            .lightLevel(state -> state.getValue(com.complexindustries.mekanism.content.block.ChemicalSolidifierFactoryBlock.ACTIVE) ? 8 : 0)));

    public static final DeferredBlock<com.complexindustries.mekanism.content.block.ChemicalSolidifierFactoryBlock> ULTIMATE_CHEMICAL_SOLIDIFIER_FACTORY = BLOCKS.register("ultimate_chemical_solidifier_factory",
            () -> new com.complexindustries.mekanism.content.block.ChemicalSolidifierFactoryBlock(
                    mekanism.common.tier.FactoryTier.ULTIMATE,
                    null,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .requiresCorrectToolForDrops()
                            .strength(3.5F, 16.0F)
                            .lightLevel(state -> state.getValue(com.complexindustries.mekanism.content.block.ChemicalSolidifierFactoryBlock.ACTIVE) ? 8 : 0)));

    public static final DeferredBlock<LiquidBlock> LIQUID_PROPYLENE_BLOCK = BLOCKS.register("liquid_propylene",
            () -> new LiquidBlock(MCIFluids.SOURCE_LIQUID_PROPYLENE.get(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noCollission().strength(100.0F).noLootTable()));

    public static final DeferredBlock<com.complexindustries.mekanism.content.block.ChemicalSoakerBlock> CHEMICAL_SOAKER = BLOCKS.register("chemical_soaker",
            () -> new com.complexindustries.mekanism.content.block.ChemicalSoakerBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .requiresCorrectToolForDrops()
                            .strength(3.5F, 16.0F)
                            .lightLevel(state -> state.getValue(com.complexindustries.mekanism.content.block.ChemicalSoakerBlock.ACTIVE) ? 8 : 0)
                            .noOcclusion()));

    public static final DeferredBlock<com.complexindustries.mekanism.content.block.ChemicalSoakingFactoryBlock> BASIC_CHEMICAL_SOAKING_FACTORY = BLOCKS.register("basic_chemical_soaking_factory",
            () -> new com.complexindustries.mekanism.content.block.ChemicalSoakingFactoryBlock(
                    mekanism.common.tier.FactoryTier.BASIC,
                    () -> new mekanism.common.registration.impl.BlockRegistryObject<>(MCIBlocks.ADVANCED_CHEMICAL_SOAKING_FACTORY, MCIItems.ADVANCED_CHEMICAL_SOAKING_FACTORY),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .requiresCorrectToolForDrops()
                            .strength(3.5F, 16.0F)
                            .lightLevel(state -> state.getValue(com.complexindustries.mekanism.content.block.ChemicalSoakingFactoryBlock.ACTIVE) ? 8 : 0)));

    public static final DeferredBlock<com.complexindustries.mekanism.content.block.ChemicalSoakingFactoryBlock> ADVANCED_CHEMICAL_SOAKING_FACTORY = BLOCKS.register("advanced_chemical_soaking_factory",
            () -> new com.complexindustries.mekanism.content.block.ChemicalSoakingFactoryBlock(
                    mekanism.common.tier.FactoryTier.ADVANCED,
                    () -> new mekanism.common.registration.impl.BlockRegistryObject<>(MCIBlocks.ELITE_CHEMICAL_SOAKING_FACTORY, MCIItems.ELITE_CHEMICAL_SOAKING_FACTORY),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .requiresCorrectToolForDrops()
                            .strength(3.5F, 16.0F)
                            .lightLevel(state -> state.getValue(com.complexindustries.mekanism.content.block.ChemicalSoakingFactoryBlock.ACTIVE) ? 8 : 0)));

    public static final DeferredBlock<com.complexindustries.mekanism.content.block.ChemicalSoakingFactoryBlock> ELITE_CHEMICAL_SOAKING_FACTORY = BLOCKS.register("elite_chemical_soaking_factory",
            () -> new com.complexindustries.mekanism.content.block.ChemicalSoakingFactoryBlock(
                    mekanism.common.tier.FactoryTier.ELITE,
                    () -> new mekanism.common.registration.impl.BlockRegistryObject<>(MCIBlocks.ULTIMATE_CHEMICAL_SOAKING_FACTORY, MCIItems.ULTIMATE_CHEMICAL_SOAKING_FACTORY),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .requiresCorrectToolForDrops()
                            .strength(3.5F, 16.0F)
                            .lightLevel(state -> state.getValue(com.complexindustries.mekanism.content.block.ChemicalSoakingFactoryBlock.ACTIVE) ? 8 : 0)));

    public static final DeferredBlock<com.complexindustries.mekanism.content.block.ChemicalSoakingFactoryBlock> ULTIMATE_CHEMICAL_SOAKING_FACTORY = BLOCKS.register("ultimate_chemical_soaking_factory",
            () -> new com.complexindustries.mekanism.content.block.ChemicalSoakingFactoryBlock(
                    mekanism.common.tier.FactoryTier.ULTIMATE,
                    null,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .requiresCorrectToolForDrops()
                            .strength(3.5F, 16.0F)
                            .lightLevel(state -> state.getValue(com.complexindustries.mekanism.content.block.ChemicalSoakingFactoryBlock.ACTIVE) ? 8 : 0)));


    public static final DeferredBlock<com.complexindustries.mekanism.content.block.CrystalGrowthChamberBlock> CRYSTAL_GROWTH_CHAMBER = BLOCKS.register("crystal_growth_chamber",
            () -> new com.complexindustries.mekanism.content.block.CrystalGrowthChamberBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
                            .requiresCorrectToolForDrops()
                            .strength(3.5F, 16.0F)
                            .lightLevel(state -> state.getValue(com.complexindustries.mekanism.content.block.CrystalGrowthChamberBlock.ACTIVE) ? 12 : 0)
                            .noOcclusion()));

    public static final DeferredBlock<com.complexindustries.mekanism.content.block.BlockSiliconSlicer> SILICON_SLICER = BLOCKS.register("silicon_slicer",
            () -> new com.complexindustries.mekanism.content.block.BlockSiliconSlicer(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .requiresCorrectToolForDrops()
                            .strength(3.5F, 16.0F)
                            .lightLevel(state -> state.getValue(com.complexindustries.mekanism.content.block.BlockSiliconSlicer.ACTIVE) ? 8 : 0)));

    public static final DeferredBlock<com.complexindustries.mekanism.content.block.FilteredGlassBlock> FILTERED_GLASS = BLOCKS.register("filtered_glass",
            () -> new com.complexindustries.mekanism.content.block.FilteredGlassBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
                            .requiresCorrectToolForDrops()
                            .strength(3.0F, 8.0F)
                            .sound(net.minecraft.world.level.block.SoundType.GLASS)
                            .noOcclusion()));

    public static final DeferredBlock<com.complexindustries.mekanism.content.block.BlockPhotolithographyMachine> PHOTOLITHOGRAPHY_MACHINE = BLOCKS.register("photolithography_machine",
            () -> new com.complexindustries.mekanism.content.block.BlockPhotolithographyMachine(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .requiresCorrectToolForDrops()
                            .strength(3.5F, 16.0F)
                            .lightLevel(state -> state.getValue(com.complexindustries.mekanism.content.block.BlockPhotolithographyMachine.ACTIVE) ? 8 : 0)));

    public static final DeferredBlock<com.complexindustries.mekanism.content.coater.BlockChemicalFilmCoater> CHEMICAL_FILM_COATER = BLOCKS.register("chemical_film_coater",
            () -> new com.complexindustries.mekanism.content.coater.BlockChemicalFilmCoater(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .requiresCorrectToolForDrops()
                            .strength(3.5F, 16.0F)
                            .lightLevel(state -> state.getValue(com.complexindustries.mekanism.content.coater.BlockChemicalFilmCoater.ACTIVE) ? 8 : 0)));

    private MCIBlocks() {}
}
