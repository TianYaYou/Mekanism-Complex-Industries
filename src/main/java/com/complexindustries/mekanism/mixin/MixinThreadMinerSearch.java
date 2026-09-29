package com.complexindustries.mekanism.mixin;

import com.complexindustries.mekanism.registration.MCIBlocks;
import com.complexindustries.mekanism.util.MCIUpgradeHelper;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import mekanism.common.content.filter.SortableFilterManager;
import mekanism.common.content.miner.ThreadMinerSearch;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

@Mixin(value = ThreadMinerSearch.class, remap = false)
public abstract class MixinThreadMinerSearch {

    @Shadow
    private TileEntityDigitalMiner tile;

    @Redirect(
        method = "run",
        at = @At(
            value = "INVOKE",
            target = "Lmekanism/common/content/filter/SortableFilterManager;hasEnabledFilters()Z"
        ),
        remap = false
    )
    private boolean mci$redirectHasEnabledFilters(SortableFilterManager<?> manager) {
        if (MCIUpgradeHelper.hasPetroleumUpgrade(this.tile)) {
            return true;
        }
        return manager.hasEnabledFilters();
    }

    @Redirect(
        method = "run",
        at = @At(
            value = "INVOKE",
            target = "Lmekanism/common/util/MekanismUtils;isLiquidBlock(Lnet/minecraft/world/level/block/Block;)Z"
        ),
        remap = false
    )
    private boolean mci$redirectIsLiquidBlock(Block block) {
        if (block == MCIBlocks.CRUDE_OIL_BLOCK.get()) {
            if (MCIUpgradeHelper.canHarvestOil(this.tile)) {
                return false;
            }
        }
        return MekanismUtils.isLiquidBlock(block);
    }

    @Redirect(
        method = "run",
        at = @At(
            value = "INVOKE",
            target = "Lit/unimi/dsi/fastutil/objects/Reference2BooleanMap;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Predicate;)Z"
        ),
        remap = false
    )
    private boolean mci$redirectComputeIfAbsent(Reference2BooleanMap<Block> map, Object key, Predicate<Block> predicate) {
        if (key == MCIBlocks.CRUDE_OIL_BLOCK.get() && MCIUpgradeHelper.canHarvestOil(this.tile)) {
            return true;
        }
        return map.computeIfAbsent((Block) key, predicate);
    }
}
