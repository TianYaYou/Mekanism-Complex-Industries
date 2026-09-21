package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.MCIConstants;
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

    private MCIBlocks() {}
}
