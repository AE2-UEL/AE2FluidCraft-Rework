package com.glodblock.github.nei.recipes;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;
import com.glodblock.github.nei.object.IRecipeExtractor;
import com.glodblock.github.nei.object.OrderStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public final class FluidRecipe {

    private static final HashMap<String, IRecipeExtractor> IdentifierMap = new HashMap<>();

    public static void addRecipeMap(String recipeIdentifier, IRecipeExtractor extractor) {
        IdentifierMap.put(recipeIdentifier, extractor);
    }

    public static List<OrderStack<?>> getPackageInputs(IRecipeHandler recipe, int index) {
        if (recipe == null || !IdentifierMap.containsKey(recipe.getOverlayIdentifier())) return new ArrayList<>();
        IRecipeExtractor extractor = IdentifierMap.get(recipe.getOverlayIdentifier());
        if (extractor == null) return new ArrayList<>();
        List<PositionedStack> tmp = new ArrayList<>(recipe.getIngredientStacks(index));
        return extractor.getInputIngredients(tmp);
    }

    public static List<OrderStack<?>> getPackageOutputs(IRecipeHandler recipe, int index, boolean useOther) {
        if (recipe == null || !IdentifierMap.containsKey(recipe.getOverlayIdentifier())) return new ArrayList<>();
        IRecipeExtractor extractor = IdentifierMap.get(recipe.getOverlayIdentifier());
        if (extractor == null) return new ArrayList<>();
        List<PositionedStack> tmp = new ArrayList<>(Collections.singleton(recipe.getResultStack(index)));
        if (useOther) tmp.addAll(recipe.getOtherStacks(index));
        return extractor.getOutputIngredients(tmp);
    }

    public static List<String> getSupportRecipes() {
        return new ArrayList<>(IdentifierMap.keySet());
    }

}
