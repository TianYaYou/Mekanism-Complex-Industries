package com.complexindustries.mekanism.network;

import mekanism.common.lib.Version;
import mekanism.common.network.BasePacketHandler;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;

public class MCIPacketHandler extends BasePacketHandler {

    public MCIPacketHandler(IEventBus modEventBus, Version version) {
        super(modEventBus, version);
    }

    @Override
    protected void registerClientToServer(PacketRegistrar registrar) {
        registrar.play(PacketSetCoolerEnergy.TYPE, PacketSetCoolerEnergy.STREAM_CODEC);
        registrar.play(PacketToggleFactorySorting.TYPE, PacketToggleFactorySorting.STREAM_CODEC);
        registrar.play(PacketSetFlowRate.TYPE, PacketSetFlowRate.STREAM_CODEC);
        registrar.play(PacketSetInterfacePriority.TYPE, PacketSetInterfacePriority.STREAM_CODEC);
        registrar.play(PacketSetOutputFilter.TYPE, PacketSetOutputFilter.STREAM_CODEC);
        registrar.play(PacketSetRedstoneMode.TYPE, PacketSetRedstoneMode.STREAM_CODEC);
    }

    @Override
    protected void registerServerToClient(PacketRegistrar registrar) {
        registrar.play(PacketSyncOutputFilter.TYPE, PacketSyncOutputFilter.STREAM_CODEC);
    }

    public static void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }
}
