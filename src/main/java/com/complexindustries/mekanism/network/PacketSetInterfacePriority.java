package com.complexindustries.mekanism.network;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.content.pipe.TileEntityIndustrialPipe;
import com.complexindustries.mekanism.content.pipe.interfaces.IInputInterface;
import com.complexindustries.mekanism.content.pipe.interfaces.IOutputInterface;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PacketSetInterfacePriority(BlockPos pos, boolean isAttachment, @Nullable Direction face, int delta) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketSetInterfacePriority> TYPE =
            new CustomPacketPayload.Type<>(MCIConstants.rl("set_interface_priority"));

    public static final StreamCodec<FriendlyByteBuf, PacketSetInterfacePriority> STREAM_CODEC = StreamCodec.ofMember(
            PacketSetInterfacePriority::write,
            PacketSetInterfacePriority::decode
    );

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeBoolean(isAttachment);
        if (isAttachment) {
            buf.writeByte(face != null ? face.ordinal() : 0);
        }
        buf.writeVarInt(delta);
    }

    public static PacketSetInterfacePriority decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        boolean isAttachment = buf.readBoolean();
        Direction face = isAttachment ? Direction.values()[buf.readByte() & 255] : null;
        int delta = buf.readVarInt();
        return new PacketSetInterfacePriority(pos, isAttachment, face, delta);
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketSetInterfacePriority> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        BlockEntity be = player.level().getBlockEntity(pos);

        if (isAttachment && be instanceof TileEntityIndustrialPipe pipe) {
            var att = pipe.getAttachment(face);
            if (att instanceof IInputInterface in) {
                in.setPriority(in.getPriority() + delta);
            } else if (att instanceof IOutputInterface out) {
                out.setPriority(out.getPriority() + delta);
            }
        } else if (be instanceof IInputInterface in) {
            in.setPriority(in.getPriority() + delta);
        } else if (be instanceof IOutputInterface out) {
            out.setPriority(out.getPriority() + delta);
        }
    }
}
