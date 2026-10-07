package com.complexindustries.mekanism.content.pipe;

import com.complexindustries.mekanism.content.pipe.container.ContainerPowerInterface;
import com.complexindustries.mekanism.content.pipe.interfaces.IPipeNode;
import com.complexindustries.mekanism.content.pipe.interfaces.IPowerInterface;
import com.complexindustries.mekanism.content.pipe.network.IndustrialPipeNetwork;
import com.complexindustries.mekanism.content.pipe.network.PipeNetworkManager;
import com.complexindustries.mekanism.registration.MCITileEntityTypes;
import mekanism.api.Action;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TileEntityPowerInterface extends BlockEntity implements IPipeNode, IPowerInterface {

    private IndustrialPipeNetwork network;

    private final IEnergyStorage energyStorage;
    private final IStrictEnergyHandler strictEnergyHandler;

    public TileEntityPowerInterface(BlockPos pos, BlockState state) {
        super(MCITileEntityTypes.POWER_INTERFACE.get(), pos, state);

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
                if (amount <= 0) return 0L;
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

    private boolean isUnloading = false;

    @Override
    public void onLoad() {
        super.onLoad();
        this.isUnloading = false;
        if (level != null && !level.isClientSide()) {
            PipeNetworkManager.onNodeAdded(level, worldPosition, this);
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = worldPosition.relative(dir);
                BlockState neighborState = level.getBlockState(neighborPos);
                if (neighborState.getBlock() instanceof IndustrialPipeBlock pipeBlock) {
                    BlockState updated = pipeBlock.updateConnections(neighborState, level, neighborPos);
                    if (updated != neighborState) {
                        level.setBlock(neighborPos, updated, 3);
                    }
                }
            }
        }
    }

    @Override
    public void onChunkUnloaded() {
        this.isUnloading = true;
        if (level != null && !level.isClientSide()) {
            PipeNetworkManager.onNodeUnloaded(level, worldPosition, this);
        }
        super.onChunkUnloaded();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        this.isUnloading = false;
    }

    @Override
    public void setRemoved() {
        if (!this.isUnloading && level != null && !level.isClientSide()) {
            PipeNetworkManager.onNodeRemoved(level, worldPosition, this);
        }
        super.setRemoved();
    }

    @Nullable
    @Override
    public Level getNodeLevel() {
        return level;
    }

    @Override
    public BlockPos getNodePos() {
        return worldPosition;
    }

    @Nullable
    @Override
    public IndustrialPipeNetwork getPipeNetwork() {
        return network;
    }

    @Override
    public void setPipeNetwork(@Nullable IndustrialPipeNetwork network) {
        if (this.network != null && this.network != network) {
            this.network.removePowerInterface(this);
        }
        this.network = network;
        if (this.network != null) {
            this.network.addPowerInterface(this);
        }
    }

    @Override
    public boolean canConnectPipe(Direction direction) {
        return true;
    }

    @Nullable
    @Override
    public Level getInterfaceLevel() {
        return level;
    }

    @Override
    public BlockPos getInterfacePos() {
        return worldPosition;
    }

    @Nullable
    @Override
    public Direction getAttachedFace() {
        return null;
    }

    private List<Direction> getExternalCandidateFaces() {
        List<Direction> candidates = new ArrayList<>();
        if (level == null) return candidates;
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = worldPosition.relative(dir);
            if (!WorldUtils.isBlockLoaded(level, neighborPos)) {
                continue;
            }
            BlockEntity neighbor = WorldUtils.getTileEntity(level, neighborPos);
            if (!(neighbor instanceof IPipeNode)) {
                candidates.add(dir);
            }
        }
        return candidates;
    }

    @Override
    public int extractEnergyFromExternal(int maxAmount, boolean simulate) {
        if (level == null || maxAmount <= 0) {
            return 0;
        }
        List<Direction> candidates = getExternalCandidateFaces();
        int totalExtracted = 0;
        int needed = maxAmount;

        for (Direction dir : candidates) {
            BlockPos targetPos = worldPosition.relative(dir);
            if (!WorldUtils.isBlockLoaded(level, targetPos)) {
                continue;
            }
            Direction opposite = dir.getOpposite();

            // Check Mekanism strict energy
            IStrictEnergyHandler mekHandler = EnergyCompatUtils.getStrictEnergyHandler(level, targetPos, null, null, opposite);
            if (mekHandler != null) {
                long toExtract = (long) (needed * 2.5);
                long extracted = mekHandler.extractEnergy(0, toExtract, simulate ? Action.SIMULATE : Action.EXECUTE);
                int fe = (int) Math.min(needed, (long) (extracted * 0.4));
                totalExtracted += fe;
                needed -= fe;
                if (needed <= 0) break;
                continue;
            }

            // Check Forge/NeoForge energy
            IEnergyStorage storage = WorldUtils.getCapability(level, Capabilities.EnergyStorage.BLOCK, targetPos, opposite);
            if (storage != null && storage.canExtract()) {
                int extracted = storage.extractEnergy(needed, simulate);
                totalExtracted += extracted;
                needed -= extracted;
                if (needed <= 0) break;
            }
        }

        return totalExtracted;
    }

    @Override
    public int receiveEnergyFromExternal(int amount, boolean simulate) {
        return 0;
    }

    public InteractionResult openMenu(Player player) {
        if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new ContainerPowerInterface(id, inv, this),
                    Component.translatable("gui.mekanism_complex_industries.power_interface")
            ), buf -> {
                buf.writeBlockPos(worldPosition);
                buf.writeBoolean(false); // is block form
            });
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide());
    }

    public IEnergyStorage getEnergyHandler(@Nullable Direction side) {
        return energyStorage;
    }

    public IStrictEnergyHandler getStrictEnergyHandler(@Nullable Direction side) {
        return strictEnergyHandler;
    }
}
