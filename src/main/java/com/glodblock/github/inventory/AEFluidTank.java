package com.glodblock.github.inventory;

import appeng.api.storage.data.IAEFluidStack;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;

public class AEFluidTank extends FluidTank implements IAEFluidTank
{
    private final IAEFluidInventory host;

    public AEFluidTank( IAEFluidInventory host, int capacity )
    {
        super( capacity );
        this.host = host;
        if( host instanceof TileEntity) {
            this.tile = (TileEntity) host;
        }
    }

    protected void onContentsChanged()
    {
        if( this.host != null && Platform.isServer() )
        {
            this.host.onFluidInventoryChanged( this, 0 );
        }
    }

    @Override
    public void setFluidInSlot( int slot, IAEFluidStack fluid )
    {
        if( slot == 0 )
        {
            this.setFluid( fluid == null ? null : fluid.getFluidStack() );
            this.onContentsChanged();
        }
    }

    @Override
    public IAEFluidStack getFluidInSlot(int slot )
    {
        if( slot == 0 )
        {
            return AEFluidStack.create( this.getFluid() );
        }
        return null;
    }

    @Override
    public int getSlots()
    {
        return 1;
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        return super.fill(resource, doFill);
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        if (resource == null || !resource.equals(this.getFluid())) return null;
        return super.drain(resource.amount, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        return super.drain(maxDrain, doDrain);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return super.fill(new FluidStack(fluid, 1), false) == 1;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return drain(ForgeDirection.UNKNOWN, new FluidStack(fluid, 1), false) != null;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return new FluidTankInfo[]{super.getInfo()};
    }
}
