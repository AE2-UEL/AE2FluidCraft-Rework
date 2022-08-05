package com.glodblock.github.inventory;

import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class FluidConvertingInventoryCrafting extends InventoryCrafting {

    public FluidConvertingInventoryCrafting(Container container, int width, int height) {
        super(container, width, height);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (stack != null && stack.getItem() instanceof ItemFluidDrop) {
            FluidStack fluid = ItemFluidDrop.getFluidStack(stack);
            if (fluid != null) {
                super.setInventorySlotContents(index, ItemFluidPacket.newStack(new FluidStack(fluid, stack.stackSize)));
            } else {
                // wtf?
                super.setInventorySlotContents(index, ItemFluidPacket.newStack(new FluidStack(FluidRegistry.WATER, 1000)));
            }
        } else {
            super.setInventorySlotContents(index, stack);
        }
    }

}
