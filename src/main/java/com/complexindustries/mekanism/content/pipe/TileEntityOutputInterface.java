package com.complexindustries.mekanism.content.pipe;

import com.complexindustries.mekanism.content.pipe.container.ContainerOutputInterface;
import com.complexindustries.mekanism.content.pipe.interfaces.IOutputInterface;
import com.complexindustries.mekanism.content.pipe.interfaces.IPipeNode;
import com.complexindustries.mekanism.content.pipe.interfaces.OutputInterfaceFilter;
import com.complexindustries.mekanism.content.pipe.network.IndustrialPipeNetwork;
import com.complexindustries.mekanism.content.pipe.network.PipeNetworkManager;
import com.complexindustries.mekanism.registration.MCITileEntityTypes;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.Capabilities;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TileEntityOutputInterface extends BlockEntity implements IPipeNode, IOutputInterface {

    private IndustrialPipeNetwork network;
    private int priority = 0;
    private final OutputInterfaceFilter filter = new OutputInterfaceFilter();
    private int roundRobinFaceIndex = 0;

    private final IEnergyStorage energyStorage;
    private final IStrictEnergyHandler strictEnergyHandler;

    public TileEntityOutputInterface(BlockPos pos, BlockState state) {
        super(MCITileEntityTypes.OUTPUT_INTERFACE.get(), pos, state);

        this.energyStorage = new IEnergyStorage() {
            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                return 0;
            }

            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                IndustrialPipeNetwork net = getPipeNetwork();
                return net != null ? net.extractEnergy(maxExtract, simulate) : 0;
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
                return true;
            }

            @Override
            public boolean canReceive() {
                return false;
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
                return 0L;
            }

            @Override
            public long insertEnergy(int container, long amount, @NotNull Action action) {
                return amount;
            }

            @Override
            public long extractEnergy(int container, long amount, @NotNull Action action) {
                IndustrialPipeNetwork net = getPipeNetwork();
                if (net == null || amount <= 0) {
                    return 0L;
                }
                int feAmount = (int) Math.min(Integer.MAX_VALUE, (long) (amount * 0.4));
                int extractedFE = net.extractEnergy(feAmount, action.simulate());
                return (long) (extractedFE * 2.5);
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
            this.network.removeOutputInterface(this);
        }
        this.network = network;
        if (this.network != null) {
            this.network.addOutputInterface(this);
        }
    }

    @Override
    public boolean canConnectPipe(Direction direction) {
        return true;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
        setChanged();
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

    public OutputInterfaceFilter getFilter() {
        return filter;
    }

    @Override
    public List<ItemStack> getItemFilters() {
        return filter.getItemFilters();
    }

    @Override
    public List<FluidStack> getFluidFilters() {
        return filter.getFluidFilters();
    }

    @Override
    public List<ChemicalStack> getChemicalFilters() {
        return filter.getChemicalFilters();
    }

    @Override
    public void setFilter(int index, OutputInterfaceFilter.FilterType type, String filterId, ItemStack iconStack) {
        filter.setFilter(index, type, filterId, iconStack);
        setChanged();
    }

    @Override
    public void setFilter(int index, ItemStack rawStack) {
        filter.setFilter(index, rawStack);
        setChanged();
    }

    @Override
    public void clearFilter(int index) {
        filter.clearFilter(index);
        setChanged();
    }

    private mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl redstoneMode = mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl.DISABLED;

    @Override
    public mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    public void setRedstoneMode(mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl mode) {
        this.redstoneMode = mode != null ? mode : mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl.DISABLED;
        setChanged();
    }

    @Override
    public boolean isRedstonePowered() {
        return level != null && level.hasNeighborSignal(worldPosition);
    }

    @Override
    public boolean matchesItem(ItemStack stack) {
        if (!canOperate()) {
            return false;
        }
        return filter.matchesItem(stack);
    }

    @Override
    public boolean matchesFluid(FluidStack stack) {
        if (!canOperate()) {
            return false;
        }
        return filter.matchesFluid(stack);
    }

    @Override
    public boolean matchesChemical(ChemicalStack stack) {
        if (!canOperate()) {
            return false;
        }
        return filter.matchesChemical(stack);
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
    public ItemStack insertItemIntoTarget(ItemStack stack, boolean simulate) {
        if (level == null || stack.isEmpty()) {
            return stack;
        }
        List<Direction> candidates = getExternalCandidateFaces();
        if (candidates.isEmpty()) {
            return stack;
        }

        ItemStack remaining = stack.copy();
        int size = candidates.size();
        for (int i = 0; i < size; i++) {
            Direction dir = candidates.get((roundRobinFaceIndex + i) % size);
            BlockPos targetPos = worldPosition.relative(dir);
            if (!WorldUtils.isBlockLoaded(level, targetPos)) {
                continue;
            }
            IItemHandler target = WorldUtils.getCapability(level, net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, targetPos, dir.getOpposite());
            if (target != null) {
                ItemStack before = remaining.copy();
                remaining = ItemHandlerHelper.insertItem(target, remaining, simulate);
                if (remaining.getCount() < before.getCount() && !simulate) {
                    roundRobinFaceIndex = (roundRobinFaceIndex + i + 1) % size;
                }
                if (remaining.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return remaining;
    }

    @Override
    public FluidStack insertFluidIntoTarget(FluidStack stack, boolean simulate) {
        if (level == null || stack.isEmpty()) {
            return stack;
        }
        List<Direction> candidates = getExternalCandidateFaces();
        if (candidates.isEmpty()) {
            return stack;
        }

        FluidStack remaining = stack.copy();
        int size = candidates.size();
        for (int i = 0; i < size; i++) {
            Direction dir = candidates.get((roundRobinFaceIndex + i) % size);
            BlockPos targetPos = worldPosition.relative(dir);
            if (!WorldUtils.isBlockLoaded(level, targetPos)) {
                continue;
            }
            IFluidHandler target = WorldUtils.getCapability(level, net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK, targetPos, dir.getOpposite());
            if (target != null) {
                int accepted = target.fill(remaining, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
                if (accepted > 0 && !simulate) {
                    roundRobinFaceIndex = (roundRobinFaceIndex + i + 1) % size;
                }
                if (accepted >= remaining.getAmount()) {
                    return FluidStack.EMPTY;
                }
                remaining.shrink(accepted);
            }
        }
        return remaining;
    }

    @Override
    public ChemicalStack insertChemicalIntoTarget(ChemicalStack stack, boolean simulate) {
        if (level == null || stack.isEmpty()) {
            return stack;
        }
        List<Direction> candidates = getExternalCandidateFaces();
        if (candidates.isEmpty()) {
            return stack;
        }

        ChemicalStack remaining = stack.copy();
        int size = candidates.size();
        for (int i = 0; i < size; i++) {
            Direction dir = candidates.get((roundRobinFaceIndex + i) % size);
            BlockPos targetPos = worldPosition.relative(dir);
            if (!WorldUtils.isBlockLoaded(level, targetPos)) {
                continue;
            }
            IChemicalHandler target = WorldUtils.getCapability(level, Capabilities.CHEMICAL.block(), targetPos, dir.getOpposite());
            if (target != null) {
                ChemicalStack leftover = target.insertChemical(remaining, simulate ? Action.SIMULATE : Action.EXECUTE);
                if (leftover.getAmount() < remaining.getAmount() && !simulate) {
                    roundRobinFaceIndex = (roundRobinFaceIndex + i + 1) % size;
                }
                if (leftover.isEmpty()) {
                    return ChemicalStack.EMPTY;
                }
                remaining = leftover;
            }
        }
        return remaining;
    }

    public InteractionResult openMenu(Player player) {
        if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new ContainerOutputInterface(id, inv, this),
                    Component.translatable("gui.mekanism_complex_industries.output_interface")
            ), buf -> {
                buf.writeBlockPos(worldPosition);
                buf.writeBoolean(false); // is block form
                if (buf instanceof net.minecraft.network.RegistryFriendlyByteBuf regBuf) {
                    filter.writeToBuf(regBuf);
                }
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

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Priority", priority);
        tag.putByte("RedstoneMode", (byte) redstoneMode.ordinal());
        tag.putInt("RoundRobinIndex", roundRobinFaceIndex);
        tag.put("FilterData", filter.save(registries));
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Priority")) {
            priority = tag.getInt("Priority");
        }
        if (tag.contains("RedstoneMode")) {
            redstoneMode = com.complexindustries.mekanism.content.pipe.interfaces.IRedstoneControllable.byIndex(tag.getByte("RedstoneMode") & 255);
        }
        if (tag.contains("RoundRobinIndex")) {
            roundRobinFaceIndex = tag.getInt("RoundRobinIndex");
        }
        if (tag.contains("FilterData", CompoundTag.TAG_COMPOUND)) {
            filter.load(tag.getCompound("FilterData"), registries);
        }
    }
}
