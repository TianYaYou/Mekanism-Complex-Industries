package com.complexindustries.mekanism.content.pipe.attachment;

import com.complexindustries.mekanism.content.pipe.TileEntityIndustrialPipe;
import com.complexindustries.mekanism.content.pipe.container.ContainerOutputInterface;
import com.complexindustries.mekanism.content.pipe.interfaces.IOutputInterface;
import com.complexindustries.mekanism.content.pipe.interfaces.OutputInterfaceFilter;
import com.complexindustries.mekanism.content.pipe.network.IndustrialPipeNetwork;
import com.complexindustries.mekanism.registration.MCIItems;
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
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OutputInterfaceAttachment implements IPipeAttachment, IOutputInterface {

    private final TileEntityIndustrialPipe pipe;
    private final Direction face;
    private int priority = 0;
    private mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl redstoneMode = mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl.DISABLED;
    private final OutputInterfaceFilter filter = new OutputInterfaceFilter();

    private final IEnergyStorage energyStorage;
    private final IStrictEnergyHandler strictEnergyHandler;

    public OutputInterfaceAttachment(TileEntityIndustrialPipe pipe, Direction face) {
        this.pipe = pipe;
        this.face = face;

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

    @Override
    public AttachmentType getType() {
        return AttachmentType.OUTPUT;
    }

    @Override
    public Direction getFace() {
        return face;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
        pipe.setChanged();
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
        pipe.setChanged();
    }

    @Override
    public void setFilter(int index, ItemStack rawStack) {
        filter.setFilter(index, rawStack);
        pipe.setChanged();
    }

    @Override
    public void clearFilter(int index) {
        filter.clearFilter(index);
        pipe.setChanged();
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

    @Override
    public ItemStack insertItemIntoTarget(ItemStack stack, boolean simulate) {
        Level level = pipe.getLevel();
        if (level == null || stack.isEmpty()) {
            return stack;
        }
        BlockPos targetPos = pipe.getBlockPos().relative(face);
        if (!WorldUtils.isBlockLoaded(level, targetPos)) {
            return stack;
        }
        IItemHandler target = WorldUtils.getCapability(level, net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, targetPos, face.getOpposite());
        if (target != null) {
            return ItemHandlerHelper.insertItem(target, stack, simulate);
        }
        return stack;
    }

    @Override
    public FluidStack insertFluidIntoTarget(FluidStack stack, boolean simulate) {
        Level level = pipe.getLevel();
        if (level == null || stack.isEmpty()) {
            return stack;
        }
        BlockPos targetPos = pipe.getBlockPos().relative(face);
        if (!WorldUtils.isBlockLoaded(level, targetPos)) {
            return stack;
        }
        IFluidHandler target = WorldUtils.getCapability(level, net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK, targetPos, face.getOpposite());
        if (target != null) {
            int accepted = target.fill(stack, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
            if (accepted >= stack.getAmount()) {
                return FluidStack.EMPTY;
            }
            FluidStack copy = stack.copy();
            copy.shrink(accepted);
            return copy;
        }
        return stack;
    }

    @Override
    public ChemicalStack insertChemicalIntoTarget(ChemicalStack stack, boolean simulate) {
        Level level = pipe.getLevel();
        if (level == null || stack.isEmpty()) {
            return stack;
        }
        BlockPos targetPos = pipe.getBlockPos().relative(face);
        if (!WorldUtils.isBlockLoaded(level, targetPos)) {
            return stack;
        }
        IChemicalHandler target = WorldUtils.getCapability(level, Capabilities.CHEMICAL.block(), targetPos, face.getOpposite());
        if (target != null) {
            return target.insertChemical(stack, simulate ? Action.SIMULATE : Action.EXECUTE);
        }
        return stack;
    }

    @Override
    public void onAttachedToNetwork(IndustrialPipeNetwork network) {
        network.addOutputInterface(this);
    }

    @Override
    public void onDetachedFromNetwork(IndustrialPipeNetwork network) {
        network.removeOutputInterface(this);
    }

    @Override
    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Priority", priority);
        tag.putByte("RedstoneMode", (byte) redstoneMode.ordinal());
        tag.put("FilterData", filter.save(registries));
        return tag;
    }

    @Override
    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("Priority")) {
            priority = tag.getInt("Priority");
        }
        if (tag.contains("RedstoneMode")) {
            redstoneMode = com.complexindustries.mekanism.content.pipe.interfaces.IRedstoneControllable.byIndex(tag.getByte("RedstoneMode") & 255);
        }
        if (tag.contains("FilterData", CompoundTag.TAG_COMPOUND)) {
            filter.load(tag.getCompound("FilterData"), registries);
        }
    }

    @Override
    public ItemStack getDropItem() {
        return new ItemStack(MCIItems.OUTPUT_INTERFACE_PART.get());
    }

    @Override
    public InteractionResult openMenu(Player player) {
        if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new ContainerOutputInterface(id, inv, this),
                    Component.translatable("gui.mekanism_complex_industries.output_interface")
            ), buf -> {
                buf.writeBlockPos(pipe.getBlockPos());
                buf.writeBoolean(true); // is attachment
                buf.writeByte(face.ordinal());
                if (buf instanceof net.minecraft.network.RegistryFriendlyByteBuf regBuf) {
                    filter.writeToBuf(regBuf);
                }
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
