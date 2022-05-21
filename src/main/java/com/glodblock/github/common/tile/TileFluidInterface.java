package com.glodblock.github.common.tile;

import appeng.api.config.Actionable;
import appeng.api.config.Upgrades;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.core.features.IStackSrc;
import appeng.me.GridAccessException;
import appeng.tile.misc.TileInterface;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import com.glodblock.github.loader.ItemAndBlockHolder;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import javax.annotation.Nullable;

public class TileFluidInterface extends TileInterface implements IFluidHandler
{

    private final BaseActionSource ownActionSource = new MachineSource(this);

    private IMEMonitor<IAEFluidStack> getFluidGrid() {
        try {
            return getProxy().getGrid().<IStorageGrid>getCache(IStorageGrid.class).getFluidInventory();
        } catch (GridAccessException e) {
            return null;
        }
    }

    private IEnergyGrid getEnergyGrid() {
        try {
            return getProxy().getGrid().getCache(IEnergyGrid.class);
        } catch (GridAccessException e) {
            return null;
        }
    }

    @Nullable
    protected ItemStack getItemFromTile( final Object obj )
    {
        if (obj instanceof TileFluidInterface) {
            return ItemAndBlockHolder.INTERFACE.stack();
        }
        return null;
    }

    @Override
    public int getInstalledUpgrades( final Upgrades u )
    {
        return getInterfaceDuality().getInstalledUpgrades( u );
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        IMEMonitor<IAEFluidStack> fluidGrid = getFluidGrid();
        IEnergyGrid energyGrid = getEnergyGrid();
        if (energyGrid == null || fluidGrid == null || resource == null)
            return 0;
        int ori = resource.amount;
        IAEFluidStack remove;
        if (doFill) {
            remove = Platform.poweredInsert(energyGrid, fluidGrid, AEFluidStack.create(resource), ownActionSource);
        } else {
            remove = fluidGrid.injectItems(AEFluidStack.create(resource), Actionable.SIMULATE, ownActionSource);
        }
        return remove == null ? ori : (int) (ori - remove.getStackSize());
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        return null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        return null;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return true;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return false;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return new FluidTankInfo[]{new FluidTankInfo(null, 0)};
    }
}
