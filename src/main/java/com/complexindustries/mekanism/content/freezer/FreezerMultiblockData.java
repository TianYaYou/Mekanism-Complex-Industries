package com.complexindustries.mekanism.content.freezer;

import com.complexindustries.mekanism.registration.MCIChemicals;
import com.complexindustries.mekanism.registration.MCIFluids;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.heat.HeatAPI;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.common.capabilities.chemical.VariableCapacityChemicalTank;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.capabilities.heat.VariableHeatCapacitor;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class FreezerMultiblockData extends MultiblockData implements IValveHandler {

    public static final double THRESHOLD_TEMP = 173.15; // -100°C, required temperature condition (173.15 K)

    @ContainerSync
    public VariableCapacityFluidTank inputFluidTank;
    @ContainerSync
    public VariableCapacityFluidTank outputFluidTank;
    @ContainerSync
    public IChemicalTank inputChemicalTank;
    @ContainerSync
    public IChemicalTank outputChemicalTank;
    @ContainerSync
    public VariableHeatCapacitor heatCapacitor;

    @ContainerSync
    private double lastEfficiency;
    @ContainerSync
    private double lastEnvironmentLoss;

    private int nobleGasAccumulator = 0;
    private int nitrogenAccumulator = 0;

    private double biomeAmbientTemp = HeatAPI.AMBIENT_TEMP;

    public FreezerMultiblockData(TileEntityMultiblock<?> tile) {
        super(tile);
        fluidTanks.add(inputFluidTank = VariableCapacityFluidTank.input(this, this::getTankCapacity, this::isValidInputFluid, this));
        fluidTanks.add(outputFluidTank = VariableCapacityFluidTank.output(this, this::getTankCapacity, ConstantPredicates.alwaysTrue(), this));
        chemicalTanks.add(inputChemicalTank = VariableCapacityChemicalTank.input(this, this::getTankCapacityLong, this::isValidInputChemical, this));
        chemicalTanks.add(outputChemicalTank = VariableCapacityChemicalTank.output(this, this::getTankCapacityLong, ConstantPredicates.alwaysTrue(), this));
        heatCapacitors.add(heatCapacitor = VariableHeatCapacitor.create(Math.max(64, getVolume()) * 50.0, () -> biomeAmbientTemp, this));
    }

    public boolean isValidInputChemical(@NotNull ChemicalStack chemicalStack) {
        if (chemicalStack.isEmpty()) {
            return false;
        }
        if (chemicalStack.is(MCIChemicals.COMPRESSED_AIR.get())) {
            return true;
        }
        Level world = getLevel();
        if (world == null) {
            return true;
        }
        RotaryRecipe recipe = MekanismRecipeType.ROTARY.getInputCache().findFirstRecipe(world, chemicalStack);
        return recipe != null && recipe.hasChemicalToFluid();
    }

    public boolean isValidInputFluid(@NotNull FluidStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getFluid() == Fluids.WATER) {
            return true;
        }
        if (stack.getFluid() == MCIFluids.SOURCE_CRYOGENIC_REFRIGERANT.get() || stack.getFluid() == MCIFluids.FLOWING_CRYOGENIC_REFRIGERANT.get()) {
            return true;
        }
        Level world = getLevel();
        if (world == null) {
            return true;
        }
        RotaryRecipe recipe = MekanismRecipeType.ROTARY.getInputCache().findFirstRecipe(world, stack);
        return recipe != null;
    }

    public int getTankCapacity() {
        return Math.max(64_000, getVolume() * 16_000);
    }

    public long getTankCapacityLong() {
        return getTankCapacity();
    }

    @Override
    public void onCreated(Level world) {
        super.onCreated(world);
        biomeAmbientTemp = calculateAverageAmbientTemperature(world);
        heatCapacitor.setHeatCapacity(Math.max(64, getVolume()) * 50.0, true);
    }

    @Override
    public void remove(Level world, mekanism.common.lib.multiblock.Structure structure) {
        if (inventoryID != null) {
            markDirty();
            MCIFreezerMultiblock.FREEZER_MANAGER.handleDirtyMultiblock(this);
        }
        super.remove(world, structure);
    }

    @Override
    public void setVolume(int volume) {
        super.setVolume(volume);
        if (heatCapacitor != null) {
            heatCapacitor.setHeatCapacity(Math.max(64, volume) * 50.0, false);
        }
    }

    @Override
    public boolean tick(Level world) {
        boolean needsPacket = super.tick(world);
        lastEnvironmentLoss = simulateEnvironment();
        updateHeatCapacitors(null);

        double currentTemp = getTemperature();
        if (currentTemp <= THRESHOLD_TEMP) {
            double deltaTemp = THRESHOLD_TEMP - currentTemp;
            double tempMult = 0.10 + (deltaTemp / 50.0);
            double volMult = 1.0 + Math.max(0, getVolume() - 64) / 300.0;
            lastEfficiency = tempMult * volMult;
        } else {
            lastEfficiency = 0.0;
        }

        if (lastEfficiency > 0.0) {
            int maxProcessRate = (int) Math.round(50 * lastEfficiency);

            FluidStack inFluid = inputFluidTank.getFluid();
            ChemicalStack inGas = inputChemicalTank.getStack();
            boolean isRefrigerant = !inFluid.isEmpty() && (inFluid.getFluid() == MCIFluids.SOURCE_CRYOGENIC_REFRIGERANT.get() || inFluid.getFluid() == MCIFluids.FLOWING_CRYOGENIC_REFRIGERANT.get());
            boolean isWater = !inFluid.isEmpty() && inFluid.getFluid() == Fluids.WATER;
            boolean isCompressedAir = !inGas.isEmpty() && inGas.is(MCIChemicals.COMPRESSED_AIR.get());

            // 1. Process: 低温冷煤 + 压缩空气 -> 稀有气体(1/100 产出) + 水(1:1 融化回收)
            if (isRefrigerant && isCompressedAir) {
                int toProcess = (int) Math.min(Math.min(inFluid.getAmount(), inGas.getAmount()), (long) maxProcessRate);
                int neededFluid = outputFluidTank.getNeeded();
                toProcess = Math.min(toProcess, neededFluid);

                if (toProcess > 0) {
                    int potentialGas = (nobleGasAccumulator + toProcess) / 100;
                    if (potentialGas == 0 || outputChemicalTank.getNeeded() >= potentialGas) {
                        FluidStack outFluid = new FluidStack(Fluids.WATER, toProcess);
                        if (outputFluidTank.insert(outFluid, Action.SIMULATE, AutomationType.INTERNAL).isEmpty()) {
                            inputFluidTank.shrinkStack(toProcess, Action.EXECUTE);
                            inputChemicalTank.shrinkStack(toProcess, Action.EXECUTE);
                            outputFluidTank.insert(outFluid, Action.EXECUTE, AutomationType.INTERNAL);

                            nobleGasAccumulator += toProcess;
                            int outGasAmount = nobleGasAccumulator / 100;
                            nobleGasAccumulator %= 100;
                            if (outGasAmount > 0) {
                                outputChemicalTank.insert(MCIChemicals.NOBLE_GAS.asStack(outGasAmount), Action.EXECUTE, AutomationType.INTERNAL);
                            }

                            heatCapacitor.handleHeat(toProcess * 3.0);
                            needsPacket = true;
                        }
                    }
                }
            } else {
                // If not running Noble Gas process:
                // 2. Process: 压缩空气 -> 氮气 (产物砍半，即 2:1 转化)
                if (isCompressedAir) {
                    int toProcess = (int) Math.min(inGas.getAmount(), (long) maxProcessRate);
                    if (toProcess > 0) {
                        int potentialGas = (nitrogenAccumulator + toProcess) / 2;
                        if (potentialGas == 0 || outputChemicalTank.getNeeded() >= potentialGas) {
                            inputChemicalTank.shrinkStack(toProcess, Action.EXECUTE);

                            nitrogenAccumulator += toProcess;
                            int outGasAmount = nitrogenAccumulator / 2;
                            nitrogenAccumulator %= 2;
                            if (outGasAmount > 0) {
                                outputChemicalTank.insert(MCIChemicals.NITROGEN.asStack(outGasAmount), Action.EXECUTE, AutomationType.INTERNAL);
                            }

                            heatCapacitor.handleHeat(toProcess * 1.5);
                            needsPacket = true;
                        }
                    }
                }

                // 3. Process: 水 -> 低温冷煤 (产量效率减少到现在的 1/10)
                if (isWater) {
                    int coolantRate = Math.max(1, (int) Math.round(maxProcessRate * 0.10));
                    int toProcess = (int) Math.min(inFluid.getAmount(), (long) coolantRate);
                    int neededFluid = outputFluidTank.getNeeded();
                    toProcess = Math.min(toProcess, neededFluid);
                    if (toProcess > 0) {
                        FluidStack outFluid = new FluidStack(MCIFluids.SOURCE_CRYOGENIC_REFRIGERANT.get(), toProcess);
                        if (outputFluidTank.insert(outFluid, Action.SIMULATE, AutomationType.INTERNAL).isEmpty()) {
                            inputFluidTank.shrinkStack(toProcess, Action.EXECUTE);
                            outputFluidTank.insert(outFluid, Action.EXECUTE, AutomationType.INTERNAL);
                            heatCapacitor.handleHeat(toProcess * 2.0);
                            needsPacket = true;
                        }
                    }
                }
            }

            // 4. Generic Rotary Condensation (Chemical -> Fluid)
            if (!inGas.isEmpty() && !inGas.is(MCIChemicals.COMPRESSED_AIR.get())) {
                RotaryRecipe recipe = MekanismRecipeType.ROTARY.getInputCache().findFirstRecipe(world, inGas);
                if (recipe != null && recipe.hasChemicalToFluid()) {
                    FluidStack outputFluid = recipe.getFluidOutput(inGas);
                    int toConvert = (int) Math.min(inGas.getAmount(), (long) maxProcessRate);
                    int needed = outputFluidTank.getNeeded();
                    toConvert = Math.min(toConvert, needed);
                    if (toConvert > 0) {
                        FluidStack toInsert = outputFluid.copyWithAmount(toConvert);
                        if (outputFluidTank.insert(toInsert, Action.SIMULATE, AutomationType.INTERNAL).isEmpty()) {
                            inputChemicalTank.shrinkStack(toConvert, Action.EXECUTE);
                            outputFluidTank.insert(toInsert, Action.EXECUTE, AutomationType.INTERNAL);
                            heatCapacitor.handleHeat(toConvert * 2.0);
                            needsPacket = true;
                        }
                    }
                }
            }
        }
        return needsPacket;
    }

    @Override
    public double simulateEnvironment() {
        double currentTemp = getTemperature();
        double heatCapacity = heatCapacitor.getHeatCapacity();
        if (Math.abs(currentTemp - biomeAmbientTemp) < 0.001) {
            heatCapacitor.handleHeat(biomeAmbientTemp * heatCapacity - heatCapacitor.getHeat());
            return 0;
        } else {
            double diff = currentTemp - biomeAmbientTemp;
            double incr = 0.005 * Math.sqrt(Math.abs(diff));
            if (currentTemp > biomeAmbientTemp) {
                incr = -incr;
            }
            heatCapacitor.handleHeat(heatCapacity * incr);
            return Math.abs(incr);
        }
    }

    public double getTemperature() {
        return heatCapacitor.getTemperature();
    }

    public void setTemperature(double temp) {
        heatCapacitor.setHeat(temp * heatCapacitor.getHeatCapacity());
    }

    public double getLastEfficiency() {
        return lastEfficiency;
    }

    public void setLastEfficiency(double efficiency) {
        this.lastEfficiency = efficiency;
    }

    public double getLastEnvironmentLoss() {
        return lastEnvironmentLoss;
    }

    public void setLastEnvironmentLoss(double loss) {
        this.lastEnvironmentLoss = loss;
    }

    @Override
    public void readUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.readUpdateTag(tag, provider);
        readValves(tag);
        nobleGasAccumulator = tag.getInt("nobleGasAcc");
        nitrogenAccumulator = tag.getInt("nitrogenAcc");
    }

    @Override
    public void writeUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.writeUpdateTag(tag, provider);
        writeValves(tag);
        tag.putInt("nobleGasAcc", nobleGasAccumulator);
        tag.putInt("nitrogenAcc", nitrogenAccumulator);
    }
}
