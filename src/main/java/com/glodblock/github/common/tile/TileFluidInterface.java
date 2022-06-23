package com.glodblock.github.common.tile;

import appeng.api.config.Actionable;
import appeng.api.config.Upgrades;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.me.GridAccessException;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.misc.TileInterface;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.AEFluidInventory;
import com.glodblock.github.inventory.IAEFluidInventory;
import com.glodblock.github.inventory.IAEFluidTank;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.Util;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import javax.annotation.Nullable;
import java.io.IOException;

public class TileFluidInterface extends TileInterface implements IFluidHandler, IAEFluidInventory
{

    private final BaseActionSource ownActionSource = new MachineSource(this);
    private final AEFluidInventory invFluids = new AEFluidInventory(this, 6, 16000);
    private final AppEngInternalAEInventory config = new AppEngInternalAEInventory( this, 6 );

    private final boolean[] tickState = new boolean[]{true, true, true, true, true, true};
    private final int[] tickCount = new int[]{0, 0, 0, 0, 0, 0};

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

    public AEFluidInventory getInternalFluid() {
        return invFluids;
    }

    public AppEngInternalAEInventory getConfig() {
        return config;
    }

    @TileEvent( TileEventType.NETWORK_WRITE )
    protected void writeToStream(ByteBuf data) throws IOException {
        for (int i = 0; i < config.getSizeInventory(); i++) {
            ByteBufUtils.writeItemStack(data, config.getStackInSlot(i));
        }
        int fluidMask = 0;
        for (int i = 0; i < invFluids.getSlots(); i++) {
            if (invFluids.getFluidInSlot(i) != null) {
                fluidMask |= 1 << i;
            }
        }
        data.writeByte(fluidMask);
        for (int i = 0; i < invFluids.getSlots(); i++) {
            IAEFluidStack fluid = invFluids.getFluidInSlot(i);
            if (fluid != null) {
                fluid.writeToPacket(data);
            }
        }
    }

    @TileEvent( TileEventType.NETWORK_READ )
    protected boolean readFromStream(ByteBuf data) throws IOException {
        boolean changed = false;
        for (int i = 0; i < config.getSizeInventory(); i++) {
            ItemStack stack = ByteBufUtils.readItemStack(data);
            if (!ItemStack.areItemStacksEqual(stack, config.getStackInSlot(i))) {
                config.setInventorySlotContents(i, stack);
                changed = true;
            }
        }
        int fluidMask = data.readByte();
        for (int i = 0; i < invFluids.getSlots(); i++) {
            if ((fluidMask & (1 << i)) != 0) {
                IAEFluidStack fluid = AEFluidStack.loadFluidStackFromPacket(data);
                if (fluid != null) { // this shouldn't happen, but better safe than sorry
                    IAEFluidStack origFluid = invFluids.getFluidInSlot(i);
                    if (!fluid.equals(origFluid) || fluid.getStackSize() != origFluid.getStackSize()) {
                        invFluids.setFluidInSlot(i, fluid);
                        changed = true;
                    }
                }
            } else if (invFluids.getFluidInSlot(i) != null) {
                invFluids.setFluidInSlot(i, null);
                changed = true;
            }
        }
        return changed;
    }

    @TileEvent( TileEventType.WORLD_NBT_READ )
    public void readFromNBTEvent(NBTTagCompound data) {
        config.readFromNBT(data, "ConfigInv");
        invFluids.readFromNBT(data, "FluidInv");
    }

    @TileEvent( TileEventType.WORLD_NBT_WRITE )
    public NBTTagCompound writeToNBTEvent(NBTTagCompound data) {
        config.writeToNBT(data, "ConfigInv");
        invFluids.writeToNBT(data, "FluidInv");
        return data;
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

    @Override
    public void onFluidInventoryChanged(IAEFluidTank inv, int slot) {
        saveChanges();
        markForUpdate();
    }

    public void setConfig(int id, IAEFluidStack fluid) {
        if (id >= 0 && id < 6) {
            config.setInventorySlotContents(id, ItemFluidPacket.newDisplayStack(fluid == null ? null : fluid.getFluidStack()));
        }
    }

    public void setFluidInv(int id, IAEFluidStack fluid) {
        if (id >= 0 && id < 6) {
            invFluids.setFluidInSlot(id, fluid);
        }
    }

    @TileEvent( TileEventType.TICK )
    public void updateTick() {
        //Very Hacky thing.
        for (int i = 0; i < 6; i ++) {

            FluidStack configFluid = ItemFluidPacket.getFluidStack(config.getStackInSlot(i));
            FluidStack storedFluid = invFluids.getFluidInSlot(i) == null ? null : invFluids.getFluidInSlot(i).getFluidStack();

            if (!Util.areFluidsEqual(configFluid, storedFluid)) {
                if (storedFluid != null) {
                    int ori = storedFluid.amount;
                    int filled = fill(ForgeDirection.UNKNOWN, storedFluid, false);
                    if (ori == filled) {
                        fill(ForgeDirection.UNKNOWN, storedFluid, true);
                        invFluids.setFluidInSlot(i, null);
                    }
                    else {
                        tickState[i] = false;
                    }
                }
            }

            if (configFluid != null) {
                if (tickState[i]) {
                    if (getFluidGrid() != null) {
                        FluidStack configCopy = configFluid.copy();
                        configCopy.amount = Math.min(8000, getLeftSpace(storedFluid, configCopy));
                        IAEFluidStack fluidDrain = getFluidGrid().extractItems(AEFluidStack.create(configCopy), Actionable.MODULATE, ownActionSource);
                        if (fluidDrain != null && fluidDrain.getStackSize() != 0) {
                            invFluids.fill(i, fluidDrain.getFluidStack(), true);
                        } else {
                            tickState[i] = false;
                        }
                    }
                }
                else if (tickCount[i] % 40 == 0) {
                    if (getFluidGrid() != null) {
                        FluidStack configCopy = configFluid.copy();
                        configCopy.amount = Math.min(8000, getLeftSpace(storedFluid, configCopy));
                        IAEFluidStack fluidDrain = getFluidGrid().extractItems(AEFluidStack.create(configCopy), Actionable.MODULATE, ownActionSource);
                        if (fluidDrain != null && fluidDrain.getStackSize() != 0) {
                            invFluids.fill(i, fluidDrain.getFluidStack(), true);
                            tickState[i] = true;
                        }
                    }
                }
            }

            tickCount[i] ++;
            if (tickCount[i] > 500) {
                tickCount[i] = 1;
            }
        }
    }

    private int getLeftSpace(FluidStack stored, FluidStack req) {
        if (stored == null) {
            return 16000;
        }
        if (!Util.areFluidsEqual(stored, req)) {
            return 0;
        }
        return 16000 - stored.amount;
    }

}
