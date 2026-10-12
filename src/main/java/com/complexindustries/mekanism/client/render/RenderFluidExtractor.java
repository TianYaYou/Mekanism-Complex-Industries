package com.complexindustries.mekanism.client.render;

import com.complexindustries.mekanism.content.extractor.FluidExtractorMultiblockData;
import com.complexindustries.mekanism.content.extractor.TileEntityFluidExtractorPort;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.data.RenderData;
import mekanism.client.render.tileentity.MultiblockTileEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderFluidExtractor extends MultiblockTileEntityRenderer<FluidExtractorMultiblockData, TileEntityFluidExtractorPort> {

    public RenderFluidExtractor(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(TileEntityFluidExtractorPort tile, FluidExtractorMultiblockData multiblock, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light,
                          int overlayLight, ProfilerFiller profiler) {
        RenderData data = getRenderData(multiblock);
        if (data != null) {
            VertexConsumer buffer = renderer.getBuffer(Sheets.translucentCullBlockSheet());
            renderObject(data, multiblock.valves, tile.getBlockPos(), matrix, buffer, overlayLight, multiblock.prevScale);
        }
    }

    @Nullable
    private RenderData getRenderData(FluidExtractorMultiblockData multiblock) {
        if (multiblock.fluidTank.isEmpty()) {
            return null;
        }
        return RenderData.Builder.create(multiblock.fluidTank.getFluid()).of(multiblock).build();
    }

    @Override
    protected String getProfilerSection() {
        return "fluidExtractor";
    }

    @Override
    protected boolean shouldRender(TileEntityFluidExtractorPort tile, FluidExtractorMultiblockData multiblock, Vec3 camera) {
        return super.shouldRender(tile, multiblock, camera) && !multiblock.fluidTank.isEmpty();
    }
}
