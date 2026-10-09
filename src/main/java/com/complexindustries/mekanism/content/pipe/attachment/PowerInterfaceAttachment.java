package com.complexindustries.mekanism.content.pipe.attachment;

import com.complexindustries.mekanism.content.pipe.TileEntityIndustrialPipe;
import com.complexindustries.mekanism.content.pipe.container.ContainerPowerInterface;
import com.complexindustries.mekanism.content.pipe.interfaces.IPowerInterface;
import com.complexindustries.mekanism.content.pipe.network.IndustrialPipeNetwork;
import com.complexindustries.mekanism.registration.MCIItems;
import mekanism.api.Action;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PowerInterfaceAttachment implements IPipeAttachment, IPowerInterface {

    private final TileEntityIndustrialPipe pipe;
    private final Direction face;
    private mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl redstoneMode = mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl.DISABLED;

    private final IEnergyStorage energyStorage;
    private final IStrictEnergyHandler strictEnergyHandler;

    public PowerInterfaceAttachment(TileEntityIndustrialPipe pipe, Direction face) {
        this.pipe = pipe;
        this.face = face;

        this.energyStorage = new IEnergyStorage() {
            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                return receiveEnergyFromExternal(maxReceive, simulate);
            }

            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                return 0;
            }

            @Override
            public int getEnergyStored() {
                return 0;
            }

            @Override
            public int getMaxEnergyStored() {
                return 0;
            }

            @Override
            public boolean canExtract() {
                return false;
            }

            @Override
            public boolean canReceive() {
                return true;
            }
        };

        this.strictEnergyHandler = new IStrictEnergyHandler() {
            @Override
            public int getEnergyContainerCount() {
                return 1;
            }

            @Override
            public long getEnergy(int container) {
                return 0L;
            }

            @Override
            public void setEnergy(int container, long energy) {
            }

            @Override
            public long getMaxEnergy(int container) {
                return 0L;
            }

            @Override
            public long getNeededEnergy(int container) {
                return Long.MAX_VALUE;
            }

            @Override
            public long insertEnergy(int container, long amount, @NotNull Action action) {
                if (amount <= 0) {
                    return 0L;
                }
                int fe = (int) Math.min(Integer.MAX_VALUE, (long) (amount * 0.4));
                int acceptedFE = receiveEnergyFromExternal(fe, action.simulate());
                long accepted = (long) (acceptedFE * 2.5);
                return amount - accepted;
            }

            @Override
            public long extractEnergy(int container, long amount, @NotNull Action action) {
                return 0L;
            }
        };
    }

    @Override
    public AttachmentType getType() {
        return AttachmentType.POWER;
    }

    @Override
    public Direction getFace() {
        return face;
    }

    @Nullable
    @Override
    public IndustrialPipeNetwork getPipeNetwork() {
        return pipe.getPipeNetwork();
    }

    @Nullable
    @Override
    public Level getInterfaceLevel() {
        return pipe.getLevel();
    }

    @Override
    public BlockPos getInterfacePos() {
        return pipe.getBlockPos();
    }

    @Nullable
    @Override
    public Direction getAttachedFace() {
        return face;
    }

    @Override
    public mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    public void setRedstoneMode(mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl mode) {
        this.redstoneMode = mode != null ? mode : mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl.DISABLED;
        pipe.setChanged();
    }

    @Override
    public boolean isRedstonePowered() {
        Level level = pipe.getLevel();
        BlockPos pos = pipe.getBlockPos();
        return level != null && (level.hasNeighborSignal(pos) || level.hasSignal(pos.relative(face), face));
    }

    @Override
    public int extractEnergyFromExternal(int maxAmount, boolean simulate) {
        if (!canOperate()) {
            return 0;
        }
        Level level = pipe.getLevel();
        if (level == null || maxAmount <= 0) {
            return 0;
        }
        BlockPos targetPos = pipe.getBlockPos().relative(face);
        if (!WorldUtils.isBlockLoaded(level, targetPos)) {
            return 0;
        }
        Direction opposite = face.getOpposite();

        // Try Mekanism energy first
        IStrictEnergyHandler mekHandler = EnergyCompatUtils.getStrictEnergyHandler(level, targetPos, null, null, opposite);
        if (mekHandler != null) {
            long toExtract = (long) (maxAmount * 2.5);
            long extracted = mekHandler.extractEnergy(0, toExtract, simulate ? Action.SIMULATE : Action.EXECUTE);
            return (int) Math.min(maxAmount, (long) (extracted * 0.4));
        }

        // Try standard NeoForge EnergyStorage
        IEnergyStorage storage = WorldUtils.getCapability(level, Capabilities.EnergyStorage.BLOCK, targetPos, opposite);
        if (storage != null && storage.canExtract()) {
            return storage.extractEnergy(maxAmount, simulate);
        }
        return 0;
    }

    @Override
    public int receiveEnergyFromExternal(int amount, boolean simulate) {
        if (!canOperate()) {
            return 0;
        }
        return 0;
    }

    @Override
    public void onAttachedToNetwork(IndustrialPipeNetwork network) {
        network.addPowerInterface(this);
    }

    @Override
    public void onDetachedFromNetwork(IndustrialPipeNetwork network) {
        network.removePowerInterface(this);
    }

    @Override
    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putByte("RedstoneMode", (byte) redstoneMode.ordinal());
        return tag;
    }

    @Override
    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("RedstoneMode")) {
            redstoneMode = com.complexindustries.mekanism.content.pipe.interfaces.IRedstoneControllable.byIndex(tag.getByte("RedstoneMode") & 255);
        }
    }

    @Override
    public ItemStack getDropItem() {
        return new ItemStack(MCIItems.POWER_INTERFACE_PART.get());
    }

    @Override
    public InteractionResult openMenu(Player player) {
        if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new ContainerPowerInterface(id, inv, this),
                    Component.translatable("gui.mekanism_complex_industries.power_interface")
            ), buf -> {
                buf.writeBlockPos(pipe.getBlockPos());
                buf.writeBoolean(true); // is attachment
                buf.writeByte(face.ordinal());
            });
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide());
    }

    @Nullable
    @Override
    public IEnergyStorage getEnergyHandler() {
        return energyStorage;
    }

    @Nullable
    @Override
    public IStrictEnergyHandler getStrictEnergyHandler() {
        return strictEnergyHandler;
    }
}
