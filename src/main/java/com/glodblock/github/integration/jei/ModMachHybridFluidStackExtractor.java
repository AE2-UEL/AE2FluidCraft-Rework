package com.glodblock.github.integration.jei;

import com.glodblock.github.integration.jei.interfaces.IngredientExtractor;
import hellfirepvp.modularmachinery.common.integration.ingredient.HybridFluid;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IIngredientType;
import net.minecraftforge.fluids.FluidStack;

import java.util.Objects;
import java.util.stream.Stream;

public class ModMachHybridFluidStackExtractor implements IngredientExtractor<FluidStack> {

    private final IIngredientType<HybridFluid> ingTypeHybridFluid;

    ModMachHybridFluidStackExtractor(IModRegistry registry) {
        ingTypeHybridFluid = Objects.requireNonNull(registry.getIngredientRegistry().getIngredientType(HybridFluid.class));
    }

    public Stream<WrappedIngredient<FluidStack>> extract(IRecipeLayout recipeLayout) {
        return recipeLayout.getIngredientsGroup(ingTypeHybridFluid).getGuiIngredients().values().stream()
                .map(ing -> {
                    HybridFluid hf = ing.getDisplayedIngredient();
                    return new WrappedIngredient<>(hf != null ? hf.asFluidStack() : null, ing.isInput());
                });
    }

}
