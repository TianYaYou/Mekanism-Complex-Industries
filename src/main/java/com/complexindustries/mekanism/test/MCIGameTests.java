package com.complexindustries.mekanism.test;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.content.freezer.TileEntityFreezerController;
import com.complexindustries.mekanism.content.refinery.RefineryValidator;
import com.complexindustries.mekanism.content.refinery.TileEntityRefineryCasing;
import com.complexindustries.mekanism.content.refinery.TileEntityRefineryController;
import com.complexindustries.mekanism.content.refinery.TileEntityRefineryDredgePipe;
import com.complexindustries.mekanism.content.refinery.TileEntityRefineryValve;
import com.complexindustries.mekanism.content.refinery.TileEntityRefineryValve.ValveMode;
import com.complexindustries.mekanism.registration.MCIBlocks;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
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
}
