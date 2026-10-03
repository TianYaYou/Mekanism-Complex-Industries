package com.complexindustries.mekanism.mixin;

import com.complexindustries.mekanism.content.upgrade.MCIUpgrades;
import mekanism.api.Upgrade;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.EnumSet;
import java.util.Set;

@Mixin(value = TileEntityMekanism.class, remap = false)
public abstract class MixinTileEntityMekanism {

    @Inject(method = "supportsUpgrades", at = @At("HEAD"), cancellable = true, remap = false)
    private void mci$supportsUpgrades(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof com.complexindustries.mekanism.content.tile.TileEntityAirCompressor
                || (Object) this instanceof com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifier) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getSupportedUpgrade", at = @At("RETURN"), cancellable = true, remap = false)
    private void mci$getSupportedUpgrade(CallbackInfoReturnable<Set<Upgrade>> cir) {
        if ((Object) this instanceof TileEntityDigitalMiner) {
            Set<Upgrade> original = cir.getReturnValue();
            if (original != null && !original.contains(MCIUpgrades.PETROLEUM)) {
                Set<Upgrade> modified = EnumSet.copyOf(original);
                modified.add(MCIUpgrades.PETROLEUM);
                cir.setReturnValue(modified);
            }
        }
    }
}
