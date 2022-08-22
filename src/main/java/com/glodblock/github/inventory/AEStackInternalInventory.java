package com.glodblock.github.inventory;

import appeng.core.AELog;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class AEStackInternalInventory extends AppEngInternalInventory {

    public AEStackInternalInventory(IAEAppEngInventory inventory, int size) {
        super(inventory, size);
    }

    @Override
    public void writeToNBT( final NBTTagCompound data, final String name )
    {
        final NBTTagCompound c = new NBTTagCompound();
        this.writeToNBTEx( c );
        data.setTag( name, c );
    }

    @Override
    public void readFromNBT( final NBTTagCompound data, final String name )
    {
        final NBTTagCompound c = data.getCompoundTag( name );
        if( c != null )
        {
            this.readFromNBTEx( c );
        }
    }

    private void writeToNBTEx( final NBTTagCompound target )
    {
        for( int x = 0; x < this.getSizeInventory(); x++ )
        {
            try
            {
                final NBTTagCompound c = new NBTTagCompound();
                if( this.getStackInSlot(x) != null )
                {
                    this.getStackInSlot(x).writeToNBT( c );
                    c.setInteger("Count", this.getStackInSlot(x).stackSize);
                }
                target.setTag( "#" + x, c );
            }
            catch( final Exception ignored )
            {
            }
        }
    }

    public void readFromNBTEx( final NBTTagCompound target )
    {
        for( int x = 0; x < this.getSizeInventory(); x++ )
        {
            try
            {
                final NBTTagCompound c = target.getCompoundTag( "#" + x );
                if( c != null )
                {
                    ItemStack tmp = ItemStack.loadItemStackFromNBT( c );
                    if ( tmp != null )
                    {
                        tmp.stackSize = c.getInteger("Count");
                    }
                    this.setInventorySlotContents(x, tmp);
                }
            }
            catch( final Exception e )
            {
                AELog.debug( e );
            }
        }
    }

}
