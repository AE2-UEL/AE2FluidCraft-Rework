package com.glodblock.github.inventory.slot;

import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.OptionalSlotFakeTypeOnly;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.util.Util;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class OptionalFluidSlotFakeTypeOnly extends OptionalSlotFakeTypeOnly {

    public OptionalFluidSlotFakeTypeOnly(IInventory inv, IOptionalSlotHost containerBus, int idx, int x, int y, int offX, int offY, int groupNum) {
        super(inv, containerBus, idx, x, y, offX, offY, groupNum);
    }

    @Override
    public void putStack( ItemStack is )
    {
        FluidStack fluidStack = Util.getFluidFromItem(is);
        if (fluidStack != null) {
            ItemStack tmp = ItemFluidPacket.newStack(fluidStack);
            tmp.setStackDisplayName(fluidStack.getLocalizedName());
            super.putStack(tmp);
        }
        else {
            super.putStack(null);
        }
    }

}
