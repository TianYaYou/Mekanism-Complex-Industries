package com.complexindustries.mekanism.mixin;

import com.complexindustries.mekanism.content.upgrade.ItemPetroleumUpgrade;
import mekanism.api.AutomationType;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.UpgradeInventorySlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BasicInventorySlot.class, remap = false)
public abstract class MixinUpgradeInventorySlot {

    @Inject(method = "isItemValidForInsertion", at = @At("HEAD"), cancellable = true, remap = false)
    private void mci$isItemValidForInsertion(ItemStack stack, AutomationType automationType, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof UpgradeInventorySlot && !stack.isEmpty() && stack.getItem() instanceof ItemPetroleumUpgrade) {
            cir.setReturnValue(true);
        }
    }
}
