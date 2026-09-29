package com.complexindustries.mekanism.network;

import com.complexindustries.mekanism.MCIConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class MCIPacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MCIConstants.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int id = 0;

    public static void init() {
        INSTANCE.registerMessage(
                id++,
                PacketSetCoolerEnergy.class,
                PacketSetCoolerEnergy::encode,
                PacketSetCoolerEnergy::decode,
                PacketSetCoolerEnergy::handle
        );
    }

    private MCIPacketHandler() {}
}
