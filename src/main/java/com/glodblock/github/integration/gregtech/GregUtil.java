package com.glodblock.github.integration.gregtech;

import com.glodblock.github.util.Ae2Reflect;
import gregtech.integration.jei.recipe.GTRecipeWrapper;
import mezz.jei.api.recipe.IRecipeWrapper;

import java.lang.reflect.Method;

public class GregUtil {

    private static Method mGTRecipeWrapper_isNotConsumedItem;

    static {
        try {
            mGTRecipeWrapper_isNotConsumedItem = Ae2Reflect.reflectMethod(GTRecipeWrapper.class, "isNotConsumedItem", int.class);
        } catch (NoSuchMethodException e) {
            mGTRecipeWrapper_isNotConsumedItem = null;
        }
    }

    // because gtce and gtceu share the same id
    public static boolean isNotConsume(IRecipeWrapper wrapper, int index) {
        if (wrapper instanceof GTRecipeWrapper && mGTRecipeWrapper_isNotConsumedItem != null) {
            try {
                GTRecipeWrapper gtRecipe = (GTRecipeWrapper) wrapper;
                return (boolean) mGTRecipeWrapper_isNotConsumedItem.invoke(gtRecipe, index);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

}
