package com.complexindustries.mekanism.content.fluid;

import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;

public class CryogenicRefrigerantFluidType extends FluidType {

    public static final ResourceLocation STILL_TEXTURE = ResourceLocation.fromNamespaceAndPath("mekanism_complex_industries", "block/cryogenic_refrigerant_still");
    public static final ResourceLocation FLOWING_TEXTURE = ResourceLocation.fromNamespaceAndPath("mekanism_complex_industries", "block/cryogenic_refrigerant_flow");

    public CryogenicRefrigerantFluidType() {
        super(Properties.create()
                .descriptionId("fluid.mekanism_complex_industries.cryogenic_refrigerant")
                .density(1050)
                .viscosity(3000)
                .temperature(70) // ~ -203°C cryogenic temperature
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
                return 0xFFFFFFFF;
            }
        });
    }
}
