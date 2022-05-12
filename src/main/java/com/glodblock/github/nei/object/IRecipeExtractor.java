package com.glodblock.github.nei.object;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;

import java.util.List;

public interface IRecipeExtractor {

    List<OrderStack<?>> getInputIngredients(List<PositionedStack> rawInputs);

    List<OrderStack<?>> getOutputIngredients(List<PositionedStack> rawOutputs);

    default List<OrderStack<?>> getInputIngredients(List<PositionedStack> rawInputs, IRecipeHandler recipe, int index) {
        return getInputIngredients(rawInputs);
    }

    default List<OrderStack<?>> getOutputIngredients(List<PositionedStack> rawOutputs, IRecipeHandler recipe, int index) {
        return getOutputIngredients(rawOutputs);
    }

}
