package com.complexindustries.mekanism.client.render;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.content.pipe.TileEntityIndustrialPipe;
import com.complexindustries.mekanism.content.pipe.attachment.IPipeAttachment;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class IndustrialPipeRenderer implements BlockEntityRenderer<TileEntityIndustrialPipe> {

    private static final ResourceLocation INPUT_FRONT = ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "textures/block/input_interface_front.png");
    private static final ResourceLocation INPUT_SIDE = ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "textures/block/input_interface_side.png");

    private static final ResourceLocation OUTPUT_FRONT = ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "textures/block/output_interface_front.png");
    private static final ResourceLocation OUTPUT_SIDE = ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "textures/block/output_interface_side.png");

    private static final ResourceLocation POWER_FRONT = ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "textures/block/power_interface_front.png");
    private static final ResourceLocation POWER_SIDE = ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "textures/block/power_interface_side.png");

    public IndustrialPipeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TileEntityIndustrialPipe pipe, float partialTick, PoseStack matrix, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        IPipeAttachment[] attachments = pipe.getAllAttachments();

        for (Direction dir : Direction.values()) {
            IPipeAttachment att = attachments[dir.ordinal()];
            if (att == null) {
                continue;
            }

            ResourceLocation frontTex;
            ResourceLocation sideTex;
            switch (att.getType()) {
                case INPUT -> {
                    frontTex = INPUT_FRONT;
                    sideTex = INPUT_SIDE;
                }
                case OUTPUT -> {
                    frontTex = OUTPUT_FRONT;
                    sideTex = OUTPUT_SIDE;
                }
                case POWER -> {
                    frontTex = POWER_FRONT;
                    sideTex = POWER_SIDE;
                }
                default -> {
                    frontTex = INPUT_FRONT;
                    sideTex = INPUT_SIDE;
                }
            }

            matrix.pushPose();
            matrix.translate(0.5, 0.5, 0.5);
            matrix.mulPose(dir.getRotation());
            Matrix4f pose = matrix.last().pose();

            // 1. Interface Plate Body (Y = 0.375 to 0.500, 12x12 width)
            VertexConsumer sideConsumer = bufferSource.getBuffer(RenderType.entitySolid(sideTex));
            renderPlateSidesAndBottom(sideConsumer, pose, -0.375f, 0.375f, 0.375f, 0.500f, -0.375f, 0.375f, combinedLight);

            // 2. Interface Front Face Plate (Y = 0.500, facing outward)
            VertexConsumer frontConsumer = bufferSource.getBuffer(RenderType.entitySolid(frontTex));
            renderFace(frontConsumer, pose,
                    -0.375f, 0.500f, 0.375f, 0.0f, 1.0f,
                    0.375f, 0.500f, 0.375f, 1.0f, 1.0f,
                    0.375f, 0.500f, -0.375f, 1.0f, 0.0f,
                    -0.375f, 0.500f, -0.375f, 0.0f, 0.0f,
                    combinedLight, 0, 1, 0);

            matrix.popPose();
        }
    }

    private void renderPlateSidesAndBottom(VertexConsumer consumer, Matrix4f pose,
                                           float minX, float maxX, float minY, float maxY, float minZ, float maxZ, int light) {
        // Bottom (-Y) facing pipe core
        renderFace(consumer, pose,
                minX, minY, minZ, 0, 1,
                maxX, minY, minZ, 1, 1,
                maxX, minY, maxZ, 1, 0,
                minX, minY, maxZ, 0, 0,
                light, 0, -1, 0);
        // North (-Z)
        renderFace(consumer, pose,
                maxX, minY, minZ, 0, 1,
                minX, minY, minZ, 1, 1,
                minX, maxY, minZ, 1, 0,
                maxX, maxY, minZ, 0, 0,
                light, 0, 0, -1);
        // South (+Z)
        renderFace(consumer, pose,
                minX, minY, maxZ, 0, 1,
                maxX, minY, maxZ, 1, 1,
                maxX, maxY, maxZ, 1, 0,
                minX, maxY, maxZ, 0, 0,
                light, 0, 0, 1);
        // West (-X)
        renderFace(consumer, pose,
                minX, minY, minZ, 0, 1,
                minX, minY, maxZ, 1, 1,
                minX, maxY, maxZ, 1, 0,
                minX, maxY, minZ, 0, 0,
                light, -1, 0, 0);
        // East (+X)
        renderFace(consumer, pose,
                maxX, minY, maxZ, 0, 1,
                maxX, minY, minZ, 1, 1,
                maxX, maxY, minZ, 1, 0,
                maxX, maxY, maxZ, 0, 0,
                light, 1, 0, 0);
    }


    private void renderFace(VertexConsumer consumer, Matrix4f pose,
                            float x1, float y1, float z1, float u1, float v1,
                            float x2, float y2, float z2, float u2, float v2,
                            float x3, float y3, float z3, float u3, float v3,
                            float x4, float y4, float z4, float u4, float v4,
                            int light, float nx, float ny, float nz) {
        consumer.addVertex(pose, x1, y1, z1).setColor(255, 255, 255, 255).setUv(u1, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(nx, ny, nz);
        consumer.addVertex(pose, x2, y2, z2).setColor(255, 255, 255, 255).setUv(u2, v2)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(nx, ny, nz);
        consumer.addVertex(pose, x3, y3, z3).setColor(255, 255, 255, 255).setUv(u3, v3)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(nx, ny, nz);
        consumer.addVertex(pose, x4, y4, z4).setColor(255, 255, 255, 255).setUv(u4, v4)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(nx, ny, nz);
    }
}
