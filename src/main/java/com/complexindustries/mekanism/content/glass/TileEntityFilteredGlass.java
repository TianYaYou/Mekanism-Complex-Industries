package com.complexindustries.mekanism.content.glass;

import java.util.Comparator;
import java.util.List;
import com.complexindustries.mekanism.client.particle.UltravioletLaserParticleData;
import com.complexindustries.mekanism.registration.MCIBlocks;
import mekanism.api.IContentsListener;
import mekanism.api.heat.HeatAPI;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.heat.HeatCapacitorHelper;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.math.Pos3D;
import mekanism.common.registries.MekanismDamageTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityFilteredGlass extends TileEntityMekanism {

    private static final ThreadLocal<Integer> LASER_DEPTH = ThreadLocal.withInitial(() -> 0);

    private BasicHeatCapacitor heatCapacitor;
    private final ILaserReceptor[] sidedReceptors = new ILaserReceptor[6];
    private final ILaserReceptor nullSideReceptor;

    public TileEntityFilteredGlass(BlockPos pos, BlockState state) {
        super(MCIBlocks.FILTERED_GLASS, pos, state);
        for (Direction dir : Direction.values()) {
            sidedReceptors[dir.ordinal()] = new SidedFilteredLaserReceptor(dir);
        }
        nullSideReceptor = new SidedFilteredLaserReceptor(Direction.DOWN);
    }

    public ILaserReceptor getLaserReceptor(@Nullable Direction side) {
        return side == null ? nullSideReceptor : sidedReceptors[side.ordinal()];
    }

    @NotNull
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        HeatCapacitorHelper builder = HeatCapacitorHelper.forSide(facingSupplier);
        builder.addCapacitor(heatCapacitor = BasicHeatCapacitor.create(
                100.0,
                HeatAPI.DEFAULT_INVERSE_CONDUCTION,
                HeatAPI.DEFAULT_INVERSE_INSULATION,
                ambientTemperature,
                listener));
        return builder.build();
    }

    public double getTemperature() {
        return heatCapacitor != null ? heatCapacitor.getTemperature() : HeatAPI.AMBIENT_TEMP;
    }

    public BasicHeatCapacitor getHeatCapacitor() {
        return heatCapacitor;
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        simulate();
        if (getTemperature() > 373.15 && level != null) {
            AABB above = new AABB(worldPosition.above());
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, above)) {
                if (entity.onGround() && !entity.fireImmune() && !entity.isSteppingCarefully()) {
                    entity.igniteForSeconds(4);
                    entity.hurt(level.damageSources().hotFloor(), (float) Math.min(20.0, 1.0 + (getTemperature() - 373.15) / 100.0));
                }
            }
        }
        return sendUpdatePacket;
    }

    public void receiveLaserEnergyFrom(long incomingEnergy, Direction hitSide) {
        if (incomingEnergy <= 0) {
            return;
        }
        // 10% penetrates as Ultraviolet Laser, 90% converts into heat
        long transmittedEnergy = (long) (incomingEnergy * 0.10);
        long heatEnergy = incomingEnergy - transmittedEnergy;

        if (heatCapacitor != null) {
            heatCapacitor.handleHeat(heatEnergy);
            heatCapacitor.update();
        }

        if (transmittedEnergy > 0 && level instanceof ServerLevel serverLevel) {
            if (LASER_DEPTH.get() < 16) {
                try {
                    LASER_DEPTH.set(LASER_DEPTH.get() + 1);
                    fireUltravioletLaser(serverLevel, hitSide.getOpposite(), transmittedEnergy);
                } finally {
                    LASER_DEPTH.set(LASER_DEPTH.get() - 1);
                }
            }
        }
    }

    public static final double MAX_UV_RANGE = 10.0;

    private void fireUltravioletLaser(ServerLevel serverLevel, Direction outDirection, long energy) {
        Pos3D from = Pos3D.create(this).centre().translate(outDirection, 0.501);
        Pos3D to = from.translate(outDirection, MAX_UV_RANGE - 0.002);

        BlockHitResult result = serverLevel.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, net.minecraft.world.phys.shapes.CollisionContext.empty()));
        if (result.getType() != Type.MISS) {
            to = new Pos3D(result.getLocation());
        }

        double distance = to.distance(from);
        // Percentage decay over distance: 0% at 0m down to 100% loss at 10m
        double decayFactor = Math.max(0.0, 1.0 - (distance / MAX_UV_RANGE));
        long attenuatedEnergy = (long) (energy * decayFactor);

        // Visual beam scale is based on emitted energy (always visible across air, min scale 0.12F)
        float laserEnergyScale = (float) Math.min(((double) energy / MekanismConfig.usage.laser.get()) / 10D, 0.6D);
        if (laserEnergyScale < 0.12F && energy > 0) {
            laserEnergyScale = 0.12F;
        }

        // UV laser is harmless to entities and items (no damage, no item destruction)
        sendLaserDataToPlayers(serverLevel, new UltravioletLaserParticleData(outDirection, distance, laserEnergyScale), from);

        if (attenuatedEnergy > 0 && result.getType() != Type.MISS) {
            BlockPos hitPos = result.getBlockPos();
            ILaserReceptor nextReceptor = WorldUtils.getCapability(serverLevel, Capabilities.LASER_RECEPTOR, hitPos, result.getDirection());
            if (nextReceptor != null && !nextReceptor.canLasersDig()) {
                nextReceptor.receiveLaserEnergy(attenuatedEnergy);
            }
        }
    }

    private void sendLaserDataToPlayers(ServerLevel serverWorld, UltravioletLaserParticleData data, Vec3 from) {
        for (ServerPlayer player : serverWorld.players()) {
            serverWorld.sendParticles(player, data, true, from.x, from.y, from.z, 1, 0, 0, 0, 0);
        }
    }

    private AABB getLaserBox(Direction direction, Vec3 from, Vec3 to, float energyScale) {
        AABB aabb = new AABB(from, to);
        double halfDiameter = energyScale / 2;
        return switch (direction) {
            case DOWN, UP -> aabb.inflate(halfDiameter, 0, halfDiameter);
            case NORTH, SOUTH -> aabb.inflate(halfDiameter, halfDiameter, 0);
            case WEST, EAST -> aabb.inflate(0, halfDiameter, halfDiameter);
        };
    }

    private class SidedFilteredLaserReceptor implements ILaserReceptor {
        private final Direction hitSide;

        public SidedFilteredLaserReceptor(Direction hitSide) {
            this.hitSide = hitSide;
        }

        @Override
        public void receiveLaserEnergy(long energy) {
            TileEntityFilteredGlass.this.receiveLaserEnergyFrom(energy, hitSide);
        }

        @Override
        public boolean canLasersDig() {
            return false;
        }
    }
}
