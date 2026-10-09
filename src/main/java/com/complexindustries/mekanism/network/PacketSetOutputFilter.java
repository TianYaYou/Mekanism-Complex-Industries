package com.complexindustries.mekanism.network;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.content.pipe.TileEntityIndustrialPipe;
import com.complexindustries.mekanism.content.pipe.interfaces.IOutputInterface;
import com.complexindustries.mekanism.content.pipe.interfaces.OutputInterfaceFilter;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PacketSetOutputFilter(
        BlockPos pos,
        boolean isAttachment,
        @Nullable Direction face,
        int slotIndex,
        OutputInterfaceFilter.FilterType filterType,
        String filterId,
        ItemStack stack
) implements IMekanismPacket {

    public PacketSetOutputFilter(BlockPos pos, boolean isAttachment, @Nullable Direction face, int slotIndex, ItemStack stack) {
        this(pos, isAttachment, face, slotIndex, OutputInterfaceFilter.FilterType.ITEM, "", stack);
    }

    public static final CustomPacketPayload.Type<PacketSetOutputFilter> TYPE =
            new CustomPacketPayload.Type<>(MCIConstants.rl("set_output_filter"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSetOutputFilter> STREAM_CODEC = StreamCodec.ofMember(
            PacketSetOutputFilter::write,
            PacketSetOutputFilter::decode
    );

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeBoolean(isAttachment);
        if (isAttachment) {
            buf.writeByte(face != null ? face.ordinal() : 0);
        }
        buf.writeVarInt(slotIndex);
        buf.writeByte(filterType != null ? filterType.ordinal() : 0);
        buf.writeUtf(filterId != null ? filterId : "", 128);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack != null ? stack : ItemStack.EMPTY);
    }

    public static PacketSetOutputFilter decode(RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        boolean isAttachment = buf.readBoolean();
        Direction face = isAttachment ? Direction.values()[buf.readByte() & 255] : null;
        int slotIndex = buf.readVarInt();
        OutputInterfaceFilter.FilterType filterType = OutputInterfaceFilter.FilterType.byOrdinal(buf.readByte() & 255);
        String filterId = buf.readUtf(128);
        ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
        return new PacketSetOutputFilter(pos, isAttachment, face, slotIndex, filterType, filterId, stack);
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketSetOutputFilter> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        BlockEntity be = player.level().getBlockEntity(pos);

        IOutputInterface out = null;
        if (isAttachment && be instanceof TileEntityIndustrialPipe pipe) {
            if (pipe.getAttachment(face) instanceof IOutputInterface o) {
                out = o;
            }
        } else if (be instanceof IOutputInterface o) {
            out = o;
        }

        if (out != null) {
            out.setFilter(slotIndex, filterType, filterId, stack);
            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new PacketSyncOutputFilter(out.getFilter().getEntries()));
            }
        }
    }
}
