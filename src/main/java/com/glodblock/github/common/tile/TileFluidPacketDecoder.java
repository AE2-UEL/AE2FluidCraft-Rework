package com.glodblock.github.common.tile;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.helpers.Reflected;
import appeng.me.GridAccessException;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import com.glodblock.github.common.item.ItemFluidPacket;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

public class TileFluidPacketDecoder extends AENetworkTile implements IGridTickable, IAEAppEngInventory {

    private final AppEngInternalInventory inventory = new AppEngInternalInventory(this, 1);
    private final BaseActionSource ownActionSource = new MachineSource(this);

    @Reflected
    public TileFluidPacketDecoder() {
        getProxy().setIdlePowerUsage(1D);
        getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    public IInventory getInventory() {
        return inventory;
    }

    @Override
    public boolean canBeRotated() {
        return false;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(5, 120, false, true);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        ItemStack stack = inventory.getStackInSlot(0);
        if (stack == null || !(stack.getItem() instanceof ItemFluidPacket)) {
            return TickRateModulation.SLEEP;
        }
        FluidStack fluid = ItemFluidPacket.getFluidStack(stack);
        if (fluid == null || fluid.amount <= 0) {
            inventory.setInventorySlotContents(0, null);
            return TickRateModulation.SLEEP;
        }
        IAEFluidStack aeFluid = AEFluidStack.create(fluid);
        IEnergyGrid energyGrid = node.getGrid().getCache(IEnergyGrid.class);
        IMEMonitor<IAEFluidStack> fluidGrid = node.getGrid().<IStorageGrid>getCache(IStorageGrid.class).getFluidInventory();
        IAEFluidStack remaining = Platform.poweredInsert(energyGrid, fluidGrid, aeFluid, ownActionSource);
        if (remaining != null) {
            if (remaining.getStackSize() == aeFluid.getStackSize()) {
                return TickRateModulation.SLOWER;
            }
            inventory.setInventorySlotContents(0, ItemFluidPacket.newStack(remaining.getFluidStack()));
            return TickRateModulation.FASTER;
        } else {
            inventory.setInventorySlotContents(0, null);
            return TickRateModulation.SLEEP;
        }
    }

    @Override
    public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack) {
        try {
            getProxy().getTick().alertDevice(getProxy().getNode());
        } catch (GridAccessException e) {
            // NO-OP
        }
    }

    @TileEvent( TileEventType.WORLD_NBT_WRITE )
    public NBTTagCompound writeToNBTEvent(NBTTagCompound data) {
        inventory.writeToNBT(data, "Inventory");
        return data;
    }

    @TileEvent( TileEventType.WORLD_NBT_READ )
    public void readFromNBTEvent(NBTTagCompound data) {
        inventory.readFromNBT(data, "Inventory");
    }

}
