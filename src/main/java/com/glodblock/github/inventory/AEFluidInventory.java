package com.glodblock.github.inventory;

import appeng.api.storage.data.IAEFluidStack;
import appeng.core.AELog;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import com.glodblock.github.util.Util;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

import java.util.Objects;

public class AEFluidInventory implements IAEFluidTank
{
    private final IAEFluidStack[] fluids;
    private final IAEFluidInventory handler;
    private final int capacity;
    private FluidTankInfo[] props = null;

    public AEFluidInventory( final IAEFluidInventory handler, final int slots, final int capcity )
    {
        this.fluids = new IAEFluidStack[slots];
        this.handler = handler;
        this.capacity = capcity;
    }

    public AEFluidInventory( final IAEFluidInventory handler, final int slots )
    {
        this( handler, slots, Integer.MAX_VALUE );
    }

    @Override
    public void setFluidInSlot( final int slot, final IAEFluidStack fluid )
    {
        if( slot >= 0 && slot < this.getSlots() )
        {
            if( Objects.equals( this.fluids[slot], fluid ) )
            {
                if( fluid != null && fluid.getStackSize() != this.fluids[slot].getStackSize() )
                {
                    this.fluids[slot].setStackSize( Math.min( fluid.getStackSize(), this.capacity ) );
                    this.onContentChanged( slot );
                }
            }
            else
            {
                if( fluid == null )
                {
                    this.fluids[slot] = null;
                }
                else
                {
                    this.fluids[slot] = fluid.copy();
                    this.fluids[slot].setStackSize( Math.min( fluid.getStackSize(), this.capacity ) );
                }

                this.onContentChanged( slot );
            }
        }
    }

    private void onContentChanged( final int slot )
    {
        if( this.handler != null && Platform.isServer() )
        {
            this.handler.onFluidInventoryChanged( this, slot );
        }
    }

    @Override
    public IAEFluidStack getFluidInSlot( final int slot )
    {
        if( slot >= 0 && slot < this.getSlots() )
        {
            return this.fluids[slot];
        }
        return null;
    }

    @Override
    public int getSlots()
    {
        return this.fluids.length;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from)
    {
        if( this.props == null )
        {
            this.props = new FluidTankInfo[this.getSlots()];
            for( int i = 0; i < this.getSlots(); ++i )
            {
                this.props[i] = new FluidTankInfo(new FluidTankPropertiesWrapper( i ));
            }

        }
        return this.props;
    }

    public int fill( final int slot, final FluidStack resource, final boolean doFill )
    {
        if( resource == null || resource.amount <= 0 )
        {
            return 0;
        }

        final IAEFluidStack fluid = this.fluids[slot];

        if( fluid != null && !fluid.getFluidStack().isFluidEqual( resource ) )
        {
            return 0;
        }

        int amountToStore = this.capacity;

        if( fluid != null )
        {
            amountToStore -= fluid.getStackSize();
        }

        amountToStore = Math.min( amountToStore, resource.amount );

        if( doFill )
        {
            if( fluid == null )
            {
                this.setFluidInSlot( slot, AEFluidStack.create( resource ) );
            }
            else
            {
                fluid.setStackSize( fluid.getStackSize() + amountToStore );
                this.onContentChanged( slot );
            }
        }

        return amountToStore;
    }

    public FluidStack drain( final int slot, final FluidStack resource, final boolean doDrain )
    {
        final IAEFluidStack fluid = this.fluids[slot];
        if(fluid == null || !fluid.getFluidStack().equals(resource))
        {
            return null;
        }
        return this.drain( slot, resource.amount, doDrain );
    }

    public FluidStack drain( final int slot, final int maxDrain, boolean doDrain )
    {
        final IAEFluidStack fluid = this.fluids[slot];
        if( fluid == null || maxDrain <= 0 )
        {
            return null;
        }

        int drained = maxDrain;
        if( fluid.getStackSize() < drained )
        {
            drained = (int) fluid.getStackSize();
        }

        FluidStack stack = new FluidStack( fluid.getFluid(), drained );
        if( doDrain )
        {
            fluid.setStackSize( fluid.getStackSize() - drained );
            if( fluid.getStackSize() <= 0 )
            {
                this.fluids[slot] = null;
            }
            this.onContentChanged( slot );
        }
        return stack;
    }

    @Override
    public int fill(ForgeDirection from, final FluidStack fluid, final boolean doFill )
    {
        if( fluid == null || fluid.amount <= 0 )
        {
            return 0;
        }

        final FluidStack insert = fluid.copy();

        int totalFillAmount = 0;
        for( int slot = 0; slot < this.getSlots(); ++slot )
        {
            int fillAmount = this.fill( slot, insert, doFill );
            totalFillAmount += fillAmount;
            insert.amount -= fillAmount;
            if( insert.amount <= 0 )
            {
                break;
            }
        }
        return totalFillAmount;
    }

    @Override
    public FluidStack drain(ForgeDirection from, final FluidStack fluid, final boolean doDrain )
    {
        if( fluid == null || fluid.amount <= 0 )
        {
            return null;
        }

        final FluidStack resource = fluid.copy();

        FluidStack totalDrained = null;
        for( int slot = 0; slot < this.getSlots(); ++slot )
        {
            FluidStack drain = this.drain( slot, resource, doDrain );
            if( drain != null )
            {
                if( totalDrained == null )
                {
                    totalDrained = drain;
                }
                else
                {
                    totalDrained.amount += drain.amount;
                }

                resource.amount -= drain.amount;
                if( resource.amount <= 0 )
                {
                    break;
                }
            }
        }
        return totalDrained;
    }

    @Override
    public FluidStack drain( ForgeDirection from, final int maxDrain, final boolean doDrain )
    {
        if( maxDrain == 0 )
        {
            return null;
        }

        FluidStack totalDrained = null;
        int toDrain = maxDrain;

        for( int slot = 0; slot < this.getSlots(); ++slot )
        {
            if( totalDrained == null )
            {
                totalDrained = this.drain( slot, toDrain, doDrain );
                if( totalDrained != null )
                {
                    toDrain -= totalDrained.amount;
                }
            }
            else
            {
                FluidStack copy = totalDrained.copy();
                copy.amount = toDrain;
                FluidStack drain = this.drain( slot, copy, doDrain );
                if( drain != null )
                {
                    totalDrained.amount += drain.amount;
                    toDrain -= drain.amount;
                }
            }

            if( toDrain <= 0 )
            {
                break;
            }
        }
        return totalDrained;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return fill(from, new FluidStack(fluid, 1), false) == 1;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return drain(from, new FluidStack(fluid, 1), false) != null;
    }

    public void writeToNBT( final NBTTagCompound data, final String name )
    {
        final NBTTagCompound c = new NBTTagCompound();
        this.writeToNBT( c );
        data.setTag( name, c );
    }

    private void writeToNBT( final NBTTagCompound target )
    {
        for( int x = 0; x < this.fluids.length; x++ )
        {
            try
            {
                final NBTTagCompound c = new NBTTagCompound();

                if( this.fluids[x] != null )
                {
                    this.fluids[x].writeToNBT( c );
                }

                target.setTag( "#" + x, c );
            }
            catch( final Exception ignored )
            {
            }
        }
    }

    public void readFromNBT( final NBTTagCompound data, final String name )
    {
        final NBTTagCompound c = data.getCompoundTag( name );
        if( c != null )
        {
            this.readFromNBT( c );
        }
    }

    private void readFromNBT( final NBTTagCompound target )
    {
        for( int x = 0; x < this.fluids.length; x++ )
        {
            try
            {
                final NBTTagCompound c = target.getCompoundTag( "#" + x );

                if( c != null )
                {
                    this.fluids[x] = Util.loadFluidStackFromNBT( c );
                }
            }
            catch( final Exception e )
            {
                AELog.debug( e );
            }
        }
    }

    private class FluidTankPropertiesWrapper implements IFluidTank
    {
        private final int slot;

        public FluidTankPropertiesWrapper( final int slot )
        {
            this.slot = slot;
        }

        @Override
        public FluidStack getFluid()
        {
            return AEFluidInventory.this.fluids[this.slot] == null ? null : AEFluidInventory.this.fluids[this.slot].getFluidStack();
        }

        @Override
        public int getFluidAmount() {
            return getFluid() == null ? 0 : getFluid().amount;
        }

        @Override
        public int getCapacity()
        {
            return Math.min( AEFluidInventory.this.capacity, Integer.MAX_VALUE );
        }

        @Override
        public FluidTankInfo getInfo() {
            return null;
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            if (resource == null || (getFluid() != null && !resource.equals(getFluid()))) return 0;
            int acc = 0;
            if (getFluid() == null) {
                acc = Math.min(resource.amount, getCapacity());
            }
            if (getFluid() != null) {
                acc = Math.max(resource.amount, getCapacity() - getFluid().amount);
            }
            if (doFill) {
                if (getFluid() == null)
                    AEFluidInventory.this.setFluidInSlot(this.slot, AEFluidStack.create(new FluidStack(resource.getFluid(), acc)));
                else getFluid().amount += acc;
            }
            return acc;
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (getFluid() == null) return null;
            FluidStack remove = new FluidStack(getFluid().getFluid(), 1);
            int amt = Math.min(maxDrain, getFluid().amount);
            remove.amount = amt;
            if (doDrain) {
                if (amt == getFluid().amount) {
                    AEFluidInventory.this.setFluidInSlot(this.slot, null);
                }
                else getFluid().amount -= amt;
            }
            return remove;
        }
    }
}
