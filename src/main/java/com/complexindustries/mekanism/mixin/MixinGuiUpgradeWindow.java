package com.complexindustries.mekanism.mixin;

import com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifier;
import mekanism.api.Upgrade;
import mekanism.client.gui.element.window.GuiUpgradeWindow;
import mekanism.common.tile.base.TileEntityMekanism;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = GuiUpgradeWindow.class, remap = false)
public abstract class MixinGuiUpgradeWindow {

    @Shadow
    @Final
    private TileEntityMekanism tile;

    @Redirect(
            method = "renderForeground",
            at = @At(value = "INVOKE", target = "Lmekanism/api/Upgrade;getMax()I"),
            remap = false
    )
    private int mci$getMax(Upgrade instance) {
        if (instance == Upgrade.MUFFLING && this.tile instanceof TileEntityChemicalSolidifier) {
            return 1;
        }
        return instance.getMax();
    }
}
