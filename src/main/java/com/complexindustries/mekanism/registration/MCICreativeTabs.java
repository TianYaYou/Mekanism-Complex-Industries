package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.MCIConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class MCICreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MCIConstants.MODID);

    public static final RegistryObject<CreativeModeTab> MCI_TAB = CREATIVE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + MCIConstants.MODID))
                    .icon(() -> new ItemStack(MCIItems.PETROLEUM_UPGRADE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(MCIItems.PETROLEUM_UPGRADE.get());
                        output.accept(MCIItems.SOLID_CRUDE_OIL.get());
                        output.accept(MCIItems.CRUDE_OIL_BUCKET.get());
                        output.accept(MCIItems.SOLID_CRUDE_OIL_ORE.get());
                        output.accept(MCIItems.DEEPSLATE_SOLID_CRUDE_OIL_ORE.get());
                    })
                    .build());

    private MCICreativeTabs() {}
}
