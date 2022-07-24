package com.glodblock.github.nei.recipes.extractor;

import codechicken.nei.PositionedStack;
import com.glodblock.github.nei.object.IRecipeExtractor;
import com.glodblock.github.nei.object.OrderStack;
import gregapi.item.ItemFluidDisplay;
import gregapi.recipes.Recipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.LinkedList;
import java.util.List;

public class GregTech6RecipeExtractor implements IRecipeExtractor {

    private final Recipe.RecipeMap Recipes;

    public GregTech6RecipeExtractor(Recipe.RecipeMap aMap) {
        Recipes = aMap;
    }

    @Override
    public List<OrderStack<?>> getInputIngredients(List<PositionedStack> rawInputs) {
        this.removeMachine(rawInputs);
        List<OrderStack<?>> tmp = new LinkedList<>();
        for (int i = 0; i < rawInputs.size(); i ++) {
            if (rawInputs.get(i) == null) continue;
            ItemStack item = rawInputs.get(i).items[0].copy();
            OrderStack<?> stack;
            if (item.getItem() instanceof ItemFluidDisplay) {
                FluidStack fluid = ((ItemFluidDisplay) item.getItem()).getFluid(item);
                if (fluid == null || fluid.amount <= 0) continue;
                stack = new OrderStack<>(fluid, i);
                tmp.add(stack);
            } else {
                stack = OrderStack.pack(rawInputs.get(i), i);
                if (stack != null) tmp.add(stack);
            }
        }
        return tmp;
    }

    @Override
    public List<OrderStack<?>> getOutputIngredients(List<PositionedStack> rawOutputs) {
        List<OrderStack<?>> tmp = new LinkedList<>();
        for (int i = 0; i < rawOutputs.size(); i ++) {
            if (rawOutputs.get(i) == null) continue;
            ItemStack item = rawOutputs.get(i).items[0].copy();
            OrderStack<?> stack;
            if (item.getItem() instanceof ItemFluidDisplay) {
                FluidStack fluid = ((ItemFluidDisplay) item.getItem()).getFluid(item);
                if (fluid == null || fluid.amount <= 0) continue;
                stack = new OrderStack<>(fluid, i);
                tmp.add(stack);
            } else {
                stack = OrderStack.pack(rawOutputs.get(i), i);
                if (stack != null) tmp.add(stack);
            }
        }
        return tmp;
    }

    private void removeMachine(List<PositionedStack> list) {
        for (int i = list.size() - 1; i >= 0; i --) {
            PositionedStack positionedStack = list.get(i);
            if (positionedStack != null) {
                for (ItemStack machine : this.Recipes.mRecipeMachineList) {
                    if (positionedStack.items[0].isItemEqual(machine)) {
                        list.remove(i);
                        break;
                    }
                }
            }
        }
    }

}
