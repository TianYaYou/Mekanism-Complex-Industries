package com.complexindustries.mekanism.client;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.client.gui.GuiAirCompressor;
import com.complexindustries.mekanism.client.gui.GuiResistiveCooler;
import com.complexindustries.mekanism.content.freezer.GuiFreezerController;
import com.complexindustries.mekanism.registration.MCIContainerTypes;
import mekanism.client.ClientRegistrationUtil;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = MCIConstants.MODID, value = Dist.CLIENT)
public class MCIClientRegistration {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        ClientRegistrationUtil.registerScreen(event, MCIContainerTypes.RESISTIVE_COOLER, GuiResistiveCooler::new);
        ClientRegistrationUtil.registerScreen(event, MCIContainerTypes.FREEZER_CONTROLLER, GuiFreezerController::new);
        ClientRegistrationUtil.registerScreen(event, MCIContainerTypes.AIR_COMPRESSOR, GuiAirCompressor::new);
        ClientRegistrationUtil.registerScreen(event, MCIContainerTypes.REFINERY_CONTROLLER, com.complexindustries.mekanism.content.refinery.GuiRefineryController::new);
    }
}
