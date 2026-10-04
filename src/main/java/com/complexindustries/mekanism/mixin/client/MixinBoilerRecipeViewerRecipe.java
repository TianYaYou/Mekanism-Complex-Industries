package com.complexindustries.mekanism.mixin.client;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.registration.MCIChemicals;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.client.recipe_viewer.recipe.BoilerRecipeViewerRecipe;
import mekanism.common.registries.MekanismChemicals;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = BoilerRecipeViewerRecipe.class, remap = false)
public abstract class MixinBoilerRecipeViewerRecipe {

    @Inject(method = "getBoilerRecipes", at = @At("RETURN"), cancellable = true, remap = false)
    private static void mci$addBoilerCrackingRecipe(CallbackInfoReturnable<List<BoilerRecipeViewerRecipe>> cir) {
        List<BoilerRecipeViewerRecipe> recipes = new ArrayList<>(cir.getReturnValue());
        // Steam Cracking: 2 Petroleum Gas + 1 Water at 800 K -> 1 Propylene + 1 Ethylene
        recipes.add(new BoilerRecipeViewerRecipe(
                ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "cracking/petroleum_gas"),
                IngredientCreatorAccess.chemicalStack().from(MCIChemicals.PETROLEUM_GAS.asStack(2)),
                IngredientCreatorAccess.fluid().from(FluidTags.WATER, 1),
                MCIChemicals.PROPYLENE.asStack(1),
                MekanismChemicals.ETHENE.asStack(1),
                800.0
        ));
        cir.setReturnValue(recipes);
    }
}
