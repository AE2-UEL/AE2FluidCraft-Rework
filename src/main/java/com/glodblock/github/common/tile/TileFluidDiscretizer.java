package com.glodblock.github.common.tile;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.*;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.*;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.helpers.IPriorityHost;
import appeng.me.GridAccessException;
import appeng.me.cache.CraftingGridCache;
import appeng.me.storage.MEInventoryHandler;
import appeng.tile.grid.AENetworkTile;
import com.glodblock.github.common.item.ItemFluidDrop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TileFluidDiscretizer extends AENetworkTile implements IPriorityHost, ICellContainer {

    private final BaseActionSource ownActionSource = new MachineSource(this);
    private final FluidDiscretizingInventory fluidDropInv = new FluidDiscretizingInventory();
    private final FluidCraftingInventory fluidCraftInv = new FluidCraftingInventory();
    private boolean prevActiveState = false;

    public TileFluidDiscretizer() {
        super();
        getProxy().setIdlePowerUsage(3D);
        getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    @Override
    public boolean canBeRotated() {
        return false;
        //based
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List<IMEInventoryHandler> getCellArray(StorageChannel channel) {
        if (getProxy().isActive()) {
            if (channel == StorageChannel.ITEMS) {
                return Collections.singletonList(fluidDropInv.invHandler);
            } else if (channel == StorageChannel.FLUIDS) {
                return Collections.singletonList(fluidCraftInv.invHandler);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void setPriority(int newValue) {
        //do nothing
    }

    @Override
    public void blinkCell(int slot) {
        //do nothing
    }

    @Override
    public void gridChanged() {
        IMEMonitor<IAEFluidStack> fluidGrid = getFluidGrid();
        if (fluidGrid != null) {
            fluidGrid.addListener(fluidDropInv, fluidGrid);
        }
    }

    @Override
    public void saveChanges(IMEInventory cellInventory) {
        markDirty();
    }

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

    private void updateState() {
        boolean isActive = getProxy().isActive();
        if (isActive != prevActiveState) {
            prevActiveState = isActive;
            try {
                getProxy().getGrid().postEvent(new MENetworkCellArrayUpdate());
            } catch (GridAccessException e) {
                // NO-OP
            }
        }
    }

    @MENetworkEventSubscribe
    public void onPowerUpdate(MENetworkPowerStatusChange event) {
        updateState();
    }

    @MENetworkEventSubscribe
    public void onChannelUpdate(MENetworkChannelsChanged event) {
        updateState();
    }

    @MENetworkEventSubscribe
    public void onStorageUpdate(MENetworkStorageEvent event) {
        updateState();
    }

    private class FluidDiscretizingInventory implements IMEInventory<IAEItemStack>, IMEMonitorHandlerReceiver<IAEFluidStack> {

        private final MEInventoryHandler<IAEItemStack> invHandler = new MEInventoryHandler<>(this, getChannel());
        private List<IAEItemStack> itemCache = null;

        FluidDiscretizingInventory() {
            invHandler.setPriority(Integer.MAX_VALUE);
        }

        @Override
        public IAEItemStack injectItems(IAEItemStack request, Actionable type, BaseActionSource src) {
            IAEFluidStack fluidStack = ItemFluidDrop.getAeFluidStack(request);
            if (fluidStack == null) {
                return request;
            }
            IMEMonitor<IAEFluidStack> fluidGrid = getFluidGrid();
            if (fluidGrid == null) {
                return request;
            }
            IEnergyGrid energyGrid = getEnergyGrid();
            if (energyGrid == null) {
                return request;
            }
            if (type == Actionable.SIMULATE) {
                return ItemFluidDrop.newAeStack(fluidGrid.injectItems(fluidStack.copy(), Actionable.SIMULATE, src));
            }
            else {
                return ItemFluidDrop.newAeStack(fluidGrid.injectItems(fluidStack.copy(), Actionable.MODULATE, src));
            }
        }

        @Override
        public IAEItemStack extractItems(IAEItemStack request, Actionable mode, BaseActionSource src) {
            IAEFluidStack fluidStack = ItemFluidDrop.getAeFluidStack(request);
            if (fluidStack == null) {
                return null;
            }
            IMEMonitor<IAEFluidStack> fluidGrid = getFluidGrid();
            if (fluidGrid == null) {
                return null;
            }
            IEnergyGrid energyGrid = getEnergyGrid();
            if (energyGrid == null) {
                return null;
            }
            if (mode == Actionable.SIMULATE) {
                return ItemFluidDrop.newAeStack(fluidGrid.extractItems(fluidStack.copy(), Actionable.SIMULATE, src));
            }
            else {
                return ItemFluidDrop.newAeStack(fluidGrid.extractItems(fluidStack.copy(), Actionable.MODULATE, src));
            }
        }

        @Override
        public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> out) {
            if (itemCache == null) {
                itemCache = new ArrayList<>();
                IMEMonitor<IAEFluidStack> fluidGrid = getFluidGrid();
                if (fluidGrid != null) {
                    for (IAEFluidStack fluid : fluidGrid.getStorageList()) {
                        IAEItemStack stack = ItemFluidDrop.newAeStack(fluid);
                        if (stack != null) {
                            itemCache.add(stack);
                        }
                    }
                }
            }
            for (IAEItemStack stack : itemCache) {
                out.addStorage(stack);
            }
            return out;
        }

        @Override
        public StorageChannel getChannel() {
            return StorageChannel.ITEMS;
        }

        @Override
        public boolean isValid(Object verificationToken) {
            IMEMonitor<IAEFluidStack> fluidGrid = getFluidGrid();
            return fluidGrid != null && fluidGrid == verificationToken /*&& !conflict*/;
        }

        @Override
        public void postChange(IBaseMonitor<IAEFluidStack> monitor, Iterable<IAEFluidStack> change, BaseActionSource actionSource) {
            itemCache = null;
            try {
                List<IAEItemStack> mappedChanges = new ArrayList<>();
                for (IAEFluidStack fluidStack : change) {
                    IAEItemStack itemStack = ItemFluidDrop.newAeStack(fluidStack);
                    if (itemStack != null) {
                        mappedChanges.add(itemStack);
                    }
                }
                getProxy().getGrid().<IStorageGrid>getCache(IStorageGrid.class)
                    .postAlterationOfStoredItems(getChannel(), mappedChanges, ownActionSource);
            } catch (GridAccessException e) {
                // NO-OP
            }
        }

        @Override
        public void onListUpdate() {
            // NO-OP
        }
    }

    private class FluidCraftingInventory implements IMEInventory<IAEFluidStack> {

        private final MEInventoryHandler<IAEFluidStack> invHandler = new MEInventoryHandler<>(this, getChannel());

        FluidCraftingInventory() {
            invHandler.setPriority(Integer.MAX_VALUE);
        }

        @Override
        @SuppressWarnings("rawtypes")
        public IAEFluidStack injectItems(IAEFluidStack input, Actionable type, BaseActionSource src) {
            ICraftingGrid craftingGrid;
            try {
                craftingGrid = getProxy().getGrid().getCache(ICraftingGrid.class);
            } catch (GridAccessException e) {
                return null;
            }
            if (craftingGrid instanceof CraftingGridCache) {
                IAEStack remaining = ((CraftingGridCache)craftingGrid).injectItems(
                    ItemFluidDrop.newAeStack(input), type, ownActionSource);
                if (remaining instanceof IAEItemStack) {
                    return ItemFluidDrop.getAeFluidStack((IAEItemStack) remaining);
                }
            }
            return null;
        }

        @Override
        public IAEFluidStack extractItems(IAEFluidStack request, Actionable mode, BaseActionSource src) {
            return null;
        }

        @Override
        public IItemList<IAEFluidStack> getAvailableItems(IItemList<IAEFluidStack> out) {
            return out;
        }

        @Override
        public StorageChannel getChannel() {
            return StorageChannel.FLUIDS;
        }

    }

}
