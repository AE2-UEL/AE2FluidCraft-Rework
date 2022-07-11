package com.glodblock.github.inventory;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.StorageFilter;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEFluidStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import java.util.*;

public class MEMonitorIFluidHandler implements IMEMonitor<IAEFluidStack> {
    private final IFluidHandler handler;
    private IItemList<IAEFluidStack> cache = AEApi.instance().storage().createFluidList();
    private final HashMap<IMEMonitorHandlerReceiver<IAEFluidStack>, Object> listeners = new HashMap<>();
    private BaseActionSource mySource;
    private StorageFilter mode;

    public MEMonitorIFluidHandler(IFluidHandler handler) {
        this.mode = StorageFilter.EXTRACTABLE_ONLY;
        this.handler = handler;
    }

    public void addListener(IMEMonitorHandlerReceiver<IAEFluidStack> l, Object verificationToken) {
        this.listeners.put(l, verificationToken);
    }

    public void removeListener(IMEMonitorHandlerReceiver<IAEFluidStack> l) {
        this.listeners.remove(l);
    }

    public IAEFluidStack injectItems(IAEFluidStack input, Actionable type, BaseActionSource src) {
        int filled = this.handler.fill(ForgeDirection.UNKNOWN, input.getFluidStack(), type == Actionable.MODULATE);
        if (filled == 0) {
            return input.copy();
        } else if ((long)filled == input.getStackSize()) {
            return null;
        } else {
            IAEFluidStack o = input.copy();
            o.setStackSize(input.getStackSize() - (long)filled);
            if (type == Actionable.MODULATE) {
                IAEFluidStack added = o.copy();
                this.cache.add(added);
                this.postDifference(Collections.singletonList(added));
                this.onTick();
            }
            return o;
        }
    }

    public IAEFluidStack extractItems(IAEFluidStack request, Actionable type, BaseActionSource src) {
        FluidStack removed = this.handler.drain(ForgeDirection.UNKNOWN, request.getFluidStack(), type == Actionable.MODULATE);
        if (removed != null && removed.amount != 0) {
            IAEFluidStack o = request.copy();
            o.setStackSize(removed.amount);
            if (type == Actionable.MODULATE) {
                IAEFluidStack cachedStack = this.cache.findPrecise(request);
                if (cachedStack != null) {
                    cachedStack.decStackSize(o.getStackSize());
                    this.postDifference(Collections.singletonList(o.copy().setStackSize(-o.getStackSize())));
                }
            }
            return o;
        } else {
            return null;
        }
    }

    @Override
    public StorageChannel getChannel()
    {
        return StorageChannel.FLUIDS;
    }

    //*Decompiled Stuff*//

    public TickRateModulation onTick() {
        boolean changed = false;
        List<IAEFluidStack> changes = new ArrayList<>();
        FluidTankInfo[] tankProperties = this.handler.getTankInfo(ForgeDirection.UNKNOWN);
        IItemList<IAEFluidStack> currentlyOnStorage = AEApi.instance().storage().createFluidList();

        for (FluidTankInfo tankProperty : tankProperties) {
            if (this.mode != StorageFilter.EXTRACTABLE_ONLY || this.handler.drain(ForgeDirection.UNKNOWN, 1, false) != null) {
                currentlyOnStorage.add(AEFluidStack.create(tankProperty.fluid));
            }
        }

        Iterator<?> var9 = this.cache.iterator();

        IAEFluidStack is;
        while(var9.hasNext()) {
            is = (IAEFluidStack)var9.next();
            is.setStackSize(-is.getStackSize());
        }

        var9 = currentlyOnStorage.iterator();

        while(var9.hasNext()) {
            is = (IAEFluidStack)var9.next();
            this.cache.add(is);
        }

        var9 = this.cache.iterator();

        while(var9.hasNext()) {
            is = (IAEFluidStack)var9.next();
            if (is.getStackSize() != 0L) {
                changes.add(is);
            }
        }

        this.cache = currentlyOnStorage;
        if (!changes.isEmpty()) {
            this.postDifference(changes);
            changed = true;
        }

        return changed ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
    }

    private void postDifference(Iterable<IAEFluidStack> a) {
        if (a != null) {
            Iterator i = this.listeners.entrySet().iterator();

            while(i.hasNext()) {
                Map.Entry<IMEMonitorHandlerReceiver<IAEFluidStack>, Object> l = (Map.Entry)i.next();
                IMEMonitorHandlerReceiver key = l.getKey();
                if (key.isValid(l.getValue())) {
                    key.postChange(this, a, this.getActionSource());
                } else {
                    i.remove();
                }
            }
        }

    }

    public AccessRestriction getAccess() {
        return AccessRestriction.READ_WRITE;
    }

    public boolean isPrioritized(IAEFluidStack input) {
        return false;
    }

    public boolean canAccept(IAEFluidStack input) {
        return true;
    }

    public int getPriority() {
        return 0;
    }

    public int getSlot() {
        return 0;
    }

    public boolean validForPass(int i) {
        return true;
    }

    public IItemList<IAEFluidStack> getAvailableItems(IItemList out) {
        Iterator var2 = this.cache.iterator();

        while(var2.hasNext()) {
            IAEFluidStack fs = (IAEFluidStack)var2.next();
            out.addStorage(fs);
        }

        return out;
    }

    public IItemList<IAEFluidStack> getStorageList() {
        return this.cache;
    }

    private StorageFilter getMode() {
        return this.mode;
    }

    public void setMode(StorageFilter mode) {
        this.mode = mode;
    }

    private BaseActionSource getActionSource() {
        return this.mySource;
    }

    public void setActionSource(BaseActionSource mySource) {
        this.mySource = mySource;
    }
}
