package com.complexindustries.mekanism.network;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.content.solidifier.TileEntityChemicalSolidifierFactory;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketToggleFactorySorting(BlockPos pos) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketToggleFactorySorting> TYPE =
            new CustomPacketPayload.Type<>(MCIConstants.rl("toggle_factory_sorting"));

    public static final StreamCodec<FriendlyByteBuf, PacketToggleFactorySorting> STREAM_CODEC = StreamCodec.ofMember(
            PacketToggleFactorySorting::write,
            PacketToggleFactorySorting::decode
    );

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    public static PacketToggleFactorySorting decode(FriendlyByteBuf buf) {
        return new PacketToggleFactorySorting(buf.readBlockPos());
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketToggleFactorySorting> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        TileEntityChemicalSolidifierFactory solidifierFactory = WorldUtils.getTileEntity(
                TileEntityChemicalSolidifierFactory.class, player.level(), pos);
        if (solidifierFactory != null) {
            solidifierFactory.setSorting(!solidifierFactory.isSorting());
            return;
        }
        com.complexindustries.mekanism.content.soaker.TileEntityChemicalSoakingFactory soakingFactory = WorldUtils.getTileEntity(
                com.complexindustries.mekanism.content.soaker.TileEntityChemicalSoakingFactory.class, player.level(), pos);
        if (soakingFactory != null) {
            soakingFactory.setSorting(!soakingFactory.isSorting());
        }
    }
}
