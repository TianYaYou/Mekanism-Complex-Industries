package com.complexindustries.mekanism.test;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.content.freezer.TileEntityFreezerController;
import com.complexindustries.mekanism.content.refinery.RefineryMultiblockData;
import com.complexindustries.mekanism.content.refinery.RefineryValidator;
import com.complexindustries.mekanism.content.refinery.TileEntityRefineryCasing;
import com.complexindustries.mekanism.content.refinery.TileEntityRefineryController;
import com.complexindustries.mekanism.content.refinery.TileEntityRefineryDredgePipe;
import com.complexindustries.mekanism.content.refinery.TileEntityRefineryValve;
import com.complexindustries.mekanism.content.refinery.TileEntityRefineryValve.ValveMode;
import com.complexindustries.mekanism.content.tile.TileEntityResistiveCooler;
import com.complexindustries.mekanism.registration.MCIBlocks;
import com.complexindustries.mekanism.registration.MCIFluids;
import com.complexindustries.mekanism.registration.MCIItems;
import com.complexindustries.mekanism.registration.MCIChemicals;
import com.complexindustries.mekanism.content.chamber.TileEntityCrystalGrowthChamber;
import com.complexindustries.mekanism.content.chamber.ContainerCrystalGrowthChamber;
import net.minecraft.world.item.Rarity;
import mekanism.common.registries.MekanismChemicals;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.chemical.BasicChemicalTank;
import com.complexindustries.mekanism.content.slicer.TileEntitySiliconSlicer;
import com.complexindustries.mekanism.content.slicer.ContainerSiliconSlicer;
import com.complexindustries.mekanism.content.glass.TileEntityFilteredGlass;
import com.complexindustries.mekanism.content.block.FilteredGlassBlock;
import com.complexindustries.mekanism.content.lithography.TileEntityPhotolithographyMachine;
import com.complexindustries.mekanism.content.lithography.ContainerPhotolithographyMachine;
import com.complexindustries.mekanism.content.block.BlockPhotolithographyMachine;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import com.complexindustries.mekanism.content.refinery.RefineryMultiblockCache;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(MCIConstants.MODID)
@PrefixGameTestTemplate(false)
@EventBusSubscriber(modid = MCIConstants.MODID)
public class MCIGameTests {

    @SubscribeEvent
    public static void registerGameTests(RegisterGameTestsEvent event) {
        event.register(MCIGameTests.class);
    }

    @GameTest(template = "empty_10x10x10")
    public static void testDredgePipeBlock(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, MCIBlocks.REFINERY_DREDGE_PIPE.get());
        helper.assertBlockPresent(MCIBlocks.REFINERY_DREDGE_PIPE.get(), pos);
        helper.assertTrue(helper.getBlockEntity(pos) instanceof TileEntityRefineryDredgePipe, "疏通管道方块实体应为 TileEntityRefineryDredgePipe");
        helper.succeed();
    }

    @GameTest(template = "empty_10x10x10")
    public static void testResistiveCoolerAndCompressor(GameTestHelper helper) {
        BlockPos coolerPos = new BlockPos(1, 1, 1);
        BlockPos compressorPos = new BlockPos(3, 1, 1);

        helper.setBlock(coolerPos, MCIBlocks.RESISTIVE_COOLER.get());
        helper.setBlock(compressorPos, MCIBlocks.AIR_COMPRESSOR.get());

        helper.assertBlockPresent(MCIBlocks.RESISTIVE_COOLER.get(), coolerPos);
        helper.assertBlockPresent(MCIBlocks.AIR_COMPRESSOR.get(), compressorPos);

        helper.succeed();
    }

    @GameTest(template = "empty_10x10x10")
    public static void testChemicalSolidifierAndBitumen(GameTestHelper helper) {
        BlockPos bitumenPos = new BlockPos(1, 1, 1);
        BlockPos solidifierPos = new BlockPos(3, 1, 1);

        helper.setBlock(bitumenPos, MCIBlocks.BITUMEN_BLOCK.get());
        helper.assertBlockPresent(MCIBlocks.BITUMEN_BLOCK.get(), bitumenPos);

        helper.setBlock(solidifierPos, MCIBlocks.CHEMICAL_SOLIDIFIER.get());
        helper.assertBlockPresent(MCIBlocks.CHEMICAL_SOLIDIFIER.get(), solidifierPos);

        com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifier solidifier =
                (com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifier) helper.getBlockEntity(solidifierPos);
        helper.assertTrue(solidifier != null, "化学品固化机方块实体应为 TileEntityChemicalSolidifier");

        // Verify muffling upgrade limit: at most 1!
        helper.assertTrue(solidifier.getSupportedUpgrade().contains(mekanism.api.Upgrade.MUFFLING), "化学品固化机应支持静音升级！");
        int added = solidifier.getUpgradeComponent().addUpgrades(mekanism.api.Upgrade.MUFFLING, 4);
        helper.assertTrue(added == 1, "静音升级应至多安装 1 枚！实际安装: " + added);
        helper.assertTrue(solidifier.getUpgradeComponent().getUpgrades(mekanism.api.Upgrade.MUFFLING) == 1, "已安装静音升级数应为 1！");

        // Verify speed upgrade can take up to 8
        int addedSpeed = solidifier.getUpgradeComponent().addUpgrades(mekanism.api.Upgrade.SPEED, 8);
        helper.assertTrue(addedSpeed == 8, "速度升级应能安装 8 枚！实际安装: " + addedSpeed);

        // Verify security support
        helper.assertTrue(solidifier.hasSecurity(), "化学品固化机应具有安全属性（所有者与安全管理组件）");
        helper.assertTrue(solidifier.getSecurity() != null, "安全组件不应为空");

        // Verify slot positions for GUI alignment
        helper.assertTrue(solidifier.getEnergySlotX() == 144, "能量槽 X 坐标应为 144 (对齐于 y=35)");

        // Verify block items implement Mekanism-standard tooltip description
        helper.assertTrue(com.complexindustries.mekanism.registration.MCIItems.CHEMICAL_SOLIDIFIER.get() instanceof com.complexindustries.mekanism.content.block.MCIBlockItem,
                "化学品固化机物品应为 MCIBlockItem");
        helper.assertTrue(com.complexindustries.mekanism.registration.MCIItems.BITUMEN_BLOCK.get() instanceof com.complexindustries.mekanism.content.block.MCIBlockItem,
                "沥青块物品应为 MCIBlockItem");
        com.complexindustries.mekanism.content.block.MCIBlockItem solidifierItem =
                (com.complexindustries.mekanism.content.block.MCIBlockItem) com.complexindustries.mekanism.registration.MCIItems.CHEMICAL_SOLIDIFIER.get();
        helper.assertTrue(solidifierItem.getBlockDescription() != null, "化学品固化机描述条目不应为空");
        helper.assertTrue(solidifierItem.getBlockDescription().getTranslationKey().equals("description.mekanism_complex_industries.chemical_solidifier"),
                "化学品固化机描述键应正确匹配");

        helper.succeed();
    }

    @GameTest(template = "empty_10x10x10")
    public static void testDyedBitumenBlocks(GameTestHelper helper) {
        int idx = 0;
        for (net.minecraft.world.item.DyeColor color : net.minecraft.world.item.DyeColor.values()) {
            int x = (idx % 4) + 1;
            int z = (idx / 4) + 1;
            BlockPos pos = new BlockPos(x, 1, z);
            net.minecraft.world.level.block.Block block = MCIBlocks.DYED_BITUMEN_BLOCKS.get(color).get();
            helper.setBlock(pos, block);
            helper.assertBlockPresent(block, pos);

            net.minecraft.world.item.Item item = com.complexindustries.mekanism.registration.MCIItems.DYED_BITUMEN_BLOCKS.get(color).get();
            helper.assertTrue(item instanceof com.complexindustries.mekanism.content.block.MCIBlockItem, "染色沥青块物品应为 MCIBlockItem: " + color.getName());
            com.complexindustries.mekanism.content.block.MCIBlockItem blockItem = (com.complexindustries.mekanism.content.block.MCIBlockItem) item;
            helper.assertTrue(blockItem.getBlockDescription() != null, "染色沥青块描述不应为空");
            idx++;
        }
        helper.succeed();
    }

    @GameTest(template = "empty_10x10x10")
    public static void testBitumenStairsAndSlabs(GameTestHelper helper) {
        // 1. Test Plain Bitumen Stairs
        BlockPos stairsPos = new BlockPos(1, 1, 1);
        net.minecraft.world.level.block.state.BlockState stairsState = MCIBlocks.BITUMEN_STAIRS.get().defaultBlockState()
                .setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.NORTH)
                .setValue(net.minecraft.world.level.block.StairBlock.HALF, net.minecraft.world.level.block.state.properties.Half.BOTTOM);
        helper.setBlock(stairsPos, stairsState);
        helper.assertBlockPresent(MCIBlocks.BITUMEN_STAIRS.get(), stairsPos);
        helper.assertTrue(helper.getBlockState(stairsPos).getValue(net.minecraft.world.level.block.StairBlock.FACING) == Direction.NORTH, "楼梯朝向应为 NORTH");

        // 2. Test Plain Bitumen Slab & Double Slab
        BlockPos slabPos = new BlockPos(2, 1, 1);
        net.minecraft.world.level.block.state.BlockState slabState = MCIBlocks.BITUMEN_SLAB.get().defaultBlockState()
                .setValue(net.minecraft.world.level.block.SlabBlock.TYPE, net.minecraft.world.level.block.state.properties.SlabType.BOTTOM);
        helper.setBlock(slabPos, slabState);
        helper.assertBlockPresent(MCIBlocks.BITUMEN_SLAB.get(), slabPos);
        helper.assertTrue(helper.getBlockState(slabPos).getValue(net.minecraft.world.level.block.SlabBlock.TYPE) == net.minecraft.world.level.block.state.properties.SlabType.BOTTOM, "半砖类型应为 BOTTOM");

        // Double slab
        helper.setBlock(slabPos, slabState.setValue(net.minecraft.world.level.block.SlabBlock.TYPE, net.minecraft.world.level.block.state.properties.SlabType.DOUBLE));
        helper.assertTrue(helper.getBlockState(slabPos).getValue(net.minecraft.world.level.block.SlabBlock.TYPE) == net.minecraft.world.level.block.state.properties.SlabType.DOUBLE, "双层半砖类型应为 DOUBLE");

        // 3. Test Dyed Bitumen Stairs & Slabs for all 16 colors
        for (net.minecraft.world.item.DyeColor color : net.minecraft.world.item.DyeColor.values()) {
            net.minecraft.world.level.block.Block dyedStairs = MCIBlocks.DYED_BITUMEN_STAIRS.get(color).get();
            net.minecraft.world.level.block.Block dyedSlab = MCIBlocks.DYED_BITUMEN_SLABS.get(color).get();
            helper.assertTrue(dyedStairs instanceof com.complexindustries.mekanism.content.block.decorative.DyedBitumenStairsBlock, "应为 DyedBitumenStairsBlock: " + color.getName());
            helper.assertTrue(dyedSlab instanceof com.complexindustries.mekanism.content.block.decorative.DyedBitumenSlabBlock, "应为 DyedBitumenSlabBlock: " + color.getName());

            net.minecraft.world.item.Item stairsItem = com.complexindustries.mekanism.registration.MCIItems.DYED_BITUMEN_STAIRS.get(color).get();
            net.minecraft.world.item.Item slabItem = com.complexindustries.mekanism.registration.MCIItems.DYED_BITUMEN_SLABS.get(color).get();
            helper.assertTrue(stairsItem instanceof com.complexindustries.mekanism.content.block.MCIBlockItem, "染色楼梯物品应为 MCIBlockItem: " + color.getName());
            helper.assertTrue(slabItem instanceof com.complexindustries.mekanism.content.block.MCIBlockItem, "染色半砖物品应为 MCIBlockItem: " + color.getName());
        }

        helper.succeed();
    }

    @GameTest(template = "empty_10x10x10")
    public static void testChemicalSolidifierFactoryAndTierUpgrade(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, MCIBlocks.CHEMICAL_SOLIDIFIER.get().defaultBlockState().setValue(com.complexindustries.mekanism.content.block.ChemicalSolidifierBlock.FACING, Direction.WEST));
        helper.assertBlockPresent(MCIBlocks.CHEMICAL_SOLIDIFIER.get(), pos);
        helper.assertTrue(helper.getBlockState(pos).getValue(com.complexindustries.mekanism.content.block.ChemicalSolidifierBlock.FACING) == Direction.WEST, "初始固化机朝向应为 WEST");

        com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifier solidifier =
                (com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifier) helper.getBlockEntity(pos);
        helper.assertTrue(solidifier != null, "方块实体应为 TileEntityChemicalSolidifier");

        // Insert chemical & upgrades into solidifier before upgrade
        solidifier.inputTank.setStack(com.complexindustries.mekanism.registration.MCIChemicals.BITUMEN.asStack(4000));
        solidifier.getUpgradeComponent().addUpgrades(mekanism.api.Upgrade.MUFFLING, 1);
        solidifier.getUpgradeComponent().addUpgrades(mekanism.api.Upgrade.SPEED, 4);

        Player fakePlayer = FakePlayerFactory.getMinecraft(helper.getLevel());

        // 1. Upgrade with Basic Tier Installer -> Basic Factory (3 processes)
        BlockPos absolutePos = helper.absolutePos(pos);
        net.minecraft.world.item.ItemStack basicInstaller = mekanism.common.registries.MekanismItems.BASIC_TIER_INSTALLER.asStack();
        fakePlayer.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, basicInstaller);
        net.minecraft.world.phys.BlockHitResult hit = new net.minecraft.world.phys.BlockHitResult(
                new net.minecraft.world.phys.Vec3(absolutePos.getX() + 0.5, absolutePos.getY() + 0.5, absolutePos.getZ() + 0.5),
                Direction.UP, absolutePos, false);
        net.minecraft.world.item.context.UseOnContext ctx = new net.minecraft.world.item.context.UseOnContext(fakePlayer, net.minecraft.world.InteractionHand.MAIN_HAND, hit);
        net.minecraft.world.InteractionResult res = basicInstaller.getItem().useOn(ctx);
        helper.assertTrue(res == net.minecraft.world.InteractionResult.CONSUME, "初级安装器使用结果应为 CONSUME");
        helper.assertBlockPresent(MCIBlocks.BASIC_CHEMICAL_SOLIDIFIER_FACTORY.get(), pos);
        helper.assertTrue(helper.getBlockState(pos).getValue(com.complexindustries.mekanism.content.block.ChemicalSolidifierFactoryBlock.FACING) == Direction.WEST, "初级工厂升级后朝向应严格保留为 WEST！");

        com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifierFactory basicFactory =
                (com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifierFactory) helper.getBlockEntity(pos);
        helper.assertTrue(basicFactory != null, "方块实体应升级为 TileEntityChemicalSolidifierFactory");
        helper.assertTrue(basicFactory.tier == mekanism.common.tier.FactoryTier.BASIC, "工厂层级应为 BASIC");
        helper.assertTrue(basicFactory.tier.processes == 3, "初级工厂流水线数应为 3");
        helper.assertTrue(basicFactory.hasGui(), "工厂方块实体必须具备 GUI 支持 (hasGui==true)！");
        com.complexindustries.mekanism.content.solidifier.ContainerChemicalSolidifierFactory basicContainer =
                new com.complexindustries.mekanism.content.solidifier.ContainerChemicalSolidifierFactory(1, fakePlayer.getInventory(), basicFactory);
        helper.assertTrue(basicContainer.stillValid(fakePlayer), "工厂容器对于玩家应持续有效 (stillValid==true)，不应秒退！");
        basicContainer.removed(fakePlayer);
        helper.assertTrue(basicFactory.chemicalTank.getStack().getAmount() == 4000, "升级后化学品储量应保留为 4000 mB");
        helper.assertTrue(basicFactory.getUpgradeComponent().getUpgrades(mekanism.api.Upgrade.MUFFLING) == 1, "升级后静音升级数应保留为 1");
        helper.assertTrue(basicFactory.getUpgradeComponent().getUpgrades(mekanism.api.Upgrade.SPEED) == 4, "升级后速度升级数应保留为 4");

        // 2. Upgrade to Advanced Factory (5 processes)
        net.minecraft.world.item.ItemStack advInstaller = mekanism.common.registries.MekanismItems.ADVANCED_TIER_INSTALLER.asStack();
        fakePlayer.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, advInstaller);
        ctx = new net.minecraft.world.item.context.UseOnContext(fakePlayer, net.minecraft.world.InteractionHand.MAIN_HAND, hit);
        res = advInstaller.getItem().useOn(ctx);
        helper.assertTrue(res == net.minecraft.world.InteractionResult.CONSUME, "高级安装器使用结果应为 CONSUME");
        helper.assertBlockPresent(MCIBlocks.ADVANCED_CHEMICAL_SOLIDIFIER_FACTORY.get(), pos);
        helper.assertTrue(helper.getBlockState(pos).getValue(com.complexindustries.mekanism.content.block.ChemicalSolidifierFactoryBlock.FACING) == Direction.WEST, "高级工厂升级后朝向应严格保留为 WEST！");

        com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifierFactory advFactory =
                (com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifierFactory) helper.getBlockEntity(pos);
        helper.assertTrue(advFactory.tier == mekanism.common.tier.FactoryTier.ADVANCED, "工厂层级应为 ADVANCED");
        helper.assertTrue(advFactory.tier.processes == 5, "高级工厂流水线数应为 5");
        helper.assertTrue(advFactory.chemicalTank.getStack().getAmount() == 4000, "高级工厂化学品储量应保留");

        // 3. Upgrade to Elite Factory (7 processes)
        net.minecraft.world.item.ItemStack eliteInstaller = mekanism.common.registries.MekanismItems.ELITE_TIER_INSTALLER.asStack();
        fakePlayer.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, eliteInstaller);
        ctx = new net.minecraft.world.item.context.UseOnContext(fakePlayer, net.minecraft.world.InteractionHand.MAIN_HAND, hit);
        res = eliteInstaller.getItem().useOn(ctx);
        helper.assertTrue(res == net.minecraft.world.InteractionResult.CONSUME, "精英安装器使用结果应为 CONSUME");
        helper.assertBlockPresent(MCIBlocks.ELITE_CHEMICAL_SOLIDIFIER_FACTORY.get(), pos);
        helper.assertTrue(helper.getBlockState(pos).getValue(com.complexindustries.mekanism.content.block.ChemicalSolidifierFactoryBlock.FACING) == Direction.WEST, "精英工厂升级后朝向应严格保留为 WEST！");

        // 4. Upgrade to Ultimate Factory (9 processes)
        net.minecraft.world.item.ItemStack ultInstaller = mekanism.common.registries.MekanismItems.ULTIMATE_TIER_INSTALLER.asStack();
        fakePlayer.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ultInstaller);
        ctx = new net.minecraft.world.item.context.UseOnContext(fakePlayer, net.minecraft.world.InteractionHand.MAIN_HAND, hit);
        res = ultInstaller.getItem().useOn(ctx);
        helper.assertTrue(res == net.minecraft.world.InteractionResult.CONSUME, "终极安装器使用结果应为 CONSUME");
        helper.assertBlockPresent(MCIBlocks.ULTIMATE_CHEMICAL_SOLIDIFIER_FACTORY.get(), pos);
        helper.assertTrue(helper.getBlockState(pos).getValue(com.complexindustries.mekanism.content.block.ChemicalSolidifierFactoryBlock.FACING) == Direction.WEST, "终极工厂升级后朝向应严格保留为 WEST！");

        com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifierFactory ultFactory =
                (com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifierFactory) helper.getBlockEntity(pos);
        helper.assertTrue(ultFactory.tier == mekanism.common.tier.FactoryTier.ULTIMATE, "工厂层级应为 ULTIMATE");
        helper.assertTrue(ultFactory.tier.processes == 9, "终极工厂流水线数应为 9");
        helper.assertTrue(!ultFactory.canBeUpgraded(), "终极工厂不应再支持继续升级");

        helper.succeed();
    }

    @GameTest(template = "empty_10x10x10", timeoutTicks = 100)
    public static void testFreezerMultiblock(GameTestHelper helper) {
        // Build 4x4x4 Industrial Freezer cube
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = 0; z < 4; z++) {
                    boolean isWall = (x == 0 || x == 3 || y == 0 || y == 3 || z == 0 || z == 3);

                    BlockPos pos = new BlockPos(x + 1, y + 1, z + 1);
                    if (isWall) {
                        if (x == 1 && y == 1 && z == 0) {
                            helper.setBlock(pos, MCIBlocks.FREEZER_CONTROLLER.get());
                        } else if (x == 2 && y == 1 && z == 0) {
                            helper.setBlock(pos, MCIBlocks.FREEZER_VALVE.get());
                        } else {
                            helper.setBlock(pos, MCIBlocks.FREEZER_CASING.get());
                        }
                    } else {
                        helper.setBlock(pos, Blocks.AIR);
                    }
                }
            }
        }

        BlockPos controllerPos = new BlockPos(2, 2, 1);
        helper.runAfterDelay(10, () -> {
            if (helper.getBlockEntity(controllerPos) instanceof TileEntityFreezerController controller) {
                controller.getStructure().tick(controller, true);
                var res = controller.getStructure().runUpdate(controller);
                helper.assertTrue(controller.getMultiblock().isFormed(), "工业冷机多方块结构应该成功成型！: " + (res != null && res.getResultText() != null ? res.getResultText().getString() : "未知原因"));
                helper.succeed();
            } else {
                helper.fail("未能找到冷机控制器方块实体！");
            }
        });
    }

    @GameTest(template = "empty_15x25x15", timeoutTicks = 100)
    public static void testRefineryMultiblockFormation(GameTestHelper helper) {
        int x0 = 2;
        int z0 = 2;
        int y0 = 1;
        int height = 16; // Minimum 16 blocks total height (5 layers of height 3)

        buildRefineryStructure(helper, x0, y0, z0, height, false);

        BlockPos controllerPos = new BlockPos(x0 + 3, y0 + 1, z0);
        BlockPos valvePos = new BlockPos(x0 + 3, y0 + 1, z0 + 6);

        helper.runAfterDelay(10, () -> {
            if (helper.getBlockEntity(controllerPos) instanceof TileEntityRefineryController controller) {
                controller.getStructure().tick(controller, true);
                var res = controller.getStructure().runUpdate(controller);
                helper.assertTrue(controller.getMultiblock().isFormed(), "工业炼化塔多方块结构应该成功成型！: " + (res != null && res.getResultText() != null ? res.getResultText().getString() : "未知原因"));
                helper.assertTrue(controller.getMultiblock().outputChemicalTanks.size() == 5, "工业炼化塔应该具有5个输出罐！");

                // Test valve mode switching on Layer 1
                if (helper.getBlockEntity(valvePos) instanceof TileEntityRefineryValve valve) {
                    Player fakePlayer = FakePlayerFactory.getMinecraft(helper.getLevel());

                    // Verify Configurable capability
                    var cap = helper.getLevel().getCapability(mekanism.common.capabilities.Capabilities.CONFIGURABLE, valvePos, null, valve, null);
                    helper.assertTrue(cap != null, "炼化塔接口必须能够通过 BlockCapability 暴露 Capabilities.CONFIGURABLE！");

                    valve.setMode(ValveMode.INPUT);
                    cap.onRightClick(fakePlayer);
                    helper.assertTrue(valve.getMode() == ValveMode.OUTPUT, "第1层接口切换后模式应为 OUTPUT");
                    helper.assertTrue(helper.getBlockState(valvePos).getValue(com.complexindustries.mekanism.content.block.RefineryValveBlock.MODE) == ValveMode.OUTPUT, "接口方块状态 MODE 应同步为 OUTPUT！");
                    cap.onRightClick(fakePlayer);
                    helper.assertTrue(valve.getMode() == ValveMode.HEAT_INPUT, "第1层接口再次切换后模式应为 HEAT_INPUT");
                    helper.assertTrue(helper.getBlockState(valvePos).getValue(com.complexindustries.mekanism.content.block.RefineryValveBlock.MODE) == ValveMode.HEAT_INPUT, "接口方块状态 MODE 应同步为 HEAT_INPUT！");
                    cap.onRightClick(fakePlayer);
                    helper.assertTrue(valve.getMode() == ValveMode.INPUT, "第1层接口第三次切换后应回到 INPUT");
                    helper.assertTrue(helper.getBlockState(valvePos).getValue(com.complexindustries.mekanism.content.block.RefineryValveBlock.MODE) == ValveMode.INPUT, "接口方块状态 MODE 应同步为 INPUT！");
                } else {
                    helper.fail("未能在尖端找到炼化塔接口方块实体！");
                }

                helper.succeed();
            } else {
                helper.fail("未能找到炼化塔控制器方块实体！");
            }
        });
    }

    @GameTest(template = "empty_15x25x15", timeoutTicks = 100)
    public static void testRefineryMultiblockDiagonalValveFails(GameTestHelper helper) {
        int x0 = 2;
        int z0 = 2;
        int y0 = 1;
        int height = 16;

        // Build with an invalid valve on the diagonal wall [W!]
        buildRefineryStructure(helper, x0, y0, z0, height, true);

        BlockPos controllerPos = new BlockPos(x0 + 3, y0 + 1, z0);

        helper.runAfterDelay(10, () -> {
            if (helper.getBlockEntity(controllerPos) instanceof TileEntityRefineryController controller) {
                controller.getStructure().tick(controller, true);
                var res = controller.getStructure().runUpdate(controller);
                helper.assertFalse(controller.getMultiblock().isFormed(), "斜边放置接口时，工业炼化塔绝对不应该成型！");
                helper.succeed();
            } else {
                helper.fail("未能找到控制器方块实体！");
            }
        });
    }

    @GameTest(template = "empty_15x25x15", timeoutTicks = 100)
    public static void testRefineryMultiblockGlassAtTipFails(GameTestHelper helper) {
        int x0 = 2;
        int z0 = 2;
        int y0 = 1;
        int height = 16;

        buildRefineryStructure(helper, x0, y0, z0, height, false);
        // Replace a tip block with structural glass
        BlockPos tipPos = new BlockPos(x0 + 3, y0 + 2, z0);
        helper.setBlock(tipPos, MekanismBlocks.STRUCTURAL_GLASS.get());

        BlockPos controllerPos = new BlockPos(x0 + 3, y0 + 1, z0);

        helper.runAfterDelay(10, () -> {
            if (helper.getBlockEntity(controllerPos) instanceof TileEntityRefineryController controller) {
                controller.getStructure().tick(controller, true);
                var res = controller.getStructure().runUpdate(controller);
                helper.assertFalse(controller.getMultiblock().isFormed(), "尖端放置结构玻璃时，工业炼化塔绝对不应该成型！");
                helper.succeed();
            } else {
                helper.fail("未能找到控制器方块实体！");
            }
        });
    }

    @GameTest(template = "empty_15x25x15", timeoutTicks = 100)
    public static void testRefineryMultiblockGlassAtPartitionFails(GameTestHelper helper) {
        int x0 = 2;
        int z0 = 2;
        int y0 = 1;
        int height = 16;

        buildRefineryStructure(helper, x0, y0, z0, height, false);
        // Replace a partition floor diagonal perimeter block with structural glass (dy = 3, partition floor 1)
        BlockPos glassPos = new BlockPos(x0 + 2, y0 + 3, z0 + 1);
        helper.setBlock(glassPos, MekanismBlocks.STRUCTURAL_GLASS.get());

        BlockPos controllerPos = new BlockPos(x0 + 3, y0 + 1, z0);

        helper.runAfterDelay(10, () -> {
            if (helper.getBlockEntity(controllerPos) instanceof TileEntityRefineryController controller) {
                controller.getStructure().tick(controller, true);
                var res = controller.getStructure().runUpdate(controller);
                helper.assertFalse(controller.getMultiblock().isFormed(), "隔断层外壁放置结构玻璃时，工业炼化塔绝对不应该成型！");
                helper.succeed();
            } else {
                helper.fail("未能找到控制器方块实体！");
            }
        });
    }

    @GameTest(template = "empty_15x25x15", timeoutTicks = 120)
    public static void testRefineryCrackingReaction(GameTestHelper helper) {
        int x0 = 2;
        int z0 = 2;
        int y0 = 1;
        int height = 16;

        buildRefineryStructure(helper, x0, y0, z0, height, false);

        BlockPos controllerPos = new BlockPos(x0 + 3, y0 + 1, z0);

        helper.runAfterDelay(10, () -> {
            if (helper.getBlockEntity(controllerPos) instanceof TileEntityRefineryController controller) {
                controller.getStructure().tick(controller, true);
                controller.getStructure().runUpdate(controller);
                helper.assertTrue(controller.getMultiblock().isFormed(), "工业炼化塔多方块结构应该成型！");

                var mb = controller.getMultiblock();
                // 1. Supply Dense Crude Oil
                mb.getInputChemicalTank().setStack(com.complexindustries.mekanism.registration.MCIChemicals.DENSE_CRUDE_OIL.asStack(10_000));

                // 2. Set Bottom Temp to 700 K, Top Temp to 300 K (deltaT = 400 K >= 100 K)
                mb.getBottomHeatCapacitor().setHeat(700.0 * mb.getBottomHeatCapacitor().getHeatCapacity());
                mb.getTopHeatCapacitor().setHeat(300.0 * mb.getTopHeatCapacitor().getHeatCapacity());

                // 3. Tick multiblock for 20 ticks (1 second of cracking)
                for (int i = 0; i < 20; i++) {
                    mb.tick(helper.getLevel());
                }

                // 4. Verify cracking occurred
                helper.assertTrue(mb.lastCrackingRate > 0.0, "裂解速率应该大于0！当前: " + mb.lastCrackingRate);
                helper.assertTrue(mb.operatingStatus == 3, "运行状态应该为活跃裂解 (3)！当前: " + mb.operatingStatus);

                long bitumen = mb.outputTank1.getStored();
                long heavyOil = mb.outputTank2.getStored();
                long refinedFuel = mb.outputTank3.getStored();
                long naphtha = mb.outputTank4.getStored();
                long gas = mb.outputTank5.getStored();

                helper.assertTrue(gas > 0, "石油气产出应该大于0！当前: " + gas);
                helper.assertTrue(naphtha > 0, "石脑油产出应该大于0！当前: " + naphtha);
                helper.assertTrue(refinedFuel > 0, "精炼燃油产出应该大于0！当前: " + refinedFuel);
                helper.assertTrue(heavyOil > 0, "重油产出应该大于0！当前: " + heavyOil);
                helper.assertTrue(bitumen > 0, "沥青产出应该大于0！当前: " + bitumen);

                // Verify relative ratios: gas (2.0) > naphtha (0.2) > bitumen (0.15) > refinedFuel (0.1) == heavyOil (0.1)
                helper.assertTrue(gas > naphtha, "石油气产出应显著高于石脑油！");
                helper.assertTrue(naphtha > bitumen, "石脑油产出应高于沥青！");
                helper.assertTrue(bitumen > refinedFuel, "沥青产出应高于精炼燃油！");
                helper.assertTrue(refinedFuel == heavyOil, "精炼燃油产出与重油产出比例应一致！");

                // Verify scaled tank capacities (Input = 1/100 of old, Output = 1/10 of input / 1/1000 of old)
                helper.assertTrue(mb.getInputTankCapacity() == Math.max(640, mb.getVolume() * 160), "炼化塔输入容积应为原先的1/100！");
                helper.assertTrue(mb.getOutputTankCapacity() == Math.max(64, mb.getVolume() * 16), "炼化塔产物容积应额外再缩小1/10！");
                helper.assertTrue(mb.inputChemicalTank.getCapacity() == mb.getInputTankCapacity(), "输入槽容量应与 getInputTankCapacity 一致！");
                helper.assertTrue(mb.outputTank1.getCapacity() == mb.getOutputTankCapacity(), "产物槽容量应与 getOutputTankCapacity 一致！");

                helper.succeed();
            } else {
                helper.fail("未能找到控制器方块实体！");
            }
        });
    }

    @GameTest(template = "empty_15x25x15", timeoutTicks = 120)
    public static void testRefineryHeatInputClampedToSourceTemp(GameTestHelper helper) {
        int x0 = 2;
        int z0 = 2;
        int y0 = 1;
        int height = 16;

        buildRefineryStructure(helper, x0, y0, z0, height, false);

        BlockPos controllerPos = new BlockPos(x0 + 3, y0 + 1, z0);
        BlockPos valvePos = new BlockPos(x0 + 3, y0 + 1, z0 + 6);

        helper.runAfterDelay(10, () -> {
            if (helper.getBlockEntity(controllerPos) instanceof TileEntityRefineryController controller) {
                controller.getStructure().tick(controller, true);
                controller.getStructure().runUpdate(controller);
                helper.assertTrue(controller.getMultiblock().isFormed(), "工业炼化塔应该成型！");

                if (helper.getBlockEntity(valvePos) instanceof TileEntityRefineryValve valve) {
                    valve.setMode(ValveMode.HEAT_INPUT);
                    var heatHandler = helper.getLevel().getCapability(mekanism.common.capabilities.Capabilities.HEAT, valvePos, null, valve, null);
                    helper.assertTrue(heatHandler != null, "接口在热量输入模式下应该暴露热量能力！");

                    var mb = controller.getMultiblock();
                    // Verify heat capacity was scaled onCreated
                    helper.assertTrue(mb.getBottomHeatCapacitor().getHeatCapacity() >= 10_000.0, "炼化塔底部热容应该正确成型初始化！");

                    // Test legacy recovery clamp
                    mb.getBottomHeatCapacitor().setHeat(50_000.0 * mb.getBottomHeatCapacitor().getHeatCapacity());
                    mb.tick(helper.getLevel());
                    helper.assertTrue(mb.getBottomHeatCapacitor().getTemperature() <= 2500.0, "异常高热应该被自动限制到合理区间！");
                }
                helper.succeed();
            } else {
                helper.fail("未能找到控制器方块实体！");
            }
        });
    }

    @GameTest(template = "empty_15x25x15", timeoutTicks = 100)
    public static void testRefineryMobSpawnSuppression(GameTestHelper helper) {
        int x0 = 2;
        int z0 = 2;
        int y0 = 1;
        int height = 16;

        buildRefineryStructure(helper, x0, y0, z0, height, false);

        BlockPos controllerPos = new BlockPos(x0 + 3, y0 + 1, z0);

        helper.runAfterDelay(10, () -> {
            if (helper.getBlockEntity(controllerPos) instanceof TileEntityRefineryController controller) {
                controller.getStructure().tick(controller, true);
                controller.getStructure().runUpdate(controller);
                helper.assertTrue(controller.getMultiblock().isFormed(), "工业炼化塔应该成型！");

                var mb = controller.getMultiblock();
                BlockPos absoluteInsidePos = helper.absolutePos(new BlockPos(x0 + 3, y0 + 2, z0 + 3)); // hollow interior
                BlockPos absoluteNearPos = helper.absolutePos(new BlockPos(x0 - 2, y0 + 1, z0 + 3));   // 2 blocks outside tower
                BlockPos absoluteFarPos = helper.absolutePos(new BlockPos(x0 + 3, y0 + 40, z0 + 3));  // 40 blocks up, far outside any vertical/horizontal radius

                // 1. Inside check: ALL mobs suppressed
                helper.assertTrue(mb.isInsideTower(absoluteInsidePos), "内部腔体坐标应被判定为在塔内！");
                helper.assertTrue(RefineryMultiblockData.shouldSuppressSpawn(helper.getLevel(), absoluteInsidePos, MobCategory.MONSTER, Zombie.class, MobSpawnType.NATURAL), "塔内应阻止怪物自然生成！");
                helper.assertTrue(RefineryMultiblockData.shouldSuppressSpawn(helper.getLevel(), absoluteInsidePos, MobCategory.CREATURE, Cow.class, MobSpawnType.NATURAL), "塔内应阻止生物自然生成！");

                // 2. Near check: Hostile suppressed, Passive allowed
                helper.assertTrue(!mb.isInsideTower(absoluteNearPos), "外部坐标不应被判定为在塔内！");
                helper.assertTrue(mb.isNearTower(absoluteNearPos, 16, 8), "近距离外部坐标应在16格保护半径内！");
                helper.assertTrue(RefineryMultiblockData.shouldSuppressSpawn(helper.getLevel(), absoluteNearPos, MobCategory.MONSTER, Zombie.class, MobSpawnType.NATURAL), "塔周围应阻止怪物生成！");
                helper.assertTrue(!RefineryMultiblockData.shouldSuppressSpawn(helper.getLevel(), absoluteNearPos, MobCategory.CREATURE, Cow.class, MobSpawnType.NATURAL), "塔周围应允许和平动物生成！");

                // 3. Spawn egg / Command bypass check
                helper.assertTrue(!RefineryMultiblockData.shouldSuppressSpawn(helper.getLevel(), absoluteInsidePos, MobCategory.MONSTER, Zombie.class, MobSpawnType.SPAWN_EGG), "刷怪蛋不应被阻止！");
                helper.assertTrue(!RefineryMultiblockData.shouldSuppressSpawn(helper.getLevel(), absoluteInsidePos, MobCategory.MONSTER, Zombie.class, MobSpawnType.COMMAND), "指令生成不应被阻止！");

                // 4. Far check: Hostile allowed
                helper.assertTrue(!mb.isNearTower(absoluteFarPos, 16, 8), "远距离坐标不应在保护半径内！");
                helper.assertTrue(!RefineryMultiblockData.shouldSuppressSpawn(helper.getLevel(), absoluteFarPos, MobCategory.MONSTER, Zombie.class, MobSpawnType.NATURAL), "远距离应允许怪物生成！");

                helper.succeed();
            } else {
                helper.fail("未能找到控制器方块实体！");
            }
        });
    }

    @GameTest(template = "empty_15x25x15", timeoutTicks = 100)
    public static void testCoolerCoolsRefineryTopValve(GameTestHelper helper) {
        int x0 = 2;
        int z0 = 2;
        int y0 = 1;
        int height = 16;

        buildRefineryStructure(helper, x0, y0, z0, height, false);

        BlockPos controllerPos = new BlockPos(x0 + 3, y0 + 1, z0);
        // Tip at layer 5 (dy = 13, dx = 3, dz = 6): replace casing with valve
        BlockPos topValvePos = new BlockPos(x0 + 3, y0 + 13, z0 + 6);
        helper.setBlock(topValvePos, MCIBlocks.REFINERY_VALVE.get());

        // Adjacent cooler pos (South of the valve): dx = 3, dz = 7, dy = 13
        BlockPos coolerPos = new BlockPos(x0 + 3, y0 + 13, z0 + 7);
        helper.setBlock(coolerPos, MCIBlocks.RESISTIVE_COOLER.get());

        helper.runAfterDelay(10, () -> {
            if (helper.getBlockEntity(controllerPos) instanceof TileEntityRefineryController controller) {
                controller.getStructure().tick(controller, true);
                controller.getStructure().runUpdate(controller);
                helper.assertTrue(controller.getMultiblock().isFormed(), "工业炼化塔应该成型！");

                TileEntityRefineryValve valve = (TileEntityRefineryValve) helper.getBlockEntity(topValvePos);
                helper.assertTrue(valve != null, "未能找到顶层接口！");
                helper.assertTrue(valve.getEffectiveLayer() == 5, "顶层接口层数应为 5！实际: " + valve.getEffectiveLayer());

                // Set valve mode to COOLING_INPUT
                valve.setMode(TileEntityRefineryValve.ValveMode.COOLING_INPUT);
                helper.assertTrue(valve.getMode() == TileEntityRefineryValve.ValveMode.COOLING_INPUT, "接口模式应为 COOLING_INPUT！");

                TileEntityResistiveCooler cooler = (TileEntityResistiveCooler) helper.getBlockEntity(coolerPos);
                helper.assertTrue(cooler != null, "未能找到制冷器！");

                // Inject energy into cooler
                cooler.getEnergyContainer().insert(500_000L, Action.EXECUTE, AutomationType.INTERNAL);
                cooler.setEnergyUsage(5_000L); // 2,000 FE/t

                var mb = controller.getMultiblock();
                // Bottom is heated to 900 K (cracking operational temperature)
                mb.getBottomHeatCapacitor().setHeat(900.0 * mb.getBottomHeatCapacitor().getHeatCapacity());
                // Top capacitor starts at 400 K
                mb.getTopHeatCapacitor().setHeat(400.0 * mb.getTopHeatCapacitor().getHeatCapacity());
                double initialTopTemp = mb.getTopHeatCapacitor().getTemperature();

                // Tick cooler and multiblock for 30 ticks
                for (int i = 0; i < 30; i++) {
                    TileEntityMekanism.tickServer(helper.getLevel(), coolerPos, helper.getBlockState(coolerPos), cooler);
                    mb.tick(helper.getLevel());
                }

                double finalTopTemp = mb.getTopHeatCapacitor().getTemperature();
                helper.assertTrue(finalTopTemp < initialTopTemp, "顶层温度应该在制冷器作用下降温（即使底层为900K加热态）！初始: " + initialTopTemp + ", 结束: " + finalTopTemp + ", 制冷器温度: " + cooler.getTotalTemperature());
                helper.assertTrue(cooler.getTotalTemperature() < 200.0, "制冷器冷端应达到超低温！实际: " + cooler.getTotalTemperature());

                helper.succeed();
            } else {
                helper.fail("未能找到控制器方块实体！");
            }
        });
    }

    @GameTest(template = "empty_15x25x15", timeoutTicks = 120)
    public static void testRefineryMultiblockDataPersistenceAndNoUnformLoop(GameTestHelper helper) {
        int x0 = 2;
        int z0 = 2;
        int y0 = 1;
        int height = 16;

        buildRefineryStructure(helper, x0, y0, z0, height, false);

        BlockPos controllerPos = new BlockPos(x0 + 3, y0 + 1, z0);
        BlockPos testCasingPos = new BlockPos(x0 + 3, y0, z0); // Base casing block under controller

        helper.runAfterDelay(10, () -> {
            if (helper.getBlockEntity(controllerPos) instanceof TileEntityRefineryController controller) {
                controller.getStructure().tick(controller, true);
                controller.getStructure().runUpdate(controller);
                helper.assertTrue(controller.getMultiblock().isFormed(), "工业炼化塔应该成型！");

                // 1. Verify controller active blockstate update does NOT trigger unforming loop
                TileEntityMekanism.tickServer(helper.getLevel(), controllerPos, helper.getBlockState(controllerPos), controller);
                BlockState stateAfterTick = helper.getBlockState(controllerPos);
                helper.assertTrue(stateAfterTick.getValue(com.complexindustries.mekanism.content.block.RefineryControllerBlock.ACTIVE), "控制器成型后方块状态ACTIVE应为true！");
                helper.assertTrue(controller.getMultiblock().isFormed(), "控制器状态更新不应导致多方块解体（杜绝循环解体/重构粒子问题）！");

                // 2. Put chemicals into input tanks and an output tank
                var mb = controller.getMultiblock();
                long inputAmount = 5_000L;
                long nitrogenAmount = 1_200L;
                long bitumenAmount = 800L;
                mb.getInputChemicalTank().setStack(com.complexindustries.mekanism.registration.MCIChemicals.DENSE_CRUDE_OIL.asStack(inputAmount));
                mb.getNitrogenChemicalTank().setStack(com.complexindustries.mekanism.registration.MCIChemicals.NITROGEN.asStack(nitrogenAmount));
                mb.outputTank1.setStack(com.complexindustries.mekanism.registration.MCIChemicals.BITUMEN.asStack(bitumenAmount));

                java.util.UUID cachedID = controller.getCacheID();
                helper.assertTrue(cachedID != null, "成型状态下控制器应持有有效的缓存ID！");

                // 3. Break one casing block to simulate disruption
                helper.setBlock(testCasingPos, Blocks.AIR);
                controller.getStructure().invalidate(helper.getLevel());
                helper.assertTrue(!controller.getMultiblock().isFormed(), "移除方块后炼化塔应处于未成型状态！");

                // 4. Verify the multiblock cache in REFINERY_MANAGER preserved all chemicals
                var manager = com.complexindustries.mekanism.content.refinery.MCIRefineryMultiblock.REFINERY_MANAGER;
                var cache = manager.getCache(cachedID);
                helper.assertTrue(cache != null, "REFINERY_MANAGER中应存在已保存的缓存！");
                var cachedTanks = cache.getChemicalTanks(null);
                helper.assertTrue(cachedTanks != null && cachedTanks.size() >= 7, "缓存应包含7个化学品槽位！");
                helper.assertTrue(cachedTanks.get(0).getStored() == inputAmount, "缓存中输入化学品数量不应丢失！期望: " + inputAmount + ", 实际: " + cachedTanks.get(0).getStored());
                helper.assertTrue(cachedTanks.get(1).getStored() == bitumenAmount, "缓存中沥青产物不应丢失！期望: " + bitumenAmount + ", 实际: " + cachedTanks.get(1).getStored());
                helper.assertTrue(cachedTanks.get(6).getStored() == nitrogenAmount, "缓存中氮气气氛数量不应丢失！期望: " + nitrogenAmount + ", 实际: " + cachedTanks.get(6).getStored());

                // 5. Replace the casing block and re-form
                helper.setBlock(testCasingPos, MCIBlocks.REFINERY_CASING.get());
                controller.getStructure().tick(controller, true);
                controller.getStructure().runUpdate(controller);
                helper.assertTrue(controller.getMultiblock().isFormed(), "修复方块后炼化塔应重新成型！");

                // 6. Verify multiblock chemical contents were completely restored (NOT swallowed!)
                var reformedMb = controller.getMultiblock();
                helper.assertTrue(reformedMb.getInputChemicalTank().getStored() == inputAmount, "重形成型后输入化学品不应被吞！实际: " + reformedMb.getInputChemicalTank().getStored());
                helper.assertTrue(reformedMb.getNitrogenChemicalTank().getStored() == nitrogenAmount, "重形成型后氮气气氛不应被吞！实际: " + reformedMb.getNitrogenChemicalTank().getStored());
                helper.assertTrue(reformedMb.outputTank1.getStored() == bitumenAmount, "重形成型后产物不应被吞！实际: " + reformedMb.outputTank1.getStored());

                helper.succeed();
            } else {
                helper.fail("未能找到控制器方块实体！");
            }
        });
    }

    private static void buildRefineryStructure(GameTestHelper helper, int x0, int y0, int z0, int height, boolean placeIllegalDiagonalValve) {
        int[] partitions = {3, 6, 9, 12};

        for (int dy = 0; dy < height; dy++) {
            boolean isBase = (dy == 0);
            boolean isTopRim = (dy == height - 1);
            boolean isPartition = false;
            for (int p : partitions) {
                if (dy == p) {
                    isPartition = true;
                    break;
                }
            }

            for (int dz = 0; dz < 7; dz++) {
                for (int dx = 0; dx < 7; dx++) {
                    int cellType = RefineryValidator.GRID_TEMPLATE[dz][dx];
                    BlockPos pos = new BlockPos(x0 + dx, y0 + dy, z0 + dz);

                    if (cellType == RefineryValidator.TYPE_IGNORED) {
                        helper.setBlock(pos, Blocks.AIR);
                        continue;
                    }

                    if (isBase) {
                        helper.setBlock(pos, MCIBlocks.REFINERY_CASING.get());
                    } else if (isTopRim) {
                        if (cellType == RefineryValidator.TYPE_TIP || cellType == RefineryValidator.TYPE_DIAGONAL) {
                            helper.setBlock(pos, MCIBlocks.REFINERY_CASING.get());
                        } else {
                            helper.setBlock(pos, Blocks.AIR);
                        }
                    } else if (isPartition) {
                        if (cellType == RefineryValidator.TYPE_CENTER) {
                            helper.setBlock(pos, MCIBlocks.REFINERY_DREDGE_PIPE.get());
                        } else if (cellType == RefineryValidator.TYPE_INNER || cellType == RefineryValidator.TYPE_TIP || cellType == RefineryValidator.TYPE_DIAGONAL) {
                            helper.setBlock(pos, MCIBlocks.REFINERY_CASING.get());
                        }
                    } else {
                        // Working cavity layer
                        if (cellType == RefineryValidator.TYPE_CENTER || cellType == RefineryValidator.TYPE_INNER) {
                            helper.setBlock(pos, Blocks.AIR);
                        } else if (cellType == RefineryValidator.TYPE_TIP) {
                            if (dx == 3 && dz == 0 && dy == 1) {
                                helper.setBlock(pos, MCIBlocks.REFINERY_CONTROLLER.get());
                            } else if (dx == 3 && dz == 6 && dy == 1) {
                                helper.setBlock(pos, MCIBlocks.REFINERY_VALVE.get());
                            } else {
                                helper.setBlock(pos, MCIBlocks.REFINERY_CASING.get());
                            }
                        } else if (cellType == RefineryValidator.TYPE_DIAGONAL) {
                            if (placeIllegalDiagonalValve && dx == 2 && dz == 1 && dy == 1) {
                                helper.setBlock(pos, MCIBlocks.REFINERY_VALVE.get()); // ILLEGAL VALVE
                            } else {
                                helper.setBlock(pos, MekanismBlocks.STRUCTURAL_GLASS.get());
                            }
                        }
                    }
                }
            }
        }
    }

    @GameTest(template = "empty_10x10x10")
    public static void testDenseCrudeOilRecipes(GameTestHelper helper) {
        var recipeManager = helper.getLevel().getRecipeManager();
        var rotaryRecipes = MekanismRecipeType.ROTARY.getRecipes(recipeManager);
        var reactionRecipes = MekanismRecipeType.REACTION.getRecipes(recipeManager);
        boolean foundRotary = rotaryRecipes.stream().anyMatch(r -> r.id().toString().contains("mekanism_complex_industries:rotary/crude_oil"));
        boolean foundReactionFluid = reactionRecipes.stream().anyMatch(r -> r.id().toString().contains("mekanism_complex_industries:reaction/dense_crude_oil_from_fluid"));
        boolean foundReactionSolid = reactionRecipes.stream().anyMatch(r -> r.id().toString().contains("mekanism_complex_industries:reaction/dense_crude_oil_from_solid"));
        helper.assertTrue(foundRotary, "未找到浓稠石油 Rotary 循环转化配方");
        helper.assertTrue(foundReactionFluid, "未找到浓稠石油 PRC 流体合成配方");
        helper.assertTrue(foundReactionSolid, "未找到浓稠石油 PRC 固态合成配方");
        helper.succeed();
    }

    @GameTest(template = "empty_15x25x15", timeoutTicks = 120)
    public static void testNitrogenDissipationAndYieldScaling(GameTestHelper helper) {
        int x0 = 2;
        int z0 = 2;
        int y0 = 1;
        int height = 16;

        buildRefineryStructure(helper, x0, y0, z0, height, false);
        BlockPos controllerPos = new BlockPos(x0 + 3, y0 + 1, z0);

        helper.runAfterDelay(10, () -> {
            if (helper.getBlockEntity(controllerPos) instanceof TileEntityRefineryController controller) {
                controller.getStructure().tick(controller, true);
                controller.getStructure().runUpdate(controller);
                helper.assertTrue(controller.getMultiblock().isFormed(), "工业炼化塔应该成型！");

                var mb = controller.getMultiblock();

                // 1. 0% nitrogen yield multiplier is exactly 0.20
                helper.assertTrue(Math.abs(mb.getYieldMultiplier() - 0.20) < 0.001, "无氮气时产率倍率应为 0.20！实际: " + mb.getYieldMultiplier());

                // 2. Insert 1000 mB Nitrogen
                mb.getNitrogenChemicalTank().setStack(com.complexindustries.mekanism.registration.MCIChemicals.NITROGEN.asStack(1000));
                long initialStored = mb.getNitrogenChemicalTank().getStored();
                helper.assertTrue(initialStored == 1000, "氮气存量应为 1000 mB！");

                // 3. Tick multiblock and verify dissipation (1/1000 per tick)
                mb.tick(helper.getLevel());
                long after1Tick = mb.getNitrogenChemicalTank().getStored();
                helper.assertTrue(after1Tick == 999, "1 tick 后氮气应自然逸散 1 mB (0.1%)！实际: " + after1Tick);

                // 4. Fill nitrogen to 100% capacity
                long cap = mb.getInputTankCapacityLong();
                mb.getNitrogenChemicalTank().setStack(com.complexindustries.mekanism.registration.MCIChemicals.NITROGEN.asStack(cap));
                helper.assertTrue(Math.abs(mb.getNitrogenRatio() - 1.0) < 0.001, "氮气注满时比例应为 100%！");
                helper.assertTrue(Math.abs(mb.getYieldMultiplier() - 1.20) < 0.001, "满氮气时产率倍率应为 1.20！实际: " + mb.getYieldMultiplier());

                // 5. Test 50% capacity
                mb.getNitrogenChemicalTank().setStack(com.complexindustries.mekanism.registration.MCIChemicals.NITROGEN.asStack(cap / 2));
                helper.assertTrue(Math.abs(mb.getYieldMultiplier() - 0.70) < 0.01, "50% 氮气时产率倍率应为 0.70！实际: " + mb.getYieldMultiplier());

                helper.succeed();
            } else {
                helper.fail("未能找到控制器方块实体！");
            }
        });
    }

    @GameTest(template = "empty_10x10x10")
    public static void testLegacy6TankCacheBackwardCompatibility(GameTestHelper helper) {
        // Simulate a legacy save with only 6 chemical tanks
        var cache = new RefineryMultiblockCache();
        var cacheTanks = cache.getChemicalTanks(null);
        for (int i = 0; i < 6; i++) {
            cacheTanks.add(BasicChemicalTank.createAllValid(Long.MAX_VALUE, cache));
        }
        helper.assertTrue(cacheTanks.size() == 6, "旧存档缓存应为 6 个化学品槽！");

        BlockPos controllerPos = new BlockPos(1, 1, 1);
        helper.setBlock(controllerPos, MCIBlocks.REFINERY_CONTROLLER.get());
        if (helper.getBlockEntity(controllerPos) instanceof TileEntityRefineryController controller) {
            var mb = controller.getMultiblock();
            // sync data into the 6-tank cache - MUST NOT THROW IndexOutOfBoundsException!
            cache.sync(mb);
            helper.assertTrue(cache.getChemicalTanks(null).size() == 7, "同步后缓存应自动安全扩展至 7 个化学品槽！");
            helper.succeed();
        } else {
            helper.fail("未能找到控制器方块实体！");
        }
    }

    @GameTest(template = "empty_10x10x10")
    public static void testFlowRegulatorBlockAndDyeing(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, MCIBlocks.FLOW_REGULATOR.get().defaultBlockState().setValue(com.complexindustries.mekanism.content.flowregulator.BlockFlowRegulator.FACING, Direction.NORTH));
        helper.assertBlockPresent(MCIBlocks.FLOW_REGULATOR.get(), pos);

        if (helper.getBlockEntity(pos) instanceof com.complexindustries.mekanism.content.flowregulator.TileEntityFlowRegulator regulator) {
            helper.assertTrue(regulator.getDirection() == Direction.NORTH, "限流阀朝向应为 NORTH");
            helper.assertTrue(regulator.getRingColor() == net.minecraft.world.item.DyeColor.WHITE, "限流阀圆环初始颜色应为白色");

            regulator.setRingColor(net.minecraft.world.item.DyeColor.RED);
            helper.assertTrue(regulator.getRingColor() == net.minecraft.world.item.DyeColor.RED, "限流阀染色后圆环颜色应为红色");
            helper.succeed();
        } else {
            helper.fail("未能找到限流阀方块实体！");
        }
    }

    @GameTest(template = "empty_10x10x10")
    public static void testFlowRegulatorRateLimitingAndDirectionality(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 2, 3);
        helper.setBlock(pos, MCIBlocks.FLOW_REGULATOR.get().defaultBlockState().setValue(com.complexindustries.mekanism.content.flowregulator.BlockFlowRegulator.FACING, Direction.NORTH));

        if (helper.getBlockEntity(pos) instanceof com.complexindustries.mekanism.content.flowregulator.TileEntityFlowRegulator regulator) {
            // Set limit to 200 mB/s (= 10 mB/t)
            regulator.setFlowRate(200L);
            helper.assertTrue(regulator.getFlowRate() == 200L, "流速限制应为 200 mB/s");

            var nitrogen = com.complexindustries.mekanism.registration.MCIChemicals.NITROGEN.asStack(500);

            // 1. Output face (NORTH) must REJECT insertion
            var insertOutput = regulator.insertChemical(nitrogen.copy(), Direction.NORTH, Action.EXECUTE);
            helper.assertTrue(insertOutput.getAmount() == 500L, "输出面应当拒绝化学品输入");

            // 2. Input face (SOUTH) must ACCEPT insertion
            var insertInput = regulator.insertChemical(nitrogen.copy(), Direction.SOUTH, Action.EXECUTE);
            helper.assertTrue(insertInput.isEmpty(), "输入面应当接收化学品输入");
            helper.assertTrue(regulator.bufferTank.getStored() == 500L, "内部缓冲槽应存入 500 mB");

            // 3. Input face (SOUTH) must REJECT extraction
            var extractInput = regulator.extractChemical(100L, Direction.SOUTH, Action.EXECUTE);
            helper.assertTrue(extractInput.isEmpty(), "输入面应当拒绝化学品抽取");

            // 4. Tick server to calculate budget (10 mB this tick)
            TileEntityMekanism.tickServer(helper.getLevel(), pos, regulator.getBlockState(), regulator);

            // 5. Output face (NORTH) extraction should be capped at 10 mB
            var extractOutput = regulator.extractChemical(100L, Direction.NORTH, Action.EXECUTE);
            helper.assertTrue(extractOutput.getAmount() == 10L, "单 tick 抽取应严格受限于 10 mB (200 mB/s)");
            helper.assertTrue(regulator.bufferTank.getStored() == 490L, "抽取后内部缓冲槽剩余 490 mB");

            // Further extraction in the same tick should be empty (budget exhausted)
            var extractMore = regulator.extractChemical(100L, Direction.NORTH, Action.EXECUTE);
            helper.assertTrue(extractMore.isEmpty(), "同 tick 预算用尽后应当无法继续抽取");

            helper.succeed();
        } else {
            helper.fail("未能找到限流阀方块实体！");
        }
    }

    @GameTest(template = "empty_10x10x10")
    public static void testFlowRegulatorRedstoneControl(GameTestHelper helper) {
        BlockPos pos = new BlockPos(4, 2, 4);
        helper.setBlock(pos, MCIBlocks.FLOW_REGULATOR.get().defaultBlockState().setValue(com.complexindustries.mekanism.content.flowregulator.BlockFlowRegulator.FACING, Direction.NORTH));

        if (helper.getBlockEntity(pos) instanceof com.complexindustries.mekanism.content.flowregulator.TileEntityFlowRegulator regulator) {
            regulator.setControlType(mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl.HIGH);
            helper.assertTrue(!regulator.canFunction(), "高电平模式且无红石信号时，限流阀应关闭！");

            var nitrogen = com.complexindustries.mekanism.registration.MCIChemicals.NITROGEN.asStack(200);
            var inserted = regulator.insertChemical(nitrogen.copy(), Direction.SOUTH, Action.EXECUTE);
            helper.assertTrue(inserted.getAmount() == 200L, "红石关闭时应拒绝化学品输入！");

            regulator.setControlType(mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl.DISABLED);
            helper.assertTrue(regulator.canFunction(), "禁用红石控制模式下，限流阀应处于开启工作状态！");
            var insertedEnabled = regulator.insertChemical(nitrogen.copy(), Direction.SOUTH, Action.EXECUTE);
            helper.assertTrue(insertedEnabled.isEmpty(), "开启状态下应正常接收化学品输入！");

            helper.succeed();
        } else {
            helper.fail("未能找到限流阀方块实体！");
        }
    }

    @GameTest(template = "empty_10x10x10")
    public static void testNewPetrochemicalsRegistered(GameTestHelper helper) {
        // Verify chemicals are registered and valid
        helper.assertTrue(com.complexindustries.mekanism.registration.MCIChemicals.PROPYLENE.get() != null, "丙烯 (Propylene) 应成功注册！");
        helper.assertTrue(com.complexindustries.mekanism.registration.MCIChemicals.BENZENE.get() != null, "苯 (Benzene) 应成功注册！");
        helper.assertTrue(com.complexindustries.mekanism.registration.MCIChemicals.STYRENE.get() != null, "苯乙烯 (Styrene) 应成功注册！");

        var propyleneStack = com.complexindustries.mekanism.registration.MCIChemicals.PROPYLENE.asStack(100);
        helper.assertTrue(propyleneStack.getAmount() == 100L, "丙烯 ChemicalStack 数量应为 100");

        var benzeneStack = com.complexindustries.mekanism.registration.MCIChemicals.BENZENE.asStack(250);
        helper.assertTrue(benzeneStack.getAmount() == 250L, "苯 ChemicalStack 数量应为 250");

        var styreneStack = com.complexindustries.mekanism.registration.MCIChemicals.STYRENE.asStack(500);
        helper.assertTrue(styreneStack.getAmount() == 500L, "苯乙烯 ChemicalStack 数量应为 500");

        helper.succeed();
    }

    @GameTest(template = "empty_10x10x10")
    public static void testChemicalInfuserRecipes(GameTestHelper helper) {
        var recipeManager = helper.getLevel().getRecipeManager();

        // 1. Petroleum Gas + Hydrogen -> Propylene (1:1 -> 1)
        net.minecraft.resources.ResourceLocation propyleneId =
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "chemical_infusing/propylene");
        var propyleneHolder = recipeManager.byKey(propyleneId).orElse(null);
        helper.assertTrue(propyleneHolder != null, "配方 chemical_infusing/propylene 应存在于配方管理器中！");
        var propyleneRecipe = (mekanism.api.recipes.basic.BasicChemicalInfuserRecipe) propyleneHolder.value();
        helper.assertTrue(propyleneRecipe.getOutputRaw().is(com.complexindustries.mekanism.registration.MCIChemicals.PROPYLENE),
                "丙烯配方产物应为 Propylene！");

        // 2. Naphtha + Oxygen -> Benzene (1:1 -> 1)
        net.minecraft.resources.ResourceLocation benzeneId =
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "chemical_infusing/benzene");
        var benzeneHolder = recipeManager.byKey(benzeneId).orElse(null);
        helper.assertTrue(benzeneHolder != null, "配方 chemical_infusing/benzene 应存在于配方管理器中！");
        var benzeneRecipe = (mekanism.api.recipes.basic.BasicChemicalInfuserRecipe) benzeneHolder.value();
        helper.assertTrue(benzeneRecipe.getOutputRaw().is(com.complexindustries.mekanism.registration.MCIChemicals.BENZENE),
                "苯配方产物应为 Benzene！");

        // 3. Benzene + Ethene -> Styrene (1:1 -> 1)
        net.minecraft.resources.ResourceLocation styreneId =
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "chemical_infusing/styrene");
        var styreneHolder = recipeManager.byKey(styreneId).orElse(null);
        helper.assertTrue(styreneHolder != null, "配方 chemical_infusing/styrene 应存在于配方管理器中！");
        var styreneRecipe = (mekanism.api.recipes.basic.BasicChemicalInfuserRecipe) styreneHolder.value();
        helper.assertTrue(styreneRecipe.getOutputRaw().is(com.complexindustries.mekanism.registration.MCIChemicals.STYRENE),
                "苯乙烯配方产物应为 Styrene！");

        helper.succeed();
    }

    @GameTest(template = "empty_10x10x10")
    public static void testThermoelectricBoilerCrackingReaction(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, MekanismBlocks.BOILER_CASING.get());
        mekanism.common.tile.multiblock.TileEntityBoilerCasing casing =
                (mekanism.common.tile.multiblock.TileEntityBoilerCasing) helper.getBlockEntity(pos);
        helper.assertTrue(casing != null, "锅炉外壳方块实体不应为空！");

        mekanism.common.content.boiler.BoilerMultiblockData mb = new mekanism.common.content.boiler.BoilerMultiblockData(casing);
        mb.setFormedForce(true);
        mb.setWaterVolume(10);
        mb.setSteamVolume(10);
        mb.superheatingElements = 4;
        mb.heatCapacitor.setHeatCapacity(1000.0, true);

        // 1. Verify Petroleum Gas can be inserted into superheatedCoolantTank
        var insertedGas = mb.superheatedCoolantTank.insert(
                com.complexindustries.mekanism.registration.MCIChemicals.PETROLEUM_GAS.asStack(2000),
                Action.EXECUTE, AutomationType.EXTERNAL);
        helper.assertTrue(insertedGas.isEmpty(), "石油气应能成功注入锅炉加热冷却剂槽！");
        helper.assertTrue(mb.superheatedCoolantTank.getStored() == 2000L, "加热冷却剂槽石油气存量应为 2000 mB！");

        // 2. Verify Water can be inserted into waterTank
        var insertedWater = mb.waterTank.insert(
                new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1000),
                Action.EXECUTE, AutomationType.EXTERNAL);
        helper.assertTrue(insertedWater.isEmpty(), "水应能成功注入锅炉水槽！");
        helper.assertTrue(mb.waterTank.getFluidAmount() == 1000, "锅炉水存量应为 1000 mB！");

        // 3. Test temperature < 800 K (e.g. 600 K): normal boiling suppressed, cracking NOT running
        mb.heatCapacitor.setHeat(600.0 * mb.heatCapacitor.getHeatCapacity());
        mb.tick(helper.getLevel());

        helper.assertTrue(mb.lastBoilRate == 0, "低于 800 K 时不应发生反应，boilRate 应为 0！");
        helper.assertTrue(mb.steamTank.isEmpty(), "低于 800 K 且注入石油气时，常规沸腾应被抑制，蒸汽槽应保持为空！");
        helper.assertTrue(mb.superheatedCoolantTank.getStored() == 2000L, "低于 800 K 时石油气不应被消耗！");
        helper.assertTrue(mb.waterTank.getFluidAmount() == 1000, "低于 800 K 时水不应被消耗！");

        // 4. Test temperature >= 800 K (e.g. 850 K): steam cracking runs!
        mb.heatCapacitor.setHeat(850.0 * mb.heatCapacitor.getHeatCapacity());
        mb.tick(helper.getLevel());

        helper.assertTrue(mb.lastBoilRate > 0, "达到 850 K 时裂解反应应启动，boilRate 应大于 0！实际: " + mb.lastBoilRate);
        int cracked = mb.lastBoilRate;

        // Check products in steamTank (Propylene) and cooledCoolantTank (Ethylene)
        helper.assertTrue(mb.steamTank.getStack().is(com.complexindustries.mekanism.registration.MCIChemicals.PROPYLENE),
                "锅炉蒸汽产物槽应产出丙烯 (Propylene)！");
        helper.assertTrue(mb.steamTank.getStored() == (long) cracked,
                "丙烯产出量应与裂解量完全匹配 (1:1)！期望: " + cracked + ", 实际: " + mb.steamTank.getStored());

        helper.assertTrue(mb.cooledCoolantTank.getStack().is(mekanism.common.registries.MekanismChemicals.ETHENE),
                "锅炉冷却剂槽应产出乙烯 (Ethene)！");
        helper.assertTrue(mb.cooledCoolantTank.getStored() == (long) cracked,
                "乙烯产出量应与裂解量完全匹配 (1:1)！期望: " + cracked + ", 实际: " + mb.cooledCoolantTank.getStored());

        // Check inputs consumed: Petroleum Gas consumed 2x, Water consumed 1x (2:1 -> 1:1)
        long expectedGasRemaining = 2000L - (cracked * 2L);
        int expectedWaterRemaining = 1000 - cracked;
        helper.assertTrue(mb.superheatedCoolantTank.getStored() == expectedGasRemaining,
                "石油气消耗应严格为裂解量的2倍！期望剩余: " + expectedGasRemaining + ", 实际: " + mb.superheatedCoolantTank.getStored());
        helper.assertTrue(mb.waterTank.getFluidAmount() == expectedWaterRemaining,
                "水消耗应严格为裂解量的1倍！期望剩余: " + expectedWaterRemaining + ", 实际: " + mb.waterTank.getFluidAmount());

        helper.succeed();
    }

    @GameTest(template = "empty_10x10x10")
    public static void testLiquidPropyleneAndRotaryCondensentrator(GameTestHelper helper) {
        // 1. Verify fluid and bucket registration
        helper.assertTrue(MCIFluids.SOURCE_LIQUID_PROPYLENE.get() != null, "液态丙烯源流体应成功注册！");
        helper.assertTrue(MCIFluids.FLOWING_LIQUID_PROPYLENE.get() != null, "液态丙烯流动流体应成功注册！");
        helper.assertTrue(com.complexindustries.mekanism.registration.MCIItems.LIQUID_PROPYLENE_BUCKET.get() != null, "液态丙烯桶应成功注册！");

        // 2. Verify Rotary Condensentrator recipe
        var recipeManager = helper.getLevel().getRecipeManager();
        net.minecraft.resources.ResourceLocation rotaryId =
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "rotary/liquid_propylene");
        var holder = recipeManager.byKey(rotaryId).orElse(null);
        helper.assertTrue(holder != null, "旋转冷凝机配方 rotary/liquid_propylene 应存在！");
        helper.assertTrue(holder.value() instanceof mekanism.api.recipes.basic.BasicRotaryRecipe, "配方类型应为 BasicRotaryRecipe！");

        var recipe = (mekanism.api.recipes.basic.BasicRotaryRecipe) holder.value();
        helper.assertTrue(recipe.hasChemicalToFluid(), "配方应支持 气态丙烯 -> 液态丙烯！");
        helper.assertTrue(recipe.hasFluidToChemical(), "配方应支持 液态丙烯 -> 气态丙烯！");
        helper.assertTrue(recipe.getChemicalOutputRaw().is(com.complexindustries.mekanism.registration.MCIChemicals.PROPYLENE), "汽化产物应为气态丙烯！");
        helper.assertTrue(recipe.getFluidOutputRaw().getFluid() == MCIFluids.SOURCE_LIQUID_PROPYLENE.get(), "冷凝产物应为液态丙烯！");

        helper.succeed();
    }

    @GameTest(template = "empty_10x10x10")
    public static void testPolypropylenePelletReactionRecipe(GameTestHelper helper) {
        helper.assertTrue(com.complexindustries.mekanism.registration.MCIItems.POLYPROPYLENE_PELLET.get() != null, "聚丙烯颗粒应成功注册！");

        var recipeManager = helper.getLevel().getRecipeManager();
        net.minecraft.resources.ResourceLocation reactionId =
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "reaction/polypropylene_pellet");
        var holder = recipeManager.byKey(reactionId).orElse(null);
        helper.assertTrue(holder != null, "加压反应室配方 reaction/polypropylene_pellet 应存在！");
        helper.assertTrue(holder.value() instanceof mekanism.api.recipes.basic.BasicPressurizedReactionRecipe, "配方类型应为 BasicPressurizedReactionRecipe！");

        var recipe = (mekanism.api.recipes.basic.BasicPressurizedReactionRecipe) holder.value();
        helper.assertTrue(recipe.getOutputItem().is(com.complexindustries.mekanism.registration.MCIItems.POLYPROPYLENE_PELLET.get()), "反应产物物品应为聚丙烯颗粒！");
        helper.assertTrue(recipe.getInputFluid().test(new FluidStack(MCIFluids.SOURCE_LIQUID_PROPYLENE.get(), 50)), "液态丙烯消耗应为 50 mB！");
        helper.assertTrue(recipe.getInputChemical().amount() == 10, "氧气消耗应为 10 mB！");

        helper.succeed();
    }

    @GameTest(template = "empty_10x10x10")
    public static void testSoakingRodCraftingRecipe(GameTestHelper helper) {
        helper.assertTrue(com.complexindustries.mekanism.registration.MCIItems.SOAKING_ROD.get() != null, "浸泡棒应成功注册！");

        var recipeManager = helper.getLevel().getRecipeManager();
        net.minecraft.resources.ResourceLocation rodId =
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "soaking_rod");
        var holder = recipeManager.byKey(rodId).orElse(null);
        helper.assertTrue(holder != null, "浸泡棒合成配方 soaking_rod 应存在！");
        helper.assertTrue(holder.value().getResultItem(helper.getLevel().registryAccess()).is(com.complexindustries.mekanism.registration.MCIItems.SOAKING_ROD.get()), "合成产物应为浸泡棒！");

        helper.succeed();
    }

    @GameTest(template = "empty_10x10x10")
    public static void testHeavyOilFuelProperties(GameTestHelper helper) {
        var fuelItem = com.complexindustries.mekanism.registration.MCIItems.HEAVY_OIL_FUEL.get();
        helper.assertTrue(fuelItem instanceof com.complexindustries.mekanism.content.item.HeavyOilFuelItem, "重油燃料物品应为 HeavyOilFuelItem！");
        var fuelStack = new ItemStack(fuelItem);
        int burnTime = fuelItem.getBurnTime(fuelStack, null);
        helper.assertTrue(burnTime == 25600, "重油燃料燃烧时间应严格为 25600 ticks (1280 秒)！实际: " + burnTime);

        helper.succeed();
    }

    @GameTest(template = "empty_10x10x10")
    public static void testChemicalSoakerOperationAndUpgrade(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, MCIBlocks.CHEMICAL_SOAKER.get().defaultBlockState());
        helper.assertBlockPresent(MCIBlocks.CHEMICAL_SOAKER.get(), pos);

        // Verify non-full block shape / noOcclusion
        BlockState state = helper.getBlockState(pos);
        helper.assertTrue(!state.canOcclude(), "化学浸泡室方块必须为非完整方块 (noOcclusion)！");

        var te = helper.getBlockEntity(pos);
        helper.assertTrue(te instanceof com.complexindustries.mekanism.content.soaker.TileEntityChemicalSoaker, "方块实体应为 TileEntityChemicalSoaker！");
        var soaker = (com.complexindustries.mekanism.content.soaker.TileEntityChemicalSoaker) te;

        // Supply inputs: Heavy Oil (200 mB) + Sawdust (1) + Energy
        soaker.chemicalTank.setStack(com.complexindustries.mekanism.registration.MCIChemicals.HEAVY_OIL.asStack(1000));
        soaker.inputSlot.setStack(new ItemStack(mekanism.common.registries.MekanismItems.SAWDUST.asItem(), 5));
        // Add 8 Speed and 8 Energy upgrades to make processing fast and efficient
        soaker.getUpgradeComponent().addUpgrades(mekanism.api.Upgrade.SPEED, 8);
        soaker.getUpgradeComponent().addUpgrades(mekanism.api.Upgrade.ENERGY, 8);
        soaker.getUpgradeComponent().addUpgrades(mekanism.api.Upgrade.MUFFLING, 1);
        helper.assertTrue(soaker.getUpgradeComponent().getUpgrades(mekanism.api.Upgrade.MUFFLING) == 1, "静音升级应至多安装1枚！");
        soaker.getEnergyContainer().insert(1_000_000L, Action.EXECUTE, AutomationType.INTERNAL);

        // Tick soaker until reaction produces Heavy Oil Fuel
        for (int i = 0; i < 40; i++) {
            TileEntityMekanism.tickServer(helper.getLevel(), pos, helper.getBlockState(pos), soaker);
            if (!soaker.outputSlot.isEmpty()) {
                break;
            }
        }

        helper.assertTrue(!soaker.outputSlot.isEmpty(), "化学浸泡反应应完成并产生输出！");
        helper.assertTrue(soaker.outputSlot.getStack().is(com.complexindustries.mekanism.registration.MCIItems.HEAVY_OIL_FUEL.get()), "输出物品应为重油燃料！");
        helper.assertTrue(soaker.chemicalTank.getStored() == 800L, "重油消耗量应为 200 mB！剩余: " + soaker.chemicalTank.getStored());
        helper.assertTrue(soaker.inputSlot.getStack().getCount() == 4, "木屑消耗量应为 1！剩余: " + soaker.inputSlot.getStack().getCount());

        // Test Tier Installer upgrade to Basic Chemical Soaking Factory
        Player fakePlayer = FakePlayerFactory.getMinecraft(helper.getLevel());
        BlockPos absolutePos = helper.absolutePos(pos);
        ItemStack basicInstaller = mekanism.common.registries.MekanismItems.BASIC_TIER_INSTALLER.asStack();
        fakePlayer.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, basicInstaller);
        net.minecraft.world.phys.BlockHitResult hit = new net.minecraft.world.phys.BlockHitResult(
                new net.minecraft.world.phys.Vec3(absolutePos.getX() + 0.5, absolutePos.getY() + 0.5, absolutePos.getZ() + 0.5),
                Direction.UP, absolutePos, false);
        net.minecraft.world.item.context.UseOnContext ctx = new net.minecraft.world.item.context.UseOnContext(fakePlayer, net.minecraft.world.InteractionHand.MAIN_HAND, hit);
        net.minecraft.world.InteractionResult res = basicInstaller.getItem().useOn(ctx);
        helper.assertTrue(res == net.minecraft.world.InteractionResult.CONSUME, "初级安装器使用结果应为 CONSUME");
        helper.assertBlockPresent(MCIBlocks.BASIC_CHEMICAL_SOAKING_FACTORY.get(), pos);

        var basicFactory = (com.complexindustries.mekanism.content.soaker.TileEntityChemicalSoakingFactory) helper.getBlockEntity(pos);
        helper.assertTrue(basicFactory != null, "升级后实体应为 TileEntityChemicalSoakingFactory！");
        helper.assertTrue(basicFactory.tier == mekanism.common.tier.FactoryTier.BASIC, "层级应为 BASIC！");
        helper.assertTrue(basicFactory.tier.processes == 3, "初级浸泡工厂应有 3 条流水线！");
        helper.assertTrue(basicFactory.chemicalTank.getStored() == 800L, "升级后重油存量应保留 800 mB！");
        helper.assertTrue(basicFactory.inputSlots.get(0).getStack().getCount() == 4, "升级后输入槽木屑应保留！");
        helper.assertTrue(basicFactory.outputSlots.get(0).getStack().is(com.complexindustries.mekanism.registration.MCIItems.HEAVY_OIL_FUEL.get()), "升级后输出槽燃料应保留！");

        // Test Container validity
        var container = new com.complexindustries.mekanism.content.soaker.ContainerChemicalSoakingFactory(1, fakePlayer.getInventory(), basicFactory);
        helper.assertTrue(container.stillValid(fakePlayer), "工厂容器应处于有效状态！");
        container.removed(fakePlayer);

        // Test Sorting toggle
        helper.assertTrue(basicFactory.isSorting(), "初始应开启自动分流排序！");
        basicFactory.setSorting(false);
        helper.assertTrue(!basicFactory.isSorting(), "切换后分流排序应为关闭！");
        basicFactory.setSorting(true);

        // Test Upgrade chain to Ultimate Factory
        ItemStack advInstaller = mekanism.common.registries.MekanismItems.ADVANCED_TIER_INSTALLER.asStack();
        fakePlayer.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, advInstaller);
        ctx = new net.minecraft.world.item.context.UseOnContext(fakePlayer, net.minecraft.world.InteractionHand.MAIN_HAND, hit);
        advInstaller.getItem().useOn(ctx);
        helper.assertBlockPresent(MCIBlocks.ADVANCED_CHEMICAL_SOAKING_FACTORY.get(), pos);

        ItemStack eliteInstaller = mekanism.common.registries.MekanismItems.ELITE_TIER_INSTALLER.asStack();
        fakePlayer.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, eliteInstaller);
        ctx = new net.minecraft.world.item.context.UseOnContext(fakePlayer, net.minecraft.world.InteractionHand.MAIN_HAND, hit);
        eliteInstaller.getItem().useOn(ctx);
        helper.assertBlockPresent(MCIBlocks.ELITE_CHEMICAL_SOAKING_FACTORY.get(), pos);

        ItemStack ultInstaller = mekanism.common.registries.MekanismItems.ULTIMATE_TIER_INSTALLER.asStack();
        fakePlayer.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ultInstaller);
        ctx = new net.minecraft.world.item.context.UseOnContext(fakePlayer, net.minecraft.world.InteractionHand.MAIN_HAND, hit);
        ultInstaller.getItem().useOn(ctx);
        helper.assertBlockPresent(MCIBlocks.ULTIMATE_CHEMICAL_SOAKING_FACTORY.get(), pos);

        var ultFactory = (com.complexindustries.mekanism.content.soaker.TileEntityChemicalSoakingFactory) helper.getBlockEntity(pos);
        helper.assertTrue(ultFactory.tier == mekanism.common.tier.FactoryTier.ULTIMATE, "层级应为 ULTIMATE！");
        helper.assertTrue(ultFactory.tier.processes == 9, "终极浸泡工厂流水线数应为 9！");
        helper.assertTrue(!ultFactory.canBeUpgraded(), "终极工厂不应再支持继续升级！");

        helper.succeed();
    }

    @GameTest(template = "empty_10x10x10")
    public static void testCrystalGrowthChamberAndSilicon(GameTestHelper helper) {
        // 1. Verify item properties
        ItemStack plasticStack = new ItemStack(MCIItems.ENGINEERING_PLASTIC.get());
        helper.assertTrue(plasticStack.getRarity() == Rarity.RARE, "工程塑料稀有度应为 RARE (钻石浅蓝色)！");
        helper.assertTrue(MCIItems.CRUDE_SILICON.get() != null, "粗制硅物品应已注册！");
        helper.assertTrue(MCIItems.REFINED_SILICON.get() != null, "精制硅物品应已注册！");
        helper.assertTrue(MCIChemicals.STYRENE.get() != null, "苯乙烯化学品应已注册！");

        // 2. Factory naming verification
        ItemStack advFactoryStack = new ItemStack(MCIBlocks.ADVANCED_CHEMICAL_SOAKING_FACTORY.get());
        String advNameKey = advFactoryStack.getItem().getDescriptionId(advFactoryStack);
        helper.assertTrue(advNameKey.contains("advanced_chemical_soaking_factory"), "高级化学浸泡工厂描述键应正确！");

        // 3. Place Crystal Growth Chamber
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, MCIBlocks.CRYSTAL_GROWTH_CHAMBER.get().defaultBlockState());
        helper.assertBlockPresent(MCIBlocks.CRYSTAL_GROWTH_CHAMBER.get(), pos);

        TileEntityCrystalGrowthChamber chamber = (TileEntityCrystalGrowthChamber) helper.getBlockEntity(pos);
        helper.assertTrue(chamber != null, "晶体生长机实体应为 TileEntityCrystalGrowthChamber！");

        // 4. Verify no factory upgrade
        FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(helper.getLevel());
        BlockPos absolutePos = helper.absolutePos(pos);
        ItemStack basicInstaller = mekanism.common.registries.MekanismItems.BASIC_TIER_INSTALLER.asStack();
        fakePlayer.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, basicInstaller);
        net.minecraft.world.phys.BlockHitResult hit = new net.minecraft.world.phys.BlockHitResult(
                new net.minecraft.world.phys.Vec3(absolutePos.getX() + 0.5, absolutePos.getY() + 0.5, absolutePos.getZ() + 0.5),
                Direction.UP, absolutePos, false);
        net.minecraft.world.item.context.UseOnContext ctx = new net.minecraft.world.item.context.UseOnContext(fakePlayer, net.minecraft.world.InteractionHand.MAIN_HAND, hit);
        net.minecraft.world.InteractionResult res = basicInstaller.getItem().useOn(ctx);
        helper.assertTrue(res != net.minecraft.world.InteractionResult.CONSUME && res != net.minecraft.world.InteractionResult.SUCCESS,
                "晶体生长机没有工厂机器，不应接受升级安装器！");
        helper.assertBlockPresent(MCIBlocks.CRYSTAL_GROWTH_CHAMBER.get(), pos);

        // 5. Container verification
        ContainerCrystalGrowthChamber container = new ContainerCrystalGrowthChamber(1, fakePlayer.getInventory(), chamber);
        helper.assertTrue(container.stillValid(fakePlayer), "晶体生长机容器应处于有效状态！");
        container.removed(fakePlayer);

        // 6. Test Chemical Capabilities and Mutual Exclusion via Ports (North-facing: East=RelativeSide.LEFT=INPUT_1, West=RelativeSide.RIGHT=INPUT_2)
        var eastHandler = helper.getLevel().getCapability(mekanism.common.capabilities.Capabilities.CHEMICAL.block(), absolutePos, Direction.EAST);
        var westHandler = helper.getLevel().getCapability(mekanism.common.capabilities.Capabilities.CHEMICAL.block(), absolutePos, Direction.WEST);
        helper.assertTrue(eastHandler != null, "东侧（相对左侧输入口）应暴露化学品能力！");
        helper.assertTrue(westHandler != null, "西侧（相对右侧输入口）应暴露化学品能力！");

        // Pipe Noble Gas into Left Port (East = Tank A)
        var insertEast = eastHandler.insertChemical(MCIChemicals.NOBLE_GAS.asStack(500), Action.EXECUTE);
        helper.assertTrue(insertEast.isEmpty(), "东侧（左端口 Tank A）应能正常注入稀有气体！");
        helper.assertTrue(chamber.chemicalTankA.getStored() == 500L, "化学品槽 A 存量应为 500 mB！");
        helper.assertTrue(chamber.chemicalTankB.getStored() == 0L, "化学品槽 B 应仍为空，不发生串槽！");

        // Try to insert Noble Gas into Right Port (West = Tank B) -> MUST BE REJECTED (mutual exclusion: Tank A already has Noble Gas)
        var rejectWestNoble = westHandler.insertChemical(MCIChemicals.NOBLE_GAS.asStack(100), Action.EXECUTE);
        helper.assertTrue(rejectWestNoble.getAmount() == 100L, "槽 A 已存有稀有气体时，西端口（槽 B）应拒绝同种气体输入以防冲突！");
        helper.assertTrue(chamber.chemicalTankB.getStored() == 0L, "槽 B 存量应仍为 0！");

        // Pipe Hydrogen into Right Port (West = Tank B) -> MUST SUCCEED
        var insertWest = westHandler.insertChemical(mekanism.common.registries.MekanismChemicals.HYDROGEN.asStack(500), Action.EXECUTE);
        helper.assertTrue(insertWest.isEmpty(), "西侧（右端口 Tank B）应能正常注入反应所需的氢气！");
        helper.assertTrue(chamber.chemicalTankB.getStored() == 500L, "化学品槽 B 应成功存入 500 mB 氢气！");

        // Try to insert Hydrogen into Left Port (East = Tank A) -> MUST BE REJECTED
        var rejectEastHydrogen = eastHandler.insertChemical(mekanism.common.registries.MekanismChemicals.HYDROGEN.asStack(100), Action.EXECUTE);
        helper.assertTrue(rejectEastHydrogen.getAmount() == 100L, "槽 B 已存有氢气时，东端口（槽 A）应拒绝氢气输入！");

        // 7. Test Recipe processing
        chamber.inputSlot.setStack(new ItemStack(MCIItems.CRUDE_SILICON.get(), 1));
        chamber.getUpgradeComponent().addUpgrades(mekanism.api.Upgrade.SPEED, 8);
        chamber.getUpgradeComponent().addUpgrades(mekanism.api.Upgrade.ENERGY, 8);
        chamber.getEnergyContainer().insert(1_000_000L, Action.EXECUTE, AutomationType.INTERNAL);

        helper.assertTrue(chamber.chemicalTankA.getStored() == 500L, "化学品槽 A 应存储 500 mB 稀有气体！");
        helper.assertTrue(chamber.chemicalTankB.getStored() == 500L, "化学品槽 B 应存储 500 mB 氢气！");
        helper.assertTrue(chamber.inputSlot.getStack().is(MCIItems.CRUDE_SILICON.get()), "输入槽应放置粗制硅！");

        // Tick chamber until reaction produces Refined Silicon
        for (int i = 0; i < 60; i++) {
            TileEntityMekanism.tickServer(helper.getLevel(), pos, helper.getBlockState(pos), chamber);
            if (!chamber.outputSlot.isEmpty()) {
                break;
            }
        }

        helper.assertTrue(!chamber.outputSlot.isEmpty(), "晶体生长机应完成反应产出精制硅！");
        helper.assertTrue(chamber.outputSlot.getStack().is(MCIItems.REFINED_SILICON.get()),
                "输出物品应为精制硅！当前产物: " + chamber.outputSlot.getStack());
        helper.assertTrue(chamber.chemicalTankA.getStored() == 400L, "稀有气体消耗量应为 100 mB！剩余: " + chamber.chemicalTankA.getStored());
        helper.assertTrue(chamber.chemicalTankB.getStored() == 400L, "氢气消耗量应为 100 mB！剩余: " + chamber.chemicalTankB.getStored());
        helper.assertTrue(chamber.inputSlot.isEmpty(), "粗制硅输入槽应被消耗完毕！");

        helper.succeed();
    }

    @GameTest(template = "empty_10x10x10")
    public static void testSiliconSlicer(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, MCIBlocks.SILICON_SLICER.get());
        helper.assertBlockPresent(MCIBlocks.SILICON_SLICER.get(), pos);

        TileEntitySiliconSlicer slicer = (TileEntitySiliconSlicer) helper.getBlockEntity(pos);
        helper.assertTrue(slicer != null, "硅切片机方块实体应为 TileEntitySiliconSlicer");

        // 1. Verify tank capacity is 1000 mB
        helper.assertTrue(slicer.chemicalTank.getCapacity() == 1000L, "硅切片机化学品槽容量应为 1000 mB！");

        // 2. Setup power and upgrades
        slicer.getUpgradeComponent().addUpgrades(mekanism.api.Upgrade.SPEED, 8);
        slicer.getUpgradeComponent().addUpgrades(mekanism.api.Upgrade.ENERGY, 8);
        slicer.getEnergyContainer().insert(1_000_000L, Action.EXECUTE, AutomationType.INTERNAL);

        // 3. Test 80% nitrogen lock: Insert 700 mB Nitrogen (< 800 mB)
        slicer.chemicalTank.setStack(MCIChemicals.NITROGEN.asStack(700));
        slicer.inputSlot.setStack(new ItemStack(MCIItems.REFINED_SILICON.get(), 1));

        helper.assertTrue(!slicer.canFunction(), "氮气不足 80% (800 mB) 时硅切片机不应运行！");

        // Tick several times to verify machine does not process
        for (int i = 0; i < 10; i++) {
            TileEntityMekanism.tickServer(helper.getLevel(), pos, helper.getBlockState(pos), slicer);
        }
        helper.assertTrue(slicer.outputSlot.isEmpty(), "氮气不足 80% 时不应产出空白硅片！");
        helper.assertTrue(slicer.inputSlot.getCount() == 1, "精制硅不应被消耗！");

        // 4. Increase Nitrogen to >= 800 mB (e.g. 950 mB to account for dissipation during processing)
        slicer.chemicalTank.setStack(MCIChemicals.NITROGEN.asStack(950));
        helper.assertTrue(slicer.canFunction(), "氮气达到 80% 以上时硅切片机应恢复工作！");

        // Tick until slicing finishes
        for (int i = 0; i < 60; i++) {
            TileEntityMekanism.tickServer(helper.getLevel(), pos, helper.getBlockState(pos), slicer);
            if (!slicer.outputSlot.isEmpty()) {
                break;
            }
        }

        helper.assertTrue(!slicer.outputSlot.isEmpty(), "硅切片机应完成切片并产出物品！");
        helper.assertTrue(slicer.outputSlot.getStack().is(MCIItems.BLANK_SILICON_WAFER.get()), "产物应为空白硅片！");
        helper.assertTrue(slicer.outputSlot.getStack().getCount() == 8, "每次切片应产出 8 个空白硅片！当前数量: " + slicer.outputSlot.getStack().getCount());
        helper.assertTrue(slicer.inputSlot.isEmpty(), "原料精制硅应被消耗完毕！");

        // 5. Test Environmental Dissipation (0.1%/tick)
        slicer.inputSlot.setEmpty();
        long beforeDissipation = slicer.chemicalTank.getStored();
        for (int i = 0; i < 20; i++) {
            TileEntityMekanism.tickServer(helper.getLevel(), pos, helper.getBlockState(pos), slicer);
        }
        long afterDissipation = slicer.chemicalTank.getStored();
        helper.assertTrue(afterDissipation < beforeDissipation, "氮气在储罐中应自然发生逸散！之前: " + beforeDissipation + ", 之后: " + afterDissipation);

        // 6. Test Container
        FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(helper.getLevel());
        ContainerSiliconSlicer container = new ContainerSiliconSlicer(1, fakePlayer.getInventory(), slicer);
        helper.assertTrue(container.stillValid(fakePlayer), "硅切片机容器应处于有效状态！");
        container.removed(fakePlayer);

        helper.succeed();
    }

    @GameTest(template = "empty_10x10x10")
    public static void testFilteredGlass(GameTestHelper helper) {
        BlockPos pos1 = new BlockPos(2, 2, 2);
        BlockPos pos2 = new BlockPos(2, 2, 3);

        helper.setBlock(pos1, MCIBlocks.FILTERED_GLASS.get());
        helper.setBlock(pos2, MCIBlocks.FILTERED_GLASS.get());

        helper.assertBlockPresent(MCIBlocks.FILTERED_GLASS.get(), pos1);
        helper.assertBlockPresent(MCIBlocks.FILTERED_GLASS.get(), pos2);

        TileEntityFilteredGlass glass1 = (TileEntityFilteredGlass) helper.getBlockEntity(pos1);
        TileEntityFilteredGlass glass2 = (TileEntityFilteredGlass) helper.getBlockEntity(pos2);
        helper.assertTrue(glass1 != null && glass2 != null, "过滤玻璃方块实体应为 TileEntityFilteredGlass");

        BlockPos absPos1 = helper.absolutePos(pos1);
        var receptor1 = helper.getLevel().getCapability(mekanism.common.capabilities.Capabilities.LASER_RECEPTOR, absPos1, Direction.NORTH);
        helper.assertTrue(receptor1 != null, "过滤玻璃北侧应暴露激光受体能力！");
        helper.assertTrue(!receptor1.canLasersDig(), "过滤玻璃拦截激光时不应被破坏 (canLasersDig == false)！");

        // 1. Test Laser Absorption & UV Transmission
        // Incoming: 100,000 laser energy into NORTH face of pos1.
        // pos1 absorbs 90% (90,000) as heat, transmits 10% (10,000) SOUTH into pos2.
        // pos2 absorbs 90% of 10,000 = 9,000 as heat.
        receptor1.receiveLaserEnergy(100_000L);

        helper.assertTrue(glass1.getTemperature() > 300.0, "过滤玻璃 1 吸收激光后温度应显著上升！当前温度: " + glass1.getTemperature());
        helper.assertTrue(glass2.getTemperature() > 300.0, "过滤玻璃 2 接收穿透的紫外激光后温度应上升！当前温度: " + glass2.getTemperature());

        // 2. Test High Temperature StepOn Hazard (T > 373.15 K)
        glass1.getHeatCapacitor().handleHeat(100_000.0);
        glass1.getHeatCapacitor().update();
        helper.assertTrue(glass1.getTemperature() > 373.15, "过滤玻璃 1 温度应超过 373.15 K！当前温度: " + glass1.getTemperature());

        Zombie zombie = new Zombie(helper.getLevel());
        zombie.setPos(absPos1.getX() + 0.5, absPos1.getY() + 1.0, absPos1.getZ() + 0.5);
        FilteredGlassBlock glassBlock = (FilteredGlassBlock) helper.getBlockState(pos1).getBlock();
        glassBlock.stepOn(helper.getLevel(), absPos1, helper.getBlockState(pos1), zombie);
        helper.assertTrue(zombie.getRemainingFireTicks() > 0, "实体踩在高温过滤玻璃上应被点燃！当前着火 tick: " + zombie.getRemainingFireTicks());
        zombie.discard();

        helper.succeed();
    }

    @GameTest(template = "empty_15x25x15")
    public static void testUltravioletLaserHarmlessAndAttenuation(GameTestHelper helper) {
        BlockPos glassPos1 = new BlockPos(2, 2, 1);
        BlockPos targetPosNear = new BlockPos(2, 2, 4);

        helper.setBlock(glassPos1, MCIBlocks.FILTERED_GLASS.get());
        helper.setBlock(targetPosNear, MCIBlocks.FILTERED_GLASS.get());

        TileEntityFilteredGlass glass1 = (TileEntityFilteredGlass) helper.getBlockEntity(glassPos1);
        TileEntityFilteredGlass glassNear = (TileEntityFilteredGlass) helper.getBlockEntity(targetPosNear);

        BlockPos absGlassPos1 = helper.absolutePos(glassPos1);
        var receptor1 = helper.getLevel().getCapability(mekanism.common.capabilities.Capabilities.LASER_RECEPTOR, absGlassPos1, Direction.NORTH);
        helper.assertTrue(receptor1 != null, "过滤玻璃 1 北侧应暴露激光受体能力！");

        // Spawn dropped item directly in the beam line (at Z = 2)
        BlockPos itemPos = helper.absolutePos(new BlockPos(2, 2, 2));
        ItemEntity itemEntity = new ItemEntity(helper.getLevel(), itemPos.getX() + 0.5, itemPos.getY() + 0.5, itemPos.getZ() + 0.5, new ItemStack(MCIItems.BLANK_SILICON_WAFER.get(), 1));
        helper.getLevel().addFreshEntity(itemEntity);

        // Spawn mob directly in the beam line (at Z = 3)
        BlockPos mobPos = helper.absolutePos(new BlockPos(2, 2, 3));
        Zombie zombie = new Zombie(helper.getLevel());
        zombie.setPos(mobPos.getX() + 0.5, mobPos.getY(), mobPos.getZ() + 0.5);
        helper.getLevel().addFreshEntity(zombie);

        // Fire laser into North of glass1 -> emits UV laser South
        receptor1.receiveLaserEnergy(100_000L);

        // Verify UV Laser is completely harmless to entities and items
        helper.assertTrue(itemEntity.isAlive(), "紫外激光不应破坏掉落物！");
        helper.assertTrue(itemEntity.getItem().getCount() == 1, "掉落物数量不应减少！");
        helper.assertTrue(zombie.isAlive(), "紫外激光不应对生物造成伤害！");
        helper.assertTrue(zombie.getHealth() == zombie.getMaxHealth(), "生物血量不应被扣除！");
        helper.assertTrue(zombie.getRemainingFireTicks() <= 0, "紫外激光不应点燃生物！");

        // Verify beam reached near target (distance ~ 3m < 10m) and heated it
        helper.assertTrue(glassNear.getTemperature() > 300.0, "近距离 (3m) 过滤玻璃应接收到紫外激光并升温！当前温度: " + glassNear.getTemperature());

        // Discard entities
        itemEntity.discard();
        zombie.discard();

        // Remove near target so beam path extends to far target
        helper.setBlock(targetPosNear, Blocks.AIR);

        // Test Range Cap: Target placed at distance 12m (> 10m MAX_UV_RANGE)
        BlockPos targetPosFar = new BlockPos(2, 2, 13);
        helper.setBlock(targetPosFar, MCIBlocks.FILTERED_GLASS.get());
        TileEntityFilteredGlass glassFar = (TileEntityFilteredGlass) helper.getBlockEntity(targetPosFar);
        double farTempBefore = glassFar.getTemperature();

        // Fire laser again
        receptor1.receiveLaserEnergy(100_000L);

        // Verify far target did NOT receive any laser because range is capped at 10m
        double farTempAfter = glassFar.getTemperature();
        helper.assertTrue(farTempAfter == farTempBefore, "超过 10m (12m) 的目标不应接收到任何紫外激光！之前: " + farTempBefore + ", 之后: " + farTempAfter);

        helper.succeed();
    }

    @GameTest(template = "empty_10x10x10")
    public static void testPhotolithographyMachine(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, MCIBlocks.PHOTOLITHOGRAPHY_MACHINE.get().defaultBlockState().setValue(BlockPhotolithographyMachine.FACING, Direction.NORTH));
        helper.assertBlockPresent(MCIBlocks.PHOTOLITHOGRAPHY_MACHINE.get(), pos);

        TileEntityPhotolithographyMachine machine = (TileEntityPhotolithographyMachine) helper.getBlockEntity(pos);
        helper.assertTrue(machine != null, "光刻机方块实体应为 TileEntityPhotolithographyMachine");

        // 1. Verify Siding: Only designated optical right face (EAST when facing NORTH) accepts UV laser
        BlockPos absPos = helper.absolutePos(pos);
        var rightReceptor = helper.getLevel().getCapability(mekanism.common.capabilities.Capabilities.LASER_RECEPTOR, absPos, Direction.EAST);
        helper.assertTrue(rightReceptor != null, "光刻机右侧面 (EAST) 应暴露激光受体能力！");
        helper.assertTrue(!rightReceptor.canLasersDig(), "光刻机激光受体不应被破坏！");

        for (Direction side : Direction.values()) {
            if (side != Direction.EAST) {
                var otherReceptor = helper.getLevel().getCapability(mekanism.common.capabilities.Capabilities.LASER_RECEPTOR, absPos, side);
                helper.assertTrue(otherReceptor == null, "光刻机非右侧面 (" + side + ") 不应暴露激光受体能力！");
            }
        }

        // 2. Tank capacity check
        helper.assertTrue(machine.chemicalTank.getCapacity() == 1000L, "光刻机氮气储罐容量应为 1000 mB！");

        // 3. Power and upgrades
        machine.getUpgradeComponent().addUpgrades(mekanism.api.Upgrade.SPEED, 8);
        machine.getUpgradeComponent().addUpgrades(mekanism.api.Upgrade.ENERGY, 8);
        machine.getEnergyContainer().insert(1_000_000L, Action.EXECUTE, AutomationType.INTERNAL);

        // 4. Test Exposure duration scaling by laser energy
        // 100 J laser
        rightReceptor.receiveLaserEnergy(100L);
        TileEntityMekanism.tickServer(helper.getLevel(), pos, helper.getBlockState(pos), machine);
        helper.assertTrue(machine.ticksRequired == 1100, "100 J 激光照射时光刻耗时应为 1100 ticks (55s)！实际: " + machine.ticksRequired);

        // 10,000 J laser
        rightReceptor.receiveLaserEnergy(10_000L);
        TileEntityMekanism.tickServer(helper.getLevel(), pos, helper.getBlockState(pos), machine);
        helper.assertTrue(machine.ticksRequired == 100, "10,000 J 激光照射时光刻耗时应为 100 ticks (5s)！实际: " + machine.ticksRequired);

        // 5. Test 80% Nitrogen lock
        machine.chemicalTank.setStack(MCIChemicals.NITROGEN.asStack(700));
        machine.inputSlot.setStack(new ItemStack(MCIItems.BLANK_SILICON_WAFER.get(), 1));
        machine.maskSlot.setStack(new ItemStack(MCIItems.CALCULATION_MASK.get(), 1));

        rightReceptor.receiveLaserEnergy(10_000L);
        TileEntityMekanism.tickServer(helper.getLevel(), pos, helper.getBlockState(pos), machine);
        helper.assertTrue(!machine.canFunction(), "氮气不足 80% (800 mB) 时光刻机不应运行！");
        helper.assertTrue(machine.outputSlot.isEmpty(), "氮气不足 80% 时不应产出芯片！");

        // 6. Test Laser offline pause
        machine.chemicalTank.setStack(MCIChemicals.NITROGEN.asStack(950));
        for (int i = 0; i < 6; i++) {
            TileEntityMekanism.tickServer(helper.getLevel(), pos, helper.getBlockState(pos), machine);
        }
        helper.assertTrue(!machine.hasActiveLaser(), "无激光输入时光刻机激光状态应为离线！");
        helper.assertTrue(!machine.canFunction(), "无紫外激光照射时光刻机应暂停工作！");

        // 7. Full Exposure test with Calculation Mask
        machine.chemicalTank.setStack(MCIChemicals.NITROGEN.asStack(1000));
        for (int i = 0; i < 120; i++) {
            rightReceptor.receiveLaserEnergy(10_000L);
            TileEntityMekanism.tickServer(helper.getLevel(), pos, helper.getBlockState(pos), machine);
            if (!machine.outputSlot.isEmpty()) {
                break;
            }
        }

        helper.assertTrue(!machine.outputSlot.isEmpty(), "光刻机应完成曝光产出半成品芯片！");
        helper.assertTrue(machine.outputSlot.getStack().is(MCIItems.SEMIFINISHED_CALCULATION_CHIP.get()),
                "使用计算掩膜时产物应为半成品计算芯片！实际: " + machine.outputSlot.getStack());
        helper.assertTrue(!machine.maskSlot.isEmpty() && machine.maskSlot.getStack().is(MCIItems.CALCULATION_MASK.get()) && machine.maskSlot.getCount() == 1,
                "计算掩膜版属于非消耗耐用品，不应被消耗！");
        helper.assertTrue(machine.inputSlot.isEmpty(), "空白硅片原料应被消耗！");

        // 8. Full Exposure test with Logic Mask
        machine.outputSlot.setEmpty();
        machine.inputSlot.setStack(new ItemStack(MCIItems.BLANK_SILICON_WAFER.get(), 1));
        machine.maskSlot.setStack(new ItemStack(MCIItems.LOGIC_MASK.get(), 1));
        machine.chemicalTank.setStack(MCIChemicals.NITROGEN.asStack(1000));

        for (int i = 0; i < 120; i++) {
            rightReceptor.receiveLaserEnergy(10_000L);
            TileEntityMekanism.tickServer(helper.getLevel(), pos, helper.getBlockState(pos), machine);
            if (!machine.outputSlot.isEmpty()) {
                break;
            }
        }

        helper.assertTrue(!machine.outputSlot.isEmpty(), "光刻机应完成逻辑芯片曝光！");
        helper.assertTrue(machine.outputSlot.getStack().is(MCIItems.SEMIFINISHED_LOGIC_CHIP.get()),
                "使用逻辑掩膜时产物应为半成品逻辑芯片！实际: " + machine.outputSlot.getStack());
        helper.assertTrue(!machine.maskSlot.isEmpty() && machine.maskSlot.getStack().is(MCIItems.LOGIC_MASK.get()) && machine.maskSlot.getCount() == 1,
                "逻辑掩膜版属于非消耗耐用品，不应被消耗！");
        helper.assertTrue(machine.inputSlot.isEmpty(), "空白硅片原料应被消耗！");

        // 9. Test no-input idle (no ghost crafting when wafer is missing)
        machine.outputSlot.setEmpty();
        machine.inputSlot.setEmpty();
        machine.chemicalTank.setStack(MCIChemicals.NITROGEN.asStack(1000));
        for (int i = 0; i < 50; i++) {
            rightReceptor.receiveLaserEnergy(10_000L);
            TileEntityMekanism.tickServer(helper.getLevel(), pos, helper.getBlockState(pos), machine);
        }
        helper.assertTrue(machine.outputSlot.isEmpty(), "无空白硅片时光刻机绝不应自跑进度或产出物品！");
        helper.assertTrue(machine.getOperatingTicks() == 0, "无原料时光刻机进度应严格为 0！实际: " + machine.getOperatingTicks());
        helper.assertTrue(!machine.getActive(), "无原料时光刻机应处于闲置状态 (active==false)！");

        // 10. Environmental Dissipation
        long beforeDissipation = machine.chemicalTank.getStored();
        for (int i = 0; i < 20; i++) {
            TileEntityMekanism.tickServer(helper.getLevel(), pos, helper.getBlockState(pos), machine);
        }
        long afterDissipation = machine.chemicalTank.getStored();
        helper.assertTrue(afterDissipation < beforeDissipation, "氮气在光刻机储罐中应自然发生逸散！之前: " + beforeDissipation + ", 之后: " + afterDissipation);

        // 10. Container check
        FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(helper.getLevel());
        ContainerPhotolithographyMachine container = new ContainerPhotolithographyMachine(1, fakePlayer.getInventory(), machine);
        helper.assertTrue(container.stillValid(fakePlayer), "光刻机容器应处于有效状态！");
        container.removed(fakePlayer);

        helper.succeed();
    }

    @GameTest(template = "empty_10x10x10")
    public static void testChemicalFilmCoaterAndChipDurability(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, MCIBlocks.CHEMICAL_FILM_COATER.get().defaultBlockState()
                .setValue(com.complexindustries.mekanism.content.coater.BlockChemicalFilmCoater.FACING, Direction.NORTH));
        helper.assertBlockPresent(MCIBlocks.CHEMICAL_FILM_COATER.get(), pos);

        com.complexindustries.mekanism.content.coater.TileEntityChemicalFilmCoater coater =
                (com.complexindustries.mekanism.content.coater.TileEntityChemicalFilmCoater) helper.getBlockEntity(pos);
        helper.assertTrue(coater != null, "方块实体应为 TileEntityChemicalFilmCoater！");

        // Energy supply
        coater.getEnergyContainer().setEnergy(100_000L);

        // 1. Test 80% Nitrogen lock (<800 mB)
        coater.chemicalTank.setStack(MCIChemicals.NITROGEN.asStack(700));
        coater.alloySlot.setStack(mekanism.common.registries.MekanismItems.INFUSED_ALLOY.asStack());
        coater.chipSlot.setStack(new ItemStack(MCIItems.SEMIFINISHED_CALCULATION_CHIP.get()));

        TileEntityMekanism.tickServer(helper.getLevel(), pos, helper.getBlockState(pos), coater);
        helper.assertTrue(!coater.canFunction(), "氮气不足 80% (800 mB) 时化学覆膜器不应运行！");
        helper.assertTrue(coater.outputSlot.isEmpty(), "氮气不足时覆膜器不应产生输出！");

        // 2. Supply >= 800 mB Nitrogen and test 5-step durability progression
        coater.chemicalTank.setStack(MCIChemicals.NITROGEN.asStack(1000));
        helper.assertTrue(coater.canFunction(), "氮气满时化学覆膜器应可正常工作！");

        // Verify chip durability starts at 0
        ItemStack curChip = coater.chipSlot.getStack();
        helper.assertTrue(com.complexindustries.mekanism.content.item.SemiFinishedChipItem.getCoatingProgress(curChip) == 0,
                "初始半成品芯片覆膜进度应为 0 / 5");

        // Run 5 coating steps
        for (int step = 1; step <= 5; step++) {
            // Keep nitrogen and energy topped up simulating continuous supply pipe
            coater.chemicalTank.setStack(MCIChemicals.NITROGEN.asStack(1000));
            coater.getEnergyContainer().setEnergy(100_000L);
            coater.alloySlot.setStack(mekanism.common.registries.MekanismItems.INFUSED_ALLOY.asStack());

            // Run through the recipe ticks (100 ticks = 5 seconds)
            for (int t = 0; t < 110; t++) {
                TileEntityMekanism.tickServer(helper.getLevel(), pos, helper.getBlockState(pos), coater);
                if (!coater.outputSlot.isEmpty()) {
                    break;
                }
            }

            helper.assertTrue(!coater.outputSlot.isEmpty(), "第 " + step + " 档覆膜应完成并产出物品！");
            ItemStack produced = coater.outputSlot.getStack();

            if (step < 5) {
                helper.assertTrue(produced.is(MCIItems.SEMIFINISHED_CALCULATION_CHIP.get()),
                        "第 " + step + " 档产出仍应为半成品芯片！实际: " + produced);
                int prog = com.complexindustries.mekanism.content.item.SemiFinishedChipItem.getCoatingProgress(produced);
                helper.assertTrue(prog == step, "第 " + step + " 档产出进度应为 " + step + " / 5！实际: " + prog);

                // Move output back to chip input for next coating step
                coater.outputSlot.setEmpty();
                coater.chipSlot.setStack(produced);
            } else {
                // 5th coating completes full durability and transforms into finished chip!
                helper.assertTrue(produced.is(MCIItems.INFUSED_CALCULATION_CHIP.get()),
                        "第 5 档覆膜满耐久后应蜕变为灌注计算芯片！实际: " + produced);
            }
        }

        // 3. Test no-input idle (no ghost crafting when inputs are empty)
        coater.outputSlot.setEmpty();
        coater.chipSlot.setEmpty();
        coater.alloySlot.setEmpty();
        coater.chemicalTank.setStack(MCIChemicals.NITROGEN.asStack(1000));
        for (int i = 0; i < 50; i++) {
            TileEntityMekanism.tickServer(helper.getLevel(), pos, helper.getBlockState(pos), coater);
        }
        helper.assertTrue(coater.outputSlot.isEmpty(), "无芯片或合金时化学覆膜器绝不应自跑进度或产出物品！");
        helper.assertTrue(coater.getOperatingTicks() == 0, "无原料时化学覆膜器进度应严格为 0！实际: " + coater.getOperatingTicks());
        helper.assertTrue(!coater.getActive(), "无原料时化学覆膜器应处于闲置状态 (active==false)！");

        // 4. Container check
        FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(helper.getLevel());
        com.complexindustries.mekanism.content.coater.ContainerChemicalFilmCoater container =
                new com.complexindustries.mekanism.content.coater.ContainerChemicalFilmCoater(1, fakePlayer.getInventory(), coater);
        helper.assertTrue(container.stillValid(fakePlayer), "覆膜器容器应对玩家有效！");
        container.removed(fakePlayer);

        helper.succeed();
    }

    @GameTest(template = "empty_10x10x10")
    public static void testPipeNetworkFormation(GameTestHelper helper) {
        BlockPos p1 = new BlockPos(1, 1, 1);
        BlockPos p2 = new BlockPos(1, 1, 2);
        BlockPos inPos = new BlockPos(1, 1, 0);
        BlockPos outPos = new BlockPos(1, 1, 3);

        helper.setBlock(p1, MCIBlocks.INDUSTRIAL_PIPE.get().defaultBlockState());
        helper.setBlock(p2, MCIBlocks.INDUSTRIAL_PIPE.get().defaultBlockState());
        helper.setBlock(inPos, MCIBlocks.INPUT_INTERFACE.get().defaultBlockState());
        helper.setBlock(outPos, MCIBlocks.OUTPUT_INTERFACE.get().defaultBlockState());

        var pipe1 = (com.complexindustries.mekanism.content.pipe.TileEntityIndustrialPipe) helper.getBlockEntity(p1);
        var pipe2 = (com.complexindustries.mekanism.content.pipe.TileEntityIndustrialPipe) helper.getBlockEntity(p2);
        var inputTile = (com.complexindustries.mekanism.content.pipe.TileEntityInputInterface) helper.getBlockEntity(inPos);
        var outputTile = (com.complexindustries.mekanism.content.pipe.TileEntityOutputInterface) helper.getBlockEntity(outPos);

        helper.assertTrue(pipe1 != null && pipe2 != null && inputTile != null && outputTile != null, "方块实体应成功生成！");

        var net1 = pipe1.getPipeNetwork();
        var net2 = pipe2.getPipeNetwork();
        var netIn = inputTile.getPipeNetwork();
        var netOut = outputTile.getPipeNetwork();

        helper.assertTrue(net1 != null, "管道1必须成功加入管道网络！");
        helper.assertTrue(net1 == net2, "相邻管道应处于同一个管道网络！");
        helper.assertTrue(net1 == netIn, "输入接口方块应处于同一个管道网络！");
        helper.assertTrue(net1 == netOut, "输出接口方块应处于同一个管道网络！");
        helper.assertTrue(net1.getInputInterfaces().contains(inputTile), "网络必须注册输入接口！");
        helper.assertTrue(net1.getOutputInterfaces().contains(outputTile), "网络必须注册输出接口！");

        helper.succeed();
    }

    @GameTest(template = "empty_10x10x10")
    public static void testZeroBufferRoutingAndBackpressure(GameTestHelper helper) {
        BlockPos inPos = new BlockPos(1, 1, 1);
        BlockPos pipePos = new BlockPos(2, 1, 1);
        BlockPos outPos = new BlockPos(3, 1, 1);
        BlockPos chestPos = new BlockPos(4, 1, 1);

        helper.setBlock(inPos, MCIBlocks.INPUT_INTERFACE.get().defaultBlockState());
        helper.setBlock(pipePos, MCIBlocks.INDUSTRIAL_PIPE.get().defaultBlockState());
        helper.setBlock(outPos, MCIBlocks.OUTPUT_INTERFACE.get().defaultBlockState());
        helper.setBlock(chestPos, Blocks.CHEST.defaultBlockState());

        var inputTile = (com.complexindustries.mekanism.content.pipe.TileEntityInputInterface) helper.getBlockEntity(inPos);
        var outputTile = (com.complexindustries.mekanism.content.pipe.TileEntityOutputInterface) helper.getBlockEntity(outPos);
        var chest = (net.minecraft.world.level.block.entity.ChestBlockEntity) helper.getBlockEntity(chestPos);

        helper.assertTrue(inputTile != null && outputTile != null && chest != null, "组件必须初始化！");
        var inHandler = inputTile.getItemHandler(Direction.WEST);
        helper.assertTrue(inHandler != null, "输入接口必须暴露物品输入 Capability！");

        // 1. 严格白名单与反压：未设置过滤时，尝试推入铁锭必须被 100% 拒绝！
        ItemStack pushIron = new ItemStack(net.minecraft.world.item.Items.IRON_INGOT, 5);
        ItemStack remainder1 = inHandler.insertItem(0, pushIron, false);
        helper.assertTrue(remainder1.getCount() == 5, "未配置过滤白名单时必须拒绝接收（阻塞弹出），实际返回: " + remainder1.getCount());
        helper.assertTrue(chest.isEmpty(), "被拒绝的物品绝不能进入箱子！");

        // 2. 配置错误白名单（金锭）：推入铁锭依然被 100% 拒绝！
        outputTile.setFilter(0, new ItemStack(net.minecraft.world.item.Items.GOLD_INGOT));
        ItemStack remainder2 = inHandler.insertItem(0, pushIron, false);
        helper.assertTrue(remainder2.getCount() == 5, "未匹配白名单时必须拒绝接收，实际返回: " + remainder2.getCount());
        helper.assertTrue(chest.isEmpty(), "未匹配的物品绝不能进入箱子！");

        // 3. 配置正确白名单（铁锭）：推入铁锭应 100% 即时穿透到达箱子！
        outputTile.setFilter(0, new ItemStack(net.minecraft.world.item.Items.IRON_INGOT));
        ItemStack remainder3 = inHandler.insertItem(0, pushIron, false);
        helper.assertTrue(remainder3.isEmpty(), "匹配白名单时物资必须即时透传，剩余应为空！实际: " + remainder3.getCount());
        helper.assertTrue(chest.getItem(0).is(net.minecraft.world.item.Items.IRON_INGOT) && chest.getItem(0).getCount() == 5,
                "箱子内必须准确收到透传的 5 个铁锭！");

        // 4. 箱子满载时反压阻断：填满箱子后再次推入，必须被反压拒绝！
        for (int i = 0; i < chest.getContainerSize(); i++) {
            chest.setItem(i, new ItemStack(net.minecraft.world.item.Items.IRON_INGOT, 64));
        }
        ItemStack remainder4 = inHandler.insertItem(0, new ItemStack(net.minecraft.world.item.Items.IRON_INGOT, 8), false);
        helper.assertTrue(remainder4.getCount() == 8, "下游已满时输入接口必须阻挡弹出！实际返回: " + remainder4.getCount());

        helper.succeed();
    }

    @GameTest(template = "empty_10x10x10")
    public static void testOutputInterfacePriorityRouting(GameTestHelper helper) {
        BlockPos inPos = new BlockPos(2, 1, 2);
        BlockPos pipePos = new BlockPos(2, 1, 3);
        BlockPos outPosA = new BlockPos(1, 1, 3);
        BlockPos chestPosA = new BlockPos(0, 1, 3);
        BlockPos outPosB = new BlockPos(3, 1, 3);
        BlockPos chestPosB = new BlockPos(4, 1, 3);

        helper.setBlock(inPos, MCIBlocks.INPUT_INTERFACE.get().defaultBlockState());
        helper.setBlock(pipePos, MCIBlocks.INDUSTRIAL_PIPE.get().defaultBlockState());
        helper.setBlock(outPosA, MCIBlocks.OUTPUT_INTERFACE.get().defaultBlockState());
        helper.setBlock(chestPosA, Blocks.CHEST.defaultBlockState());
        helper.setBlock(outPosB, MCIBlocks.OUTPUT_INTERFACE.get().defaultBlockState());
        helper.setBlock(chestPosB, Blocks.CHEST.defaultBlockState());

        var inTile = (com.complexindustries.mekanism.content.pipe.TileEntityInputInterface) helper.getBlockEntity(inPos);
        var outTileA = (com.complexindustries.mekanism.content.pipe.TileEntityOutputInterface) helper.getBlockEntity(outPosA);
        var outTileB = (com.complexindustries.mekanism.content.pipe.TileEntityOutputInterface) helper.getBlockEntity(outPosB);
        var chestA = (net.minecraft.world.level.block.entity.ChestBlockEntity) helper.getBlockEntity(chestPosA);
        var chestB = (net.minecraft.world.level.block.entity.ChestBlockEntity) helper.getBlockEntity(chestPosB);

        // 设置白名单过滤均为铁锭
        outTileA.setFilter(0, new ItemStack(net.minecraft.world.item.Items.IRON_INGOT));
        outTileB.setFilter(0, new ItemStack(net.minecraft.world.item.Items.IRON_INGOT));

        // 设置优先级：A 为 10，B 为 0
        outTileA.setPriority(10);
        outTileB.setPriority(0);

        var inHandler = inTile.getItemHandler(Direction.UP);
        ItemStack push = new ItemStack(net.minecraft.world.item.Items.IRON_INGOT, 10);
        ItemStack rem = inHandler.insertItem(0, push, false);

        helper.assertTrue(rem.isEmpty(), "推入的 10 个铁锭必须全额接收！");
        helper.assertTrue(chestA.getItem(0).getCount() == 10, "高优先级输出接口 A 必须优先获取全部物资！实际: " + chestA.getItem(0).getCount());
        helper.assertTrue(chestB.isEmpty(), "低优先级输出接口 B 在高优先级未满时不应分流！实际: " + chestB.getItem(0).getCount());

        helper.succeed();
    }

    @GameTest(template = "empty_10x10x10")
    public static void testPipeAttachmentPlacementAndCapabilities(GameTestHelper helper) {
        BlockPos pipePos = new BlockPos(1, 1, 1);
        helper.setBlock(pipePos, MCIBlocks.INDUSTRIAL_PIPE.get().defaultBlockState());
        var pipe = (com.complexindustries.mekanism.content.pipe.TileEntityIndustrialPipe) helper.getBlockEntity(pipePos);
        helper.assertTrue(pipe != null, "管道必须生成！");

        // 附着输入面板于 NORTH
        var inAtt = new com.complexindustries.mekanism.content.pipe.attachment.InputInterfaceAttachment(pipe, Direction.NORTH);
        pipe.addAttachment(Direction.NORTH, inAtt);

        // 附着输出面板于 SOUTH
        var outAtt = new com.complexindustries.mekanism.content.pipe.attachment.OutputInterfaceAttachment(pipe, Direction.SOUTH);
        pipe.addAttachment(Direction.SOUTH, outAtt);

        helper.assertTrue(pipe.getAttachment(Direction.NORTH) == inAtt, "NORTH 必须成功挂载输入面板！");
        helper.assertTrue(pipe.getAttachment(Direction.SOUTH) == outAtt, "SOUTH 必须成功挂载输出面板！");

        // 验证 BlockState 臂连接状态同步
        var state = helper.getBlockState(pipePos);
        helper.assertTrue(state.getValue(com.complexindustries.mekanism.content.pipe.IndustrialPipeBlock.NORTH), "NORTH 挂载附件后管道应呈连接状态！");
        helper.assertTrue(state.getValue(com.complexindustries.mekanism.content.pipe.IndustrialPipeBlock.SOUTH), "SOUTH 挂载附件后管道应呈连接状态！");

        // 验证 Capability 透传
        helper.assertTrue(pipe.getItemHandler(Direction.NORTH) != null, "NORTH 面必须暴露输入物品能力！");
        helper.assertTrue(pipe.getItemHandler(Direction.EAST) == null, "未挂载附件的 EAST 面绝不暴露物品能力！");

        helper.succeed();
    }

    @GameTest(template = "empty_10x10x10")
    public static void testOutputInterfaceConfigurableFilters(GameTestHelper helper) {
        var filter = new com.complexindustries.mekanism.content.pipe.interfaces.OutputInterfaceFilter();

        // 1. 测试物品 ID 过滤 (包含全称与短路径)
        filter.setFilter(0, com.complexindustries.mekanism.content.pipe.interfaces.OutputInterfaceFilter.FilterType.ITEM, "minecraft:iron_ingot", ItemStack.EMPTY);
        filter.setFilter(1, com.complexindustries.mekanism.content.pipe.interfaces.OutputInterfaceFilter.FilterType.ITEM, "gold_ingot", ItemStack.EMPTY);

        helper.assertTrue(filter.matchesItem(new ItemStack(net.minecraft.world.item.Items.IRON_INGOT)), "应通过全称 ID 匹配铁锭！");
        helper.assertTrue(filter.matchesItem(new ItemStack(net.minecraft.world.item.Items.GOLD_INGOT)), "应通过短名称 ID 匹配金锭！");
        helper.assertTrue(!filter.matchesItem(new ItemStack(net.minecraft.world.item.Items.COPPER_INGOT)), "不应匹配未配置的铜锭！");

        // 2. 测试流体 ID 过滤
        filter.setFilter(2, com.complexindustries.mekanism.content.pipe.interfaces.OutputInterfaceFilter.FilterType.FLUID, "minecraft:water", ItemStack.EMPTY);
        helper.assertTrue(filter.matchesFluid(new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1000)), "应通过 ID 匹配水流体！");
        helper.assertTrue(!filter.matchesFluid(new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.LAVA, 1000)), "不应匹配未配置的岩浆！");

        // 3. 测试化学品 ID 过滤
        var nitrogenStack = com.complexindustries.mekanism.registration.MCIChemicals.NITROGEN.asStack(1000);
        var nobleGasStack = com.complexindustries.mekanism.registration.MCIChemicals.NOBLE_GAS.asStack(1000);

        filter.setFilter(3, com.complexindustries.mekanism.content.pipe.interfaces.OutputInterfaceFilter.FilterType.CHEMICAL, "nitrogen", ItemStack.EMPTY);
        helper.assertTrue(filter.matchesChemical(nitrogenStack), "应通过化学品名称匹配氮气！");
        helper.assertTrue(!filter.matchesChemical(nobleGasStack), "不应匹配未配置的稀有气体！");

        // 4. 测试 NBT 保存与恢复
        var registries = helper.getLevel().registryAccess();
        var tag = filter.save(registries);
        var loadedFilter = new com.complexindustries.mekanism.content.pipe.interfaces.OutputInterfaceFilter();
        loadedFilter.load(tag, registries);

        helper.assertTrue(loadedFilter.matchesItem(new ItemStack(net.minecraft.world.item.Items.IRON_INGOT)), "NBT 恢复后仍应匹配铁锭！");
        helper.assertTrue(loadedFilter.matchesFluid(new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1000)), "NBT 恢复后仍应匹配水！");
        helper.assertTrue(loadedFilter.matchesChemical(nitrogenStack), "NBT 恢复后仍应匹配氮气！");

        // 5. 测试候选提取与动态匹配
        var ironCandidates = com.complexindustries.mekanism.content.pipe.interfaces.OutputInterfaceFilter.extractCandidates(new ItemStack(net.minecraft.world.item.Items.IRON_INGOT));
        helper.assertTrue(!ironCandidates.isEmpty(), "铁锭应提取出候选类型！");
        helper.assertTrue(ironCandidates.stream().anyMatch(c -> c.id().contains("iron_ingot")), "铁锭候选应包含自身 ID！");

        filter.setFilter(4, com.complexindustries.mekanism.content.pipe.interfaces.OutputInterfaceFilter.FilterType.ITEM, "minecraft:iron_ingot", ItemStack.EMPTY);
        var matchingStacks = filter.getMatchingItemStacks(4);
        helper.assertTrue(!matchingStacks.isEmpty() && matchingStacks.get(0).is(net.minecraft.world.item.Items.IRON_INGOT), "匹配列表应返回铁锭！");
        helper.assertTrue(!filter.getDisplayStack(4, 20).isEmpty(), "循环显示应在指定刻返回有效显示物品！");

        helper.succeed();
    }

    @GameTest(template = "empty_10x10x10")
    public static void testInterfaceRedstoneControl(GameTestHelper helper) {
        BlockPos inPos = new BlockPos(1, 1, 1);
        BlockPos outPos = new BlockPos(2, 1, 1);
        helper.setBlock(inPos, com.complexindustries.mekanism.registration.MCIBlocks.INPUT_INTERFACE.get().defaultBlockState());
        helper.setBlock(outPos, com.complexindustries.mekanism.registration.MCIBlocks.OUTPUT_INTERFACE.get().defaultBlockState());

        var inTile = (com.complexindustries.mekanism.content.pipe.TileEntityInputInterface) helper.getBlockEntity(inPos);
        var outTile = (com.complexindustries.mekanism.content.pipe.TileEntityOutputInterface) helper.getBlockEntity(outPos);
        helper.assertTrue(inTile != null && outTile != null, "接口实体必须成功创建！");

        // 默认模式 DISABLED -> 始终可以工作
        helper.assertTrue(inTile.canOperate(), "DISABLED 模式下输入接口必须能正常工作！");
        helper.assertTrue(outTile.canOperate(), "DISABLED 模式下输出接口必须能正常工作！");

        // 切换为 HIGH -> 无红石信号时不工作
        inTile.setRedstoneMode(mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl.HIGH);
        outTile.setRedstoneMode(mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl.HIGH);
        helper.assertTrue(!inTile.canOperate(), "HIGH 模式且无红石时输入接口应禁止工作！");
        helper.assertTrue(!outTile.canOperate(), "HIGH 模式且无红石时输出接口应禁止工作！");

        // 切换为 LOW -> 无红石信号时正常工作
        inTile.setRedstoneMode(mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl.LOW);
        outTile.setRedstoneMode(mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl.LOW);
        helper.assertTrue(inTile.canOperate(), "LOW 模式且无红石时输入接口应正常工作！");
        helper.assertTrue(outTile.canOperate(), "LOW 模式且无红石时输出接口应正常工作！");

        helper.succeed();
    }

    private static void build3x3x3Extractor(GameTestHelper helper, BlockPos startPos) {
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 3; z++) {
                    BlockPos p = startPos.offset(x, y, z);
                    int boundCoords = (x == 0 || x == 2 ? 1 : 0) + (y == 0 || y == 2 ? 1 : 0) + (z == 0 || z == 2 ? 1 : 0);

                    if (boundCoords >= 2) {
                        // Frame edges and corners
                        helper.setBlock(p, MCIBlocks.FLUID_EXTRACTOR_CASING.get());
                    } else if (y == 0) {
                        // Floor center: Powered Pump
                        helper.setBlock(p, MCIBlocks.POWERED_PUMP.get());
                    } else if (y == 1) {
                        if (x == 1 && z == 1) {
                            // Cavity interior
                            helper.setBlock(p, Blocks.AIR);
                        } else if (x == 1 && z == 0) {
                            // Face center: Port
                            helper.setBlock(p, MCIBlocks.FLUID_EXTRACTOR_PORT.get());
                        } else {
                            // Other face centers
                            helper.setBlock(p, MCIBlocks.FLUID_EXTRACTOR_CASING.get());
                        }
                    } else {
                        // Roof center
                        helper.setBlock(p, MCIBlocks.FLUID_EXTRACTOR_CASING.get());
                    }
                }
            }
        }
    }

    @GameTest(template = "empty_10x10x10", timeoutTicks = 60)
    public static void testFluidExtractorFormation(GameTestHelper helper) {
        BlockPos origin = new BlockPos(1, 1, 1);
        build3x3x3Extractor(helper, origin);

        BlockPos portPos = origin.offset(1, 1, 0);
        helper.runAfterDelay(10, () -> {
            var tile = helper.getBlockEntity(portPos);
            helper.assertTrue(tile instanceof com.complexindustries.mekanism.content.extractor.TileEntityFluidExtractorPort, "抽取器接口实体必须存在！");
            var portTile = (com.complexindustries.mekanism.content.extractor.TileEntityFluidExtractorPort) tile;

            portTile.getStructure().tick(portTile, true);
            var res = portTile.getStructure().runUpdate(portTile);
            var mb = portTile.getMultiblock();
            helper.assertTrue(mb.isFormed(), "流体抽取器应该成功成型！: " + (res != null && res.getResultText() != null ? res.getResultText().getString() : "未知原因"));
            helper.assertTrue(mb.getPumpCount() == 1, "流体抽取器底面应检测到1台动力泵机！");
            helper.assertTrue(mb.getTankCapacity() == 16000, "1x1x1腔体的储罐容量应为16,000 mB！");
            helper.succeed();
        });
    }

    @GameTest(template = "empty_10x10x10", timeoutTicks = 60)
    public static void testFluidExtractorWaterExtraction(GameTestHelper helper) {
        BlockPos origin = new BlockPos(1, 2, 1);
        // Place water directly below the pump at (2, 1, 2)
        helper.setBlock(origin.offset(1, -1, 1), Blocks.WATER);
        build3x3x3Extractor(helper, origin);

        BlockPos portPos = origin.offset(1, 1, 0);
        helper.runAfterDelay(10, () -> {
            var portTile = (com.complexindustries.mekanism.content.extractor.TileEntityFluidExtractorPort) helper.getBlockEntity(portPos);
            portTile.getStructure().tick(portTile, true);
            portTile.getStructure().runUpdate(portTile);
            var mb = portTile.getMultiblock();
            helper.assertTrue(mb.isFormed(), "抽取器必须成型！");

            mb.scanEnvironment(helper.getLevel());
            helper.assertTrue(mb.environment == com.complexindustries.mekanism.content.extractor.ExtractorEnvironment.WATER, "底面有水时应识别为WATER环境！");

            // 注入电量并执行一次tick抽取
            mb.energyContainer.insert(50_000L, Action.EXECUTE, AutomationType.INTERNAL);
            mb.tick(helper.getLevel());

            helper.assertTrue(mb.fluidTank.getFluid().is(net.minecraft.world.level.material.Fluids.WATER), "抽取流体应为水！");
            helper.assertTrue(mb.fluidTank.getFluidAmount() == 200, "1台泵机单tick应抽取200 mB！实际: " + mb.fluidTank.getFluidAmount());
            helper.succeed();
        });
    }

    @GameTest(template = "empty_10x10x10", timeoutTicks = 60)
    public static void testFluidExtractorAirExtraction(GameTestHelper helper) {
        BlockPos origin = new BlockPos(1, 2, 1);
        // Place air directly below the pump at (2, 1, 2)
        helper.setBlock(origin.offset(1, -1, 1), Blocks.AIR);
        build3x3x3Extractor(helper, origin);

        BlockPos portPos = origin.offset(1, 1, 0);
        helper.runAfterDelay(10, () -> {
            var portTile = (com.complexindustries.mekanism.content.extractor.TileEntityFluidExtractorPort) helper.getBlockEntity(portPos);
            portTile.getStructure().tick(portTile, true);
            portTile.getStructure().runUpdate(portTile);
            var mb = portTile.getMultiblock();
            helper.assertTrue(mb.isFormed(), "抽取器必须成型！");

            mb.scanEnvironment(helper.getLevel());
            helper.assertTrue(mb.environment == com.complexindustries.mekanism.content.extractor.ExtractorEnvironment.AIR, "底面为空气时应识别为AIR环境！");

            // 注入电量并执行tick抽取
            mb.energyContainer.insert(50_000L, Action.EXECUTE, AutomationType.INTERNAL);
            mb.tick(helper.getLevel());

            helper.assertTrue(mb.fluidTank.getFluid().is(MCIFluids.SOURCE_LIQUID_AIR.get()), "抽取流体应为液态空气！");
            helper.assertTrue(mb.fluidTank.getFluidAmount() == 200, "1台泵机单tick应抽取200 mB！实际: " + mb.fluidTank.getFluidAmount());
            helper.succeed();
        });
    }

    @GameTest(template = "empty_15x25x15", timeoutTicks = 80)
    public static void testFluidExtractorLavaDetection(GameTestHelper helper) {
        BlockPos origin = new BlockPos(1, 2, 1);
        // 1. 测试不足2048方块岩浆：仅放置1个岩浆方块
        helper.setBlock(origin.offset(1, -1, 1), Blocks.LAVA);
        build3x3x3Extractor(helper, origin);

        BlockPos portPos = origin.offset(1, 1, 0);
        helper.runAfterDelay(10, () -> {
            var portTile = (com.complexindustries.mekanism.content.extractor.TileEntityFluidExtractorPort) helper.getBlockEntity(portPos);
            portTile.getStructure().tick(portTile, true);
            portTile.getStructure().runUpdate(portTile);
            var mb = portTile.getMultiblock();
            helper.assertTrue(mb.isFormed(), "抽取器必须成型！");

            mb.scanEnvironment(helper.getLevel());
            helper.assertTrue(mb.environment == com.complexindustries.mekanism.content.extractor.ExtractorEnvironment.LAVA_INSUFFICIENT, "少量岩浆应识别为LAVA_INSUFFICIENT！");
            helper.assertTrue(mb.lavaBlockCount == 1, "岩浆数量应为1！");

            // 有电但岩浆不足时禁止抽取
            mb.energyContainer.insert(50_000L, Action.EXECUTE, AutomationType.INTERNAL);
            mb.tick(helper.getLevel());
            helper.assertTrue(mb.fluidTank.isEmpty(), "岩浆不足时不应抽取任何流体！");

            // 2. 填充超过2048方块岩浆 (12 x 15 x 12 = 2160方块)
            BlockPos lavaOrigin = origin.offset(1, -1, 1);
            for (int dy = 0; dy < 15; dy++) {
                for (int dx = 0; dx < 12; dx++) {
                    for (int dz = 0; dz < 12; dz++) {
                        // 避免覆盖抽取器方块
                        BlockPos lp = lavaOrigin.offset(dx, dy, dz);
                        if (lp.getX() >= origin.getX() && lp.getX() < origin.getX() + 3
                                && lp.getY() >= origin.getY() && lp.getY() < origin.getY() + 3
                                && lp.getZ() >= origin.getZ() && lp.getZ() < origin.getZ() + 3) {
                            continue;
                        }
                        helper.setBlock(lp, Blocks.LAVA);
                    }
                }
            }

            mb.scanEnvironment(helper.getLevel());
            helper.assertTrue(mb.environment == com.complexindustries.mekanism.content.extractor.ExtractorEnvironment.LAVA, ">2048方块岩浆应判定为无限岩浆LAVA！");
            helper.assertTrue(mb.lavaBlockCount >= 2049, "BFS应扫描至上限2049！实际: " + mb.lavaBlockCount);

            // 抽取测试
            mb.tick(helper.getLevel());
            helper.assertTrue(mb.fluidTank.getFluid().is(net.minecraft.world.level.material.Fluids.LAVA), "无限岩浆环境下应抽取岩浆！");
            helper.assertTrue(mb.fluidTank.getFluidAmount() == 200, "单tick应抽取200 mB岩浆！");

            helper.succeed();
        });
    }
}
