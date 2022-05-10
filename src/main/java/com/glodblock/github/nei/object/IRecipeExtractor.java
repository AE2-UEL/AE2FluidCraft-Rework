package com.glodblock.github.nei.object;

import codechicken.nei.PositionedStack;

import java.util.List;

public interface IRecipeExtractor {

    List<OrderStack<?>> getInputIngredients(List<PositionedStack> rawInputs);

    List<OrderStack<?>> getOutputIngredients(List<PositionedStack> rawOutputs);

}
