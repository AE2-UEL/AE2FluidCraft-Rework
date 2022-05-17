package com.glodblock.github.common.tile;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.*;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.*;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.helpers.Reflected;
import appeng.me.GridAccessException;
import appeng.me.cache.CraftingGridCache;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.MEInventoryHandler;
import appeng.tile.grid.AENetworkTile;
import appeng.util.Platform;
import com.glodblock.github.common.item.ItemFluidDrop;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TileFluidDiscretizer extends AENetworkTile implements ICellContainer {

    private final FluidDiscretizingInventory fluidDropInv = new FluidDiscretizingInventory();
    private final FluidCraftingInventory fluidCraftInv = new FluidCraftingInventory();
    private final IActionSource ownActionSource = new MachineSource(this);
    private boolean prevActiveState = false;

    @Reflected
    public TileFluidDiscretizer() {
        getProxy().setIdlePowerUsage(3D);
        getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    @Override
    public boolean canBeRotated() {
        return false;
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<IMEInventoryHandler> getCellArray(IStorageChannel<?> channel) {
        if (getProxy().isActive()) {
            if (channel == AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class)) {
                return Collections.singletonList(fluidDropInv.invHandler);
            } else if (channel == AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class)) {
                return Collections.singletonList(fluidCraftInv.invHandler);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void saveChanges(@Nullable ICellInventory<?> cellInventory) {
        world.markChunkDirty(pos, this); // optimization, i guess?
    }

    @Override
    public void gridChanged() {
        IMEMonitor<IAEFluidStack> fluidGrid = getFluidGrid();
        if (fluidGrid != null) {
            fluidGrid.addListener(fluidDropInv, fluidGrid);
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

    @Override
    public void blinkCell(int slot) {
        // NO-OP
    }

    @Nullable
    private IEnergyGrid getEnergyGrid() {
        try {
            return getProxy().getGrid().getCache(IEnergyGrid.class);
        } catch (GridAccessException e) {
            return null;
        }
    }

    @Nullable
    private IMEMonitor<IAEFluidStack> getFluidGrid() {
        try {
            return getProxy().getGrid().<IStorageGrid>getCache(IStorageGrid.class)
                    .getInventory(AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class));
        } catch (GridAccessException e) {
            return null;
        }
    }

    private class FluidDiscretizingInventory implements IMEInventory<IAEItemStack>, IMEMonitorHandlerReceiver<IAEFluidStack> {

        private final MEInventoryHandler<IAEItemStack> invHandler = new MEInventoryHandler<>(this, getChannel());
        @Nullable
        private List<IAEItemStack> itemCache = null;

        FluidDiscretizingInventory() {
            invHandler.setPriority(Integer.MAX_VALUE);
        }

        @SuppressWarnings("DuplicatedCode")
        @Nullable
        @Override
        public IAEItemStack extractItems(IAEItemStack request, Actionable mode, IActionSource src) {
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
            return ItemFluidDrop.newAeStack(Platform.poweredExtraction(energyGrid, fluidGrid, fluidStack, ownActionSource, mode));
        }

        @SuppressWarnings("DuplicatedCode")
        @Nullable
        @Override
        public IAEItemStack injectItems(IAEItemStack input, Actionable type, IActionSource src) {
            IAEFluidStack fluidStack = ItemFluidDrop.getAeFluidStack(input);
            if (fluidStack == null) {
                return input;
            }
            IMEMonitor<IAEFluidStack> fluidGrid = getFluidGrid();
            if (fluidGrid == null) {
                return input;
            }
            IEnergyGrid energyGrid = getEnergyGrid();
            if (energyGrid == null) {
                return input;
            }
            return ItemFluidDrop.newAeStack(Platform.poweredInsert(energyGrid, fluidGrid, fluidStack, ownActionSource, type));
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
        public boolean isValid(Object verificationToken) {
            IMEMonitor<IAEFluidStack> fluidGrid = getFluidGrid();
            return fluidGrid != null && fluidGrid == verificationToken;
        }

        @Override
        public void postChange(IBaseMonitor<IAEFluidStack> monitor, Iterable<IAEFluidStack> change, IActionSource actionSource) {
            itemCache = null;
            try {
                List<IAEItemStack> mappedChanges = new ArrayList<>();
                for (IAEFluidStack fluidStack : change) {
                    boolean isNg = false;
                    if (fluidStack.getStackSize() < 0) {
                        isNg = true;
                        fluidStack.setStackSize( - fluidStack.getStackSize() );
                    }
                    IAEItemStack itemStack = ItemFluidDrop.newAeStack(fluidStack);
                    if (itemStack != null) {
                        if (isNg) itemStack.setStackSize( - itemStack.getStackSize() );
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

        @Override
        public IStorageChannel<IAEItemStack> getChannel() {
            return AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
        }

    }

    private class FluidCraftingInventory implements IMEInventory<IAEFluidStack> {

        private final MEInventoryHandler<IAEFluidStack> invHandler = new MEInventoryHandler<>(this, getChannel());

        FluidCraftingInventory() {
            invHandler.setPriority(Integer.MAX_VALUE);
        }

        @Nullable
        @Override
        public IAEFluidStack injectItems(IAEFluidStack input, Actionable type, IActionSource src) {
            ICraftingGrid craftingGrid;
            try {
                craftingGrid = getProxy().getGrid().getCache(ICraftingGrid.class);
            } catch (GridAccessException e) {
                return null;
            }
            if (craftingGrid instanceof CraftingGridCache) {
                IAEItemStack remaining = ((CraftingGridCache)craftingGrid).injectItems(
                        ItemFluidDrop.newAeStack(input), type, ownActionSource);
                if (remaining != null) {
                    return ItemFluidDrop.getAeFluidStack(remaining);
                }
            }
            return null;
        }

        @Nullable
        @Override
        public IAEFluidStack extractItems(IAEFluidStack request, Actionable mode, IActionSource src) {
            return null;
        }

        @Override
        public IItemList<IAEFluidStack> getAvailableItems(IItemList<IAEFluidStack> out) {
            return out;
        }

        @Override
        public IStorageChannel<IAEFluidStack> getChannel() {
            return AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
        }

    }

}
