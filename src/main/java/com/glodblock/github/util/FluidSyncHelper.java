package com.glodblock.github.util;


import appeng.api.storage.data.IAEFluidStack;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.inventory.AEFluidInventory;
import com.glodblock.github.inventory.IAEFluidTank;
import com.glodblock.github.network.SPacketFluidSlot;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ICrafting;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FluidSyncHelper {
    private final IAEFluidTank inv;
    private final IAEFluidTank cache;
    private final int idOffset;

    public FluidSyncHelper(final IAEFluidTank inv, final int idOffset )
    {
        this.inv = inv;
        this.cache = new AEFluidInventory( null, inv.getSlots() );
        this.idOffset = idOffset;
    }

    public void sendFull( final Iterable<ICrafting> listeners )
    {
        this.sendDiffMap( this.createDiffMap( true ), listeners );
    }

    public void sendDiff( final Iterable<ICrafting> listeners )
    {
        this.sendDiffMap( this.createDiffMap( false ), listeners );
    }

    public void readPacket( final Map<Integer, IAEFluidStack> data )
    {
        for( int i = 0; i < this.inv.getSlots(); ++i )
        {
            if( data.containsKey( i + this.idOffset ) )
            {
                this.inv.setFluidInSlot( i, data.get( i + this.idOffset ) );
            }
        }
    }

    private void sendDiffMap( final Map<Integer, IAEFluidStack> data, final Iterable<ICrafting> listeners )
    {
        if( data.isEmpty() )
        {
            return;
        }

        for( final ICrafting l : listeners )
        {
            if( l instanceof EntityPlayerMP)
            {
                FluidCraft.proxy.netHandler.sendTo( new SPacketFluidSlot( data ), (EntityPlayerMP) l );
            }
        }
    }

    private Map<Integer, IAEFluidStack> createDiffMap(final boolean full )
    {
        final Map<Integer, IAEFluidStack> ret = new HashMap<>();
        for( int i = 0; i < this.inv.getSlots(); ++i )
        {
            if( full || !this.equalsSlot( i ) )
            {
                ret.put( i + this.idOffset, this.inv.getFluidInSlot( i ) );
            }
            if( !full )
            {
                this.cache.setFluidInSlot( i, this.inv.getFluidInSlot( i ) );
            }
        }
        return ret;
    }

    private boolean equalsSlot(int slot )
    {
        final IAEFluidStack stackA = this.inv.getFluidInSlot( slot );
        final IAEFluidStack stackB = this.cache.getFluidInSlot( slot );

        if( !Objects.equals( stackA, stackB ) )
        {
            return false;
        }

        return stackA == null || stackA.getStackSize() == stackB.getStackSize();
    }
}
