package com.complexindustries.mekanism.network;

import mekanism.api.security.IBlockSecurityUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public final class MCIPacketSecurity {

    private MCIPacketSecurity() {}

    /**
     * Verifies that the player is valid, the target chunk is loaded, the player is within reach
     * distance (adapting to player blockInteractionRange with a minimum baseline of 8 blocks),
     * and the player has security permission to interact with the block entity.
     */
    public static boolean canAccess(Player player, BlockPos pos, @Nullable BlockEntity blockEntity) {
        if (player == null || player.level() == null || pos == null) {
            return false;
        }
        if (!WorldUtils.isBlockLoaded(player.level(), pos)) {
            return false;
        }
        // Dynamic interaction reach check: incorporates player's block interaction range + margin,
        // with a minimum reach baseline of 8 blocks (64 blocks squared)
        double reach = player.blockInteractionRange() + 3.0;
        double maxReachSqr = Math.max(64.0, reach * reach);
        if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > maxReachSqr) {
            return false;
        }
        // Resolve target block entity if not supplied
        BlockEntity target = blockEntity != null ? blockEntity : WorldUtils.getTileEntity(player.level(), pos);
        // Security and ownership check
        return IBlockSecurityUtils.INSTANCE.canAccessOrDisplayError(player, player.level(), pos, null, target);
    }
}
