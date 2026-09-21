package com.complexindustries.mekanism.mixin;

import com.complexindustries.mekanism.registration.MCIBlocks;
import com.complexindustries.mekanism.registration.MCIItems;
import mekanism.common.content.miner.MinerItemStackFilter;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MinerItemStackFilter.class, remap = false)
public abstract class MixinMinerItemStackFilter {

    @Shadow
    public abstract ItemStack getItemStack();

    @Inject(method = "canFilter", at = @At("HEAD"), cancellable = true, remap = false)
    private void mci$canFilterCrudeOil(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() == MCIBlocks.CRUDE_OIL_BLOCK.get()) {
            ItemStack stack = this.getItemStack();
            if (stack != null && !stack.isEmpty()) {
                Item filterItem = stack.getItem();
                if (filterItem == MCIItems.SOLID_CRUDE_OIL_ORE.get() ||
                    filterItem == MCIItems.DEEPSLATE_SOLID_CRUDE_OIL_ORE.get() ||
                    filterItem == MCIItems.SOLID_CRUDE_OIL.get()) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
