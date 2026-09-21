package com.complexindustries.mekanism;

import com.complexindustries.mekanism.registration.MCIBlocks;
import com.complexindustries.mekanism.registration.MCICreativeTabs;
import com.complexindustries.mekanism.registration.MCIFluids;
import com.complexindustries.mekanism.registration.MCIGases;
import com.complexindustries.mekanism.registration.MCIItems;
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
        MCIBlocks.BLOCKS.register(modEventBus);
        MCIFluids.FLUID_TYPES.register(modEventBus);
        MCIFluids.FLUIDS.register(modEventBus);
        MCIGases.GASES.register(modEventBus);
        MCIItems.ITEMS.register(modEventBus);
        MCICreativeTabs.CREATIVE_TABS.register(modEventBus);

        // Lifecycle Events
        modEventBus.addListener(this::commonSetup);

        // Register to the global forge bus
        MinecraftForge.EVENT_BUS.register(this);

        MCIConstants.LOGGER.info("Initializing {} [Petroleum Harvesting & Chemical Ecology]...", MCIConstants.MOD_NAME);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            MCIConstants.LOGGER.info("{} common setup completed.", MCIConstants.MOD_NAME);
        });
    }
}
