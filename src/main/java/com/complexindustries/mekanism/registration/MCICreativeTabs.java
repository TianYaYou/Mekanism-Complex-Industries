package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.MCIConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MCICreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MCIConstants.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MCI_TAB = CREATIVE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + MCIConstants.MODID))
                    .icon(() -> new ItemStack(MCIItems.PETROLEUM_UPGRADE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(MCIItems.PETROLEUM_UPGRADE.get());
                        output.accept(MCIItems.SOLID_CRUDE_OIL.get());
                        output.accept(MCIItems.CRUDE_OIL_BUCKET.get());
                        output.accept(MCIItems.SOLID_CRUDE_OIL_ORE.get());
                        output.accept(MCIItems.DEEPSLATE_SOLID_CRUDE_OIL_ORE.get());
                        output.accept(MCIItems.RESISTIVE_COOLER.get());
                        output.accept(MCIItems.FREEZER_CASING.get());
                        output.accept(MCIItems.FREEZER_VALVE.get());
                        output.accept(MCIItems.FREEZER_CONTROLLER.get());
                        output.accept(MCIItems.REFRIGERANT_BUCKET.get());
                        output.accept(MCIItems.AIR_COMPRESSOR.get());
                        output.accept(MCIItems.CHEMICAL_SOLIDIFIER.get());
                        output.accept(MCIItems.BASIC_CHEMICAL_SOLIDIFIER_FACTORY.get());
                        output.accept(MCIItems.ADVANCED_CHEMICAL_SOLIDIFIER_FACTORY.get());
                        output.accept(MCIItems.ELITE_CHEMICAL_SOLIDIFIER_FACTORY.get());
                        output.accept(MCIItems.ULTIMATE_CHEMICAL_SOLIDIFIER_FACTORY.get());
                        output.accept(MCIItems.LIQUID_PROPYLENE_BUCKET.get());
                        output.accept(MCIItems.POLYPROPYLENE_PELLET.get());
                        output.accept(MCIItems.SOAKING_ROD.get());
                        output.accept(MCIItems.HEAVY_OIL_FUEL.get());
                        output.accept(MCIItems.CHEMICAL_SOAKER.get());
                        output.accept(MCIItems.BASIC_CHEMICAL_SOAKING_FACTORY.get());
                        output.accept(MCIItems.ADVANCED_CHEMICAL_SOAKING_FACTORY.get());
                        output.accept(MCIItems.ELITE_CHEMICAL_SOAKING_FACTORY.get());
                        output.accept(MCIItems.ULTIMATE_CHEMICAL_SOAKING_FACTORY.get());
                        output.accept(MCIItems.REFINERY_CASING.get());
                        output.accept(MCIItems.REFINERY_VALVE.get());
                        output.accept(MCIItems.REFINERY_CONTROLLER.get());
                        output.accept(MCIItems.REFINERY_DREDGE_PIPE.get());
                        output.accept(MCIItems.ENGINEERING_PLASTIC.get());
                        output.accept(MCIItems.CRUDE_SILICON.get());
                        output.accept(MCIItems.REFINED_SILICON.get());
                        output.accept(MCIItems.CRYSTAL_GROWTH_CHAMBER.get());
                        output.accept(MCIItems.BLANK_SILICON_WAFER.get());
                        output.accept(MCIItems.SILICON_SLICER.get());
                        output.accept(MCIItems.FILTERED_GLASS.get());
                        output.accept(MCIItems.PHOTOLITHOGRAPHY_MACHINE.get());
                        output.accept(MCIItems.CALCULATION_MASK.get());
                        output.accept(MCIItems.LOGIC_MASK.get());
                        output.accept(MCIItems.POLYPROPYLENE_SHEET.get());
                        output.accept(MCIItems.REINFORCED_POLYPROPYLENE_SHEET.get());
                        output.accept(MCIItems.SEMIFINISHED_CALCULATION_CHIP.get());
                        output.accept(MCIItems.SEMIFINISHED_LOGIC_CHIP.get());
                        output.accept(MCIItems.SEMIFINISHED_INFUSED_CALCULATION_CHIP.get());
                        output.accept(MCIItems.SEMIFINISHED_INFUSED_LOGIC_CHIP.get());
                        output.accept(MCIItems.SEMIFINISHED_REINFORCED_CALCULATION_CHIP.get());
                        output.accept(MCIItems.SEMIFINISHED_REINFORCED_LOGIC_CHIP.get());
                        output.accept(MCIItems.INFUSED_CALCULATION_CHIP.get());
                        output.accept(MCIItems.INFUSED_LOGIC_CHIP.get());
                        output.accept(MCIItems.REINFORCED_CALCULATION_CHIP.get());
                        output.accept(MCIItems.REINFORCED_LOGIC_CHIP.get());
                        output.accept(MCIItems.ATOMIC_CALCULATION_CHIP.get());
                        output.accept(MCIItems.ATOMIC_LOGIC_CHIP.get());
                        output.accept(MCIItems.CHEMICAL_FILM_COATER.get());
                        output.accept(MCIItems.FLOW_REGULATOR.get());
                        output.accept(MCIItems.INDUSTRIAL_PIPE.get());
                        output.accept(MCIItems.INPUT_INTERFACE.get());
                        output.accept(MCIItems.OUTPUT_INTERFACE.get());
                        output.accept(MCIItems.POWER_INTERFACE.get());
                        output.accept(MCIItems.INPUT_INTERFACE_PART.get());
                        output.accept(MCIItems.OUTPUT_INTERFACE_PART.get());
                        output.accept(MCIItems.POWER_INTERFACE_PART.get());
                    })
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> DECORATIVE_TAB = CREATIVE_TABS.register("decorative",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + MCIConstants.MODID + ".decorative"))
                    .icon(() -> new ItemStack(MCIItems.BITUMEN_BLOCK.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(MCIItems.BITUMEN_BLOCK.get());
                        output.accept(MCIItems.BITUMEN_STAIRS.get());
                        output.accept(MCIItems.BITUMEN_SLAB.get());
                        for (net.minecraft.world.item.DyeColor color : net.minecraft.world.item.DyeColor.values()) {
                            output.accept(MCIItems.DYED_BITUMEN_BLOCKS.get(color).get());
                            output.accept(MCIItems.DYED_BITUMEN_STAIRS.get(color).get());
                            output.accept(MCIItems.DYED_BITUMEN_SLABS.get(color).get());
                        }
                    })
                    .build());

    private MCICreativeTabs() {}
}
