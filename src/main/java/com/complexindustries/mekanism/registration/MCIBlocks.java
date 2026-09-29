package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.content.block.AirCompressorBlock;
import com.complexindustries.mekanism.content.block.CryogenicRefrigerantBlock;
import com.complexindustries.mekanism.content.block.FreezerCasingBlock;
import com.complexindustries.mekanism.content.block.FreezerControllerBlock;
import com.complexindustries.mekanism.content.block.FreezerValveBlock;
import com.complexindustries.mekanism.content.block.ResistiveCoolerBlock;
import com.complexindustries.mekanism.content.block.SolidCrudeOilOreBlock;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class MCIBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MCIConstants.MODID);

    public static final RegistryObject<LiquidBlock> CRUDE_OIL_BLOCK = BLOCKS.register("crude_oil",
            () -> new LiquidBlock(MCIFluids.SOURCE_CRUDE_OIL,
                    BlockBehaviour.Properties.copy(Blocks.WATER).noCollission().strength(100.0F).noLootTable()));

    public static final RegistryObject<CryogenicRefrigerantBlock> CRYOGENIC_REFRIGERANT_BLOCK = BLOCKS.register("cryogenic_refrigerant",
            () -> new CryogenicRefrigerantBlock(MCIFluids.SOURCE_CRYOGENIC_REFRIGERANT,
                    BlockBehaviour.Properties.copy(Blocks.WATER).noCollission().strength(100.0F).noLootTable()));

    public static final RegistryObject<SolidCrudeOilOreBlock> SOLID_CRUDE_OIL_ORE = BLOCKS.register("solid_crude_oil_ore",
            () -> new SolidCrudeOilOreBlock(
                    BlockBehaviour.Properties.copy(Blocks.COAL_ORE)
                            .requiresCorrectToolForDrops()
                            .strength(3.0F, 3.0F),
                    UniformInt.of(1, 3)));

    public static final RegistryObject<SolidCrudeOilOreBlock> DEEPSLATE_SOLID_CRUDE_OIL_ORE = BLOCKS.register("deepslate_solid_crude_oil_ore",
            () -> new SolidCrudeOilOreBlock(
                    BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_COAL_ORE)
                            .requiresCorrectToolForDrops()
                            .strength(4.5F, 3.0F)
                            .sound(SoundType.DEEPSLATE),
                    UniformInt.of(1, 3)));

    public static final RegistryObject<ResistiveCoolerBlock> RESISTIVE_COOLER = BLOCKS.register("resistive_cooler",
            () -> new ResistiveCoolerBlock(
                    BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                            .requiresCorrectToolForDrops()
                            .strength(3.5F, 16.0F)
                            .lightLevel(state -> state.getValue(ResistiveCoolerBlock.ACTIVE) ? 15 : 0)
                            .noOcclusion()));

    public static final mekanism.api.providers.IBlockProvider RESISTIVE_COOLER_PROVIDER = new mekanism.api.providers.IBlockProvider() {
        @Override
        public Block getBlock() {
            return RESISTIVE_COOLER.get();
        }

        @Override
        public net.minecraft.world.item.Item asItem() {
            return RESISTIVE_COOLER.get().asItem();
        }

        @Override
        public net.minecraft.resources.ResourceLocation getRegistryName() {
            return RESISTIVE_COOLER.getId();
        }

        @Override
        public String getTranslationKey() {
            return RESISTIVE_COOLER.get().getDescriptionId();
        }
    };

    public static final RegistryObject<FreezerCasingBlock> FREEZER_CASING = BLOCKS.register("freezer_casing",
            () -> new FreezerCasingBlock(
                    BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                            .requiresCorrectToolForDrops()
                            .strength(5.0F, 9.0F)));

    public static final mekanism.api.providers.IBlockProvider FREEZER_CASING_PROVIDER = new mekanism.api.providers.IBlockProvider() {
        @Override
        public Block getBlock() {
            return FREEZER_CASING.get();
        }

        @Override
        public net.minecraft.world.item.Item asItem() {
            return FREEZER_CASING.get().asItem();
        }

        @Override
        public net.minecraft.resources.ResourceLocation getRegistryName() {
            return FREEZER_CASING.getId();
        }

        @Override
        public String getTranslationKey() {
            return FREEZER_CASING.get().getDescriptionId();
        }
    };

    public static final RegistryObject<FreezerValveBlock> FREEZER_VALVE = BLOCKS.register("freezer_valve",
            () -> new FreezerValveBlock(
                    BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                            .requiresCorrectToolForDrops()
                            .strength(5.0F, 9.0F)));

    public static final mekanism.api.providers.IBlockProvider FREEZER_VALVE_PROVIDER = new mekanism.api.providers.IBlockProvider() {
        @Override
        public Block getBlock() {
            return FREEZER_VALVE.get();
        }

        @Override
        public net.minecraft.world.item.Item asItem() {
            return FREEZER_VALVE.get().asItem();
        }

        @Override
        public net.minecraft.resources.ResourceLocation getRegistryName() {
            return FREEZER_VALVE.getId();
        }

        @Override
        public String getTranslationKey() {
            return FREEZER_VALVE.get().getDescriptionId();
        }
    };

    public static final RegistryObject<FreezerControllerBlock> FREEZER_CONTROLLER = BLOCKS.register("freezer_controller",
            () -> new FreezerControllerBlock(
                    BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                            .requiresCorrectToolForDrops()
                            .strength(5.0F, 9.0F)
                            .lightLevel(state -> state.getValue(FreezerControllerBlock.ACTIVE) ? 8 : 0)));

    public static final mekanism.api.providers.IBlockProvider FREEZER_CONTROLLER_PROVIDER = new mekanism.api.providers.IBlockProvider() {
        @Override
        public Block getBlock() {
            return FREEZER_CONTROLLER.get();
        }

        @Override
        public net.minecraft.world.item.Item asItem() {
            return FREEZER_CONTROLLER.get().asItem();
        }

        @Override
        public net.minecraft.resources.ResourceLocation getRegistryName() {
            return FREEZER_CONTROLLER.getId();
        }

        @Override
        public String getTranslationKey() {
            return FREEZER_CONTROLLER.get().getDescriptionId();
        }
    };

    public static final RegistryObject<AirCompressorBlock> AIR_COMPRESSOR = BLOCKS.register("air_compressor",
            () -> new AirCompressorBlock(
                    BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                            .requiresCorrectToolForDrops()
                            .strength(3.5F, 16.0F)
                            .lightLevel(state -> state.getValue(AirCompressorBlock.ACTIVE) ? 8 : 0)));

    public static final mekanism.api.providers.IBlockProvider AIR_COMPRESSOR_PROVIDER = new mekanism.api.providers.IBlockProvider() {
        @Override
        public Block getBlock() {
            return AIR_COMPRESSOR.get();
        }

        @Override
        public net.minecraft.world.item.Item asItem() {
            return AIR_COMPRESSOR.get().asItem();
        }

        @Override
        public net.minecraft.resources.ResourceLocation getRegistryName() {
            return AIR_COMPRESSOR.getId();
        }

        @Override
        public String getTranslationKey() {
            return AIR_COMPRESSOR.get().getDescriptionId();
        }
    };

    private MCIBlocks() {}
}
