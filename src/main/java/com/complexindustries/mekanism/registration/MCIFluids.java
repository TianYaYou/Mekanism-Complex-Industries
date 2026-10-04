package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.content.fluid.CrudeOilFluidType;
import com.complexindustries.mekanism.content.fluid.CryogenicRefrigerantFluidType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class MCIFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, MCIConstants.MODID);

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(BuiltInRegistries.FLUID, MCIConstants.MODID);

    public static final DeferredHolder<FluidType, FluidType> CRUDE_OIL_TYPE = FLUID_TYPES.register("crude_oil", CrudeOilFluidType::new);

    public static final DeferredHolder<Fluid, FlowingFluid> SOURCE_CRUDE_OIL = FLUIDS.register("crude_oil",
            () -> new BaseFlowingFluid.Source(MCIFluids.CRUDE_OIL_PROPERTIES));

    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_CRUDE_OIL = FLUIDS.register("flowing_crude_oil",
            () -> new BaseFlowingFluid.Flowing(MCIFluids.CRUDE_OIL_PROPERTIES));

    public static final BaseFlowingFluid.Properties CRUDE_OIL_PROPERTIES = new BaseFlowingFluid.Properties(
            CRUDE_OIL_TYPE,
            SOURCE_CRUDE_OIL,
            FLOWING_CRUDE_OIL)
            .bucket(MCIItems.CRUDE_OIL_BUCKET)
            .block(MCIBlocks.CRUDE_OIL_BLOCK)
            .slopeFindDistance(2)
            .levelDecreasePerBlock(2)
            .tickRate(20);

    public static final DeferredHolder<FluidType, FluidType> CRYOGENIC_REFRIGERANT_TYPE =
            FLUID_TYPES.register("cryogenic_refrigerant", CryogenicRefrigerantFluidType::new);

    public static final DeferredHolder<Fluid, FlowingFluid> SOURCE_CRYOGENIC_REFRIGERANT = FLUIDS.register("cryogenic_refrigerant",
            () -> new BaseFlowingFluid.Source(MCIFluids.CRYOGENIC_REFRIGERANT_PROPERTIES));

    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_CRYOGENIC_REFRIGERANT = FLUIDS.register("flowing_cryogenic_refrigerant",
            () -> new BaseFlowingFluid.Flowing(MCIFluids.CRYOGENIC_REFRIGERANT_PROPERTIES));

    public static final BaseFlowingFluid.Properties CRYOGENIC_REFRIGERANT_PROPERTIES = new BaseFlowingFluid.Properties(
            CRYOGENIC_REFRIGERANT_TYPE,
            SOURCE_CRYOGENIC_REFRIGERANT,
            FLOWING_CRYOGENIC_REFRIGERANT)
            .bucket(MCIItems.REFRIGERANT_BUCKET)
            .block(MCIBlocks.CRYOGENIC_REFRIGERANT_BLOCK)
            .slopeFindDistance(2)
            .levelDecreasePerBlock(2)
            .tickRate(15);

    public static final DeferredHolder<FluidType, FluidType> LIQUID_PROPYLENE_TYPE =
            FLUID_TYPES.register("liquid_propylene", com.complexindustries.mekanism.content.fluid.LiquidPropyleneFluidType::new);

    public static final DeferredHolder<Fluid, FlowingFluid> SOURCE_LIQUID_PROPYLENE = FLUIDS.register("liquid_propylene",
            () -> new BaseFlowingFluid.Source(MCIFluids.LIQUID_PROPYLENE_PROPERTIES));

    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_LIQUID_PROPYLENE = FLUIDS.register("flowing_liquid_propylene",
            () -> new BaseFlowingFluid.Flowing(MCIFluids.LIQUID_PROPYLENE_PROPERTIES));

    public static final BaseFlowingFluid.Properties LIQUID_PROPYLENE_PROPERTIES = new BaseFlowingFluid.Properties(
            LIQUID_PROPYLENE_TYPE,
            SOURCE_LIQUID_PROPYLENE,
            FLOWING_LIQUID_PROPYLENE)
            .bucket(MCIItems.LIQUID_PROPYLENE_BUCKET)
            .block(MCIBlocks.LIQUID_PROPYLENE_BLOCK)
            .slopeFindDistance(4)
            .levelDecreasePerBlock(1)
            .tickRate(5);

    private MCIFluids() {}
}
