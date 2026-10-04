package com.complexindustries.mekanism.network;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.content.flowregulator.TileEntityFlowRegulator;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketSetFlowRate(BlockPos pos, long flowRate) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketSetFlowRate> TYPE =
            new CustomPacketPayload.Type<>(MCIConstants.rl("set_flow_rate"));

    public static final StreamCodec<FriendlyByteBuf, PacketSetFlowRate> STREAM_CODEC = StreamCodec.ofMember(
            PacketSetFlowRate::write,
            PacketSetFlowRate::decode
    );

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeVarLong(flowRate);
    }

    public static PacketSetFlowRate decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        long flowRate = buf.readVarLong();
        return new PacketSetFlowRate(pos, flowRate);
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketSetFlowRate> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        TileEntityFlowRegulator regulator = WorldUtils.getTileEntity(
                TileEntityFlowRegulator.class, player.level(), pos);
        if (regulator != null) {
            regulator.setFlowRate(flowRate);
        }
    }
}
