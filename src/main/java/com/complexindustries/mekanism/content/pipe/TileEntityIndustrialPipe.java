package com.complexindustries.mekanism.content.pipe;

import com.complexindustries.mekanism.content.pipe.attachment.*;
import com.complexindustries.mekanism.content.pipe.interfaces.IPipeNode;
import com.complexindustries.mekanism.content.pipe.network.IndustrialPipeNetwork;
import com.complexindustries.mekanism.content.pipe.network.PipeNetworkManager;
import com.complexindustries.mekanism.registration.MCITileEntityTypes;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityIndustrialPipe extends BlockEntity implements IPipeNode {

    private IndustrialPipeNetwork network;
    private final IPipeAttachment[] attachments = new IPipeAttachment[6];

    private boolean isUnloading = false;

    public TileEntityIndustrialPipe(BlockPos pos, BlockState state) {
        super(MCITileEntityTypes.INDUSTRIAL_PIPE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.isUnloading = false;
        if (level != null && !level.isClientSide()) {
            PipeNetworkManager.onNodeAdded(level, worldPosition, this);
            BlockState current = getBlockState();
            if (current.getBlock() instanceof IndustrialPipeBlock pipeBlock) {
                BlockState updated = pipeBlock.updateConnections(current, level, worldPosition);
                if (updated != current) {
                    level.setBlock(worldPosition, updated, 3);
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
        this.network = network;
    }

    @Override
    public boolean canConnectPipe(Direction direction) {
        // A pipe connection can only form if this face does not have an attached interface panel
        return attachments[direction.ordinal()] == null;
    }

    @Nullable
    @Override
    public IPipeAttachment getAttachment(Direction face) {
        return attachments[face.ordinal()];
    }

    public void addAttachment(Direction face, IPipeAttachment attachment) {
        attachments[face.ordinal()] = attachment;
        if (network != null) {
            attachment.onAttachedToNetwork(network);
        }
        setChanged();
        if (level != null) {
            net.minecraft.world.level.block.state.BlockState current = getBlockState();
            if (current.getBlock() instanceof IndustrialPipeBlock pipeBlock) {
                level.setBlock(worldPosition, pipeBlock.updateConnections(current, level, worldPosition), 3);
            }
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void removeAttachment(Direction face) {
        IPipeAttachment existing = attachments[face.ordinal()];
        if (existing != null) {
            if (network != null) {
                existing.onDetachedFromNetwork(network);
            }
            attachments[face.ordinal()] = null;
            setChanged();
            if (level != null) {
                net.minecraft.world.level.block.state.BlockState current = getBlockState();
                if (current.getBlock() instanceof IndustrialPipeBlock pipeBlock) {
                    level.setBlock(worldPosition, pipeBlock.updateConnections(current, level, worldPosition), 3);
                }
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    public IPipeAttachment[] getAllAttachments() {
        return attachments;
    }

    // --- Capabilities delegation ---

    @Nullable
    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) return null;
        IPipeAttachment attachment = attachments[side.ordinal()];
        return attachment != null ? attachment.getItemHandler() : null;
    }

    @Nullable
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        if (side == null) return null;
        IPipeAttachment attachment = attachments[side.ordinal()];
        return attachment != null ? attachment.getFluidHandler() : null;
    }

    @Nullable
    public IChemicalHandler getChemicalHandler(@Nullable Direction side) {
        if (side == null) return null;
        IPipeAttachment attachment = attachments[side.ordinal()];
        return attachment != null ? attachment.getChemicalHandler() : null;
    }

    @Nullable
    public IEnergyStorage getEnergyHandler(@Nullable Direction side) {
        if (side == null) return null;
        IPipeAttachment attachment = attachments[side.ordinal()];
        return attachment != null ? attachment.getEnergyHandler() : null;
    }

    @Nullable
    public IStrictEnergyHandler getStrictEnergyHandler(@Nullable Direction side) {
        if (side == null) return null;
        IPipeAttachment attachment = attachments[side.ordinal()];
        return attachment != null ? attachment.getStrictEnergyHandler() : null;
    }

    // --- NBT & Sync ---

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag list = new ListTag();
        for (Direction dir : Direction.values()) {
            IPipeAttachment attachment = attachments[dir.ordinal()];
            if (attachment != null) {
                CompoundTag attTag = new CompoundTag();
                attTag.putByte("Face", (byte) dir.ordinal());
                attTag.putString("Type", attachment.getType().getName());
                attTag.put("Data", attachment.save(registries));
                list.add(attTag);
            }
        }
        tag.put("Attachments", list);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        for (int i = 0; i < 6; i++) {
            attachments[i] = null;
        }
        if (tag.contains("Attachments", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Attachments", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag attTag = list.getCompound(i);
                int faceIdx = attTag.getByte("Face") & 255;
                if (faceIdx < 6) {
                    Direction dir = Direction.values()[faceIdx];
                    String typeName = attTag.getString("Type");
                    IPipeAttachment attachment = createAttachmentByType(typeName, dir);
                    if (attachment != null) {
                        if (attTag.contains("Data", Tag.TAG_COMPOUND)) {
                            attachment.load(attTag.getCompound("Data"), registries);
                        }
                        attachments[faceIdx] = attachment;
                        if (network != null) {
                            attachment.onAttachedToNetwork(network);
                        }
                    }
                }
            }
        }
    }

    private IPipeAttachment createAttachmentByType(String name, Direction face) {
        return switch (name) {
            case "input" -> new InputInterfaceAttachment(this, face);
            case "output" -> new OutputInterfaceAttachment(this, face);
            case "power" -> new PowerInterfaceAttachment(this, face);
            default -> null;
        };
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider lookupProvider) {
        loadAdditional(tag, lookupProvider);
    }

    @Override
    public void onDataPacket(@NotNull net.minecraft.network.Connection net, @NotNull ClientboundBlockEntityDataPacket pkt, @NotNull HolderLookup.Provider lookupProvider) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            loadAdditional(tag, lookupProvider);
        }
    }
}
