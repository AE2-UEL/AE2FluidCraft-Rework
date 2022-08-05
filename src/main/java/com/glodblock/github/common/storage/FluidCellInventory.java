package com.glodblock.github.common.storage;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.exceptions.AppEngException;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import com.glodblock.github.common.Config;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.Util;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

public class FluidCellInventory implements IFluidCellInventory {

    private static final String FLUID_TYPE_TAG = "ft";
    private static final String FLUID_COUNT_TAG = "fc";
    private static final String FLUID_SLOT = "#";
    private static final String FLUID_SLOT_COUNT = "@";
    private IStorageFluidCell cellType;
    private static String[] fluidSlots;
    private static String[] fluidSlotCount;
    private final ItemStack cellItem;
    private final ISaveProvider container;
    private final static int MAX_TYPE = 1;
    private int storedFluidCount;
    private short storedFluids;
    private IItemList<IAEFluidStack> cellItems;
    private final NBTTagCompound tagCompound;

    public FluidCellInventory( final ItemStack o, final ISaveProvider container ) throws AppEngException {

        if( fluidSlots == null )
        {
            fluidSlots = new String[MAX_TYPE];
            fluidSlotCount = new String[MAX_TYPE];

            for( int x = 0; x < MAX_TYPE; x++ )
            {
                fluidSlots[x] = FLUID_SLOT + x;
                fluidSlotCount[x] = FLUID_SLOT_COUNT + x;
            }
        }

        if( o == null )
        {
            throw new AppEngException( "ItemStack was used as a cell, but was not a cell!" );
        }

        this.cellType = null;
        this.cellItem = o;

        final Item type = this.cellItem.getItem();

        if( type instanceof IStorageFluidCell)
        {
            this.cellType = (IStorageFluidCell) this.cellItem.getItem();
        }

        if( this.cellType == null )
        {
            throw new AppEngException( "ItemStack was used as a cell, but was not a cell!" );
        }

        if( !this.cellType.isStorageCell( this.cellItem ) )
        {
            throw new AppEngException( "ItemStack was used as a cell, but was not a cell!" );
        }

        this.container = container;
        this.tagCompound = Platform.openNbtData( o );
        this.storedFluids = this.tagCompound.getShort( FLUID_TYPE_TAG );
        this.storedFluidCount = this.tagCompound.getInteger( FLUID_COUNT_TAG );
        this.cellItems = null;
    }

    public static IMEInventoryHandler<IAEFluidStack> getCell(final ItemStack o, final ISaveProvider container2 )
    {
        try
        {
            return new FluidCellInventoryHandler( new FluidCellInventory( o, container2 ) );
        }
        catch( final AppEngException e )
        {
            return null;
        }
    }

    public static boolean isCell( final ItemStack itemStack )
    {
        if( itemStack == null )
        {
            return false;
        }

        final Item type = itemStack.getItem();

        if( type instanceof IStorageFluidCell)
        {
            return ( (IStorageFluidCell) type ).isStorageCell( itemStack );
        }

        return false;
    }

    @Override
    public ItemStack getItemStack() {
        return this.cellItem;
    }

    @Override
    public double getIdleDrain() {
        return this.cellType.getIdleDrain();
    }

    @Override
    public IInventory getConfigInventory() {
        return this.cellType.getConfigInventory( this.cellItem );
    }

    @Override
    public int getBytesPerType() {
        return this.cellType.getBytesPerType( this.cellItem );
    }

    @Override
    public boolean canHoldNewFluid() {
        final long bytesFree = this.getFreeBytes();

        return ( bytesFree > this.getBytesPerType() || ( bytesFree == this.getBytesPerType() && this.getUnusedFluidCount() > 0 ) ) && this.getRemainingFluidTypes() > 0;
    }

    @Override
    public long getTotalBytes() {
        return this.cellType.getBytes( this.cellItem );
    }

    @Override
    public long getFreeBytes() {
        return this.getTotalBytes() - this.getUsedBytes();
    }

    @Override
    public long getUsedBytes() {
        final long bytesForItemCount = ( this.getStoredFluidCount() + this.getUnusedFluidCount() ) / 8 / 256;
        return this.getStoredFluidTypes() * this.getBytesPerType() + bytesForItemCount;
    }

    @Override
    public long getTotalFluidTypes() {
        return MAX_TYPE;
    }

    @Override
    public long getStoredFluidCount() {
        return this.storedFluidCount;
    }

    @Override
    public long getStoredFluidTypes() {
        return this.storedFluids;
    }

    public long getRemainingFluidTypes() {
        final long basedOnStorage = this.getFreeBytes() / this.getBytesPerType();
        final long baseOnTotal = this.getTotalFluidTypes() - this.getStoredFluidTypes();

        return Math.min(basedOnStorage, baseOnTotal);
    }

    @Override
    public long getRemainingFluidCount() {
        final long remaining = this.getFreeBytes() * 8 * 256 + this.getUnusedFluidCount();
        return remaining > 0 ? remaining : 0;
    }

    @Override
    public int getUnusedFluidCount() {
        final int div = (int) ( this.getStoredFluidCount() % 8 );

        if( div == 0 )
        {
            return 0;
        }

        return 8 - div;
    }

    @Override
    public int getStatusForCell() {
        if( this.canHoldNewFluid() )
        {
            return 1;
        }
        if( this.getRemainingFluidCount() > 0 )
        {
            return 2;
        }
        return 3;
    }

    private void loadCellItems()
    {
        if( this.cellItems == null )
        {
            this.cellItems = AEApi.instance().storage().createFluidList();
        }

        this.cellItems.resetStatus(); // clears totals and stuff.

        final int types = (int) this.getStoredFluidTypes();

        for( int x = 0; x < types; x++ )
        {
            final FluidStack t = FluidStack.loadFluidStackFromNBT( this.tagCompound.getCompoundTag( fluidSlots[x] ) );

            if( t != null )
            {
                t.amount = this.tagCompound.getInteger( fluidSlotCount[x] );

                if( t.amount > 0 )
                {
                    this.cellItems.add( AEFluidStack.create( t ) );
                }
            }
        }
    }

    private IItemList<IAEFluidStack> getCellItems()
    {
        if( this.cellItems == null )
        {
            this.loadCellItems();
        }

        return this.cellItems;
    }

    private void updateFluidCount( final long delta )
    {
        this.storedFluidCount += delta;
        this.tagCompound.setInteger( FLUID_COUNT_TAG, this.storedFluidCount );
    }

    private void saveChanges()
    {
        int itemCount = 0;

        int x = 0;

        for( final IAEFluidStack v : this.cellItems )
        {
            itemCount += v.getStackSize();

            final NBTBase c = this.tagCompound.getTag( fluidSlots[x] );

            if( c instanceof NBTTagCompound )
            {
                v.writeToNBT( (NBTTagCompound) c );
            }
            else
            {
                final NBTTagCompound g = new NBTTagCompound();
                v.writeToNBT( g );
                this.tagCompound.setTag( fluidSlots[x], g );
            }

            /*
             * NBTBase tagSlotCount = tagCompound.getTag( itemSlotCount[x] ); if ( tagSlotCount instanceof
             * NBTTagInt ) ((NBTTagInt) tagSlotCount).data = (int) v.getStackSize(); else
             */
            this.tagCompound.setInteger( fluidSlotCount[x], (int) v.getStackSize() );

            x++;
        }

        // NBTBase tagType = tagCompound.getTag( ITEM_TYPE_TAG );
        // NBTBase tagCount = tagCompound.getTag( ITEM_COUNT_TAG );
        final short oldStoredItems = this.storedFluids;

        /*
         * if ( tagType instanceof NBTTagShort ) ((NBTTagShort) tagType).data = storedItems = (short) cellItems.size();
         * else
         */
        this.storedFluids = (short) this.cellItems.size();

        if( this.cellItems.isEmpty() )
        {
            this.tagCompound.removeTag( FLUID_TYPE_TAG );
        }
        else
        {
            this.tagCompound.setShort( FLUID_TYPE_TAG, this.storedFluids );
        }

        /*
         * if ( tagCount instanceof NBTTagInt ) ((NBTTagInt) tagCount).data = storedItemCount = itemCount; else
         */
        this.storedFluidCount = itemCount;

        if( itemCount == 0 )
        {
            this.tagCompound.removeTag( FLUID_COUNT_TAG );
        }
        else
        {
            this.tagCompound.setInteger( FLUID_COUNT_TAG, itemCount );
        }

        // clean any old crusty stuff...
        for( ; x < oldStoredItems && x < MAX_TYPE; x++ )
        {
            this.tagCompound.removeTag( fluidSlots[x] );
            this.tagCompound.removeTag( fluidSlotCount[x] );
        }

        if( this.container != null )
        {
            this.container.saveChanges( this );
        }
    }

    @Override
    public IAEFluidStack injectItems(IAEFluidStack input, Actionable mode, BaseActionSource src) {
        if( input == null )
        {
            return null;
        }

        if( input.getStackSize() == 0 )
        {
            return null;
        }

        if(this.cellType.isBlackListed( this.cellItem, input ))
        {
            return input;
        }

        final FluidStack sharedFluidStack = input.getFluidStack();

        final IAEFluidStack l = this.getCellItems().findPrecise( input );

        if( l != null )
        {
            final long remainingItemSlots = this.getRemainingFluidCount();

            if( remainingItemSlots < 0 )
            {
                return input;
            }

            if( input.getStackSize() > remainingItemSlots )
            {
                final IAEFluidStack r = input.copy();
                r.setStackSize( r.getStackSize() - remainingItemSlots );

                if( mode == Actionable.MODULATE )
                {
                    l.setStackSize( l.getStackSize() + remainingItemSlots );
                    this.updateFluidCount( remainingItemSlots );
                    this.saveChanges();
                }

                return r;
            }
            else
            {
                if( mode == Actionable.MODULATE )
                {
                    l.setStackSize( l.getStackSize() + input.getStackSize() );
                    this.updateFluidCount( input.getStackSize() );
                    this.saveChanges();
                }

                return null;
            }
        }

        if( this.canHoldNewFluid() ) // room for new type, and for at least one item!
        {
            final long remainingItemCount = this.getRemainingFluidCount() - this.getBytesPerType() * 8;

            if( remainingItemCount > 0 )
            {
                if( input.getStackSize() > remainingItemCount )
                {
                    final FluidStack toReturn = Util.cloneFluidStack(sharedFluidStack);
                    toReturn.amount = (int) (sharedFluidStack.amount - remainingItemCount);

                    if( mode == Actionable.MODULATE )
                    {
                        final FluidStack toWrite = Util.cloneFluidStack( sharedFluidStack );
                        toWrite.amount = (int) remainingItemCount;

                        this.cellItems.add( AEFluidStack.create( toWrite ) );
                        this.updateFluidCount( toWrite.amount );

                        this.saveChanges();
                    }

                    return AEFluidStack.create( toReturn );
                }

                if( mode == Actionable.MODULATE )
                {
                    this.updateFluidCount( input.getStackSize() );
                    this.cellItems.add( input );
                    this.saveChanges();
                }

                return null;
            }
        }

        return input;
    }

    @Override
    public IAEFluidStack extractItems(IAEFluidStack request, Actionable mode, BaseActionSource src) {
        if( request == null )
        {
            return null;
        }

        final long size = Math.min( Integer.MAX_VALUE, request.getStackSize() );

        IAEFluidStack results = null;

        final IAEFluidStack l = this.getCellItems().findPrecise( request );

        if( l != null )
        {
            results = l.copy();

            if( l.getStackSize() <= size )
            {
                results.setStackSize( l.getStackSize() );

                if( mode == Actionable.MODULATE )
                {
                    this.updateFluidCount( -l.getStackSize() );
                    l.setStackSize( 0 );
                    this.saveChanges();
                }
            }
            else
            {
                results.setStackSize( size );

                if( mode == Actionable.MODULATE )
                {
                    l.setStackSize( l.getStackSize() - size );
                    this.updateFluidCount( -size );
                    this.saveChanges();
                }
            }
        }

        return results;
    }

    @Override
    public IItemList<IAEFluidStack> getAvailableItems(IItemList<IAEFluidStack> out) {
        for( final IAEFluidStack i : this.getCellItems() )
        {
            out.add( i );
        }

        return out;
    }

    @Override
    public StorageChannel getChannel() {
        return StorageChannel.FLUIDS;
    }
}
