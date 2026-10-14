package com.complexindustries.mekanism.network;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.content.pipe.TileEntityIndustrialPipe;
import com.complexindustries.mekanism.content.pipe.interfaces.IInputInterface;
import com.complexindustries.mekanism.content.pipe.interfaces.IOutputInterface;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.util.WorldUtils;
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
        Direction face = null;
        if (isAttachment) {
            int faceOrd = buf.readByte() & 255;
            Direction[] dirs = Direction.values();
            face = faceOrd < dirs.length ? dirs[faceOrd] : null;
        }
        int delta = buf.readVarInt();
        // Clamp single delta step to a reasonable range [-1000, 1000]
        delta = Math.max(-1000, Math.min(1000, delta));
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
        if (!MCIPacketSecurity.canAccess(player, pos, null)) {
            return;
        }
        BlockEntity be = WorldUtils.getTileEntity(player.level(), pos);
        if (be == null) {
            return;
        }

        // Clamp priority to safe bounds [-10000, 10000]
        if (isAttachment && be instanceof TileEntityIndustrialPipe pipe) {
            var att = pipe.getAttachment(face);
            if (att instanceof IInputInterface in) {
                int next = (int) Math.max(-10000L, Math.min(10000L, (long) in.getPriority() + delta));
                in.setPriority(next);
            } else if (att instanceof IOutputInterface out) {
                int next = (int) Math.max(-10000L, Math.min(10000L, (long) out.getPriority() + delta));
                out.setPriority(next);
            }
        } else if (be instanceof IInputInterface in) {
            int next = (int) Math.max(-10000L, Math.min(10000L, (long) in.getPriority() + delta));
            in.setPriority(next);
        } else if (be instanceof IOutputInterface out) {
            int next = (int) Math.max(-10000L, Math.min(10000L, (long) out.getPriority() + delta));
            out.setPriority(next);
        }
    }
}
