package com.complexindustries.mekanism.client.jei;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.client.gui.GuiAirCompressor;
import com.complexindustries.mekanism.content.freezer.GuiFreezerController;
import com.complexindustries.mekanism.content.refinery.GuiRefineryController;
import com.complexindustries.mekanism.registration.MCIBlocks;
import com.complexindustries.mekanism.registration.MCIChemicals;
import com.complexindustries.mekanism.registration.MCIFluids;
import com.complexindustries.mekanism.registration.MCIItems;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class MCIJEIPlugin implements IModPlugin {

    public static final ResourceLocation PLUGIN_UID = ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "jei_plugin");

    public static final RecipeType<FreezerJEIRecipe> FREEZER_JEI_TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "freezer"), FreezerJEIRecipe.class);

    public static final RecipeType<AirCompressorJEIRecipe> AIR_COMPRESSOR_JEI_TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "air_compressor"), AirCompressorJEIRecipe.class);

    public static final RecipeType<RefineryJEIRecipe> REFINERY_JEI_TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "refinery"), RefineryJEIRecipe.class);

    public static final RecipeType<ChemicalSolidifierJEIRecipe> CHEMICAL_SOLIDIFIER_JEI_TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "chemical_solidifying"), ChemicalSolidifierJEIRecipe.class);

    public static final RecipeType<ChemicalSoakingJEIRecipe> CHEMICAL_SOAKING_JEI_TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "chemical_soaking"), ChemicalSoakingJEIRecipe.class);

    public static final RecipeType<CrystalGrowthJEIRecipe> CRYSTAL_GROWTH_JEI_TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "crystal_growth"), CrystalGrowthJEIRecipe.class);

    public static final RecipeType<SiliconSlicingJEIRecipe> SILICON_SLICING_JEI_TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "silicon_slicing"), SiliconSlicingJEIRecipe.class);

    public static final RecipeType<PhotolithographyJEIRecipe> PHOTOLITHOGRAPHY_JEI_TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "photolithography"), PhotolithographyJEIRecipe.class);

    @NotNull
    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        if (!MekanismJEI.shouldLoad()) {
            return;
        }
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(new FreezerRecipeCategory(guiHelper, FREEZER_JEI_TYPE));
        registry.addRecipeCategories(new AirCompressorRecipeCategory(guiHelper, AIR_COMPRESSOR_JEI_TYPE));
        registry.addRecipeCategories(new RefineryRecipeCategory(guiHelper, REFINERY_JEI_TYPE));
        registry.addRecipeCategories(new ChemicalSolidifierRecipeCategory(guiHelper, CHEMICAL_SOLIDIFIER_JEI_TYPE));
        registry.addRecipeCategories(new ChemicalSoakingRecipeCategory(guiHelper, CHEMICAL_SOAKING_JEI_TYPE));
        registry.addRecipeCategories(new CrystalGrowthRecipeCategory(guiHelper, CRYSTAL_GROWTH_JEI_TYPE));
        registry.addRecipeCategories(new SiliconSlicingRecipeCategory(guiHelper, SILICON_SLICING_JEI_TYPE));
        registry.addRecipeCategories(new PhotolithographyRecipeCategory(guiHelper, PHOTOLITHOGRAPHY_JEI_TYPE));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry) {
        if (!MekanismJEI.shouldLoad()) {
            return;
        }
        // 1. Freezer Recipes
        List<FreezerJEIRecipe> freezerRecipes = new ArrayList<>();

        // Process 1: 压缩空气 -> 氮气 (产物砍半: 1000 mB 空气 -> 500 mB 氮气)
        freezerRecipes.add(new FreezerJEIRecipe(
                null,
                MCIChemicals.COMPRESSED_AIR.asStack(1000),
                null,
                MCIChemicals.NITROGEN.asStack(500),
                Component.translatable("gui.mekanism_complex_industries.jei.process.nitrogen"),
                Component.translatable("gui.mekanism_complex_industries.jei.temp_requirement")
        ));

        // Process 2: 低温冷煤 + 压缩空气 -> 稀有气体(1/100 产出) + 水(1:1 融化回收)
        freezerRecipes.add(new FreezerJEIRecipe(
                new FluidStack(MCIFluids.SOURCE_CRYOGENIC_REFRIGERANT.get(), 1000),
                MCIChemicals.COMPRESSED_AIR.asStack(1000),
                new FluidStack(Fluids.WATER, 1000),
                MCIChemicals.NOBLE_GAS.asStack(10),
                Component.translatable("gui.mekanism_complex_industries.jei.process.noble_gas"),
                Component.translatable("gui.mekanism_complex_industries.jei.temp_requirement")
        ));

        // Process 3: 水 -> 低温冷煤 (产量效率减少至 1/10)
        freezerRecipes.add(new FreezerJEIRecipe(
                new FluidStack(Fluids.WATER, 1000),
                null,
                new FluidStack(MCIFluids.SOURCE_CRYOGENIC_REFRIGERANT.get(), 1000),
                null,
                Component.translatable("gui.mekanism_complex_industries.jei.process.refrigerant"),
                Component.translatable("gui.mekanism_complex_industries.jei.temp_requirement")
        ));

        registry.addRecipes(FREEZER_JEI_TYPE, freezerRecipes);

        // 2. Air Compressor Recipes
        List<AirCompressorJEIRecipe> airCompressorRecipes = new ArrayList<>();
        airCompressorRecipes.add(new AirCompressorJEIRecipe(
                MCIChemicals.COMPRESSED_AIR.asStack(1000),
                Component.translatable("gui.mekanism_complex_industries.jei.air_compressor.usage"),
                Component.translatable("gui.mekanism_complex_industries.jei.air_compressor.rate"),
                Component.translatable("gui.mekanism_complex_industries.jei.air_compressor.source")
        ));
        registry.addRecipes(AIR_COMPRESSOR_JEI_TYPE, airCompressorRecipes);

        // 3. Refinery Cracking Recipes
        List<RefineryJEIRecipe> refineryRecipes = new ArrayList<>();
        refineryRecipes.add(new RefineryJEIRecipe(
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
        ));
        registry.addRecipes(REFINERY_JEI_TYPE, refineryRecipes);

        // 4. Chemical Solidifier Recipes
        List<ChemicalSolidifierJEIRecipe> solidifierRecipes = new ArrayList<>();
        solidifierRecipes.add(new ChemicalSolidifierJEIRecipe(
                MCIChemicals.BITUMEN.asStack(200),
                new ItemStack(MCIBlocks.BITUMEN_BLOCK.get()),
                Component.translatable("gui.mekanism_complex_industries.jei.chemical_solidifier.usage_val"),
                Component.translatable("gui.mekanism_complex_industries.jei.chemical_solidifier.duration_val")
        ));
        registry.addRecipes(CHEMICAL_SOLIDIFIER_JEI_TYPE, solidifierRecipes);

        // 5. Chemical Soaking Recipes
        List<ChemicalSoakingJEIRecipe> soakingRecipes = new ArrayList<>();
        soakingRecipes.add(new ChemicalSoakingJEIRecipe(
                List.of(new ItemStack(mekanism.common.registries.MekanismItems.SAWDUST.asItem())),
                List.of(MCIChemicals.HEAVY_OIL.asStack(200)),
                new ItemStack(MCIItems.HEAVY_OIL_FUEL.get())
        ));
        soakingRecipes.add(new ChemicalSoakingJEIRecipe(
                List.of(new ItemStack(MCIItems.POLYPROPYLENE_PELLET.get())),
                List.of(MCIChemicals.STYRENE.asStack(50)),
                new ItemStack(MCIItems.ENGINEERING_PLASTIC.get())
        ));
        registry.addRecipes(CHEMICAL_SOAKING_JEI_TYPE, soakingRecipes);

        // 6. Crystal Growth Recipes
        List<CrystalGrowthJEIRecipe> crystalGrowthRecipes = new ArrayList<>();
        crystalGrowthRecipes.add(new CrystalGrowthJEIRecipe(
                List.of(new ItemStack(MCIItems.CRUDE_SILICON.get())),
                List.of(MCIChemicals.NOBLE_GAS.asStack(100)),
                List.of(mekanism.common.registries.MekanismChemicals.HYDROGEN.asStack(100)),
                new ItemStack(MCIItems.REFINED_SILICON.get())
        ));
        registry.addRecipes(CRYSTAL_GROWTH_JEI_TYPE, crystalGrowthRecipes);

        // 7. Silicon Slicing Recipes
        List<SiliconSlicingJEIRecipe> siliconSlicingRecipes = new ArrayList<>();
        siliconSlicingRecipes.add(new SiliconSlicingJEIRecipe(
                List.of(new ItemStack(MCIItems.REFINED_SILICON.get())),
                List.of(MCIChemicals.NITROGEN.asStack(100)),
                new ItemStack(MCIItems.BLANK_SILICON_WAFER.get(), 8)
        ));
        registry.addRecipes(SILICON_SLICING_JEI_TYPE, siliconSlicingRecipes);

        // 8. Photolithography Recipes
        List<PhotolithographyJEIRecipe> lithoRecipes = new ArrayList<>();
        lithoRecipes.add(new PhotolithographyJEIRecipe(
                List.of(new ItemStack(MCIItems.BLANK_SILICON_WAFER.get())),
                List.of(new ItemStack(MCIItems.CALCULATION_MASK.get())),
                List.of(MCIChemicals.NITROGEN.asStack(50)),
                new ItemStack(MCIItems.SEMIFINISHED_CALCULATION_CHIP.get())
        ));
        lithoRecipes.add(new PhotolithographyJEIRecipe(
                List.of(new ItemStack(MCIItems.BLANK_SILICON_WAFER.get())),
                List.of(new ItemStack(MCIItems.LOGIC_MASK.get())),
                List.of(MCIChemicals.NITROGEN.asStack(50)),
                new ItemStack(MCIItems.SEMIFINISHED_LOGIC_CHIP.get())
        ));
        registry.addRecipes(PHOTOLITHOGRAPHY_JEI_TYPE, lithoRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
        if (!MekanismJEI.shouldLoad()) {
            return;
        }
        registry.addRecipeCatalyst(new ItemStack(MCIBlocks.FREEZER_CONTROLLER.get()), FREEZER_JEI_TYPE);
        registry.addRecipeCatalyst(new ItemStack(MCIBlocks.FREEZER_CASING.get()), FREEZER_JEI_TYPE);
        registry.addRecipeCatalyst(new ItemStack(MCIBlocks.FREEZER_VALVE.get()), FREEZER_JEI_TYPE);

        registry.addRecipeCatalyst(new ItemStack(MCIBlocks.AIR_COMPRESSOR.get()), AIR_COMPRESSOR_JEI_TYPE);
        registry.addRecipeCatalyst(new ItemStack(MCIBlocks.CHEMICAL_SOLIDIFIER.get()), CHEMICAL_SOLIDIFIER_JEI_TYPE);
        registry.addRecipeCatalyst(new ItemStack(MCIBlocks.BASIC_CHEMICAL_SOLIDIFIER_FACTORY.get()), CHEMICAL_SOLIDIFIER_JEI_TYPE);
        registry.addRecipeCatalyst(new ItemStack(MCIBlocks.ADVANCED_CHEMICAL_SOLIDIFIER_FACTORY.get()), CHEMICAL_SOLIDIFIER_JEI_TYPE);
        registry.addRecipeCatalyst(new ItemStack(MCIBlocks.ELITE_CHEMICAL_SOLIDIFIER_FACTORY.get()), CHEMICAL_SOLIDIFIER_JEI_TYPE);
        registry.addRecipeCatalyst(new ItemStack(MCIBlocks.ULTIMATE_CHEMICAL_SOLIDIFIER_FACTORY.get()), CHEMICAL_SOLIDIFIER_JEI_TYPE);

        registry.addRecipeCatalyst(new ItemStack(MCIBlocks.CHEMICAL_SOAKER.get()), CHEMICAL_SOAKING_JEI_TYPE);
        registry.addRecipeCatalyst(new ItemStack(MCIBlocks.BASIC_CHEMICAL_SOAKING_FACTORY.get()), CHEMICAL_SOAKING_JEI_TYPE);
        registry.addRecipeCatalyst(new ItemStack(MCIBlocks.ADVANCED_CHEMICAL_SOAKING_FACTORY.get()), CHEMICAL_SOAKING_JEI_TYPE);
        registry.addRecipeCatalyst(new ItemStack(MCIBlocks.ELITE_CHEMICAL_SOAKING_FACTORY.get()), CHEMICAL_SOAKING_JEI_TYPE);
        registry.addRecipeCatalyst(new ItemStack(MCIBlocks.ULTIMATE_CHEMICAL_SOAKING_FACTORY.get()), CHEMICAL_SOAKING_JEI_TYPE);

        registry.addRecipeCatalyst(new ItemStack(MCIBlocks.CRYSTAL_GROWTH_CHAMBER.get()), CRYSTAL_GROWTH_JEI_TYPE);
        registry.addRecipeCatalyst(new ItemStack(MCIBlocks.SILICON_SLICER.get()), SILICON_SLICING_JEI_TYPE);
        registry.addRecipeCatalyst(new ItemStack(MCIBlocks.PHOTOLITHOGRAPHY_MACHINE.get()), PHOTOLITHOGRAPHY_JEI_TYPE);

        registry.addRecipeCatalyst(new ItemStack(MCIBlocks.REFINERY_CONTROLLER.get()), REFINERY_JEI_TYPE);
        registry.addRecipeCatalyst(new ItemStack(MCIBlocks.REFINERY_CASING.get()), REFINERY_JEI_TYPE);
        registry.addRecipeCatalyst(new ItemStack(MCIBlocks.REFINERY_VALVE.get()), REFINERY_JEI_TYPE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registry) {
        if (!MekanismJEI.shouldLoad()) {
            return;
        }
        registry.addRecipeClickArea(GuiFreezerController.class, 42, 18, 92, 44, FREEZER_JEI_TYPE);
        registry.addRecipeClickArea(GuiAirCompressor.class, 48, 23, 80, 42, AIR_COMPRESSOR_JEI_TYPE);
        registry.addRecipeClickArea(GuiRefineryController.class, 31, 13, 96, 56, REFINERY_JEI_TYPE);
        registry.addRecipeClickArea(com.complexindustries.mekanism.client.gui.GuiChemicalSolidifier.class, 68, 40, 32, 10, CHEMICAL_SOLIDIFIER_JEI_TYPE);
        registry.addRecipeClickArea(com.complexindustries.mekanism.client.gui.GuiChemicalSoaker.class, 86, 38, 20, 10, CHEMICAL_SOAKING_JEI_TYPE);
        registry.addRecipeClickArea(com.complexindustries.mekanism.client.gui.GuiCrystalGrowthChamber.class, 75, 39, 20, 10, CRYSTAL_GROWTH_JEI_TYPE);
        registry.addRecipeClickArea(com.complexindustries.mekanism.client.gui.GuiSiliconSlicer.class, 79, 43, 24, 16, SILICON_SLICING_JEI_TYPE);
        registry.addRecipeClickArea(com.complexindustries.mekanism.client.gui.GuiPhotolithographyMachine.class, 79, 42, 24, 16, PHOTOLITHOGRAPHY_JEI_TYPE);
    }
}
