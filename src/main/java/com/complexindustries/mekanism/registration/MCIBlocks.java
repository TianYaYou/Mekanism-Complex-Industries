package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.content.extractor.TileEntityCrudeOilExtractor;
import com.complexindustries.mekanism.content.fluid.CrudeOilBlock;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.item.block.machine.ItemBlockMachine;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class MCIBlocks {
    public static final BlockDeferredRegister BLOCKS =
            new BlockDeferredRegister(MCIConstants.MODID);

    public static final DeferredRegister<Block> FLUID_BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MCIConstants.MODID);

    public static final BlockRegistryObject<Block, BlockItem> COMPLEX_CASING = BLOCKS.register("complex_casing",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(5.0F, 12.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()));

    public static final BlockRegistryObject<BlockTile<TileEntityCrudeOilExtractor, BlockTypeTile<TileEntityCrudeOilExtractor>>, ItemBlockMachine> CRUDE_OIL_EXTRACTOR =
            BLOCKS.register("crude_oil_extractor",
                    () -> new BlockTile<>(MCIBlockTypes.CRUDE_OIL_EXTRACTOR, BlockBehaviour.Properties.of()
                            .strength(3.5F, 8.0F)
                            .requiresCorrectToolForDrops()),
                    ItemBlockMachine::new);

    public static final RegistryObject<CrudeOilBlock> CRUDE_OIL_BLOCK = FLUID_BLOCKS.register("crude_oil",
            () -> new CrudeOilBlock(MCIFluids.CRUDE_OIL_SOURCE, BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable()));

    private MCIBlocks() {}
}
