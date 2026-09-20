package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.MCIConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class MCICreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MCIConstants.MODID);

    public static final RegistryObject<CreativeModeTab> MCI_TAB = CREATIVE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + MCIConstants.MODID))
                    .icon(() -> MCIItems.COMPLEX_ALLOY.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(MCIItems.COMPLEX_ALLOY.get());
                        output.accept(MCIItems.INDUSTRIAL_CIRCUIT.get());
                        output.accept(MCIBlocks.COMPLEX_CASING.get());
                    })
                    .build());

    private MCICreativeTabs() {}
}
