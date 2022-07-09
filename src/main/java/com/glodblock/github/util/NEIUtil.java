package com.glodblock.github.util;

import codechicken.nei.PositionedStack;
import net.minecraft.item.ItemStack;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class NEIUtil {

    public static List<PositionedStack> compress(List<PositionedStack> list) {
        List<PositionedStack> comp = new LinkedList<>();
        for (PositionedStack positionedStack : list) {
            if (positionedStack == null) continue;
            ItemStack currentStack = positionedStack.items[0].copy();
            if (currentStack.stackSize == 0) continue;
            boolean find = false;
            for (PositionedStack storedStack : comp) {
                if (storedStack == null) continue;
                ItemStack firstStack = storedStack.items[0].copy();
                boolean areItemStackEqual = firstStack.isItemEqual(currentStack) && ItemStack.areItemStackTagsEqual(firstStack, currentStack);
                if (areItemStackEqual && (firstStack.stackSize + currentStack.stackSize) <= firstStack.getMaxStackSize()) {
                    find = true;
                    storedStack.items[0].stackSize = firstStack.stackSize + currentStack.stackSize;
                }
            }
            if (!find) {
                comp.add(positionedStack.copy());
            }
        }
        return comp.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

}
