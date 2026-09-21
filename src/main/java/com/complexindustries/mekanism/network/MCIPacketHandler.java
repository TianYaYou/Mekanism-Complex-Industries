package com.complexindustries.mekanism.network;

import com.complexindustries.mekanism.MCIConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class MCIPacketHandler {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MCIConstants.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void initialize() {
        INSTANCE.registerMessage(
                packetId++,
                PacketExtractorGuiInteract.class,
                PacketExtractorGuiInteract::encode,
                PacketExtractorGuiInteract::decode,
                PacketExtractorGuiInteract::handle
        );
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }
}
