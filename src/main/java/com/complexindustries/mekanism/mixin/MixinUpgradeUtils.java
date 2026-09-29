package com.complexindustries.mekanism.mixin;

import com.complexindustries.mekanism.content.upgrade.MCIUpgrades;
import com.complexindustries.mekanism.registration.MCIItems;
import mekanism.api.Upgrade;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = UpgradeUtils.class, remap = false)
public abstract class MixinUpgradeUtils {

    @Inject(method = "getStack(Lmekanism/api/Upgrade;I)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), cancellable = true, remap = false)
    private static void mci$getStack(Upgrade upgrade, int count, CallbackInfoReturnable<ItemStack> cir) {
        if (upgrade == MCIUpgrades.PETROLEUM) {
            cir.setReturnValue(new ItemStack(MCIItems.PETROLEUM_UPGRADE.get(), count));
        }
    }
}
