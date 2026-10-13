package com.complexindustries.mekanism.client;

import com.complexindustries.mekanism.content.block.MCIKeyLookup;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import net.minecraft.network.chat.Component;

/**
 * Client-side implementation of MCIKeyLookup that accesses Mekanism's key handlers.
 * This class is strictly isolated to client dist to avoid Dedicated Server classloading failures.
 */
public class ClientKeyLookup implements MCIKeyLookup.IKeyLookup {

    @Override
    public boolean isDescriptionKeyPressed() {
        return MekKeyHandler.isKeyPressed(MekanismKeyHandler.descriptionKey);
    }

    @Override
    public boolean isDetailsKeyPressed() {
        return MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey);
    }

    @Override
    public Component getDescriptionKeyMessage() {
        return MekanismKeyHandler.descriptionKey.getTranslatedKeyMessage();
    }

    @Override
    public Component getDetailsKeyMessage() {
        return MekanismKeyHandler.detailsKey.getTranslatedKeyMessage();
    }
}
