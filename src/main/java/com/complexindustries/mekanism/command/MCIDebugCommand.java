package com.complexindustries.mekanism.command;

import com.complexindustries.mekanism.content.extractor.TileEntityCrudeOilExtractor;
import com.complexindustries.mekanism.registration.MCIFluids;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.math.FloatingLong;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.fluids.FluidStack;

public class MCIDebugCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mci")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("spawn_oil_pool")
                        .executes(ctx -> spawnOilPool(ctx.getSource(), 4))
                        .then(Commands.argument("radius", IntegerArgumentType.integer(1, 16))
                                .executes(ctx -> spawnOilPool(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "radius")))))
                .then(Commands.literal("fill_machine")
                        .executes(ctx -> fillMachine(ctx.getSource())))
                .then(Commands.literal("locate_oil")
                        .executes(ctx -> locateOil(ctx.getSource(), 32))
                        .then(Commands.argument("radius", IntegerArgumentType.integer(4, 64))
                                .executes(ctx -> locateOil(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "radius")))))
        );
    }

    private static int spawnOilPool(CommandSourceStack source, int radius) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("This command must be executed by a player."));
            return 0;
        }

        Level level = player.level();
        BlockPos center = player.blockPosition();

        int depth = Math.min(3, Math.max(1, radius / 2));
        int created = 0;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double distSq = dx * dx + dz * dz;
                if (distSq <= radius * radius) {
                    for (int dy = 0; dy >= -depth; dy--) {
                        BlockPos target = center.offset(dx, dy, dz);
                        level.setBlock(target, com.complexindustries.mekanism.registration.MCIBlocks.CRUDE_OIL_BLOCK.get().defaultBlockState(), 3);
                        created++;
                    }
                    // Place barrier below pool
                    BlockPos bed = center.offset(dx, -depth - 1, dz);
                    level.setBlock(bed, Blocks.TUFF.defaultBlockState(), 3);
                } else if (distSq <= (radius + 1) * (radius + 1)) {
                    // Place rim around pool
                    for (int dy = 0; dy >= -depth; dy--) {
                        BlockPos rim = center.offset(dx, dy, dz);
                        if (!level.getBlockState(rim).isAir()) {
                            level.setBlock(rim, Blocks.TUFF.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }

        int finalCreated = created;
        source.sendSuccess(() -> Component.literal("Successfully spawned Crude Oil pool (Radius: " + radius + ", Blocks: " + finalCreated + ") at " + center.toShortString())
                .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int fillMachine(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("This command must be executed by a player."));
            return 0;
        }

        HitResult hit = player.pick(5.0D, 0.0F, false);
        BlockPos targetPos = null;
        if (hit.getType() == HitResult.Type.BLOCK) {
            targetPos = ((BlockHitResult) hit).getBlockPos();
        } else {
            targetPos = player.blockPosition().below();
        }

        BlockEntity be = player.level().getBlockEntity(targetPos);
        if (be instanceof TileEntityCrudeOilExtractor extractor) {
            extractor.getEnergyContainer().insert(FloatingLong.createConst(1_000_000), Action.EXECUTE, AutomationType.INTERNAL);
            extractor.waterTank.setStack(new FluidStack(Fluids.WATER, 16_000));
            BlockPos finalTargetPos = targetPos;
            source.sendSuccess(() -> Component.literal("Filled Crude Oil Extractor at " + finalTargetPos.toShortString() + " with 16k Water & Max Energy.")
                    .withStyle(ChatFormatting.GREEN), true);
            return 1;
        }

        source.sendFailure(Component.literal("No Crude Oil Extractor targeted within range."));
        return 0;
    }

    private static int locateOil(CommandSourceStack source, int radius) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("This command must be executed by a player."));
            return 0;
        }

        Level level = player.level();
        BlockPos center = player.blockPosition();
        int count = 0;
        BlockPos closest = null;
        double closestDist = Double.MAX_VALUE;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    BlockPos p = center.offset(dx, dy, dz);
                    if (TileEntityCrudeOilExtractor.isCrudeOilSource(level, p)) {
                        count++;
                        double d = p.distSqr(center);
                        if (d < closestDist) {
                            closestDist = d;
                            closest = p;
                        }
                    }
                }
            }
        }

        if (count > 0 && closest != null) {
            BlockPos finalClosest = closest;
            int finalCount = count;
            source.sendSuccess(() -> Component.literal("Found " + finalCount + " Crude Oil blocks within radius " + radius + ". Closest at " + finalClosest.toShortString())
                    .withStyle(ChatFormatting.AQUA), false);
            return count;
        } else {
            source.sendFailure(Component.literal("No Crude Oil found within radius " + radius + "."));
            return 0;
        }
    }
}
