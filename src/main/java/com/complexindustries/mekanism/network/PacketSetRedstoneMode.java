package com.complexindustries.mekanism.network;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.content.pipe.TileEntityIndustrialPipe;
import com.complexindustries.mekanism.content.pipe.interfaces.IRedstoneControllable;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PacketSetRedstoneMode(BlockPos pos, boolean isAttachment, @Nullable Direction face, RedstoneControl mode) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketSetRedstoneMode> TYPE =
            new CustomPacketPayload.Type<>(MCIConstants.rl("set_redstone_mode"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSetRedstoneMode> STREAM_CODEC = StreamCodec.ofMember(
            PacketSetRedstoneMode::write,
            PacketSetRedstoneMode::decode
    );

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeBoolean(isAttachment);
        if (isAttachment) {
            buf.writeByte(face != null ? face.ordinal() : 0);
        }
        buf.writeByte(mode != null ? mode.ordinal() : 0);
    }

    public static PacketSetRedstoneMode decode(RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        boolean isAttachment = buf.readBoolean();
        Direction face = isAttachment ? Direction.values()[buf.readByte() & 255] : null;
        RedstoneControl mode = IRedstoneControllable.byIndex(buf.readByte() & 255);
        return new PacketSetRedstoneMode(pos, isAttachment, face, mode);
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketSetRedstoneMode> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        BlockEntity be = player.level().getBlockEntity(pos);

        IRedstoneControllable target = null;
        if (isAttachment && be instanceof TileEntityIndustrialPipe pipe) {
            if (pipe.getAttachment(face) instanceof IRedstoneControllable c) {
                target = c;
            }
        } else if (be instanceof IRedstoneControllable c) {
            target = c;
        }

        if (target != null) {
            target.setRedstoneMode(mode);
        }
    }
}
