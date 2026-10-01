package com.complexindustries.mekanism.content.upgrade;

import com.complexindustries.mekanism.MCIConstants;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import mekanism.api.Upgrade;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.common.util.EnumUtils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.IntFunction;

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

            // 3. Set Upgrade class specific fields: name, langKey, descLangKey, maxStack, color
            Field rawNameField = Upgrade.class.getDeclaredField("name");
            Field langKeyField = Upgrade.class.getDeclaredField("langKey");
            Field descLangKeyField = Upgrade.class.getDeclaredField("descLangKey");
            Field maxStackField = Upgrade.class.getDeclaredField("maxStack");
            Field colorField = Upgrade.class.getDeclaredField("color");

            ILangEntry langKey = () -> "upgrade.mekanism_complex_industries.petroleum";
            ILangEntry descLangKey = () -> "description.mekanism_complex_industries.petroleum";

            unsafe.putObject(instance, unsafe.objectFieldOffset(rawNameField), "petroleum");
            unsafe.putObject(instance, unsafe.objectFieldOffset(langKeyField), langKey);
            unsafe.putObject(instance, unsafe.objectFieldOffset(descLangKeyField), descLangKey);
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

            // 5. Update Upgrade.BY_ID, STREAM_CODEC, and CODEC for 1.21.1
            try {
                IntFunction<Upgrade> newById = ByIdMap.continuous(Upgrade::ordinal, newValues, ByIdMap.OutOfBoundsStrategy.WRAP);
                Field byIdField = Upgrade.class.getDeclaredField("BY_ID");
                Object byIdBase = unsafe.staticFieldBase(byIdField);
                long byIdOffset = unsafe.staticFieldOffset(byIdField);
                unsafe.putObject(byIdBase, byIdOffset, newById);

                Field streamCodecField = Upgrade.class.getDeclaredField("STREAM_CODEC");
                Object scBase = unsafe.staticFieldBase(streamCodecField);
                long scOffset = unsafe.staticFieldOffset(streamCodecField);
                StreamCodec<ByteBuf, Upgrade> newStreamCodec = ByteBufCodecs.idMapper(newById, Upgrade::ordinal);
                unsafe.putObject(scBase, scOffset, newStreamCodec);

                Field codecField = Upgrade.class.getDeclaredField("CODEC");
                Object codecBase = unsafe.staticFieldBase(codecField);
                long codecOffset = unsafe.staticFieldOffset(codecField);
                Function<String, Upgrade> nameLookup = StringRepresentable.createNameLookup(newValues, Function.identity());
                Function<String, Upgrade> remapper = it -> "gas".equals(it) ? Upgrade.CHEMICAL : nameLookup.apply(it);
                Codec<Upgrade> newCodec = new StringRepresentable.EnumCodec<>(newValues, remapper);
                unsafe.putObject(codecBase, codecOffset, newCodec);
            } catch (Throwable t) {
                MCIConstants.LOGGER.warn("Could not update Upgrade codecs: {}", t.getMessage());
            }

            // 6. Clear Class.enumConstants and Class.enumConstantDirectory on Upgrade.class
            clearEnumCache(Upgrade.class, unsafe);

            // 7. Ensure EnumUtils class is loaded and initialized
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
