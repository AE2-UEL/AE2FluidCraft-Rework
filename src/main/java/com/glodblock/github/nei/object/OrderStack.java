package com.glodblock.github.nei.object;

import codechicken.nei.PositionedStack;
import com.glodblock.github.util.Util;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;

public class OrderStack<T> {

    private final T RealStack;
    private final int index;

    public final static int ITEM = 1;
    public final static int FLUID = 2;
    public final static int CUSTOM = 3;

    public OrderStack(T stack, int order) {
        if (stack == null || order < 0)
            throw new IllegalArgumentException("Trying to create a null or negative order stack!");
        this.RealStack = stack;
        this.index = order;
    }

    public T getStack() {
        return RealStack;
    }

    public int getIndex() {
        return index;
    }

    public static OrderStack<ItemStack> pack(PositionedStack stack, int index) {
        if (stack != null && stack.items != null && stack.items.length > 0) {
            if (Items.feather.getDamage(stack.items[0]) == OreDictionary.WILDCARD_VALUE) {
                ItemStack tmp = stack.items[0].copy();
                tmp.setItemDamage(0);
                return new OrderStack<>(tmp, index);
            }
            else return new OrderStack<>(stack.items[0].copy(), index);
        }
        return null;
    }

    public static OrderStack<?> packFluid(PositionedStack stack, int index) {
        if (stack != null && stack.items != null && stack.items.length > 0) {
            FluidStack fluid = Util.getFluidFromItem(stack.items[0]);
            if (fluid != null) return new OrderStack<>(fluid.copy(), index);
            else return pack(stack, index);
        }
        return null;
    }

    protected void customNBTWriter(NBTTagCompound buf) {
    }

    protected T customNBTReader(NBTTagCompound buf) {
        return null;
    }

    public final void writeToNBT(NBTTagCompound buf) {
        if (RealStack instanceof ItemStack) {
            NBTTagCompound tmp = new NBTTagCompound();
            tmp.setByte("t", (byte) ITEM);
            ((ItemStack) RealStack).writeToNBT(tmp);
            buf.setTag(index + ":", tmp);
        }
        else if (RealStack instanceof FluidStack) {
            NBTTagCompound tmp = new NBTTagCompound();
            tmp.setByte("t", (byte) FLUID);
            ((FluidStack) RealStack).writeToNBT(tmp);
            buf.setTag(index + ":", tmp);
        }
        else {
            NBTTagCompound tmp = new NBTTagCompound();
            tmp.setByte("t", (byte) CUSTOM);
            customNBTWriter(tmp);
            buf.setTag(index + ":", tmp);
        }
    }

    public static OrderStack<?> readFromNBT(NBTTagCompound buf, @Nullable OrderStack<?> dummy, int index) {
        if (!buf.hasKey(index + ":")) return null;
        NBTTagCompound info = buf.getCompoundTag(index + ":");
        byte id = info.getByte("t");
        switch (id) {
            case ITEM:
                return new OrderStack<>(ItemStack.loadItemStackFromNBT(info), index);
            case FLUID:
                return new OrderStack<>(FluidStack.loadFluidStackFromNBT(info), index);
            case CUSTOM:
                if (dummy == null) throw new IllegalOrderStackID(id);
                return new OrderStack<>(dummy.customNBTReader(buf), index);
            default:
                throw new IllegalOrderStackID(id);
        }
    }

}
