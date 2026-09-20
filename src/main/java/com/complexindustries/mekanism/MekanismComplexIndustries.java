package com.complexindustries.mekanism;

import com.complexindustries.mekanism.registration.MCIBlocks;
import com.complexindustries.mekanism.registration.MCICreativeTabs;
import com.complexindustries.mekanism.registration.MCIItems;
import com.complexindustries.mekanism.registration.MCIMenuTypes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MCIConstants.MODID)
public class MekanismComplexIndustries {

    public MekanismComplexIndustries() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register Deferred Registers
        MCIItems.ITEMS.register(modEventBus);
        MCIBlocks.BLOCKS.register(modEventBus);
        MCICreativeTabs.CREATIVE_TABS.register(modEventBus);
        MCIMenuTypes.MENU_TYPES.register(modEventBus);

        // Lifecycle Events
        modEventBus.addListener(this::commonSetup);

        // Register to the global forge bus
        MinecraftForge.EVENT_BUS.register(this);

        MCIConstants.LOGGER.info("Initializing {}...", MCIConstants.MOD_NAME);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            MCIConstants.LOGGER.info("{} common setup completed.", MCIConstants.MOD_NAME);
        });
    }
}
