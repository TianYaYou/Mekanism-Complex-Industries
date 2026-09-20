package com.complexindustries.mekanism.content.fluid;

import com.complexindustries.mekanism.MCIConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;

import java.util.function.Consumer;

public class CrudeOilFluidType extends FluidType {

    public CrudeOilFluidType(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            private static final ResourceLocation STILL = MCIConstants.rl("block/crude_oil_still");
            private static final ResourceLocation FLOWING = MCIConstants.rl("block/crude_oil_flow");

            @Override
            public ResourceLocation getStillTexture() {
                return STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return FLOWING;
            }
        });
    }
}
