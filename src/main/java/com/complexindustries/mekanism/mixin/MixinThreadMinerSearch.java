package com.complexindustries.mekanism.mixin;

import com.complexindustries.mekanism.registration.MCIBlocks;
import com.complexindustries.mekanism.util.MCIUpgradeHelper;
import mekanism.common.content.filter.SortableFilterManager;
import mekanism.common.content.miner.ThreadMinerSearch;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.BiPredicate;

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

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Redirect(
        method = "run",
        at = @At(
            value = "INVOKE",
            target = "Lmekanism/common/content/filter/SortableFilterManager;anyEnabledMatch(Ljava/lang/Object;Ljava/util/function/BiPredicate;)Z"
        ),
        remap = false
    )
    private boolean mci$redirectAnyEnabledMatch(SortableFilterManager<?> manager, Object state, BiPredicate<?, ?> predicate) {
        if (state instanceof BlockState blockState && blockState.getBlock() == MCIBlocks.CRUDE_OIL_BLOCK.get()) {
            if (MCIUpgradeHelper.canHarvestOil(this.tile)) {
                return true;
            }
        }
        return ((SortableFilterManager) manager).anyEnabledMatch(state, (BiPredicate) predicate);
    }
}
