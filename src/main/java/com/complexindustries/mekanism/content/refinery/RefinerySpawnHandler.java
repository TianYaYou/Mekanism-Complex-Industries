package com.complexindustries.mekanism.content.refinery;

import com.complexindustries.mekanism.MCIConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@EventBusSubscriber(modid = MCIConstants.MODID)
public class RefinerySpawnHandler {

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof Level level) {
            RefineryMultiblockData.onLevelUnload(level);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        RefineryMultiblockData.clearAll();
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        RefineryMultiblockData.clearAll();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onSpawnPlacementCheck(MobSpawnEvent.SpawnPlacementCheck event) {
        Level level = event.getLevel() != null ? event.getLevel().getLevel() : null;
        if (level == null) {
            return;
        }
        EntityType<?> type = event.getEntityType();
        if (type == null) {
            return;
        }
        if (RefineryMultiblockData.shouldSuppressSpawn(level, event.getPos(), type.getCategory(), type.getBaseClass(), event.getSpawnType())) {
            event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.FAIL);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPositionCheck(MobSpawnEvent.PositionCheck event) {
        Level level = event.getLevel() != null ? event.getLevel().getLevel() : null;
        if (level == null) {
            return;
        }
        BlockPos pos = BlockPos.containing(event.getX(), event.getY(), event.getZ());
        Mob mob = event.getEntity();
        if (RefineryMultiblockData.shouldSuppressSpawn(level, pos, mob, event.getSpawnType())) {
            event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        Level level = event.getLevel() != null ? event.getLevel().getLevel() : null;
        if (level == null) {
            return;
        }
        BlockPos pos = BlockPos.containing(event.getX(), event.getY(), event.getZ());
        Mob mob = event.getEntity();
        if (RefineryMultiblockData.shouldSuppressSpawn(level, pos, mob, event.getSpawnType())) {
            event.setSpawnCancelled(true);
            event.setCanceled(true);
        }
    }
}
