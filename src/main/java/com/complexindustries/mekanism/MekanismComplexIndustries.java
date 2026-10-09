package com.complexindustries.mekanism;

import com.complexindustries.mekanism.network.MCIPacketHandler;
import com.complexindustries.mekanism.registration.MCIBlocks;
import com.complexindustries.mekanism.registration.MCIChemicals;
import com.complexindustries.mekanism.registration.MCIContainerTypes;
import com.complexindustries.mekanism.registration.MCICreativeTabs;
import com.complexindustries.mekanism.registration.MCIFluids;
import com.complexindustries.mekanism.registration.MCIItems;
import com.complexindustries.mekanism.registration.MCITileEntityTypes;
import mekanism.common.lib.Version;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(MCIConstants.MODID)
public class MekanismComplexIndustries {
    private static MekanismComplexIndustries instance;
    private final MCIPacketHandler packetHandler;
    private final Version versionNumber;

    public MekanismComplexIndustries(ModContainer modContainer, IEventBus modEventBus) {
        instance = this;
        versionNumber = new Version(modContainer);

        // Initialize dynamic upgrade injection into Mekanism Upgrade enum
        com.complexindustries.mekanism.content.upgrade.MCIUpgrades.init();

        // Register Deferred Registers
        MCIBlocks.BLOCKS.register(modEventBus);
        MCITileEntityTypes.TILE_ENTITY_TYPES.register(modEventBus);
        MCIContainerTypes.CONTAINER_TYPES.register(modEventBus);
        MCIFluids.FLUID_TYPES.register(modEventBus);
        MCIFluids.FLUIDS.register(modEventBus);
        MCIChemicals.CHEMICALS.register(modEventBus);
        MCIItems.ITEMS.register(modEventBus);
        MCICreativeTabs.CREATIVE_TABS.register(modEventBus);
        com.complexindustries.mekanism.registration.MCIRecipeTypes.RECIPE_TYPES.register(modEventBus);
        com.complexindustries.mekanism.registration.MCIRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        com.complexindustries.mekanism.registration.MCIParticleTypes.PARTICLE_TYPES.register(modEventBus);

        // Network Handler
        packetHandler = new MCIPacketHandler(modEventBus, versionNumber);

        // Lifecycle Events
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerCapabilities);

        // Ensure multiblock managers are classloaded and registered with Mekanism before world load
        com.complexindustries.mekanism.content.freezer.MCIFreezerMultiblock.FREEZER_MANAGER.getName();
        com.complexindustries.mekanism.content.refinery.MCIRefineryMultiblock.REFINERY_MANAGER.getName();
        com.complexindustries.mekanism.content.extractor.MCIFluidExtractorMultiblock.EXTRACTOR_MANAGER.getName();

        MCIConstants.LOGGER.info("Initializing {} [Petroleum Harvesting & Chemical Ecology]...", MCIConstants.MOD_NAME);
    }

    private void registerCapabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
        for (var itemHolder : MCIItems.ITEMS.getEntries()) {
            if (itemHolder.get() instanceof mekanism.common.capabilities.ICapabilityAware capabilityAware) {
                capabilityAware.attachCapabilities(event);
            }
        }
    }

    public static MekanismComplexIndustries instance() {
        return instance;
    }

    public static MCIPacketHandler packetHandler() {
        return instance.packetHandler;
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            String freezer = com.complexindustries.mekanism.content.freezer.MCIFreezerMultiblock.FREEZER_MANAGER.getName();
            String refinery = com.complexindustries.mekanism.content.refinery.MCIRefineryMultiblock.REFINERY_MANAGER.getName();
            String extractor = com.complexindustries.mekanism.content.extractor.MCIFluidExtractorMultiblock.EXTRACTOR_MANAGER.getName();
            MCIConstants.LOGGER.info("{} common setup completed (registered multiblocks: {}, {}, {}).", MCIConstants.MOD_NAME, freezer, refinery, extractor);
        });
    }
}
