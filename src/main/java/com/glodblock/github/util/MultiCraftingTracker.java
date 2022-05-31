package com.glodblock.github.util;

import appeng.api.AEApi;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.*;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.helpers.NonNullArrayIterator;
import appeng.util.InventoryAdaptor;
import com.google.common.collect.ImmutableSet;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MultiCraftingTracker {
    private final int size;
    private final ICraftingRequester owner;

    private Future<ICraftingJob>[] jobs = null;
    private ICraftingLink[] links = null;

    public MultiCraftingTracker( final ICraftingRequester o, final int size )
    {
        this.owner = o;
        this.size = size;
    }

    public void readFromNBT( final NBTTagCompound extra )
    {
        for( int x = 0; x < this.size; x++ )
        {
            final NBTTagCompound link = extra.getCompoundTag( "links-" + x );

            if(!link.isEmpty())
            {
                this.setLink( x, AEApi.instance().storage().loadCraftingLink( link, this.owner ) );
            }
        }
    }

    public void writeToNBT( final NBTTagCompound extra )
    {
        for( int x = 0; x < this.size; x++ )
        {
            final ICraftingLink link = this.getLink( x );

            if( link != null )
            {
                final NBTTagCompound ln = new NBTTagCompound();
                link.writeToNBT( ln );
                extra.setTag( "links-" + x, ln );
            }
        }
    }

    public boolean handleCrafting(int x, long itemToCraft, IAEItemStack ais, InventoryAdaptor d, World w, IGrid g, ICraftingGrid cg, IActionSource mySrc) {
        if (ais != null) {
            ItemStack inputStack = ais.getCachedItemStack(ais.getStackSize());
            ItemStack remaining = d.simulateAdd(inputStack);

            if (remaining.isEmpty()) {
                ais.setCachedItemStack(inputStack);
                Future<ICraftingJob> craftingJob = this.getJob(x);

                if (this.getLink(x) != null) {
                    return false;
                }

                if (craftingJob == null && this.getLink(x) == null) {
                    IAEItemStack aisC = ais.copy();
                    aisC.setStackSize(itemToCraft);
                    this.setJob(x, cg.beginCraftingJob(w, g, mySrc, aisC, null));
                    craftingJob = this.getJob(x);
                }

                if (craftingJob == null) {
                    return false;
                }

                try {
                    ICraftingJob job = craftingJob.get();

                    if (job != null) {
                        ICraftingLink link = cg.submitJob(job, this.owner, null, false, mySrc);
                        this.setJob(x, null);
                        if (link != null) {
                            this.setLink(x, link);
                            return true;
                        }
                    }
                } catch (InterruptedException | ExecutionException ignored) {
                }
            } else {
                ais.setCachedItemStack(remaining);
            }
        }

        return false;
    }

    public ImmutableSet<ICraftingLink> getRequestedJobs()
    {
        if( this.links == null )
        {
            return ImmutableSet.of();
        }

        return ImmutableSet.copyOf( new NonNullArrayIterator<>( this.links ) );
    }

    public void jobStateChange( final ICraftingLink link )
    {
        if( this.links != null )
        {
            for( int x = 0; x < this.links.length; x++ )
            {
                if( this.links[x] == link )
                {
                    this.setLink( x, null );
                    return;
                }
            }
        }
    }

    int getSlot( final ICraftingLink link )
    {
        if( this.links != null )
        {
            for( int x = 0; x < this.links.length; x++ )
            {
                if( this.links[x] == link )
                {
                    return x;
                }
            }
        }

        return -1;
    }

    void cancel()
    {
        if( this.links != null )
        {
            for( final ICraftingLink l : this.links )
            {
                if( l != null )
                {
                    l.cancel();
                }
            }

            this.links = null;
        }

        if( this.jobs != null )
        {
            for( final Future<ICraftingJob> l : this.jobs )
            {
                if( l != null )
                {
                    l.cancel( true );
                }
            }

            this.jobs = null;
        }
    }

    boolean isBusy( final int slot )
    {
        return this.getLink( slot ) != null || this.getJob( slot ) != null;
    }

    private ICraftingLink getLink( final int slot )
    {
        if( this.links == null )
        {
            return null;
        }

        return this.links[slot];
    }

    private void setLink( final int slot, final ICraftingLink l )
    {
        if( this.links == null )
        {
            this.links = new ICraftingLink[this.size];
        }

        this.links[slot] = l;

        boolean hasStuff = false;
        for( int x = 0; x < this.links.length; x++ )
        {
            final ICraftingLink g = this.links[x];

            if( g == null || g.isCanceled() || g.isDone() )
            {
                this.links[x] = null;
            }
            else
            {
                hasStuff = true;
            }
        }

        if( !hasStuff )
        {
            this.links = null;
        }
    }

    private Future<ICraftingJob> getJob( final int slot )
    {
        if( this.jobs == null )
        {
            return null;
        }

        return this.jobs[slot];
    }

    @SuppressWarnings("unchecked")
    private void setJob( final int slot, final Future<ICraftingJob> l )
    {
        if( this.jobs == null )
        {
            this.jobs = new Future[this.size];
        }

        this.jobs[slot] = l;

        boolean hasStuff = false;

        for( final Future<ICraftingJob> job : this.jobs )
        {
            if (job != null) {
                hasStuff = true;
                break;
            }
        }

        if( !hasStuff )
        {
            this.jobs = null;
        }
    }
}
