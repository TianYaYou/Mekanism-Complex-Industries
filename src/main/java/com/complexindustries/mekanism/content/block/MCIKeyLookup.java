package com.complexindustries.mekanism.content.block;

import net.minecraft.network.chat.Component;

/**
 * Common abstraction for querying key states (e.g. description/details tooltips)
 * without directly referencing client-only classes like MekKeyHandler or MekanismKeyHandler
 * in common item code.
 */
public final class MCIKeyLookup {

    private static IKeyLookup INSTANCE = new IKeyLookup() {
        @Override
        public boolean isDescriptionKeyPressed() {
            return false;
        }

        @Override
        public boolean isDetailsKeyPressed() {
            return false;
        }

        @Override
        public Component getDescriptionKeyMessage() {
            return Component.literal("Shift");
        }

        @Override
        public Component getDetailsKeyMessage() {
            return Component.literal("Shift");
        }
    };

    private MCIKeyLookup() {}

    public static void setInstance(IKeyLookup instance) {
        if (instance != null) {
            INSTANCE = instance;
        }
    }

    public static boolean isDescriptionKeyPressed() {
        return INSTANCE.isDescriptionKeyPressed();
    }

    public static boolean isDetailsKeyPressed() {
        return INSTANCE.isDetailsKeyPressed();
    }

    public static Component getDescriptionKeyMessage() {
        return INSTANCE.getDescriptionKeyMessage();
    }

    public static Component getDetailsKeyMessage() {
        return INSTANCE.getDetailsKeyMessage();
    }

    public interface IKeyLookup {
        boolean isDescriptionKeyPressed();
        boolean isDetailsKeyPressed();
        Component getDescriptionKeyMessage();
        Component getDetailsKeyMessage();
    }
}
