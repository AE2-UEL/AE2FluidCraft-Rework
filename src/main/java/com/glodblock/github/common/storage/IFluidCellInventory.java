package com.glodblock.github.common.storage;

import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEFluidStack;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public interface IFluidCellInventory extends IMEInventory<IAEFluidStack>
{

    /**
     * @return the item stack of this storage cell.
     */
    ItemStack getItemStack();

    /**
     * @return idle cost for this Storage Cell
     */
    double getIdleDrain();

    /**
     * @return access configured list
     */
    IInventory getConfigInventory();

    /**
     * @return How many bytes are used for each type?
     */
    int getBytesPerType();

    /**
     * @return true if a new item type can be added.
     */
    boolean canHoldNewFluid();

    /**
     * @return total byte storage.
     */
    long getTotalBytes();

    /**
     * @return how many bytes are free.
     */
    long getFreeBytes();

    /**
     * @return how many bytes are in use.
     */
    long getUsedBytes();

    /**
     * @return how many items are stored.
     */
    long getStoredFluidCount();

    /**
     * @return how many more items can be stored.
     */
    long getRemainingFluidCount();

    /**
     * @return how many items can be added without consuming another byte.
     */
    int getUnusedFluidCount();

    /**
     * @return the status number for this drive.
     */
    int getStatusForCell();

    long getStoredFluidTypes();

    long getTotalFluidTypes();
}
