package com.complexindustries.mekanism.mixin;

import com.complexindustries.mekanism.registration.MCIChemicals;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.capabilities.heat.VariableHeatCapacitor;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.util.HeatUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(value = BoilerMultiblockData.class, remap = false)
public abstract class MixinBoilerMultiblockData {

    @Shadow(remap = false)
    public IChemicalTank superheatedCoolantTank;

    @Shadow(remap = false)
    public IChemicalTank cooledCoolantTank;

    @Shadow(remap = false)
    public VariableCapacityFluidTank waterTank;

    @Shadow(remap = false)
    public IChemicalTank steamTank;

    @Shadow(remap = false)
    public VariableHeatCapacitor heatCapacitor;

    @Shadow(remap = false)
    public int superheatingElements;

    @Shadow(remap = false)
    public int lastBoilRate;

    @Shadow(remap = false)
    public int lastMaxBoil;

    @ModifyArg(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lmekanism/common/capabilities/chemical/VariableCapacityChemicalTank;input(Lmekanism/common/lib/multiblock/MultiblockData;Ljava/util/function/LongSupplier;Ljava/util/function/Predicate;Lmekanism/api/IContentsListener;)Lmekanism/api/chemical/IChemicalTank;"),
        index = 2,
        remap = false
    )
    private Predicate<ChemicalStack> mci$modifySuperheatedCoolantTankValidator(Predicate<ChemicalStack> original) {
        return stack -> original.test(stack) || stack.is(MCIChemicals.PETROLEUM_GAS);
    }

    @ModifyArg(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lmekanism/common/capabilities/chemical/VariableCapacityChemicalTank;output(Lmekanism/common/lib/multiblock/MultiblockData;Ljava/util/function/LongSupplier;Ljava/util/function/Predicate;Lmekanism/api/IContentsListener;)Lmekanism/api/chemical/IChemicalTank;", ordinal = 0),
        index = 2,
        remap = false
    )
    private Predicate<ChemicalStack> mci$modifySteamTankValidator(Predicate<ChemicalStack> original) {
        return stack -> original.test(stack) || stack.is(MCIChemicals.PROPYLENE);
    }

    @ModifyArg(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lmekanism/common/capabilities/chemical/VariableCapacityChemicalTank;output(Lmekanism/common/lib/multiblock/MultiblockData;Ljava/util/function/LongSupplier;Ljava/util/function/Predicate;Lmekanism/api/IContentsListener;)Lmekanism/api/chemical/IChemicalTank;", ordinal = 1),
        index = 2,
        remap = false
    )
    private Predicate<ChemicalStack> mci$modifyCooledCoolantTankValidator(Predicate<ChemicalStack> original) {
        return stack -> original.test(stack) || stack.is(MekanismChemicals.ETHENE);
    }

    @Redirect(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lmekanism/common/content/boiler/BoilerMultiblockData;getTotalTemperature()D", ordinal = 1),
        remap = false
    )
    private double mci$suppressNormalBoilingIfCracking(BoilerMultiblockData instance) {
        if (this.superheatedCoolantTank.getStack().is(MCIChemicals.PETROLEUM_GAS)) {
            return 0.0;
        }
        return instance.getTotalTemperature();
    }

    @Inject(
        method = "tick",
        at = @At(value = "FIELD", target = "Lmekanism/common/content/boiler/BoilerMultiblockData;chemicalOutputTargets:Ljava/util/List;", ordinal = 0),
        remap = false
    )
    private void mci$performSteamCracking(Level world, CallbackInfoReturnable<Boolean> cir) {
        if (!this.superheatedCoolantTank.getStack().is(MCIChemicals.PETROLEUM_GAS)) {
            return;
        }

        double totalTemp = this.heatCapacitor.getTemperature();
        if (totalTemp < 800.0) {
            this.lastBoilRate = 0;
            this.lastMaxBoil = 0;
            return;
        }

        double heatAbove800 = Math.max(0, (this.heatCapacitor.getTemperature() - 800.0) * (this.heatCapacitor.getHeatCapacity() * MekanismConfig.general.boilerWaterConductivity.get()));
        double heatAvailable = Math.min(heatAbove800, MekanismConfig.general.superheatingHeatTransfer.get() * this.superheatingElements);
        int maxByHeat = Mth.floor(HeatUtils.getSteamEnergyEfficiency() * heatAvailable / HeatUtils.getWaterThermalEnthalpy());

        int maxByGas = MathUtils.clampToInt(this.superheatedCoolantTank.getStored() / 2);
        int maxByWater = this.waterTank.getFluidAmount();

        int maxByPropylene = 0;
        if (this.steamTank.isEmpty()) {
            maxByPropylene = MathUtils.clampToInt(this.steamTank.getCapacity());
        } else if (this.steamTank.getStack().is(MCIChemicals.PROPYLENE)) {
            maxByPropylene = MathUtils.clampToInt(this.steamTank.getNeeded());
        }

        int maxByEthylene = 0;
        if (this.cooledCoolantTank.isEmpty()) {
            maxByEthylene = MathUtils.clampToInt(this.cooledCoolantTank.getCapacity());
        } else if (this.cooledCoolantTank.getStack().is(MekanismChemicals.ETHENE)) {
            maxByEthylene = MathUtils.clampToInt(this.cooledCoolantTank.getNeeded());
        }

        int amountToCrack = Math.min(maxByHeat, Math.min(maxByGas, Math.min(maxByWater, Math.min(maxByPropylene, maxByEthylene))));

        if (amountToCrack > 0) {
            this.superheatedCoolantTank.shrinkStack(amountToCrack * 2L, Action.EXECUTE);
            this.waterTank.shrinkStack(amountToCrack, Action.EXECUTE);

            if (this.steamTank.isEmpty()) {
                this.steamTank.setStack(MCIChemicals.PROPYLENE.asStack(amountToCrack));
            } else {
                this.steamTank.growStack(amountToCrack, Action.EXECUTE);
            }

            if (this.cooledCoolantTank.isEmpty()) {
                this.cooledCoolantTank.setStack(MekanismChemicals.ETHENE.asStack(amountToCrack));
            } else {
                this.cooledCoolantTank.growStack(amountToCrack, Action.EXECUTE);
            }

            double heatToAbsorb = amountToCrack * HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getSteamEnergyEfficiency();
            this.heatCapacitor.handleHeat(-heatToAbsorb);

            this.lastBoilRate = amountToCrack;
            this.lastMaxBoil = maxByHeat;
        } else {
            this.lastBoilRate = 0;
            this.lastMaxBoil = maxByHeat;
        }
    }
}
