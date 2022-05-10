package com.glodblock.github.client.gui.container;

import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.client.me.InternalSlotME;
import appeng.client.me.SlotME;
import appeng.container.implementations.ContainerUpgradeable;
import appeng.container.slot.*;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import com.glodblock.github.client.gui.IFluidSyncContainer;
import com.glodblock.github.inventory.IAEFluidTank;
import com.glodblock.github.util.Ae2Reflect;
import com.glodblock.github.util.FluidSyncHelper;
import com.glodblock.github.util.Util;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class ContainerFluidConfigurable extends ContainerUpgradeable implements IFluidSyncContainer
{
    private FluidSyncHelper sync = null;

    public ContainerFluidConfigurable(InventoryPlayer ip, IUpgradeableHost te )
    {
        super( ip, te );
    }

    public abstract IAEFluidTank getFluidConfigInventory();

    private FluidSyncHelper getSynchHelper()
    {
        if( this.sync == null )
        {
            this.sync = new FluidSyncHelper( this.getFluidConfigInventory(), 0 );
        }
        return this.sync;
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer p, final int idx )
    {
        if( Platform.isClient() )
        {
            return null;
        }

        boolean hasMETiles = false;
        for( final Object is : this.inventorySlots )
        {
            if( is instanceof InternalSlotME)
            {
                hasMETiles = true;
                break;
            }
        }

        if( hasMETiles && Platform.isClient() )
        {
            return null;
        }

        final AppEngSlot clickSlot = (AppEngSlot) this.inventorySlots.get( idx ); // require AE SLots!

        if( clickSlot instanceof SlotDisabled || clickSlot instanceof SlotInaccessible)
        {
            return null;
        }
        if( clickSlot != null && clickSlot.getHasStack() )
        {
            ItemStack tis = clickSlot.getStack();

            if( tis == null )
            {
                return null;
            }

            final List<Slot> selectedSlots = new ArrayList<Slot>();

            /**
             * Gather a list of valid destinations.
             */
            if( clickSlot.isPlayerSide() )
            {
                tis = this.transferStackToContainer( tis );

                // target slots in the container...
                for( final Object inventorySlot : this.inventorySlots )
                {
                    final AppEngSlot cs = (AppEngSlot) inventorySlot;

                    if( !( cs.isPlayerSide() ) && !( cs instanceof SlotFake) && !( cs instanceof SlotCraftingMatrix) )
                    {
                        if( cs.isItemValid( tis ) )
                        {
                            selectedSlots.add( cs );
                        }
                    }
                }
            }
            else
            {
                // target slots in the container...
                for( final Object inventorySlot : this.inventorySlots )
                {
                    final AppEngSlot cs = (AppEngSlot) inventorySlot;

                    if( ( cs.isPlayerSide() ) && !( cs instanceof SlotFake ) && !( cs instanceof SlotCraftingMatrix ) )
                    {
                        if( cs.isItemValid( tis ) )
                        {
                            selectedSlots.add( cs );
                        }
                    }
                }
            }

            /**
             * Handle Fake Slot Shift clicking.
             */
            if( selectedSlots.isEmpty() && clickSlot.isPlayerSide() )
            {
                if( tis != null )
                {
                    // target slots in the container...
                    for( final Object inventorySlot : this.inventorySlots )
                    {
                        final AppEngSlot cs = (AppEngSlot) inventorySlot;
                        final ItemStack destination = cs.getStack();

                        if( !( cs.isPlayerSide() ) && cs instanceof SlotFake )
                        {
                            if( Platform.isSameItemPrecise( destination, tis ) )
                            {
                                break;
                            }
                            else if( destination == null )
                            {
                                cs.putStack( tis.copy() );
                                cs.onSlotChanged();
                                this.detectAndSendChanges();
                                break;
                            }
                        }
                    }
                }
            }

            if( tis != null )
            {
                // find partials..
                for( final Slot d : selectedSlots )
                {
                    if( d instanceof SlotDisabled || d instanceof SlotME)
                    {
                        continue;
                    }

                    if( d.isItemValid( tis ) )
                    {
                        if( d.getHasStack() )
                        {
                            final ItemStack t = d.getStack();

                            if( Platform.isSameItemPrecise( tis, t ) ) // t.isItemEqual(tis))
                            {
                                int maxSize = t.getMaxStackSize();
                                if( maxSize > d.getSlotStackLimit() )
                                {
                                    maxSize = d.getSlotStackLimit();
                                }

                                int placeAble = maxSize - t.stackSize;

                                if( tis.stackSize < placeAble )
                                {
                                    placeAble = tis.stackSize;
                                }

                                t.stackSize += placeAble;
                                tis.stackSize -= placeAble;

                                if( tis.stackSize <= 0 )
                                {
                                    clickSlot.putStack( null );
                                    d.onSlotChanged();

                                    // if ( hasMETiles ) updateClient();

                                    this.detectAndSendChanges();
                                    this.detectAndSendChanges();
                                    return null;
                                }
                                else
                                {
                                    this.detectAndSendChanges();
                                }
                            }
                        }
                    }
                }

                // any match..
                for( final Slot d : selectedSlots )
                {
                    if( d instanceof SlotDisabled || d instanceof SlotME )
                    {
                        continue;
                    }

                    if( d.isItemValid( tis ) )
                    {
                        if( d.getHasStack() )
                        {
                            final ItemStack t = d.getStack();

                            if( Platform.isSameItemPrecise( t, tis ) )
                            {
                                int maxSize = t.getMaxStackSize();
                                if( d.getSlotStackLimit() < maxSize )
                                {
                                    maxSize = d.getSlotStackLimit();
                                }

                                int placeAble = maxSize - t.stackSize;

                                if( tis.stackSize < placeAble )
                                {
                                    placeAble = tis.stackSize;
                                }

                                t.stackSize += placeAble;
                                tis.stackSize -= placeAble;

                                if( tis.stackSize <= 0 )
                                {
                                    clickSlot.putStack( null );
                                    d.onSlotChanged();

                                    // if ( worldEntity != null )
                                    // worldEntity.markDirty();
                                    // if ( hasMETiles ) updateClient();

                                    this.detectAndSendChanges();
                                    this.detectAndSendChanges();
                                    return null;
                                }
                                else
                                {
                                    this.detectAndSendChanges();
                                }
                            }
                        }
                        else
                        {
                            int maxSize = tis.getMaxStackSize();
                            if( maxSize > d.getSlotStackLimit() )
                            {
                                maxSize = d.getSlotStackLimit();
                            }

                            final ItemStack tmp = tis.copy();
                            if( tmp.stackSize > maxSize )
                            {
                                tmp.stackSize = maxSize;
                            }

                            tis.stackSize -= tmp.stackSize;
                            d.putStack( tmp );

                            if( tis.stackSize <= 0 )
                            {
                                clickSlot.putStack( null );
                                d.onSlotChanged();

                                // if ( worldEntity != null )
                                // worldEntity.markDirty();
                                // if ( hasMETiles ) updateClient();

                                this.detectAndSendChanges();
                                this.detectAndSendChanges();
                                return null;
                            }
                            else
                            {
                                this.detectAndSendChanges();
                            }
                        }
                    }
                }
            }

            clickSlot.putStack( tis != null ? tis.copy() : null );
        }

        this.detectAndSendChanges();
        return null;
    }

    protected ItemStack transferStackToContainer(ItemStack input )
    {
        FluidStack fs = Util.getFluidFromItem( input );
        if( fs != null )
        {
            final IAEFluidTank t = this.getFluidConfigInventory();
            final IAEFluidStack stack = AEFluidStack.create( fs );
            for( int i = 0; i < t.getSlots(); ++i )
            {
                if( t.getFluidInSlot( i ) == null && this.isValidForConfig( i, stack ) )
                {
                    t.setFluidInSlot( i, stack );
                    break;
                }
            }
        }
        return input;
    }

    protected boolean isValidForConfig( int slot, IAEFluidStack fs )
    {
        if( this.supportCapacity() )
        {
            // assumes 4 slots per upgrade
            final int upgrades = Ae2Reflect.getUpgrade(this).getInstalledUpgrades( Upgrades.CAPACITY );

            if( slot > 0 && upgrades < 1 )
            {
                return false;
            }
            if( slot > 4 && upgrades < 2 )
            {
                return false;
            }
        }

        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void standardDetectAndSendChanges()
    {
        if( Platform.isServer() )
        {
            this.getSynchHelper().sendDiff( this.crafters );

            // clear out config items that are no longer valid (eg capacity upgrade removed)
            final IAEFluidTank t = this.getFluidConfigInventory();
            for( int i = 0; i < t.getSlots(); ++i )
            {
                if( t.getFluidInSlot( i ) != null && !this.isValidForConfig( i, t.getFluidInSlot( i ) ) )
                {
                    t.setFluidInSlot( i, null );
                }
            }
        }
        super.standardDetectAndSendChanges();
    }

    @Override
    public void addCraftingToCrafters( ICrafting listener )
    {
        super.addCraftingToCrafters( listener );
        this.getSynchHelper().sendFull( Collections.singleton( listener ) );
    }

    @Override
    public void receiveFluidSlots( Map<Integer, IAEFluidStack> fluids )
    {
        this.getSynchHelper().readPacket( fluids );
    }

}
