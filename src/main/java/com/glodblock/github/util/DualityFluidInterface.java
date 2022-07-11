package com.glodblock.github.util;

import appeng.api.config.Actionable;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.core.settings.TickRates;
import appeng.helpers.IInterfaceHost;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.storage.MEMonitorPassThrough;
import appeng.me.storage.NullInventory;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import com.glodblock.github.inventory.AEFluidInventory;
import com.glodblock.github.inventory.IAEFluidInventory;
import com.glodblock.github.inventory.IAEFluidTank;
import com.glodblock.github.inventory.MEMonitorIFluidHandler;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import java.util.Objects;

public class DualityFluidInterface implements IGridTickable, IStorageMonitorable, IAEFluidInventory, IUpgradeableHost, IConfigManagerHost, IFluidHandler {

    public static final int NUMBER_OF_TANKS = 6;
    public static final long TANK_CAPACITY = 16000;
    private final ConfigManager cm = new ConfigManager(this);
    private final AENetworkProxy gridProxy;
    private final IInterfaceHost iHost;
    private final BaseActionSource mySource;
    private boolean hasConfig = false;
    private final AEFluidInventory tanks = new AEFluidInventory(this, NUMBER_OF_TANKS, (int) TANK_CAPACITY);
    private final AEFluidInventory config = new AEFluidInventory( this, NUMBER_OF_TANKS, Integer.MAX_VALUE );
    private final IAEFluidStack[] requireWork;
    private int isWorking = -1;
    private final MEMonitorPassThrough<IAEItemStack> items = new MEMonitorPassThrough<IAEItemStack>( new NullInventory<IAEItemStack>(), StorageChannel.ITEMS );
    private final MEMonitorPassThrough<IAEFluidStack> fluids = new MEMonitorPassThrough<IAEFluidStack>(new NullInventory<>(), StorageChannel.FLUIDS );
    private boolean resetConfigCache = true;

    public DualityFluidInterface(final AENetworkProxy networkProxy, final IInterfaceHost ih) {
        this.gridProxy = networkProxy;
        this.gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
        this.iHost = ih;
        this.mySource = new MachineSource(this.iHost);
        this.fluids.setChangeSource(this.mySource);
        this.items.setChangeSource(this.mySource);
        this.requireWork = new IAEFluidStack[6];

        for(int i = 0; i < 6; ++i) {
            this.requireWork[i] = null;
        }
    }

    public IAEFluidStack getStandardFluid(IAEFluidStack fluid) {
        if (fluid == null) {
            return null;
        }
        else {
            return AEFluidStack.create(new FluidStack(fluid.getFluid(), 8000));
        }
    }

    public IAEFluidStack getStandardFluid(FluidStack fluid) {
        if (fluid == null) {
            return null;
        }
        else {
            return AEFluidStack.create(new FluidStack(fluid.getFluid(), 8000));
        }
    }

    public void onChannelStateChange(final MENetworkChannelsChanged c) {
        this.notifyNeighbors();
    }

    public void onPowerStateChange(final MENetworkPowerStatusChange c) {
        this.notifyNeighbors();
    }

    public void gridChanged() {
        try {
            this.items.setInternal(this.gridProxy.getStorage().getItemInventory());
            this.fluids.setInternal(this.gridProxy.getStorage().getFluidInventory());
        } catch (GridAccessException var2) {
            this.items.setInternal(new NullInventory<>());
            this.fluids.setInternal(new NullInventory<>());
        }
        this.notifyNeighbors();
    }

    public void writeToNBT(NBTTagCompound data) {
        this.tanks.writeToNBT(data, "storage");
        this.config.writeToNBT(data, "config");
    }

    public void readFromNBT(NBTTagCompound data) {
        this.config.readFromNBT(data, "config");
        this.tanks.readFromNBT(data, "storage");
        this.readConfig();
    }

    public AEFluidInventory getConfig() {
        return this.config;
    }

    public AEFluidInventory getTanks() {
        return this.tanks;
    }

    private IMEMonitor<IAEFluidStack> getFluidGrid() {
        try {
            return gridProxy.getGrid().<IStorageGrid>getCache(IStorageGrid.class).getFluidInventory();
        } catch (GridAccessException e) {
            return null;
        }
    }

    @Override
    public int getInstalledUpgrades(Upgrades u) {
        return 0;
    }

    @Override
    public TileEntity getTile() {
        return (TileEntity) (this.iHost instanceof TileEntity ? this.iHost : null);
    }

    @Override
    public IInventory getInventoryByName(String name) {
        return null;
    }

    public IFluidHandler getFluidInventoryByName(String name) {
        return name.equals("config") ? this.config : null;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.Interface.getMin(), TickRates.Interface.getMax(), !this.hasWorkToDo(), true);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall) {
        if (!this.gridProxy.isActive()) {
            return TickRateModulation.SLEEP;
        } else {
            boolean couldDoWork = this.updateStorage();
            return this.hasWorkToDo() ? (couldDoWork ? TickRateModulation.URGENT : TickRateModulation.SLOWER) : TickRateModulation.SLEEP;
        }
    }

    @Override
    public IMEMonitor<IAEItemStack> getItemInventory() {
        if( this.hasConfig )
        {
            return null;
        }
        return this.items;
    }

    @Override
    public IMEMonitor<IAEFluidStack> getFluidInventory() {
        if( this.hasConfig )
        {
            if (this.resetConfigCache) {
                this.resetConfigCache = false;
                return new InterfaceInventory(this);
            }
        }
        return this.fluids;
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.cm;
    }

    @Override
    public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue) {

    }

    @Override
    public void onFluidInventoryChanged(IAEFluidTank inventory, int slot) {
        if (this.isWorking != slot) {
            boolean had;
            if (inventory == this.config) {
                had = this.hasConfig;
                this.readConfig();
                if (had != this.hasConfig) {
                    this.resetConfigCache = true;
                    this.notifyNeighbors();
                }
            } else if (inventory == this.tanks) {
                this.saveChanges();
                had = this.hasWorkToDo();
                this.updatePlan(slot);
                boolean now = this.hasWorkToDo();
                if (had != now) {
                    try {
                        if (now) {
                            this.gridProxy.getTick().alertDevice(this.gridProxy.getNode());
                        } else {
                            this.gridProxy.getTick().sleepDevice(this.gridProxy.getNode());
                        }
                    } catch (GridAccessException ignored) {
                    }
                }
            }

        }
    }

    private boolean hasWorkToDo() {
        for (IAEFluidStack requiredWork : this.requireWork) {
            if (requiredWork != null) {
                return true;
            }
        }
        return false;
    }

    private boolean updateStorage() {
        boolean didSomething = false;
        for(int x = 0; x < 6; ++x) {
            if (this.requireWork[x] != null) {
                didSomething = this.usePlan(x) || didSomething;
            }
        }
        return didSomething;
    }

    private boolean usePlan(int slot) {
        IAEFluidStack work = this.requireWork[slot];
        this.isWorking = slot;
        boolean changed = false;

        IMEInventory<IAEFluidStack> dest = getFluidGrid();
        if (dest == null) {
            this.isWorking = -1;
            return false;
        }
        IAEFluidStack toStore;
        if (work.getStackSize() > 0L) {
            if ((long)this.tanks.fill(slot, work.getFluidStack(), false) != work.getStackSize()) {
                changed = true;
            } else if (Objects.requireNonNull(getFluidGrid()).getStorageList().findPrecise(work) != null) {
                toStore = dest.extractItems(work, Actionable.MODULATE, this.mySource);
                if (toStore != null) {
                    changed = true;
                    int filled = this.tanks.fill(slot, toStore.getFluidStack(), true);
                    if ((long)filled != toStore.getStackSize()) {
                        throw new IllegalStateException("bad attempt at managing tanks. ( fill )");
                    }
                }
            }
        } else if (work.getStackSize() < 0L) {
            toStore = work.copy();
            toStore.setStackSize(-toStore.getStackSize());
            FluidStack canExtract = this.tanks.drain(slot, toStore.getFluidStack(), false);
            if (canExtract != null && (long)canExtract.amount == toStore.getStackSize()) {
                IAEFluidStack notStored = dest.injectItems(toStore, Actionable.MODULATE, this.mySource);;
                toStore.setStackSize(toStore.getStackSize() - (notStored == null ? 0L : notStored.getStackSize()));
                if (toStore.getStackSize() > 0L) {
                    changed = true;
                    FluidStack removed = this.tanks.drain(slot, toStore.getFluidStack(), true);
                    if (removed == null || toStore.getStackSize() != (long)removed.amount) {
                        throw new IllegalStateException("bad attempt at managing tanks. ( drain )");
                    }
                }
            } else {
                changed = true;
            }
        }

        if (changed) {
            this.updatePlan(slot);
        }

        this.isWorking = -1;
        return changed;
    }

    private void updatePlan(int slot) {
        IAEFluidStack req = this.config.getFluidInSlot(slot);
        IAEFluidStack stored = this.tanks.getFluidInSlot(slot);
        IAEFluidStack work;
        if (req == null && stored != null && stored.getStackSize() > 0L) {
            work = stored.copy();
            this.requireWork[slot] = work.setStackSize(-work.getStackSize());
        } else {
            if (req != null) {
                if (stored == null || stored.getStackSize() == 0L) {
                    this.requireWork[slot] = req.copy();
                    this.requireWork[slot].setStackSize(TANK_CAPACITY);
                    return;
                }

                if (!req.equals(stored)) {
                    work = stored.copy();
                    this.requireWork[slot] = work.setStackSize(-work.getStackSize());
                    return;
                }

                if (stored.getStackSize() < TANK_CAPACITY) {
                    this.requireWork[slot] = req.copy();
                    this.requireWork[slot].setStackSize(TANK_CAPACITY - stored.getStackSize());
                    return;
                }
            }
            this.requireWork[slot] = null;
        }
    }

    public void notifyNeighbors() {
        if (this.gridProxy.isActive()) {
            try {
                this.gridProxy.getTick().wakeDevice(this.gridProxy.getNode());
            } catch (GridAccessException ignored) {
            }
        }
        TileEntity te = this.iHost.getTileEntity();
        if (te != null && te.getWorldObj() != null) {
            Platform.notifyBlocksOfNeighbors(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord);
        }
    }

    public void saveChanges() {
        this.iHost.saveChanges();
    }

    private void readConfig() {
        this.hasConfig = false;

        for(int i = 0; i < this.config.getSlots(); ++i) {
            if (this.config.getFluidInSlot(i) != null) {
                this.hasConfig = true;
                break;
            }
        }

        boolean had = this.hasWorkToDo();

        for(int x = 0; x < 6; ++x) {
            this.updatePlan(x);
        }

        boolean has = this.hasWorkToDo();
        if (had != has) {
            try {
                if (has) {
                    this.gridProxy.getTick().alertDevice(this.gridProxy.getNode());
                } else {
                    this.gridProxy.getTick().sleepDevice(this.gridProxy.getNode());
                }
            } catch (GridAccessException ignored) {
            }
        }

        this.notifyNeighbors();
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        IMEMonitor<IAEFluidStack> fluidGrid = getFluidGrid();
        if (fluidGrid == null || resource == null)
            return 0;
        int ori = resource.amount;
        IAEFluidStack remove;
        if (doFill) {
            remove = fluidGrid.injectItems(AEFluidStack.create(resource), Actionable.MODULATE, this.mySource);
        } else {
            remove = fluidGrid.injectItems(AEFluidStack.create(resource), Actionable.SIMULATE, this.mySource);
        }
        return remove == null ? ori : (int) (ori - remove.getStackSize());
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        return this.tanks.drain(from, resource, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        return this.tanks.drain(from, maxDrain, doDrain);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return true;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return true;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return this.tanks.getTankInfo(from);
    }

    private static class InterfaceInventory extends MEMonitorIFluidHandler {
        InterfaceInventory(DualityFluidInterface tileInterface) {
            super(tileInterface.tanks);
        }
    }

}
