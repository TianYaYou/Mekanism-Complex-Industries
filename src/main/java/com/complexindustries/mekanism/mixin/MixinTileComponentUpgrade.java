package com.complexindustries.mekanism.mixin;

import com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifier;
import mekanism.api.Upgrade;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentUpgrade;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = TileComponentUpgrade.class, remap = false)
public abstract class MixinTileComponentUpgrade {

    @Shadow
    @Final
    private TileEntityMekanism tile;

    @Redirect(
            method = "tickServer",
            at = @At(value = "INVOKE", target = "Lmekanism/api/Upgrade;getMax()I"),
            remap = false
    )
    private int mci$getMaxTick(Upgrade instance) {
        if (instance == Upgrade.MUFFLING && this.tile instanceof TileEntityChemicalSolidifier) {
            return 1;
        }
        return instance.getMax();
    }

    @Redirect(
            method = "addUpgrades(Lmekanism/api/Upgrade;II)I",
            at = @At(value = "INVOKE", target = "Lmekanism/api/Upgrade;getMax()I"),
            remap = false
    )
    private int mci$getMaxAdd(Upgrade instance) {
        if (instance == Upgrade.MUFFLING && this.tile instanceof TileEntityChemicalSolidifier) {
            return 1;
        }
        return instance.getMax();
    }
}
