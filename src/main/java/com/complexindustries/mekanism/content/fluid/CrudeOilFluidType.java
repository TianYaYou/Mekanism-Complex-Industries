package com.complexindustries.mekanism.content.fluid;

import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

public class CrudeOilFluidType extends FluidType {

    public static final ResourceLocation STILL_TEXTURE = new ResourceLocation("mekanism_complex_industries", "block/crude_oil_still");
    public static final ResourceLocation FLOWING_TEXTURE = new ResourceLocation("mekanism_complex_industries", "block/crude_oil_flow");

    public CrudeOilFluidType() {
        super(Properties.create()
                .descriptionId("fluid.mekanism_complex_industries.crude_oil")
                .density(1100)
                .viscosity(3000)
                .temperature(310)
                .motionScale(0.007D)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY));
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return STILL_TEXTURE;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return FLOWING_TEXTURE;
            }

            @Override
            public int getTintColor() {
                return 0xFF1C1917; // Dark crude oil brownish-black
            }
        });
    }
}
