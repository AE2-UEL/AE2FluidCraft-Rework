package com.glodblock.github.common.storage;

import appeng.api.AEApi;
import appeng.api.config.IncludeExclude;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.me.storage.MEInventoryHandler;
import appeng.me.storage.MEPassThrough;
import appeng.util.item.AEFluidStack;
import appeng.util.prioitylist.PrecisePriorityList;
import com.glodblock.github.util.Ae2Reflect;
import com.glodblock.github.util.Util;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class FluidCellInventoryHandler extends MEInventoryHandler<IAEFluidStack> implements IFluidCellInventoryHandler {

    FluidCellInventoryHandler( final IMEInventory<IAEFluidStack> c )
    {
        super( c, StorageChannel.FLUIDS );

        final IFluidCellInventory ci = this.getCellInv();

        if( ci != null )
        {
            final IInventory config = ci.getConfigInventory();

            final IItemList<IAEFluidStack> priorityList = AEApi.instance().storage().createFluidList();
            for (int x = 0; x < config.getSizeInventory(); x++) {
                final ItemStack is = config.getStackInSlot(x);
                if (is != null && Util.getFluidFromItem(is) != null) {
                    priorityList.add(AEFluidStack.create(Util.getFluidFromItem(is)));
                    break;
                }
            }
            if (!priorityList.isEmpty()) {
                this.setPartitionList(new PrecisePriorityList<>(priorityList));
            }
        }
    }

    @Override
    public IFluidCellInventory getCellInv()
    {
        Object o = this.getInternal();

        if( o instanceof MEPassThrough)
        {
            o = Ae2Reflect.getInternal( (MEPassThrough) o );
        }

        return (IFluidCellInventory) ( o instanceof IFluidCellInventory ? o : null );
    }

    @Override
    public boolean isPreformatted()
    {
        return !Ae2Reflect.getPartitionList(this).isEmpty();
    }

    @Override
    public IncludeExclude getIncludeExcludeMode()
    {
        return IncludeExclude.WHITELIST;
    }

    public int getStatusForCell()
    {
        int val = this.getCellInv().getStatusForCell();

        if( val == 1 && this.isPreformatted() )
        {
            val = 2;
        }

        return val;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterable<IAEFluidStack> getPartitionInv() {
        return (Iterable<IAEFluidStack>) Ae2Reflect.getPartitionList(this).getItems();
    }
}
