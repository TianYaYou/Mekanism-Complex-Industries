package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.content.fluid.CrudeOilFluidType;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
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

    public static final RegistryObject<FluidType> CRUDE_OIL_TYPE = FLUID_TYPES.register("crude_oil", CrudeOilFluidType::new);

    public static final RegistryObject<FlowingFluid> SOURCE_CRUDE_OIL = FLUIDS.register("crude_oil",
            () -> new ForgeFlowingFluid.Source(MCIFluids.CRUDE_OIL_PROPERTIES));

    public static final RegistryObject<FlowingFluid> FLOWING_CRUDE_OIL = FLUIDS.register("flowing_crude_oil",
            () -> new ForgeFlowingFluid.Flowing(MCIFluids.CRUDE_OIL_PROPERTIES));

    public static final ForgeFlowingFluid.Properties CRUDE_OIL_PROPERTIES = new ForgeFlowingFluid.Properties(
            CRUDE_OIL_TYPE,
            SOURCE_CRUDE_OIL,
            FLOWING_CRUDE_OIL)
            .bucket(MCIItems.CRUDE_OIL_BUCKET)
            .block(MCIBlocks.CRUDE_OIL_BLOCK)
            .slopeFindDistance(2)
            .levelDecreasePerBlock(2)
            .tickRate(20);

    private MCIFluids() {}
}
