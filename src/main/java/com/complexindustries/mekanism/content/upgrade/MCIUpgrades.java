package com.complexindustries.mekanism.content.upgrade;

import com.complexindustries.mekanism.MCIConstants;
import mekanism.api.Upgrade;
import mekanism.api.text.EnumColor;
import mekanism.common.util.EnumUtils;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Arrays;

public final class MCIUpgrades {

    public static final Upgrade PETROLEUM;

    static {
        PETROLEUM = injectUpgrade();
    }

    public static void init() {
        MCIConstants.LOGGER.info("MCIUpgrades initialized. Injected PETROLEUM upgrade at ordinal {}", PETROLEUM.ordinal());
    }

    private static Upgrade injectUpgrade() {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe) unsafeField.get(null);

            // 1. Allocate Upgrade instance without invoking private enum constructor
            Upgrade instance = (Upgrade) unsafe.allocateInstance(Upgrade.class);

            // 2. Set java.lang.Enum fields: name, ordinal
            Field nameField = Enum.class.getDeclaredField("name");
            Field ordinalField = Enum.class.getDeclaredField("ordinal");
            unsafe.putObject(instance, unsafe.objectFieldOffset(nameField), "PETROLEUM");

            Upgrade[] currentValues = Upgrade.values();
            int ordinal = currentValues.length;
            unsafe.putInt(instance, unsafe.objectFieldOffset(ordinalField), ordinal);

            // 3. Set Upgrade class specific fields: name, maxStack, color
            Field rawNameField = Upgrade.class.getDeclaredField("name");
            Field maxStackField = Upgrade.class.getDeclaredField("maxStack");
            Field colorField = Upgrade.class.getDeclaredField("color");

            unsafe.putObject(instance, unsafe.objectFieldOffset(rawNameField), "petroleum");
            unsafe.putInt(instance, unsafe.objectFieldOffset(maxStackField), 1);
            unsafe.putObject(instance, unsafe.objectFieldOffset(colorField), EnumColor.DARK_AQUA);

            // 4. Update Upgrade.$VALUES
            Field valuesField = Upgrade.class.getDeclaredField("$VALUES");
            Object valuesBase = unsafe.staticFieldBase(valuesField);
            long valuesOffset = unsafe.staticFieldOffset(valuesField);
            Upgrade[] oldValues = (Upgrade[]) unsafe.getObject(valuesBase, valuesOffset);
            Upgrade[] newValues = Arrays.copyOf(oldValues, oldValues.length + 1);
            newValues[newValues.length - 1] = instance;
            unsafe.putObject(valuesBase, valuesOffset, newValues);

            // 5. Update Upgrade.UPGRADES
            Field upgradesField = Upgrade.class.getDeclaredField("UPGRADES");
            Object upgradesBase = unsafe.staticFieldBase(upgradesField);
            long upgradesOffset = unsafe.staticFieldOffset(upgradesField);
            Upgrade[] oldUpgrades = (Upgrade[]) unsafe.getObject(upgradesBase, upgradesOffset);
            Upgrade[] newUpgrades = Arrays.copyOf(oldUpgrades, oldUpgrades.length + 1);
            newUpgrades[newUpgrades.length - 1] = instance;
            unsafe.putObject(upgradesBase, upgradesOffset, newUpgrades);

            // 6. Clear Class.enumConstants and Class.enumConstantDirectory on Upgrade.class
            clearEnumCache(Upgrade.class, unsafe);

            // 7. Ensure EnumUtils class is loaded and initialized (its <clinit> runs UPGRADES = Upgrade.values())
            try {
                Class.forName("mekanism.common.util.EnumUtils", true, EnumUtils.class.getClassLoader());
            } catch (Throwable t) {
                MCIConstants.LOGGER.warn("Could not force initialize EnumUtils: {}", t.getMessage());
            }

            // 8. Update EnumUtils.UPGRADES if not already containing instance
            try {
                Field euField = EnumUtils.class.getDeclaredField("UPGRADES");
                Object euBase = unsafe.staticFieldBase(euField);
                long euOffset = unsafe.staticFieldOffset(euField);
                Upgrade[] oldEu = (Upgrade[]) unsafe.getObject(euBase, euOffset);
                if (oldEu != null) {
                    boolean found = false;
                    for (Upgrade u : oldEu) {
                        if (u == instance) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        Upgrade[] newEu = Arrays.copyOf(oldEu, oldEu.length + 1);
                        newEu[newEu.length - 1] = instance;
                        unsafe.putObject(euBase, euOffset, newEu);
                    }
                }
            } catch (Throwable t) {
                MCIConstants.LOGGER.warn("Could not update EnumUtils.UPGRADES: {}", t.getMessage());
            }

            return instance;
        } catch (Throwable e) {
            MCIConstants.LOGGER.error("Failed to dynamically inject PETROLEUM upgrade into Mekanism Upgrade enum", e);
            throw new RuntimeException("Failed to dynamically inject PETROLEUM upgrade into Mekanism Upgrade enum", e);
        }
    }

    private static void clearEnumCache(Class<?> clazz, Unsafe unsafe) {
        try {
            Field enumConstantsField = Class.class.getDeclaredField("enumConstants");
            unsafe.putObject(clazz, unsafe.objectFieldOffset(enumConstantsField), null);
        } catch (Throwable ignored) {}
        try {
            Field enumConstantDirectoryField = Class.class.getDeclaredField("enumConstantDirectory");
            unsafe.putObject(clazz, unsafe.objectFieldOffset(enumConstantDirectoryField), null);
        } catch (Throwable ignored) {}
    }
}
