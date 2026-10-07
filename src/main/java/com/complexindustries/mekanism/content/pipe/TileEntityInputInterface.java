package com.complexindustries.mekanism.content.pipe;

import com.complexindustries.mekanism.content.pipe.container.ContainerInputInterface;
import com.complexindustries.mekanism.content.pipe.interfaces.IInputInterface;
import com.complexindustries.mekanism.content.pipe.interfaces.IPipeNode;
import com.complexindustries.mekanism.content.pipe.network.IndustrialPipeNetwork;
import com.complexindustries.mekanism.content.pipe.network.PipeNetworkManager;
import com.complexindustries.mekanism.registration.MCITileEntityTypes;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.energy.IStrictEnergyHandler;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityInputInterface extends BlockEntity implements IPipeNode, IInputInterface {

    private IndustrialPipeNetwork network;
    private int priority = 0;

    private final IItemHandler itemHandler;
    private final IFluidHandler fluidHandler;
    private final IChemicalHandler chemicalHandler;
    private final IEnergyStorage energyStorage;
    private final IStrictEnergyHandler strictEnergyHandler;

    public TileEntityInputInterface(BlockPos pos, BlockState state) {
        super(MCITileEntityTypes.INPUT_INTERFACE.get(), pos, state);

        this.itemHandler = new IItemHandler() {
            @Override
            public int getSlots() {
                return 1;
            }

            @NotNull
            @Override
            public ItemStack getStackInSlot(int slot) {
                return ItemStack.EMPTY;
            }

            @NotNull
            @Override
            public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                return routeItem(stack, simulate);
            }

            @NotNull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return ItemStack.EMPTY;
            }

            @Override
            public int getSlotLimit(int slot) {
                return 64;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return true;
            }
        };

        this.fluidHandler = new IFluidHandler() {
            @Override
            public int getTanks() {
                return 1;
            }

            @NotNull
            @Override
            public FluidStack getFluidInTank(int tank) {
                return FluidStack.EMPTY;
            }

            @Override
            public int getTankCapacity(int tank) {
                return 1000;
            }

            @Override
            public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
                return true;
            }

            @Override
            public int fill(FluidStack resource, FluidAction action) {
                if (resource.isEmpty()) return 0;
                FluidStack remainder = routeFluid(resource, action.simulate());
                return resource.getAmount() - remainder.getAmount();
            }

            @NotNull
            @Override
            public FluidStack drain(FluidStack resource, FluidAction action) {
                return FluidStack.EMPTY;
            }

            @NotNull
            @Override
            public FluidStack drain(int maxDrain, FluidAction action) {
                return FluidStack.EMPTY;
            }
        };

        this.chemicalHandler = new IChemicalHandler() {
            @Override
            public int getChemicalTanks() {
                return 1;
            }

            @NotNull
            @Override
            public ChemicalStack getChemicalInTank(int tank) {
                return ChemicalStack.EMPTY;
            }

            @Override
            public void setChemicalInTank(int tank, @NotNull ChemicalStack stack) {
            }

            @Override
            public long getChemicalTankCapacity(int tank) {
                return 1000;
            }

            @Override
            public boolean isValid(int tank, @NotNull ChemicalStack stack) {
                return true;
            }

            @NotNull
            @Override
            public ChemicalStack insertChemical(int tank, @NotNull ChemicalStack stack, @NotNull Action action) {
                return routeChemical(stack, action.simulate());
            }

            @NotNull
            @Override
            public ChemicalStack extractChemical(int tank, long amount, @NotNull Action action) {
                return ChemicalStack.EMPTY;
            }
        };

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
            this.network.removeInputInterface(this);
        }
        this.network = network;
        if (this.network != null) {
            this.network.addInputInterface(this);
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
        return null; // Block state: not attached to a single face
    }

    public InteractionResult openMenu(Player player) {
        if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new ContainerInputInterface(id, inv, this),
                    Component.translatable("gui.mekanism_complex_industries.input_interface")
            ), buf -> {
                buf.writeBlockPos(worldPosition);
                buf.writeBoolean(false); // is block form
            });
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide());
    }

    // Capability getters
    public IItemHandler getItemHandler(@Nullable Direction side) {
        return itemHandler;
    }

    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        return fluidHandler;
    }

    public IChemicalHandler getChemicalHandler(@Nullable Direction side) {
        return chemicalHandler;
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
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Priority")) {
            priority = tag.getInt("Priority");
        }
    }
}
