package com.complexindustries.mekanism.mixin;

import com.complexindustries.mekanism.content.upgrade.MCIUpgrades;
import mekanism.api.Upgrade;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Upgrade.class, remap = false)
public abstract class MixinUpgrade {

    @Inject(method = "getTranslationKey", at = @At("HEAD"), cancellable = true, remap = false)
    private void mci$getTranslationKey(CallbackInfoReturnable<String> cir) {
        if ((Object) this == MCIUpgrades.PETROLEUM) {
            cir.setReturnValue("upgrade.mekanism_complex_industries.petroleum");
        }
    }

    @Inject(method = "getDescription", at = @At("HEAD"), cancellable = true, remap = false)
    private void mci$getDescription(CallbackInfoReturnable<Component> cir) {
        if ((Object) this == MCIUpgrades.PETROLEUM) {
            cir.setReturnValue(Component.translatable("upgrade.mekanism_complex_industries.petroleum.desc"));
        }
    }
}
