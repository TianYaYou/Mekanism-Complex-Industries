package com.complexindustries.mekanism.content.tile;

import com.complexindustries.mekanism.content.block.ResistiveCoolerBlock;
import com.complexindustries.mekanism.content.energy.ResistiveCoolerEnergyContainer;
import com.complexindustries.mekanism.registration.MCIBlocks;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.heat.HeatCapacitorHelper;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityResistiveCooler extends TileEntityMekanism {
    // -200°C in Kelvin is 73.15 K
    public static final double MIN_TEMPERATURE = 73.15D;
    // Maximum cooling energy rate: 25,000 J/t (10,000 FE/t)
    public static final long MAX_ENERGY_USAGE = 25_000L;
    // Baseline power for full cryogenic cooling (-200°C): 12,500 J/t (5,000 FE/t)
    public static final double FULL_COOLING_ENERGY = 12_500.0D;

    private float soundScale = 1.0F;
    private double lastEnvironmentLoss;
    private double lastTransferLoss;
    private long clientEnergyUsed = 0L;

    private ResistiveCoolerEnergyContainer energyContainer;
    private BasicHeatCapacitor heatCapacitor;
    private EnergyInventorySlot energySlot;

    public TileEntityResistiveCooler(BlockPos pos, BlockState state) {
        super(MCIBlocks.RESISTIVE_COOLER, pos, state);
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper helper = EnergyContainerHelper.forSide(this::getDirection);
        helper.addContainer(energyContainer = ResistiveCoolerEnergyContainer.input(this, listener));
        return helper.build();
    }

    @NotNull
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        HeatCapacitorHelper helper = HeatCapacitorHelper.forSide(this::getDirection);
        helper.addCapacitor(heatCapacitor = BasicHeatCapacitor.create(100.0D, 50.0D, 10.0D, ambientTemperature, listener));
        return helper.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper helper = InventorySlotHelper.forSide(this::getDirection);
        helper.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 15, 35));
        return helper.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        long toUse = 0L;

        if (this.canFunction()) {
            long requestedUsage = energyContainer.getEnergyPerTick();
            toUse = energyContainer.extract(requestedUsage, Action.SIMULATE, AutomationType.INTERNAL);

            if (toUse > 0L) {
                double ambient = getAmbientTemperature(null);
                double powerRatio = Math.min(1.0, (double) toUse / FULL_COOLING_ENERGY);
                // Target temperature: scales from ambient down to -200°C (73.15 K) at 5,000 FE/t (12,500 J/t)
                double targetTemp = Math.max(MIN_TEMPERATURE, ambient - powerRatio * (ambient - MIN_TEMPERATURE));
                double currentHeat = heatCapacitor.getHeat();
                double targetHeat = targetTemp * heatCapacitor.getHeatCapacity();

                if (currentHeat > targetHeat) {
                    double coolingRate = (double) toUse * MekanismConfig.general.resistiveHeaterEfficiency.get() * 50.0;
                    double toRemove = Math.min(coolingRate, currentHeat - targetHeat);
                    heatCapacitor.handleHeat(-toRemove);
                }

                energyContainer.extract(toUse, Action.EXECUTE, AutomationType.INTERNAL);
            }
        }

        setActive(toUse > 0L);
        clientEnergyUsed = toUse;

        // Mekanism heat transfer simulation (transfers cold / absorbs ambient heat)
        HeatAPI.HeatTransfer transfer = simulate();
        lastEnvironmentLoss = transfer.environmentTransfer();
        lastTransferLoss = transfer.adjacentTransfer();

        float newSoundScale = (float) toUse / 12_500.0F;
        if (Math.abs(newSoundScale - soundScale) > 0.01F) {
            soundScale = newSoundScale;
            sendUpdatePacket = true;
        }
        return sendUpdatePacket;
    }

    @Override
    public void setActive(boolean active) {
        super.setActive(active);
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            if (state.hasProperty(ResistiveCoolerBlock.ACTIVE) && state.getValue(ResistiveCoolerBlock.ACTIVE) != active) {
                level.setBlock(worldPosition, state.setValue(ResistiveCoolerBlock.ACTIVE, active), 3);
            }
        }
    }

    @Override
    public boolean getActive() {
        if (isRemote()) {
            BlockState state = getBlockState();
            if (state.hasProperty(ResistiveCoolerBlock.ACTIVE)) {
                return state.getValue(ResistiveCoolerBlock.ACTIVE);
            }
        }
        return super.getActive();
    }

    public float getVolume() {
        return (float) Math.sqrt(soundScale);
    }

    public long getEnergyUsage() {
        return energyContainer.getEnergyPerTick();
    }

    public long getEnergyUsed() {
        return clientEnergyUsed;
    }

    @Override
    public double simulateAdjacent() {
        double adjacentTransfer = 0;
        for (Direction side : EnumUtils.DIRECTIONS) {
            IHeatHandler sink = getAdjacent(side);
            if (sink == null && level != null) {
                sink = level.getCapability(Capabilities.HEAT, worldPosition.relative(side), side.getOpposite());
            }
            if (sink != null) {
                double myTemp = getTotalTemperature(side);
                double sinkTemp = sink.getTotalTemperature();
                double invConduction = Math.max(1.0, sink.getTotalInverseConduction() + getTotalInverseConductionCoefficient(side));

                if (sinkTemp > myTemp) {
                    // Heat flows from the hotter sink into the cooler
                    double tempDiff = sinkTemp - myTemp;
                    double sinkCapacity = sink.getTotalHeatCapacity();
                    double effectiveCapacity = Math.max(getTotalHeatCapacity(side), Math.min(sinkCapacity, 10_000.0));
                    double heatToExtract = (tempDiff / invConduction) * effectiveCapacity;

                    // Ensure we don't extract more than would balance temperatures
                    double maxHeatBeforeEqualizing = tempDiff * sinkCapacity * 0.5;
                    heatToExtract = Math.min(heatToExtract, maxHeatBeforeEqualizing);

                    // Clamp to maximum extraction rate per tick per face
                    heatToExtract = Math.min(heatToExtract, 50_000.0);

                    if (heatToExtract > 0) {
                        sink.handleHeat(-heatToExtract);
                        handleHeat(heatToExtract, side);
                        adjacentTransfer += (heatToExtract / Math.max(1.0, getTotalHeatCapacity(side)));
                    }
                } else if (myTemp > sinkTemp) {
                    // Normal heat transfer to colder sink
                    double tempDiff = myTemp - sinkTemp;
                    double heatToTransfer = (tempDiff / invConduction) * getTotalHeatCapacity(side);
                    heatToTransfer = Math.min(heatToTransfer, tempDiff * getTotalHeatCapacity(side) * 0.5);
                    if (heatToTransfer > 0) {
                        handleHeat(-heatToTransfer, side);
                        sink.handleHeat(heatToTransfer);
                        adjacentTransfer += (heatToTransfer / Math.max(1.0, getTotalHeatCapacity(side)));
                    }
                }
            }
        }
        return adjacentTransfer;
    }

    public double getLastTransferLoss() {
        return lastTransferLoss;
    }

    public double getLastEnvironmentLoss() {
        return lastEnvironmentLoss;
    }

    public MachineEnergyContainer<TileEntityResistiveCooler> getEnergyContainer() {
        return energyContainer;
    }

    public BasicHeatCapacitor getHeatCapacitor() {
        return heatCapacitor;
    }

    public void setEnergyUsage(long newUsage) {
        long clamped = Math.max(0L, Math.min(newUsage, MAX_ENERGY_USAGE));
        energyContainer.updateEnergyUsage(clamped);
        markForSave();
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableDouble.create(this::getLastTransferLoss, val -> lastTransferLoss = val));
        container.track(SyncableDouble.create(this::getLastEnvironmentLoss, val -> lastEnvironmentLoss = val));
        container.track(SyncableLong.create(this::getEnergyUsed, val -> clientEnergyUsed = val));
    }

    @Override
    public CompoundTag getConfigurationData(HolderLookup.Provider provider, Player player) {
        CompoundTag data = super.getConfigurationData(provider, player);
        data.putLong("energyUsage", energyContainer.getEnergyPerTick());
        return data;
    }

    @Override
    public void setConfigurationData(HolderLookup.Provider provider, Player player, CompoundTag data) {
        super.setConfigurationData(provider, player, data);
        NBTUtils.setLegacyEnergyIfPresent(data, "energyUsage", this::setEnergyUsage);
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag(@NotNull HolderLookup.Provider provider) {
        CompoundTag tag = super.getReducedUpdateTag(provider);
        tag.putFloat("soundScale", soundScale);
        return tag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        NBTUtils.setFloatIfPresent(tag, "soundScale", scale -> soundScale = scale);
    }
}
