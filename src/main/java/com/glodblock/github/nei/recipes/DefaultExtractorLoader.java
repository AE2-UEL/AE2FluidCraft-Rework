package com.glodblock.github.nei.recipes;

import com.glodblock.github.nei.recipes.extractor.GregTech5RecipeExtractor;
import com.glodblock.github.nei.recipes.extractor.VanillaRecipeExtractor;
import com.glodblock.github.util.ModAndClassUtil;
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
    }

}
