package com.complexindustries.mekanism.content.fluid;

import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;

public class LiquidOzoneFluidType extends FluidType {

    public static final ResourceLocation STILL_TEXTURE = ResourceLocation.fromNamespaceAndPath("mekanism", "liquid/liquid");
    public static final ResourceLocation FLOWING_TEXTURE = ResourceLocation.fromNamespaceAndPath("mekanism", "liquid/liquid_flow");

    public LiquidOzoneFluidType() {
        super(Properties.create()
                .descriptionId("fluid.mekanism_complex_industries.liquid_ozone")
                .density(1350)
                .viscosity(600)
                .temperature(161) // 161 Kelvin: boiling point of liquid ozone (-112°C)
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
                return 0xFF3385FF; // Deep indigo-blue cryogenic liquid ozone tint
            }
        });
    }
}
