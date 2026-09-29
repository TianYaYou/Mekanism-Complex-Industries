package com.complexindustries.mekanism.content.tile;

import com.complexindustries.mekanism.content.block.AirCompressorBlock;
import com.complexindustries.mekanism.registration.MCIBlocks;
import com.complexindustries.mekanism.registration.MCIGases;
import java.util.EnumSet;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.Upgrade;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.chemical.GasInventorySlot;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityAirCompressor extends TileEntityMekanism {

    // 100 FE/s = 5 FE/t = 12.5 J/t
    public static final FloatingLong BASE_ENERGY_PER_TICK = FloatingLong.create(12.5);
    public static final int BASE_PRODUCTION_RATE = 10; // 10 mB/t = 200 mB/s
    public static final long MAX_GAS = 40_000;

    public IGasTank outputTank;
    public MachineEnergyContainer<TileEntityAirCompressor> energyContainer;
    private EnergyInventorySlot energySlot;
    private GasInventorySlot outputSlot;

    private FloatingLong clientEnergyUsed = FloatingLong.ZERO;
    private int productionRate = BASE_PRODUCTION_RATE;
    private FloatingLong energyPerTick = BASE_ENERGY_PER_TICK;

    public TileEntityAirCompressor(BlockPos pos, BlockState state) {
        super(MCIBlocks.AIR_COMPRESSOR_PROVIDER, pos, state);
        if (this.upgradeComponent == null) {
            this.upgradeComponent = new mekanism.common.tile.component.TileComponentUpgrade(this);
        }
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD, this));
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper helper = EnergyContainerHelper.forSide(this::getDirection);
        helper.addContainer(energyContainer = new com.complexindustries.mekanism.content.energy.AirCompressorEnergyContainer(this, listener));
        return helper.build();
    }

    @NotNull
    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener) {
        ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSide(this::getDirection);
        builder.addTank(outputTank = ChemicalTankBuilder.GAS.output(MAX_GAS, listener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper helper = InventorySlotHelper.forSide(this::getDirection);
        helper.addSlot(outputSlot = GasInventorySlot.rotaryFill(outputTank, () -> true, listener, 143, 19));
        helper.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 143, 40));
        return helper.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        outputSlot.fillTankOrConvert();

        FloatingLong toUse = FloatingLong.ZERO;
        if (MekanismUtils.canFunction(this)) {
            toUse = energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL);
            if (!toUse.isZero() && outputTank.getNeeded() > 0) {
                int toProduce = (int) Math.min((long) productionRate, outputTank.getNeeded());
                if (toProduce > 0) {
                    energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
                    outputTank.insert(MCIGases.COMPRESSED_AIR.getStack(toProduce), Action.EXECUTE, AutomationType.INTERNAL);
                }
            } else {
                toUse = FloatingLong.ZERO;
            }
        }

        setActive(!toUse.isZero());
        clientEnergyUsed = toUse;

        // Auto-eject compressed air to adjacent pressurized tubes or acceptors
        if (!outputTank.isEmpty()) {
            ChemicalUtil.emit(outputTank, this);
        }
    }

    @Override
    public void setActive(boolean active) {
        super.setActive(active);
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            if (state.hasProperty(AirCompressorBlock.ACTIVE) && state.getValue(AirCompressorBlock.ACTIVE) != active) {
                level.setBlock(worldPosition, state.setValue(AirCompressorBlock.ACTIVE, active), 3);
            }
        }
    }

    @Override
    public boolean getActive() {
        if (isRemote()) {
            BlockState state = getBlockState();
            if (state.hasProperty(AirCompressorBlock.ACTIVE)) {
                return state.getValue(AirCompressorBlock.ACTIVE);
            }
        }
        return super.getActive();
    }

    @NotNull
    @Override
    public Set<Upgrade> getSupportedUpgrade() {
        return EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING);
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.SPEED || upgrade == Upgrade.ENERGY) {
            energyPerTick = MekanismUtils.getEnergyPerTick(this, BASE_ENERGY_PER_TICK);
        }
        if (upgrade == Upgrade.SPEED && upgradeComponent != null) {
            productionRate = (int) Math.round(BASE_PRODUCTION_RATE * Math.pow(1.25, upgradeComponent.getUpgrades(Upgrade.SPEED)));
        }
    }

    public float getVolume() {
        if (upgradeComponent != null && upgradeComponent.isUpgradeInstalled(Upgrade.MUFFLING)) {
            return (float) Math.pow(0.5, upgradeComponent.getUpgrades(Upgrade.MUFFLING));
        }
        return 1.0F;
    }

    public FloatingLong getEnergyUsed() {
        return clientEnergyUsed;
    }

    public int getProductionRate() {
        return productionRate;
    }

    public MachineEnergyContainer<TileEntityAirCompressor> getEnergyContainer() {
        return energyContainer;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableFloatingLong.create(this::getEnergyUsed, val -> clientEnergyUsed = val));
        container.track(SyncableInt.create(this::getProductionRate, val -> productionRate = val));
    }
}
