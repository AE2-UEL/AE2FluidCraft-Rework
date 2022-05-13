package com.glodblock.github.integration.jei.interfaces;

import com.glodblock.github.integration.jei.WrappedIngredient;
import mezz.jei.api.gui.IRecipeLayout;

import java.util.stream.Stream;

public interface IngredientExtractor<T> {

    Stream<WrappedIngredient<T>> extract(IRecipeLayout recipeLayout);

}
