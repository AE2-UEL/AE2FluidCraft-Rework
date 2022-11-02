package com.glodblock.github.util;

import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.AEFluidStack;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nonnull;
import java.io.IOException;
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

    public static ItemStack getEmptiedContainer(ItemStack stack) {
        if (!stack.isEmpty() && stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            ItemStack dummy = stack.copy();
            IFluidHandlerItem fh = dummy.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fh != null) {
                fh.drain(Integer.MAX_VALUE, true);
                return fh.getContainer();
            }
        }
        return stack;
    }

    public static void writeFluidInventoryToBuffer(@Nonnull AEFluidInventory inv, ByteBuf data) throws IOException {
        int fluidMask = 0;
        for (int i = 0; i < inv.getSlots(); i++) {
            if (inv.getFluidInSlot(i) != null) {
                fluidMask |= 1 << i;
            }
        }
        data.writeByte(fluidMask);
        for (int i = 0; i < inv.getSlots(); i++) {
            IAEFluidStack fluid = inv.getFluidInSlot(i);
            if (fluid != null) {
                fluid.writeToPacket(data);
            }
        }
    }

    public static boolean readFluidInventoryToBuffer(@Nonnull AEFluidInventory inv, ByteBuf data) throws IOException {
        boolean changed = false;
        int fluidMask = data.readByte();
        for (int i = 0; i < inv.getSlots(); i++) {
            if ((fluidMask & (1 << i)) != 0) {
                IAEFluidStack fluid = AEFluidStack.fromPacket(data);
                if (fluid != null) { // this shouldn't happen, but better safe than sorry
                    IAEFluidStack origFluid = inv.getFluidInSlot(i);
                    if (!fluid.equals(origFluid) || fluid.getStackSize() != origFluid.getStackSize()) {
                        inv.setFluidInSlot(i, fluid);
                        changed = true;
                    }
                }
            } else if (inv.getFluidInSlot(i) != null) {
                inv.setFluidInSlot(i, null);
                changed = true;
            }
        }
        return changed;
    }

}
