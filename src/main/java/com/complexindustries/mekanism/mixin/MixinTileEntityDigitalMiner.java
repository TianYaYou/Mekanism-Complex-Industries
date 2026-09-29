package com.complexindustries.mekanism.mixin;

import com.complexindustries.mekanism.content.miner.PetroleumMinerFilter;
import com.complexindustries.mekanism.content.upgrade.MCIUpgrades;
import com.complexindustries.mekanism.registration.MCIBlocks;
import com.complexindustries.mekanism.registration.MCIItems;
import com.complexindustries.mekanism.util.MCIUpgradeHelper;
import mekanism.api.Upgrade;
import mekanism.common.content.filter.SortableFilterManager;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(value = TileEntityDigitalMiner.class, remap = false)
public abstract class MixinTileEntityDigitalMiner {

    @Shadow
    public abstract boolean getSilkTouch();

    @Inject(method = "canMine", at = @At("HEAD"), cancellable = true, remap = false)
    private void mci$canMineCrudeOil(BlockState state, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() == MCIBlocks.CRUDE_OIL_BLOCK.get()) {
            TileEntityDigitalMiner miner = (TileEntityDigitalMiner) (Object) this;
            if (MCIUpgradeHelper.canHarvestOil(miner) && MCIUpgradeHelper.isCrudeOilSource(state)) {
                cir.setReturnValue(true);
            } else {
                cir.setReturnValue(false);
            }
        }
    }

    @Redirect(
        method = "tryMineBlock",
        at = @At(
            value = "INVOKE",
            target = "Lmekanism/common/content/filter/SortableFilterManager;getEnabledFilters()Ljava/util/List;"
        ),
        remap = false
    )
    private List<MinerFilter<?>> mci$redirectGetEnabledFilters(SortableFilterManager<MinerFilter<?>> manager) {
        List<MinerFilter<?>> original = manager.getEnabledFilters();
        TileEntityDigitalMiner miner = (TileEntityDigitalMiner) (Object) this;
        if (MCIUpgradeHelper.canHarvestOil(miner)) {
            List<MinerFilter<?>> list = new ArrayList<>(original.size() + 1);
            list.addAll(original);
            list.add(PetroleumMinerFilter.INSTANCE);
            return list;
        }
        return original;
    }

    @Inject(method = "setReplace", at = @At("HEAD"), cancellable = true, remap = false)
    private void mci$setReplaceCrudeOil(BlockState state, BlockPos pos, MinerFilter<?> filter, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() == MCIBlocks.CRUDE_OIL_BLOCK.get()) {
            TileEntityDigitalMiner miner = (TileEntityDigitalMiner) (Object) this;
            Level level = miner.getLevel();
            if (level != null) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
                level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(null, state));
                cir.setReturnValue(true);
            } else {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "getDrops", at = @At("HEAD"), cancellable = true, remap = false)
    private void mci$getDropsForCrudeOil(BlockState state, BlockPos pos, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (state.getBlock() == MCIBlocks.CRUDE_OIL_BLOCK.get()) {
            if (MCIUpgradeHelper.isCrudeOilSource(state)) {
                if (this.getSilkTouch()) {
                    cir.setReturnValue(List.of(new ItemStack(MCIBlocks.SOLID_CRUDE_OIL_ORE.get())));
                } else {
                    int count = 1;
                    TileEntityDigitalMiner miner = (TileEntityDigitalMiner) (Object) this;
                    Level level = miner.getLevel();
                    if (level != null && level.random.nextFloat() < 0.25f) {
                        count += 1;
                    }
                    cir.setReturnValue(List.of(new ItemStack(MCIItems.SOLID_CRUDE_OIL.get(), count)));
                }
            } else {
                cir.setReturnValue(Collections.emptyList());
            }
        }
    }

    @Inject(method = "getInfo", at = @At("HEAD"), cancellable = true, remap = false)
    private void mci$getInfo(Upgrade upgrade, CallbackInfoReturnable<List<Component>> cir) {
        if (upgrade == MCIUpgrades.PETROLEUM) {
            cir.setReturnValue(List.of(Component.translatable("upgrade.mekanism_complex_industries.petroleum.info")));
        }
    }
}
