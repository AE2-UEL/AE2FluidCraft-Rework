package com.glodblock.github.common.storage;

import appeng.api.storage.ICellWorkbenchItem;
import appeng.api.storage.data.IAEFluidStack;
import net.minecraft.item.ItemStack;

public interface IStorageFluidCell extends ICellWorkbenchItem {

    int getBytes( ItemStack cellItem );

    int getBytesPerType( ItemStack cellItem );

    boolean isBlackListed( ItemStack cellItem, IAEFluidStack requestedAddition );

    boolean storableInStorageCell();

    boolean isStorageCell( ItemStack i );

    double getIdleDrain();

    int getTotalTypes( ItemStack cellItem );

}
