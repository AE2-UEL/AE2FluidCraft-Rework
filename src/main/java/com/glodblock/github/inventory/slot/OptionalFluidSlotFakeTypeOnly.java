package com.glodblock.github.inventory.slot;

import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.OptionalSlotFakeTypeOnly;
import appeng.util.item.AEFluidStack;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.AEFluidInventory;
import com.glodblock.github.util.Util;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class OptionalFluidSlotFakeTypeOnly extends OptionalSlotFakeTypeOnly {

    AEFluidInventory fluidInv;

    public OptionalFluidSlotFakeTypeOnly(IInventory inv, AEFluidInventory fluidInv, IOptionalSlotHost containerBus, int idx, int x, int y, int offX, int offY, int groupNum) {
        super(inv, containerBus, idx, x, y, offX, offY, groupNum);
        this.fluidInv = fluidInv;
    }

    @Override
    public void putStack( ItemStack is )
    {
        FluidStack fluidStack = Util.getFluidFromItem(is);
        if (fluidStack != null) {
            ItemStack tmp = ItemFluidPacket.newDisplayStack(fluidStack);
            if (fluidInv != null) {
                FluidStack standard = fluidStack.copy();
                standard.amount = 8000;
                fluidInv.setFluidInSlot(getSlotIndex(), AEFluidStack.create(standard));
            }
            super.putStack(tmp);
        }
        else {
            super.putStack(null);
            if (fluidInv != null) {
                fluidInv.setFluidInSlot(getSlotIndex(), null);
            }
        }
    }

}
