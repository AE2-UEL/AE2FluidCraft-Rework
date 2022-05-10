package com.glodblock.github.nei.recipes.extractor;

import codechicken.nei.PositionedStack;
import com.glodblock.github.nei.object.IRecipeExtractor;
import com.glodblock.github.nei.object.OrderStack;

import java.util.LinkedList;
import java.util.List;

public class VanillaRecipeExtractor implements IRecipeExtractor {

    private boolean c;

    public VanillaRecipeExtractor(boolean isCraft) {
        c = isCraft;
    }

    @Override
    public List<OrderStack<?>> getInputIngredients(List<PositionedStack> rawInputs) {
        List<OrderStack<?>> tmp = new LinkedList<>();
        for (int i = 0; i < rawInputs.size(); i ++) {
            if (rawInputs.get(i) == null) continue;
            final int col = (rawInputs.get(i).relx - 25) / 18;
            final int row = (rawInputs.get(i).rely - 6) / 18;
            int index = col + row * 3;
            OrderStack<?> stack = OrderStack.pack(rawInputs.get(i), c ? index : i);
            if (stack != null) tmp.add(stack);
        }
        return tmp;
    }

    @Override
    public List<OrderStack<?>> getOutputIngredients(List<PositionedStack> rawOutputs) {
        List<OrderStack<?>> tmp = new LinkedList<>();
        for (int i = 0; i < rawOutputs.size(); i ++) {
            if (rawOutputs.get(i) == null) continue;
            final int col = (rawOutputs.get(i).relx - 25) / 18;
            final int row = (rawOutputs.get(i).rely - 6) / 18;
            int index = col + row * 3;
            OrderStack<?> stack = OrderStack.pack(rawOutputs.get(i), c ? index : i);
            if (stack != null) tmp.add(stack);
        }
        return tmp;
    }

}
