package com.glodblock.github.inventory;

import appeng.api.storage.data.IAEFluidStack;
import net.minecraftforge.fluids.IFluidHandler;

public interface IAEFluidTank extends IFluidHandler {
    void setFluidInSlot( final int slot, final IAEFluidStack fluid );

    IAEFluidStack getFluidInSlot( final int slot );

    int getSlots();

}
