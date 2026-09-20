package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.content.fluid.CrudeOilFluidType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class MCIFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, MCIConstants.MODID);

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, MCIConstants.MODID);

    public static final RegistryObject<FluidType> CRUDE_OIL_TYPE = FLUID_TYPES.register("crude_oil",
            () -> new CrudeOilFluidType(FluidType.Properties.create()
                    .descriptionId("fluid.mekanism_complex_industries.crude_oil")
                    .density(3000)
                    .viscosity(6000)
                    .temperature(300)
                    .motionScale(0.007D)
                    .canSwim(false)
                    .canDrown(true)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)));

    public static final RegistryObject<FlowingFluid> CRUDE_OIL_SOURCE = FLUIDS.register("crude_oil",
            () -> new ForgeFlowingFluid.Source(MCIFluids.OIL_PROPERTIES));

    public static final RegistryObject<FlowingFluid> CRUDE_OIL_FLOWING = FLUIDS.register("crude_oil_flowing",
            () -> new ForgeFlowingFluid.Flowing(MCIFluids.OIL_PROPERTIES));

    public static final ForgeFlowingFluid.Properties OIL_PROPERTIES = new ForgeFlowingFluid.Properties(
            CRUDE_OIL_TYPE,
            CRUDE_OIL_SOURCE,
            CRUDE_OIL_FLOWING
    ).bucket(MCIItems.CRUDE_OIL_BUCKET)
     .block(MCIBlocks.CRUDE_OIL_BLOCK)
     .slopeFindDistance(2)
     .levelDecreasePerBlock(2)
     .tickRate(30)
     .explosionResistance(100.0F);

    private MCIFluids() {}
}
