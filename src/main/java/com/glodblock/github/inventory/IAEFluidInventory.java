package com.glodblock.github.inventory;

@FunctionalInterface
public interface IAEFluidInventory
{
    void onFluidInventoryChanged( final IAEFluidTank inv, final int slot );
}
