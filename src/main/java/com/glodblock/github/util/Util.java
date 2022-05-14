package com.glodblock.github.util;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import java.util.Objects;

public final class Util {

    public static FluidStack getFluidFromItem(ItemStack stack) {
        if (!stack.isEmpty() && stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            if (stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) != null) {
                IFluidTankProperties[] tanks = Objects.requireNonNull(stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)).getTankProperties();
                for (IFluidTankProperties tank : tanks) {
                    if (tank != null && tank.getContents() != null) {
                        return tank.getContents().copy();
                    }
                }
            }
        }
        return null;
    }

}
