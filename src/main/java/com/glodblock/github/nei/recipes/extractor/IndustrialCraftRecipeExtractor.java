package com.glodblock.github.nei.recipes.extractor;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;
import com.glodblock.github.nei.object.IRecipeExtractor;
import com.glodblock.github.nei.object.OrderStack;
import com.glodblock.github.util.Ae2Reflect;
import ic2.neiIntegration.core.recipehandler.FluidCannerRecipeHandler;
import ic2.neiIntegration.core.recipehandler.OreWashingRecipeHandler;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class IndustrialCraftRecipeExtractor implements IRecipeExtractor {

    @Override
    public List<OrderStack<?>> getInputIngredients(List<PositionedStack> rawInputs) {
        List<OrderStack<?>> tmp = new LinkedList<>();
        for (int i = 0; i < rawInputs.size(); i ++) {
            if (rawInputs.get(i) == null) continue;
            tmp.add(OrderStack.pack(rawInputs.get(i), i));
        }
        return tmp;
    }

    @Override
    public List<OrderStack<?>> getOutputIngredients(List<PositionedStack> rawOutputs) {
        List<OrderStack<?>> tmp = new LinkedList<>();
        for (int i = 0; i < rawOutputs.size(); i ++) {
            if (rawOutputs.get(i) == null) continue;
            tmp.add(OrderStack.pack(rawOutputs.get(i), i));
        }
        return tmp;
    }

    @Override
    public List<OrderStack<?>> getInputIngredients(List<PositionedStack> rawInputs, IRecipeHandler recipe, int index) {
        List<OrderStack<?>> stacks = new LinkedList<>();

        if (recipe instanceof FluidCannerRecipeHandler) {
            FluidCannerRecipeHandler.CachedFluidCannerRecipe cachedRecipe = (FluidCannerRecipeHandler.CachedFluidCannerRecipe) ((FluidCannerRecipeHandler) recipe).arecipes.get(index);
            FluidStack input = ReflectIC2.getInputFluid(cachedRecipe);
            PositionedStack add = rawInputs.size() > 1 ? rawInputs.get(1) : null;
            stacks.add(OrderStack.pack(add, 0));
            stacks.add(new OrderStack<>(input, 1));
        }
        else if (recipe instanceof OreWashingRecipeHandler) {
            stacks = getInputIngredients(rawInputs);
            int water = ReflectIC2.getOreWasherWater((OreWashingRecipeHandler) recipe, index);
            stacks.add(new OrderStack<>(new FluidStack(FluidRegistry.WATER, water), stacks.size()));
        } else {
            stacks = getInputIngredients(rawInputs);
        }

        return stacks.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public List<OrderStack<?>> getOutputIngredients(List<PositionedStack> rawOutputs, IRecipeHandler recipe, int index) {
        List<OrderStack<?>> stacks = new LinkedList<>();

        if (recipe instanceof FluidCannerRecipeHandler) {
            FluidCannerRecipeHandler.CachedFluidCannerRecipe cachedRecipe = (FluidCannerRecipeHandler.CachedFluidCannerRecipe) ((FluidCannerRecipeHandler) recipe).arecipes.get(index);
            FluidStack output = ReflectIC2.getOutputFluid(cachedRecipe);
            stacks.add(new OrderStack<>(output, 0));
        } else {
            stacks = getOutputIngredients(rawOutputs);
        }

        return stacks.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private static class ReflectIC2 {

        private final static Field inputsF;
        private final static Field resultF;

        private final static Method oreWasherWater;

        static {
            try {
                inputsF = Ae2Reflect.reflectField(FluidCannerRecipeHandler.CachedFluidCannerRecipe.class, "fluidInput");
                resultF = Ae2Reflect.reflectField(FluidCannerRecipeHandler.CachedFluidCannerRecipe.class, "fluidOutput");
                oreWasherWater = Ae2Reflect.reflectMethod(OreWashingRecipeHandler.class, "getreqWater", int.class);
            } catch (NoSuchFieldException | NoSuchMethodException e) {
                throw new IllegalStateException("Failed to initialize IC2 reflection hacks!", e);
            }
        }

        private static FluidStack getInputFluid(FluidCannerRecipeHandler.CachedFluidCannerRecipe recipe) {
            return Ae2Reflect.readField(recipe, inputsF);
        }

        private static FluidStack getOutputFluid(FluidCannerRecipeHandler.CachedFluidCannerRecipe recipe) {
            return Ae2Reflect.readField(recipe, resultF);
        }

        private static int getOreWasherWater(OreWashingRecipeHandler recipeHandler, int index) {
            try {
                return (int) oreWasherWater.invoke(recipeHandler, index);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to invoke method: " + oreWasherWater, e);
            }
        }

    }

}
