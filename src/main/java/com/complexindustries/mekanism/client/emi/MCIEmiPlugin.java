package com.complexindustries.mekanism.client.emi;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.client.jei.AirCompressorJEIRecipe;
import com.complexindustries.mekanism.client.jei.ChemicalSolidifierJEIRecipe;
import com.complexindustries.mekanism.client.jei.FreezerJEIRecipe;
import com.complexindustries.mekanism.client.jei.RefineryJEIRecipe;
import com.complexindustries.mekanism.registration.MCIBlocks;
import com.complexindustries.mekanism.registration.MCIChemicals;
import com.complexindustries.mekanism.registration.MCIFluids;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

@EmiEntrypoint
public class MCIEmiPlugin implements EmiPlugin {

    public static final EmiRecipeCategory FREEZER_CATEGORY = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "freezer"),
            EmiStack.of(MCIBlocks.FREEZER_CONTROLLER.get()),
            EmiStack.of(MCIBlocks.FREEZER_CONTROLLER.get())
    ) {
        @Override
        public Component getName() {
            return Component.translatable("gui.mekanism_complex_industries.freezer.category");
        }
    };

    public static final EmiRecipeCategory AIR_COMPRESSOR_CATEGORY = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "air_compressor"),
            EmiStack.of(MCIBlocks.AIR_COMPRESSOR.get()),
            EmiStack.of(MCIBlocks.AIR_COMPRESSOR.get())
    ) {
        @Override
        public Component getName() {
            return Component.translatable("gui.mekanism_complex_industries.air_compressor.category");
        }
    };

    public static final EmiRecipeCategory REFINERY_CATEGORY = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "refinery"),
            EmiStack.of(MCIBlocks.REFINERY_CONTROLLER.get()),
            EmiStack.of(MCIBlocks.REFINERY_CONTROLLER.get())
    ) {
        @Override
        public Component getName() {
            return Component.translatable("gui.mekanism_complex_industries.refinery.category");
        }
    };

    public static final EmiRecipeCategory CHEMICAL_SOLIDIFIER_CATEGORY = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "chemical_solidifying"),
            EmiStack.of(MCIBlocks.CHEMICAL_SOLIDIFIER.get()),
            EmiStack.of(MCIBlocks.CHEMICAL_SOLIDIFIER.get())
    ) {
        @Override
        public Component getName() {
            return Component.translatable("gui.mekanism_complex_industries.chemical_solidifier.category");
        }
    };

    @Override
    public void register(EmiRegistry registry) {
        // 1. Categories
        registry.addCategory(FREEZER_CATEGORY);
        registry.addCategory(AIR_COMPRESSOR_CATEGORY);
        registry.addCategory(REFINERY_CATEGORY);
        registry.addCategory(CHEMICAL_SOLIDIFIER_CATEGORY);

        // 2. Workstations
        registry.addWorkstation(FREEZER_CATEGORY, EmiStack.of(MCIBlocks.FREEZER_CONTROLLER.get()));
        registry.addWorkstation(FREEZER_CATEGORY, EmiStack.of(MCIBlocks.FREEZER_CASING.get()));
        registry.addWorkstation(FREEZER_CATEGORY, EmiStack.of(MCIBlocks.FREEZER_VALVE.get()));

        registry.addWorkstation(AIR_COMPRESSOR_CATEGORY, EmiStack.of(MCIBlocks.AIR_COMPRESSOR.get()));

        registry.addWorkstation(REFINERY_CATEGORY, EmiStack.of(MCIBlocks.REFINERY_CONTROLLER.get()));
        registry.addWorkstation(REFINERY_CATEGORY, EmiStack.of(MCIBlocks.REFINERY_CASING.get()));
        registry.addWorkstation(REFINERY_CATEGORY, EmiStack.of(MCIBlocks.REFINERY_VALVE.get()));

        registry.addWorkstation(CHEMICAL_SOLIDIFIER_CATEGORY, EmiStack.of(MCIBlocks.CHEMICAL_SOLIDIFIER.get()));

        // 3. Freezer Recipes
        registry.addRecipe(new FreezerEmiRecipe(FREEZER_CATEGORY,
                ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "freezer/nitrogen"),
                new FreezerJEIRecipe(
                        null,
                        MCIChemicals.COMPRESSED_AIR.asStack(1000),
                        null,
                        MCIChemicals.NITROGEN.asStack(500),
                        Component.translatable("gui.mekanism_complex_industries.jei.process.nitrogen"),
                        Component.translatable("gui.mekanism_complex_industries.jei.temp_requirement")
                )));

        registry.addRecipe(new FreezerEmiRecipe(FREEZER_CATEGORY,
                ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "freezer/noble_gas"),
                new FreezerJEIRecipe(
                        new FluidStack(MCIFluids.SOURCE_CRYOGENIC_REFRIGERANT.get(), 1000),
                        MCIChemicals.COMPRESSED_AIR.asStack(1000),
                        new FluidStack(Fluids.WATER, 1000),
                        MCIChemicals.NOBLE_GAS.asStack(10),
                        Component.translatable("gui.mekanism_complex_industries.jei.process.noble_gas"),
                        Component.translatable("gui.mekanism_complex_industries.jei.temp_requirement")
                )));

        registry.addRecipe(new FreezerEmiRecipe(FREEZER_CATEGORY,
                ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "freezer/refrigerant"),
                new FreezerJEIRecipe(
                        new FluidStack(Fluids.WATER, 1000),
                        null,
                        new FluidStack(MCIFluids.SOURCE_CRYOGENIC_REFRIGERANT.get(), 1000),
                        null,
                        Component.translatable("gui.mekanism_complex_industries.jei.process.refrigerant"),
                        Component.translatable("gui.mekanism_complex_industries.jei.temp_requirement")
                )));

        // 4. Air Compressor Recipes
        registry.addRecipe(new AirCompressorEmiRecipe(AIR_COMPRESSOR_CATEGORY,
                ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "air_compressor/air"),
                new AirCompressorJEIRecipe(
                        MCIChemicals.COMPRESSED_AIR.asStack(1000),
                        Component.translatable("gui.mekanism_complex_industries.jei.air_compressor.usage"),
                        Component.translatable("gui.mekanism_complex_industries.jei.air_compressor.rate"),
                        Component.translatable("gui.mekanism_complex_industries.jei.air_compressor.source")
                )));

        // 5. Refinery Cracking Recipes (Peak 640 mB/s)
        registry.addRecipe(new RefineryEmiRecipe(REFINERY_CATEGORY,
                ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "refinery/cracking"),
                new RefineryJEIRecipe(
                        MCIChemicals.DENSE_CRUDE_OIL.asStack(1000),
                        MCIChemicals.NITROGEN.asStack(1000),
                        MCIChemicals.BITUMEN.asStack(150),
                        MCIChemicals.HEAVY_OIL.asStack(100),
                        MCIChemicals.REFINED_FUEL.asStack(100),
                        MCIChemicals.NAPHTHA.asStack(200),
                        MCIChemicals.PETROLEUM_GAS.asStack(2000),
                        Component.translatable("gui.mekanism_complex_industries.jei.refinery.title"),
                        Component.translatable("gui.mekanism_complex_industries.jei.refinery.short_heat"),
                        Component.translatable("gui.mekanism_complex_industries.jei.refinery.short_delta"),
                        Component.translatable("gui.mekanism_complex_industries.jei.refinery.short_rate")
                )));

        // 6. Chemical Solidifier Recipes
        registry.addRecipe(new ChemicalSolidifierEmiRecipe(CHEMICAL_SOLIDIFIER_CATEGORY,
                ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "chemical_solidifier/bitumen"),
                new ChemicalSolidifierJEIRecipe(
                        MCIChemicals.BITUMEN.asStack(200),
                        new ItemStack(MCIBlocks.BITUMEN_BLOCK.get()),
                        Component.translatable("gui.mekanism_complex_industries.jei.chemical_solidifier.usage_val"),
                        Component.translatable("gui.mekanism_complex_industries.jei.chemical_solidifier.duration_val")
                )));

        // 7. Chemical Soaking Recipes
        registry.addCategory(CHEMICAL_SOAKING_CATEGORY);
        registry.addWorkstation(CHEMICAL_SOAKING_CATEGORY, EmiStack.of(MCIBlocks.CHEMICAL_SOAKER.get()));
        registry.addWorkstation(CHEMICAL_SOAKING_CATEGORY, EmiStack.of(MCIBlocks.BASIC_CHEMICAL_SOAKING_FACTORY.get()));
        registry.addWorkstation(CHEMICAL_SOAKING_CATEGORY, EmiStack.of(MCIBlocks.ADVANCED_CHEMICAL_SOAKING_FACTORY.get()));
        registry.addWorkstation(CHEMICAL_SOAKING_CATEGORY, EmiStack.of(MCIBlocks.ELITE_CHEMICAL_SOAKING_FACTORY.get()));
        registry.addWorkstation(CHEMICAL_SOAKING_CATEGORY, EmiStack.of(MCIBlocks.ULTIMATE_CHEMICAL_SOAKING_FACTORY.get()));

        registry.addRecipe(new ChemicalSoakingEmiRecipe(CHEMICAL_SOAKING_CATEGORY,
                ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "chemical_soaking/heavy_oil_fuel"),
                new com.complexindustries.mekanism.client.jei.ChemicalSoakingJEIRecipe(
                        java.util.List.of(new ItemStack(mekanism.common.registries.MekanismItems.SAWDUST.asItem())),
                        java.util.List.of(MCIChemicals.HEAVY_OIL.asStack(200)),
                        new ItemStack(com.complexindustries.mekanism.registration.MCIItems.HEAVY_OIL_FUEL.get())
                )));
    }

    public static final EmiRecipeCategory CHEMICAL_SOAKING_CATEGORY = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "chemical_soaking"),
            EmiStack.of(MCIBlocks.CHEMICAL_SOAKER.get()),
            EmiStack.of(MCIBlocks.CHEMICAL_SOAKER.get())
    ) {
        @Override
        public Component getName() {
            return Component.translatable("gui.mekanism_complex_industries.chemical_soaker.category");
        }
    };
}
