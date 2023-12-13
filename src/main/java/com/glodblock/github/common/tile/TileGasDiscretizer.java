package com.glodblock.github.common.tile;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.events.MENetworkStorageEvent;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.helpers.Reflected;
import appeng.me.GridAccessException;
import appeng.me.cache.CraftingGridCache;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.MEInventoryHandler;
import appeng.tile.grid.AENetworkTile;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.integration.mek.FakeGases;
import com.glodblock.github.util.Util;
import com.the9grounds.aeadditions.api.gas.IAEGasStack;
import com.the9grounds.aeadditions.api.gas.IGasStorageChannel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class TileGasDiscretizer extends AENetworkTile implements ICellContainer {

    private final GasDiscretizingInventory gasDropInv = new GasDiscretizingInventory();
    private final GasCraftingInventory gasCraftInv = new GasCraftingInventory();
    private final IActionSource ownActionSource = new MachineSource(this);
    private boolean prevActiveState = false;

    @Reflected
    public TileGasDiscretizer() {
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
            if (channel == Util.getItemChannel()) {
                return Collections.singletonList(gasDropInv.invHandler);
            } else if (channel == getGasChannel()) {
                return Collections.singletonList(gasCraftInv.invHandler);
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
        IMEMonitor<IAEGasStack> gasGrid = getGasGrid();
        if (gasGrid != null) {
            gasGrid.addListener(gasDropInv, gasGrid);
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
    private IMEMonitor<IAEGasStack> getGasGrid() {
        try {
            return getProxy().getGrid().<IStorageGrid>getCache(IStorageGrid.class)
                    .getInventory(getGasChannel());
        } catch (GridAccessException e) {
            return null;
        }
    }

    private static IStorageChannel<IAEGasStack> getGasChannel() {
        return AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class);
    }

    private class GasDiscretizingInventory implements IMEInventory<IAEItemStack>, IMEMonitorHandlerReceiver<IAEGasStack> {

        private final MEInventoryHandler<IAEItemStack> invHandler = new MEInventoryHandler<>(this, getChannel());
        @Nullable
        private ObjectArrayList<IAEItemStack> itemCache = null;

        GasDiscretizingInventory() {
            invHandler.setPriority(Integer.MAX_VALUE);
        }

        @SuppressWarnings("DuplicatedCode")
        @Nullable
        @Override
        public IAEItemStack extractItems(IAEItemStack request, Actionable mode, IActionSource src) {
            Object gasStack = FakeItemRegister.getAEStack(request);
            if (!(gasStack instanceof IAEGasStack)) {
                return null;
            }
            IMEMonitor<IAEGasStack> gasGrid = getGasGrid();
            if (gasGrid == null) {
                return null;
            }
            IEnergyGrid energyGrid = getEnergyGrid();
            if (energyGrid == null) {
                return null;
            }
            System.out.print(gasStack + "\n");
            return FakeGases.packGas2AEDrops(gasGrid.extractItems((IAEGasStack) gasStack, mode, ownActionSource));
        }

        @SuppressWarnings("DuplicatedCode")
        @Nullable
        @Override
        public IAEItemStack injectItems(IAEItemStack input, Actionable type, IActionSource src) {
            Object gasStack = FakeItemRegister.getAEStack(input);
            if (!(gasStack instanceof IAEGasStack)) {
                return input;
            }
            IMEMonitor<IAEGasStack> gasGrid = getGasGrid();
            if (gasGrid == null) {
                return input;
            }
            IEnergyGrid energyGrid = getEnergyGrid();
            if (energyGrid == null) {
                return input;
            }
            return FakeGases.packGas2AEDrops(gasGrid.injectItems((IAEGasStack) gasStack, type, ownActionSource));
        }

        @Override
        public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> out) {
            if (itemCache == null) {
                itemCache = new ObjectArrayList<>();
                IMEMonitor<IAEGasStack> gasGrid = getGasGrid();
                if (gasGrid != null) {
                    for (IAEGasStack gas : gasGrid.getStorageList()) {
                        IAEItemStack stack = FakeGases.packGas2AEDrops(gas);
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
            IMEMonitor<IAEGasStack> gasGrid = getGasGrid();
            return gasGrid != null && gasGrid == verificationToken;
        }

        @Override
        public void postChange(IBaseMonitor<IAEGasStack> monitor, Iterable<IAEGasStack> change, IActionSource actionSource) {
            itemCache = null;
            try {
                ObjectArrayList<IAEItemStack> mappedChanges = new ObjectArrayList<>();
                for (IAEGasStack gasStack : change) {
                    boolean isNg = false;
                    if (gasStack.getStackSize() < 0) {
                        isNg = true;
                        gasStack.setStackSize( - gasStack.getStackSize() );
                    }
                    IAEItemStack itemStack = FakeGases.packGas2AEDrops(gasStack);
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
            return Util.getItemChannel();
        }

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private class GasCraftingInventory implements IMEInventory {

        private final MEInventoryHandler invHandler = new MEInventoryHandler<>(this, this.getChannel());

        GasCraftingInventory() {
            invHandler.setPriority(Integer.MAX_VALUE);
        }

        @Nullable
        @Override
        public IAEStack injectItems(IAEStack aeStack, Actionable type, IActionSource src) {
            if (!(aeStack instanceof IAEGasStack)) {
                return null;
            }
            IAEGasStack input = (IAEGasStack) aeStack;
            ICraftingGrid craftingGrid;
            try {
                craftingGrid = getProxy().getGrid().getCache(ICraftingGrid.class);
            } catch (GridAccessException e) {
                return null;
            }
            if (craftingGrid instanceof CraftingGridCache) {
                IAEItemStack remaining = ((CraftingGridCache)craftingGrid).injectItems(
                        FakeGases.packGas2AEDrops(input), type, ownActionSource);
                if (remaining != null) {
                    return FakeItemRegister.getAEStack(remaining);
                }
            }
            return null;
        }

        @Nullable
        @Override
        public IAEStack extractItems(IAEStack request, Actionable mode, IActionSource src) {
            return null;
        }

        @Override
        public IItemList getAvailableItems(IItemList out) {
            return out;
        }

        @Override
        public IStorageChannel<IAEGasStack> getChannel() {
            return getGasChannel();
        }

    }

}
