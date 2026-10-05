package com.complexindustries.mekanism.client;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.client.gui.GuiAirCompressor;
import com.complexindustries.mekanism.client.gui.GuiChemicalSolidifier;
import com.complexindustries.mekanism.client.gui.GuiChemicalSolidifierFactory;
import com.complexindustries.mekanism.client.gui.GuiResistiveCooler;
import com.complexindustries.mekanism.content.flowregulator.GuiFlowRegulator;
import com.complexindustries.mekanism.content.flowregulator.TileEntityFlowRegulator;
import com.complexindustries.mekanism.content.freezer.GuiFreezerController;
import com.complexindustries.mekanism.content.refinery.GuiRefineryController;
import com.complexindustries.mekanism.registration.MCIBlocks;
import com.complexindustries.mekanism.registration.MCIContainerTypes;
import mekanism.client.ClientRegistrationUtil;
import mekanism.common.util.WorldUtils;
import net.minecraft.world.item.DyeColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = MCIConstants.MODID, value = Dist.CLIENT)
public class MCIClientRegistration {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        ClientRegistrationUtil.registerScreen(event, MCIContainerTypes.RESISTIVE_COOLER, GuiResistiveCooler::new);
        ClientRegistrationUtil.registerScreen(event, MCIContainerTypes.FREEZER_CONTROLLER, GuiFreezerController::new);
        ClientRegistrationUtil.registerScreen(event, MCIContainerTypes.AIR_COMPRESSOR, GuiAirCompressor::new);
        ClientRegistrationUtil.registerScreen(event, MCIContainerTypes.REFINERY_CONTROLLER, GuiRefineryController::new);
        ClientRegistrationUtil.registerScreen(event, MCIContainerTypes.CHEMICAL_SOLIDIFIER, GuiChemicalSolidifier::new);
        ClientRegistrationUtil.registerScreen(event, MCIContainerTypes.CHEMICAL_SOLIDIFIER_FACTORY, GuiChemicalSolidifierFactory::new);
        ClientRegistrationUtil.registerScreen(event, MCIContainerTypes.FLOW_REGULATOR, GuiFlowRegulator::new);
        ClientRegistrationUtil.registerScreen(event, MCIContainerTypes.CHEMICAL_SOAKER, com.complexindustries.mekanism.client.gui.GuiChemicalSoaker::new);
        ClientRegistrationUtil.registerScreen(event, MCIContainerTypes.CHEMICAL_SOAKING_FACTORY, com.complexindustries.mekanism.client.gui.GuiChemicalSoakingFactory::new);
        ClientRegistrationUtil.registerScreen(event, MCIContainerTypes.CRYSTAL_GROWTH_CHAMBER, com.complexindustries.mekanism.client.gui.GuiCrystalGrowthChamber::new);
        ClientRegistrationUtil.registerScreen(event, MCIContainerTypes.SILICON_SLICER, com.complexindustries.mekanism.client.gui.GuiSiliconSlicer::new);
        ClientRegistrationUtil.registerScreen(event, MCIContainerTypes.PHOTOLITHOGRAPHY_MACHINE, com.complexindustries.mekanism.client.gui.GuiPhotolithographyMachine::new);
    }

    @SubscribeEvent
    public static void registerParticleFactories(net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(com.complexindustries.mekanism.registration.MCIParticleTypes.ULTRAVIOLET_LASER.get(),
                com.complexindustries.mekanism.client.particle.UltravioletLaserParticle.Factory::new);
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> {
            if (tintIndex == 0 && level != null && pos != null) {
                TileEntityFlowRegulator tile = WorldUtils.getTileEntity(TileEntityFlowRegulator.class, level, pos);
                if (tile != null) {
                    return tile.getRingColor().getTextureDiffuseColor();
                }
            }
            return -1;
        }, MCIBlocks.FLOW_REGULATOR.get());
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
            if (tintIndex == 0) {
                return DyeColor.WHITE.getTextureDiffuseColor();
            }
            return -1;
        }, MCIBlocks.FLOW_REGULATOR.asItem());
    }

    @SubscribeEvent
    public static void registerRenderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(com.complexindustries.mekanism.registration.MCITileEntityTypes.CRYSTAL_GROWTH_CHAMBER.get(),
                com.complexindustries.mekanism.client.render.CrystalGrowthChamberRenderer::new);
    }
}

