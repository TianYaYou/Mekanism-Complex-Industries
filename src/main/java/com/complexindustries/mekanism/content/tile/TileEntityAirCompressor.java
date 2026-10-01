package com.complexindustries.mekanism.content.tile;

import com.complexindustries.mekanism.content.block.AirCompressorBlock;
import com.complexindustries.mekanism.content.energy.AirCompressorEnergyContainer;
import com.complexindustries.mekanism.registration.MCIBlocks;
import com.complexindustries.mekanism.registration.MCIChemicals;
import java.util.EnumSet;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.Upgrade;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityAirCompressor extends TileEntityMekanism {

    // 100 FE/s = 5 FE/t = 5 J/t (or 20 L)
    public static final long BASE_ENERGY_PER_TICK = 20L;
    public static final int BASE_PRODUCTION_RATE = 10; // 10 mB/t = 200 mB/s
    public static final long MAX_GAS = 40_000L;

    public IChemicalTank outputTank;
    public MachineEnergyContainer<TileEntityAirCompressor> energyContainer;
    private EnergyInventorySlot energySlot;
    private ChemicalInventorySlot outputSlot;

    private long clientEnergyUsed = 0L;
    private int productionRate = BASE_PRODUCTION_RATE;
    private long energyPerTick = BASE_ENERGY_PER_TICK;

    public TileEntityAirCompressor(BlockPos pos, BlockState state) {
        super(MCIBlocks.AIR_COMPRESSOR, pos, state);
        if (this.upgradeComponent == null) {
            this.upgradeComponent = new mekanism.common.tile.component.TileComponentUpgrade(this);
        }
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper helper = EnergyContainerHelper.forSide(this::getDirection);
        helper.addContainer(energyContainer = new AirCompressorEnergyContainer(this, listener));
        return helper.build();
    }

    @NotNull
    @Override
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener) {
        ChemicalTankHelper builder = ChemicalTankHelper.forSide(this::getDirection);
        builder.addTank(outputTank = BasicChemicalTank.output(MAX_GAS, listener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper helper = InventorySlotHelper.forSide(this::getDirection);
        helper.addSlot(outputSlot = ChemicalInventorySlot.drain(outputTank, listener, 143, 19));
        helper.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 143, 40));
        return helper.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        outputSlot.drainTank();

        long toUse = 0L;
        if (this.canFunction()) {
            toUse = energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL);
            if (toUse > 0L && outputTank.getNeeded() > 0) {
                int toProduce = (int) Math.min((long) productionRate, outputTank.getNeeded());
                if (toProduce > 0) {
                    energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
                    outputTank.insert(MCIChemicals.COMPRESSED_AIR.asStack(toProduce), Action.EXECUTE, AutomationType.INTERNAL);
                }
            } else {
                toUse = 0L;
            }
        }

        setActive(toUse > 0L);
        clientEnergyUsed = toUse;

        // Auto-eject compressed air to adjacent pressurized tubes or acceptors
        if (!outputTank.isEmpty() && level != null) {
            for (Direction direction : Direction.values()) {
                if (outputTank.isEmpty()) {
                    break;
                }
                BlockPos targetPos = worldPosition.relative(direction);
                IChemicalHandler target = level.getCapability(Capabilities.CHEMICAL.block(), targetPos, direction.getOpposite());
                if (target != null) {
                    ChemicalStack toSend = outputTank.getStack().copy();
                    ChemicalStack remainder = target.insertChemical(toSend, Action.EXECUTE);
                    long sent = toSend.getAmount() - remainder.getAmount();
                    if (sent > 0) {
                        outputTank.shrinkStack(sent, Action.EXECUTE);
                    }
                }
            }
        }
        return sendUpdatePacket;
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

    public long getEnergyUsed() {
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
        container.track(SyncableLong.create(this::getEnergyUsed, val -> clientEnergyUsed = val));
        container.track(SyncableInt.create(this::getProductionRate, val -> productionRate = val));
    }
}
