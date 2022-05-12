package com.glodblock.github.nei.recipes.extractor;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.glodblock.github.nei.object.IRecipeExtractor;
import com.glodblock.github.nei.object.OrderStack;
import com.glodblock.github.util.Ae2Reflect;
import crazypants.enderio.machine.crusher.CrusherRecipeManager;
import crazypants.enderio.nei.VatRecipeHandler;
import net.minecraftforge.fluids.FluidStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class EnderIORecipeExtractor implements IRecipeExtractor {

    public EnderIORecipeExtractor() {
    }

    @Override
    public List<OrderStack<?>> getInputIngredients(List<PositionedStack> rawInputs) {
        List<OrderStack<?>> tmp = new LinkedList<>();
        for (int i = 0; i < rawInputs.size(); i ++) {
            if (rawInputs.get(i) == null) continue;
            OrderStack<?> stack = OrderStack.pack(rawInputs.get(i), i);
            if (stack != null) tmp.add(stack);
        }
        return tmp;
    }

    @Override
    public List<OrderStack<?>> getOutputIngredients(List<PositionedStack> rawOutputs) {
        List<OrderStack<?>> tmp = new LinkedList<>();
        for (int i = 0; i < rawOutputs.size(); i ++) {
            if (rawOutputs.get(i) == null) continue;
            OrderStack<?> stack = OrderStack.pack(rawOutputs.get(i), i);
            if (stack != null) tmp.add(stack);
        }
        return tmp;
    }

    @Override
    public List<OrderStack<?>> getInputIngredients(List<PositionedStack> rawInputs, IRecipeHandler recipe, int index) {
        TemplateRecipeHandler tRecipe = (TemplateRecipeHandler) recipe;
        List<OrderStack<?>> tmp = new LinkedList<>();
        if (tRecipe.arecipes.get(index) instanceof VatRecipeHandler.InnerVatRecipe) {
            VatRecipeHandler.InnerVatRecipe vatRecipe = (VatRecipeHandler.InnerVatRecipe) tRecipe.arecipes.get(index);
            ArrayList<PositionedStack> inputs = ReflectEIO.getInputs(vatRecipe);
            for (int i = 0; i < inputs.size(); i ++) {
                if (inputs.get(i) == null) continue;
                OrderStack<?> stack = OrderStack.pack(rawInputs.get(i), i);
                if (stack != null) tmp.add(stack);
            }
            FluidStack in = ReflectEIO.getInputFluid(vatRecipe);
            if (in != null) {
                tmp.add(new OrderStack<>(in, inputs.size()));
            }
            return tmp;
        } else if (tRecipe.getOverlayIdentifier().equals("EnderIOSagMill")) {
            for (int i = rawInputs.size() - 1; i >= 0; i --) {
                PositionedStack stack = rawInputs.get(i);
                if (stack != null && CrusherRecipeManager.getInstance().isValidSagBall(stack.items[0])) {
                    rawInputs.remove(i);
                    break;
                }
            }
            return getInputIngredients(rawInputs);
        }
        else {
            return getInputIngredients(rawInputs);
        }
    }

    @Override
    public List<OrderStack<?>> getOutputIngredients(List<PositionedStack> rawOutputs, IRecipeHandler recipe, int index) {
        TemplateRecipeHandler tRecipe = (TemplateRecipeHandler) recipe;
        List<OrderStack<?>> tmp = new LinkedList<>();
        if (tRecipe.arecipes.get(index) instanceof VatRecipeHandler.InnerVatRecipe) {
            VatRecipeHandler.InnerVatRecipe vatRecipe = (VatRecipeHandler.InnerVatRecipe) tRecipe.arecipes.get(index);
            FluidStack result = ReflectEIO.getResult(vatRecipe);
            if (result != null) {
                tmp.add(new OrderStack<>(result, 0));
            }
            return tmp;
        }
        else {
            return getOutputIngredients(rawOutputs);
        }
    }

    private static class ReflectEIO {

        private final static Field inputsF;
        private final static Field resultF;
        private final static Field inFluidF;

        static {
            try {
                inputsF = Ae2Reflect.reflectField(VatRecipeHandler.InnerVatRecipe.class, "inputs");
                resultF = Ae2Reflect.reflectField(VatRecipeHandler.InnerVatRecipe.class, "result");
                inFluidF = Ae2Reflect.reflectField(VatRecipeHandler.InnerVatRecipe.class, "inFluid");
            } catch (NoSuchFieldException e) {
                throw new IllegalStateException("Failed to initialize EIO reflection hacks!", e);
            }
        }

        private static ArrayList<PositionedStack> getInputs(VatRecipeHandler.InnerVatRecipe vat) {
            return Ae2Reflect.readField(vat, inputsF);
        }

        private static FluidStack getResult(VatRecipeHandler.InnerVatRecipe vat) {
            return Ae2Reflect.readField(vat, resultF);
        }

        private static FluidStack getInputFluid(VatRecipeHandler.InnerVatRecipe vat) {
            return Ae2Reflect.readField(vat, inFluidF);
        }

    }

}
