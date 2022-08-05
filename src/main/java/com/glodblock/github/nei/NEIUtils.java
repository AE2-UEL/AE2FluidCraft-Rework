package com.glodblock.github.nei;

import com.glodblock.github.nei.object.OrderStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class NEIUtils {

    public static List<OrderStack<?>> compress(List<OrderStack<?>> list) {
        List<OrderStack<?>> comp = new LinkedList<>();
        for (OrderStack<?> orderStack : list) {
            if (orderStack == null) continue;
            if (orderStack.getStack() instanceof FluidStack) {
                comp.add(orderStack);
                continue;
            }
            ItemStack currentStack = (ItemStack) orderStack.getStack();
            if (currentStack.stackSize == 0) continue;
            boolean find = false;
            for (OrderStack<?> storedStack : comp) {
                if (storedStack == null || !(orderStack.getStack() instanceof ItemStack)) continue;
                ItemStack firstStack = (ItemStack) storedStack.getStack();
                boolean areItemStackEqual = firstStack.isItemEqual(currentStack) && ItemStack.areItemStackTagsEqual(firstStack, currentStack);
                if (areItemStackEqual && (firstStack.stackSize + currentStack.stackSize) <= firstStack.getMaxStackSize()) {
                    find = true;
                    ((ItemStack) storedStack.getStack()).stackSize = firstStack.stackSize + currentStack.stackSize;
                }
            }
            if (!find) {
                comp.add(orderStack);
            }
        }
        return comp.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static List<OrderStack<?>> clearNull(List<OrderStack<?>> list) {
        List<OrderStack<?>> cleared = new LinkedList<>();
        HashMap<Integer, Object> map = new HashMap<>();
        int upper = 0;
        for (OrderStack<?> orderStack : list) {
            if (orderStack != null && orderStack.getStack() != null) {
                if (orderStack.getStack() instanceof ItemStack && ((ItemStack) orderStack.getStack()).stackSize == 0)
                    continue;
                map.put(orderStack.getIndex(), orderStack.getStack());
                upper = Math.max(upper, orderStack.getIndex());
            }
        }
        int id = 0;
        for (int i = 0; i <= upper; i ++) {
            if (map.containsKey(i)) {
                cleared.add(new OrderStack<>(map.get(i), id));
                id ++;
            }
        }
        return cleared;
    }

}
