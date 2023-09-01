package com.glodblock.github.integration.gregtech;

import gregtech.integration.jei.recipe.GTRecipeWrapper;
import mezz.jei.api.recipe.IRecipeWrapper;

public class GregUtil {

    public static boolean isNotConsume(IRecipeWrapper wrapper, int index) {
        if (wrapper instanceof GTRecipeWrapper) {
            GTRecipeWrapper gtRecipe = (GTRecipeWrapper) wrapper;
            return gtRecipe.isNotConsumedItem(index);
        }
        return false;
    }

}
