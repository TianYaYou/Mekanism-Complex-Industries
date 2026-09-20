package com.complexindustries.mekanism;

import com.complexindustries.mekanism.command.MCIDebugCommand;
import com.complexindustries.mekanism.registration.MCIBlockEntityTypes;
import com.complexindustries.mekanism.registration.MCIBlocks;
import com.complexindustries.mekanism.registration.MCICreativeTabs;
import com.complexindustries.mekanism.registration.MCIFluids;
import com.complexindustries.mekanism.registration.MCIItems;
import com.complexindustries.mekanism.registration.MCIMenuTypes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
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
        MCIBlocks.FLUID_BLOCKS.register(modEventBus);
        MCIFluids.FLUID_TYPES.register(modEventBus);
        MCIFluids.FLUIDS.register(modEventBus);
        MCIBlockEntityTypes.TILE_ENTITY_TYPES.register(modEventBus);
        MCIMenuTypes.CONTAINER_TYPES.register(modEventBus);
        MCIItems.ITEMS.register(modEventBus);
        MCICreativeTabs.CREATIVE_TABS.register(modEventBus);

        // Lifecycle Events
        modEventBus.addListener(this::commonSetup);

        // Register to the global forge bus
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);

        MCIConstants.LOGGER.info("Initializing {}...", MCIConstants.MOD_NAME);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            MCIConstants.LOGGER.info("{} common setup completed.", MCIConstants.MOD_NAME);
        });
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        MCIDebugCommand.register(event.getDispatcher());
    }
}
