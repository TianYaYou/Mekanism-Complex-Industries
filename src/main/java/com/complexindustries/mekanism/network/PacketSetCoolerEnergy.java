package com.complexindustries.mekanism.network;

import com.complexindustries.mekanism.content.tile.TileEntityResistiveCooler;
import mekanism.api.math.FloatingLong;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSetCoolerEnergy {
    private final BlockPos pos;
    private final FloatingLong energyUsage;

    public PacketSetCoolerEnergy(BlockPos pos, FloatingLong energyUsage) {
        this.pos = pos;
        this.energyUsage = energyUsage;
    }

    public static void encode(PacketSetCoolerEnergy msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        msg.energyUsage.writeToBuffer(buf);
    }

    public static PacketSetCoolerEnergy decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        FloatingLong energyUsage = FloatingLong.readFromBuffer(buf);
        return new PacketSetCoolerEnergy(pos, energyUsage);
    }

    public static void handle(PacketSetCoolerEnergy msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null) {
                TileEntityResistiveCooler cooler = WorldUtils.getTileEntity(
                        TileEntityResistiveCooler.class, sender.level(), msg.pos);
                if (cooler != null) {
                    cooler.setEnergyUsage(msg.energyUsage);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
