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
                    valve.setMode(ValveMode.INPUT);
                    valve.cycleMode(fakePlayer);
                    helper.assertTrue(valve.getMode() == ValveMode.OUTPUT, "第1层接口切换后模式应为 OUTPUT");
                    valve.cycleMode(fakePlayer);
                    helper.assertTrue(valve.getMode() == ValveMode.HEAT_INPUT, "第1层接口再次切换后模式应为 HEAT_INPUT");
                    valve.cycleMode(fakePlayer);
                    helper.assertTrue(valve.getMode() == ValveMode.INPUT, "第1层接口第三次切换后应回到 INPUT");
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
                        } else if (cellType == RefineryValidator.TYPE_INNER) {
                            helper.setBlock(pos, MCIBlocks.REFINERY_CASING.get());
                        } else if (cellType == RefineryValidator.TYPE_TIP) {
                            helper.setBlock(pos, MCIBlocks.REFINERY_CASING.get());
                        } else if (cellType == RefineryValidator.TYPE_DIAGONAL) {
                            helper.setBlock(pos, MekanismBlocks.STRUCTURAL_GLASS.get());
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
