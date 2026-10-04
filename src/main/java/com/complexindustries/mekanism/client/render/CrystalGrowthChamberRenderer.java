package com.complexindustries.mekanism.client.render;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.content.chamber.TileEntityCrystalGrowthChamber;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class CrystalGrowthChamberRenderer implements BlockEntityRenderer<TileEntityCrystalGrowthChamber> {

    private static final ResourceLocation BOULE_TEXTURE = ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "textures/block/refined_silicon_boule.png");

    public CrystalGrowthChamberRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TileEntityCrystalGrowthChamber tile, float partialTick, PoseStack matrix, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        float progress = tile.getRenderProgress(partialTick);
        if (progress <= 0.001f) {
            return;
        }

        matrix.pushPose();
        matrix.translate(0.5, 0.0, 0.5);

        // Starts hanging from the seed rod tip at Y = 0.44 down toward crucible at Y = 0.25
        float topY = 0.44f;
        float height = 0.03f + progress * 0.18f;
        float bottomY = topY - height;
        float radius = 0.035f + progress * 0.075f;

        int light = tile.getActive() ? LightTexture.FULL_BRIGHT : combinedLight;
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutout(BOULE_TEXTURE));
        Matrix4f pose = matrix.last().pose();

        int sides = 8;
        for (int i = 0; i < sides; i++) {
            double angle1 = (2 * Math.PI / sides) * i;
            double angle2 = (2 * Math.PI / sides) * (i + 1);

            float x1 = (float) (Math.cos(angle1) * radius);
            float z1 = (float) (Math.sin(angle1) * radius);
            float x2 = (float) (Math.cos(angle2) * radius);
            float z2 = (float) (Math.sin(angle2) * radius);

            float u1 = (float) i / sides;
            float u2 = (float) (i + 1) / sides;

            // Side quad
            vertex(consumer, pose, x1, bottomY, z1, u1, 1.0f, light);
            vertex(consumer, pose, x2, bottomY, z2, u2, 1.0f, light);
            vertex(consumer, pose, x2, topY, z2, u2, 0.0f, light);
            vertex(consumer, pose, x1, topY, z1, u1, 0.0f, light);

            // Bottom cap
            vertex(consumer, pose, 0, bottomY - 0.02f * progress, 0, 0.5f, 0.5f, light);
            vertex(consumer, pose, x2, bottomY, z2, u2, 1.0f, light);
            vertex(consumer, pose, x1, bottomY, z1, u1, 1.0f, light);
            vertex(consumer, pose, 0, bottomY - 0.02f * progress, 0, 0.5f, 0.5f, light);
        }

        matrix.popPose();
    }

    private void vertex(VertexConsumer consumer, Matrix4f pose, float x, float y, float z, float u, float v, int light) {
        consumer.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0, 1, 0);
    }
}
