package com.complexindustries.mekanism.client;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.client.gui.GuiCrudeOilExtractor;
import com.complexindustries.mekanism.registration.MCIBlocks;
import com.complexindustries.mekanism.registration.MCIFluids;
import com.complexindustries.mekanism.registration.MCIMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = MCIConstants.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientSetup {

    private ClientSetup() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(MCIMenuTypes.CRUDE_OIL_EXTRACTOR.get(), GuiCrudeOilExtractor::new);

            ItemBlockRenderTypes.setRenderLayer(MCIFluids.CRUDE_OIL_SOURCE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(MCIFluids.CRUDE_OIL_FLOWING.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(MCIBlocks.CRUDE_OIL_BLOCK.get(), RenderType.translucent());

            MCIConstants.LOGGER.info("Initialized {} client setup.", MCIConstants.MOD_NAME);
        });
    }
}
