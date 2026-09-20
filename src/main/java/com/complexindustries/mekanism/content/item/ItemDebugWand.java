package com.complexindustries.mekanism.content.item;

import com.complexindustries.mekanism.content.extractor.TileEntityCrudeOilExtractor;
import com.complexindustries.mekanism.registration.MCIFluids;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.math.FloatingLong;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

public class ItemDebugWand extends Item {

    public ItemDebugWand() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileEntityCrudeOilExtractor extractor) {
                // Instant charge and fill
                extractor.getEnergyContainer().insert(FloatingLong.createConst(1_000_000), Action.EXECUTE, AutomationType.INTERNAL);
                extractor.waterTank.setStack(new FluidStack(Fluids.WATER, 16_000));
                if (player != null) {
                    player.sendSystemMessage(Component.literal("MCI Debug: Extractor at " + pos.toShortString() + " filled with 16k Water & Max Energy.")
                            .withStyle(ChatFormatting.GREEN));
                }
                return InteractionResult.SUCCESS;
            }

            if (player != null && player.isShiftKeyDown()) {
                // Spawn an oil pool around clicked pos
                BlockPos targetCenter = pos.above();
                int radius = 3;
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (dx * dx + dz * dz <= radius * radius) {
                            BlockPos surface = targetCenter.offset(dx, 0, dz);
                            level.setBlock(surface.below(), Blocks.TUFF.defaultBlockState(), 3);
                            level.setBlock(surface, com.complexindustries.mekanism.registration.MCIBlocks.CRUDE_OIL_BLOCK.get().defaultBlockState(), 3);
                        }
                    }
                }
                player.sendSystemMessage(Component.literal("MCI Debug: Spawned Crude Oil Pool (Radius 3) at " + targetCenter.toShortString())
                        .withStyle(ChatFormatting.AQUA));
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            player.sendSystemMessage(Component.literal("MCI Debug Wand: Right-click machine to fill with Water/Energy, or Sneak-right-click ground to spawn Crude Oil.")
                    .withStyle(ChatFormatting.YELLOW));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
