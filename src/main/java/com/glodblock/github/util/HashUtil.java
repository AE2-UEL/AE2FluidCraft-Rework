package com.glodblock.github.util;

import it.unimi.dsi.fastutil.Hash;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.Objects;

public class HashUtil {

    public static final Hash.Strategy<Fluid> FLUID = new Hash.Strategy<Fluid>() {
        @Override
        public int hashCode(Fluid o) {
            return Objects.requireNonNull(o.getRegistryName()).hashCode();
        }

        @Override
        public boolean equals(Fluid a, Fluid b) {
            return a == b || (a != null && b != null && Objects.equals(a.getRegistryName(), b.getRegistryName()));
        }
    };

    public static final Hash.Strategy<Class<?>> CLASS = new Hash.Strategy<Class<?>>() {
        @Override
        public int hashCode(Class<?> o) {
            return o.getName().hashCode();
        }

        @Override
        public boolean equals(Class<?> a, Class<?> b) {
            return a == b || (a != null && b != null && Objects.equals(a.getName(), b.getName()));
        }
    };

    public static ItemHandlerHash hashItemHandler(IItemHandler handler) {
        return new ItemHandlerHash(handler, false);
    }

    public static ItemHandlerHash hashItemHandler(IItemHandler handler, boolean amount) {
        return new ItemHandlerHash(handler, amount);
    }

    public static final class ItemHandlerHash {

        ItemStack[] record;
        boolean amountCheck;

        public ItemHandlerHash(@Nonnull IItemHandler handler, boolean checkAmount) {
            this.record = new ItemStack[handler.getSlots()];
            for (int i = 0; i < handler.getSlots(); i ++) {
                this.record[i] = handler.getStackInSlot(i).copy();
            }
            this.amountCheck = checkAmount;
        }

        @Override
        public int hashCode() {
            int code = 1;
            for (int i = 0; i < record.length; i ++) {
                ItemStack stack = record[i];
                int midCode = 1;
                if (!stack.isEmpty()) {
                    midCode = stack.getItem().hashCode();
                    midCode = midCode * 31 + stack.getDamage();
                    if (stack.hasTag()) {
                        assert stack.getTag() != null;
                        midCode = midCode * 31 + stack.getTag().hashCode();
                    }
                    if (amountCheck) {
                        midCode = midCode * 31 + stack.getCount();
                    }
                }
                code = 31 * code + midCode;
                code = 31 * code + i;
            }
            return code;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ItemHandlerHash) {
                ItemStack[] oHandler = ((ItemHandlerHash) o).record;
                boolean oCheck = ((ItemHandlerHash) o).amountCheck;
                if (oCheck != amountCheck || oHandler.length != record.length) {
                    return false;
                }
                for(int i = 0; i < record.length; i ++) {
                    ItemStack stack = record[i];
                    ItemStack oStack = oHandler[i];
                    if (!ItemStack.areItemsEqual(stack, oStack)) {
                        return false;
                    }
                    if (!ItemStack.areItemStackTagsEqual(stack, oStack)) {
                        return false;
                    }
                    if (amountCheck) {
                        if (stack.getCount() != oStack.getCount()) {
                            return false;
                        }
                    }
                }
                return true;
            }
            return false;
        }

    }

}
