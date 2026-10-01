package com.complexindustries.mekanism.network;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.content.tile.TileEntityResistiveCooler;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketSetCoolerEnergy(BlockPos pos, long energyUsage) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketSetCoolerEnergy> TYPE =
            new CustomPacketPayload.Type<>(MCIConstants.rl("set_cooler_energy"));

    public static final StreamCodec<FriendlyByteBuf, PacketSetCoolerEnergy> STREAM_CODEC = StreamCodec.ofMember(
            PacketSetCoolerEnergy::write,
            PacketSetCoolerEnergy::decode
    );

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeVarLong(energyUsage);
    }

    public static PacketSetCoolerEnergy decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        long energyUsage = buf.readVarLong();
        return new PacketSetCoolerEnergy(pos, energyUsage);
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketSetCoolerEnergy> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        TileEntityResistiveCooler cooler = WorldUtils.getTileEntity(
                TileEntityResistiveCooler.class, player.level(), pos);
        if (cooler != null) {
            cooler.setEnergyUsage(energyUsage);
        }
    }
}
