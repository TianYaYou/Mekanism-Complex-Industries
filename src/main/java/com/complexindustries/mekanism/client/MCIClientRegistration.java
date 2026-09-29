package com.complexindustries.mekanism.client;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.client.gui.GuiResistiveCooler;
import com.complexindustries.mekanism.registration.MCIContainerTypes;
import mekanism.client.ClientRegistrationUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = MCIConstants.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MCIClientRegistration {

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClientRegistrationUtil.registerScreen(MCIContainerTypes.RESISTIVE_COOLER, GuiResistiveCooler::new);
            ClientRegistrationUtil.registerScreen(MCIContainerTypes.FREEZER_CONTROLLER, com.complexindustries.mekanism.content.freezer.GuiFreezerController::new);
            ClientRegistrationUtil.registerScreen(MCIContainerTypes.AIR_COMPRESSOR, com.complexindustries.mekanism.client.gui.GuiAirCompressor::new);
        });
    }
}
