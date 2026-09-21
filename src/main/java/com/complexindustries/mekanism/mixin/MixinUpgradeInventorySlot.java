package com.complexindustries.mekanism.mixin;

import com.complexindustries.mekanism.content.upgrade.ItemPetroleumUpgrade;
import mekanism.api.AutomationType;
import mekanism.api.Upgrade;
import mekanism.common.inventory.slot.UpgradeInventorySlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(value = UpgradeInventorySlot.class, remap = false)
public abstract class MixinUpgradeInventorySlot {

    @Inject(method = "lambda$new$1", at = @At("HEAD"), cancellable = true, remap = false)
    private static void mci$allowNewUpgrade(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!stack.isEmpty() && stack.getItem() instanceof ItemPetroleumUpgrade) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "lambda$input$0", at = @At("HEAD"), cancellable = true, remap = false)
    private static void mci$allowInputUpgrade(Set<Upgrade> supportedTypes, ItemStack stack, AutomationType automationType, CallbackInfoReturnable<Boolean> cir) {
        if (!stack.isEmpty() && stack.getItem() instanceof ItemPetroleumUpgrade) {
            cir.setReturnValue(true);
        }
    }
}
