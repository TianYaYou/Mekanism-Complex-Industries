package com.complexindustries.mekanism.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mekanism.common.lib.math.Pos3D;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class UltravioletLaserParticle extends TextureSheetParticle {

    private static final float RADIAN_45 = 45 * Mth.DEG_TO_RAD;
    private static final float RADIAN_90 = 90 * Mth.DEG_TO_RAD;

    private final Direction direction;
    private final float halfLength;

    public UltravioletLaserParticle(ClientLevel world, Vec3 start, Vec3 end, Direction dir, float energyScale) {
        super(world, (start.x + end.x) / 2D, (start.y + end.y) / 2D, (start.z + end.z) / 2D);
        lifetime = 5;
        // Deep vibrant ultraviolet / purple color with visible fading over distance
        rCol = 0.78F;
        gCol = 0.15F;
        bCol = 1.00F;
        double dist = end.distanceTo(start);
        alpha = (float) Math.max(0.18, 0.45 * (1.0 - (dist / 15.0)));
        quadSize = Math.max(0.10F, energyScale);
        halfLength = (float) (dist / 2);
        direction = dir;
        updateBoundingBox();
    }

    @Override
    protected int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    @Override
    public void render(@NotNull VertexConsumer vertexBuilder, Camera renderInfo, float partialTicks) {
        Vec3 view = renderInfo.getPosition();
        float newX = (float) (Mth.lerp(partialTicks, xo, x) - view.x());
        float newY = (float) (Mth.lerp(partialTicks, yo, y) - view.y());
        float newZ = (float) (Mth.lerp(partialTicks, zo, z) - view.z());
        float uMin = getU0();
        float uMax = getU1();
        float vMin = getV0();
        float vMax = getV1();
        int light = getLightColor(partialTicks);
        float quadSize = getQuadSize(partialTicks);
        Quaternionf quaternion = direction.getRotation();
        quaternion.mul(Axis.YP.rotation(RADIAN_45));
        drawComponent(vertexBuilder, getResultVector(quaternion, newX, newY, newZ, quadSize), uMin, uMax, vMin, vMax, light);
        Quaternionf quaternion2 = new Quaternionf(quaternion);
        quaternion2.mul(Axis.YP.rotation(RADIAN_90));
        drawComponent(vertexBuilder, getResultVector(quaternion2, newX, newY, newZ, quadSize), uMin, uMax, vMin, vMax, light);
    }

    private Vector3f[] getResultVector(Quaternionf quaternion, float newX, float newY, float newZ, float quadSize) {
        Vector3f[] resultVector = {
                new Vector3f(-quadSize, -halfLength, 0),
                new Vector3f(-quadSize, halfLength, 0),
                new Vector3f(quadSize, halfLength, 0),
                new Vector3f(quadSize, -halfLength, 0)
        };
        for (Vector3f vec : resultVector) {
            quaternion.transform(vec);
            vec.add(newX, newY, newZ);
        }
        return resultVector;
    }

    private void drawComponent(VertexConsumer vertexBuilder, Vector3f[] resultVector, float uMin, float uMax, float vMin, float vMax, int light) {
        addVertex(vertexBuilder, resultVector[0], uMax, vMax, light);
        addVertex(vertexBuilder, resultVector[1], uMax, vMin, light);
        addVertex(vertexBuilder, resultVector[2], uMin, vMin, light);
        addVertex(vertexBuilder, resultVector[3], uMin, vMax, light);
        // Draw back faces
        addVertex(vertexBuilder, resultVector[1], uMax, vMin, light);
        addVertex(vertexBuilder, resultVector[0], uMax, vMax, light);
        addVertex(vertexBuilder, resultVector[3], uMin, vMax, light);
        addVertex(vertexBuilder, resultVector[2], uMin, vMin, light);
    }

    private void addVertex(VertexConsumer vertexBuilder, Vector3f pos, float u, float v, int light) {
        vertexBuilder.addVertex(pos.x(), pos.y(), pos.z())
                .setUv(u, v)
                .setColor(rCol, gCol, bCol, alpha)
                .setLight(light);
    }

    @NotNull
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected void setSize(float particleWidth, float particleHeight) {
        if (particleWidth != this.bbWidth || particleHeight != this.bbHeight) {
            this.bbWidth = particleWidth;
            this.bbHeight = particleHeight;
        }
    }

    @Override
    public void setPos(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        if (direction != null) {
            updateBoundingBox();
        }
    }

    private void updateBoundingBox() {
        float halfDiameter = quadSize / 2;
        setBoundingBox(switch (direction) {
            case DOWN, UP -> new AABB(x - halfDiameter, y - halfLength, z - halfDiameter, x + halfDiameter, y + halfLength, z + halfDiameter);
            case NORTH, SOUTH -> new AABB(x - halfDiameter, y - halfDiameter, z - halfLength, x + halfDiameter, y + halfDiameter, z + halfLength);
            case WEST, EAST -> new AABB(x - halfLength, y - halfDiameter, z - halfDiameter, x + halfLength, y + halfDiameter, z + halfDiameter);
        });
    }

    @Override
    public AABB getRenderBoundingBox(float partialTicks) {
        return getBoundingBox();
    }

    public static class Factory implements ParticleProvider<UltravioletLaserParticleData> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public UltravioletLaserParticle createParticle(UltravioletLaserParticleData data, @NotNull ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            Pos3D start = new Pos3D(x, y, z);
            Pos3D end = start.translate(data.direction(), data.distance());
            UltravioletLaserParticle particleLaser = new UltravioletLaserParticle(world, start, end, data.direction(), data.energyScale());
            particleLaser.pickSprite(this.spriteSet);
            return particleLaser;
        }
    }
}
