package com.complexindustries.mekanism.mixin;

import com.complexindustries.mekanism.registration.MCIBlocks;
import com.complexindustries.mekanism.registration.MCIItems;
import com.complexindustries.mekanism.util.MCIUpgradeHelper;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin(value = TileEntityDigitalMiner.class, remap = false)
public abstract class MixinTileEntityDigitalMiner {

    @Shadow
    public abstract boolean getSilkTouch();

    @Inject(method = "canMine", at = @At("HEAD"), cancellable = true, remap = false)
    private void mci$canMineCrudeOil(BlockState state, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() == MCIBlocks.CRUDE_OIL_BLOCK.get()) {
            TileEntityDigitalMiner miner = (TileEntityDigitalMiner) (Object) this;
            if (!MCIUpgradeHelper.isCrudeOilSource(state) || !MCIUpgradeHelper.canHarvestOil(miner)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "getDrops", at = @At("HEAD"), cancellable = true, remap = false)
    private void mci$getDropsForCrudeOil(BlockState state, BlockPos pos, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (state.getBlock() == MCIBlocks.CRUDE_OIL_BLOCK.get()) {
            if (MCIUpgradeHelper.isCrudeOilSource(state)) {
                if (this.getSilkTouch()) {
                    cir.setReturnValue(List.of(new ItemStack(MCIBlocks.SOLID_CRUDE_OIL_ORE.get())));
                } else {
                    int count = 1;
                    TileEntityDigitalMiner miner = (TileEntityDigitalMiner) (Object) this;
                    Level level = miner.getLevel();
                    if (level != null && level.random.nextFloat() < 0.25f) {
                        count += 1;
                    }
                    cir.setReturnValue(List.of(new ItemStack(MCIItems.SOLID_CRUDE_OIL.get(), count)));
                }
            } else {
                cir.setReturnValue(Collections.emptyList());
            }
        }
    }
}
