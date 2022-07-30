package com.glodblock.github.nei.recipes;

import com.glodblock.github.nei.recipes.extractor.*;
import com.glodblock.github.util.ModAndClassUtil;
import forestry.factory.recipes.nei.*;
import gregapi.recipes.Recipe;
import gregtech.api.util.GTPP_Recipe;
import gregtech.api.util.GT_Recipe;

public class DefaultExtractorLoader implements Runnable {

    @Override
    public void run() {
        FluidRecipe.addRecipeMap("smelting", new VanillaRecipeExtractor(false));
        FluidRecipe.addRecipeMap("brewing", new VanillaRecipeExtractor(false));
        FluidRecipe.addRecipeMap("crafting", new VanillaRecipeExtractor(true));
        FluidRecipe.addRecipeMap("crafting2x2", new VanillaRecipeExtractor(true));

        if (ModAndClassUtil.GT5) {
            for (GT_Recipe.GT_Recipe_Map tMap : GT_Recipe.GT_Recipe_Map.sMappings) {
                FluidRecipe.addRecipeMap(tMap.mNEIName, new GregTech5RecipeExtractor(tMap.mNEIName.equals("gt.recipe.scanner") || tMap.mNEIName.equals("gt.recipe.fakeAssemblylineProcess")));
            }
        }

        if (ModAndClassUtil.GT6) {
            for (Recipe.RecipeMap tMap : Recipe.RecipeMap.RECIPE_MAPS.values()) {
                FluidRecipe.addRecipeMap(tMap.mNameNEI, new GregTech6RecipeExtractor(tMap));
            }
        }

        if (ModAndClassUtil.GTPP) {
            for (GTPP_Recipe.GTPP_Recipe_Map_Internal gtppMap : GTPP_Recipe.GTPP_Recipe_Map_Internal.sMappingsEx) {
                if (gtppMap.mNEIAllowed) {
                    FluidRecipe.addRecipeMap(gtppMap.mNEIName, new GTPPRecipeExtractor());
                }
            }
        }

        if (ModAndClassUtil.EIO) {
            FluidRecipe.addRecipeMap("EIOEnchanter", new EnderIORecipeExtractor());
            FluidRecipe.addRecipeMap("EnderIOAlloySmelter", new EnderIORecipeExtractor());
            FluidRecipe.addRecipeMap("EnderIOSagMill", new EnderIORecipeExtractor());
            FluidRecipe.addRecipeMap("EnderIOSliceAndSplice", new EnderIORecipeExtractor());
            FluidRecipe.addRecipeMap("EnderIOSoulBinder", new EnderIORecipeExtractor());
            FluidRecipe.addRecipeMap("EnderIOVat", new EnderIORecipeExtractor());
        }

        if (ModAndClassUtil.FTR) {
            // GTNH
            FluidRecipe.addRecipeMap("forestry.bottler", new ForestryRecipeExtractor(new NEIHandlerBottler()));
            FluidRecipe.addRecipeMap("forestry.carpenter", new ForestryRecipeExtractor(new NEIHandlerCarpenter()));
            FluidRecipe.addRecipeMap("forestry.centrifuge", new ForestryRecipeExtractor(new NEIHandlerCentrifuge()));
            FluidRecipe.addRecipeMap("forestry.fabricator", new ForestryRecipeExtractor(new NEIHandlerFabricator()));
            FluidRecipe.addRecipeMap("forestry.fermenter", new ForestryRecipeExtractor(new NEIHandlerFermenter()));
            FluidRecipe.addRecipeMap("forestry.moistener", new ForestryRecipeExtractor(new NEIHandlerMoistener()));
            FluidRecipe.addRecipeMap("forestry.squeezer", new ForestryRecipeExtractor(new NEIHandlerSqueezer()));
            FluidRecipe.addRecipeMap("forestry.still", new ForestryRecipeExtractor(new NEIHandlerStill()));

            // LEGACY
            FluidRecipe.addRecipeMap(null, new ForestryRecipeExtractor(new NEIHandlerBottler()));
            FluidRecipe.addRecipeMap(null, new ForestryRecipeExtractor(new NEIHandlerCarpenter()));
            FluidRecipe.addRecipeMap(null, new ForestryRecipeExtractor(new NEIHandlerCentrifuge()));
            FluidRecipe.addRecipeMap(null, new ForestryRecipeExtractor(new NEIHandlerFabricator()));
            FluidRecipe.addRecipeMap(null, new ForestryRecipeExtractor(new NEIHandlerFermenter()));
            FluidRecipe.addRecipeMap(null, new ForestryRecipeExtractor(new NEIHandlerMoistener()));
            FluidRecipe.addRecipeMap(null, new ForestryRecipeExtractor(new NEIHandlerSqueezer()));
            FluidRecipe.addRecipeMap(null, new ForestryRecipeExtractor(new NEIHandlerStill()));
        }

        if (ModAndClassUtil.IC2) {
            FluidRecipe.addRecipeMap("blastfurnace", new IndustrialCraftRecipeExtractor());
            FluidRecipe.addRecipeMap("BlockCutter", new IndustrialCraftRecipeExtractor());
            FluidRecipe.addRecipeMap("centrifuge", new IndustrialCraftRecipeExtractor());
            FluidRecipe.addRecipeMap("compressor", new IndustrialCraftRecipeExtractor());
            FluidRecipe.addRecipeMap("extractor", new IndustrialCraftRecipeExtractor());
            FluidRecipe.addRecipeMap("fluidcanner", new IndustrialCraftRecipeExtractor());
            FluidRecipe.addRecipeMap("macerator", new IndustrialCraftRecipeExtractor());
            FluidRecipe.addRecipeMap("metalformer", new IndustrialCraftRecipeExtractor());
            FluidRecipe.addRecipeMap("oreWashing", new IndustrialCraftRecipeExtractor());
            FluidRecipe.addRecipeMap("solidcanner", new IndustrialCraftRecipeExtractor());
        }

    }

}
