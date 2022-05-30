package com.glodblock.github.common.tile;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.GridFlags;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AELog;
import appeng.fluids.util.AEFluidStack;
import appeng.helpers.MultiCraftingTracker;
import appeng.helpers.Reflected;
import appeng.me.GridAccessException;
import appeng.me.helpers.MachineSource;
import appeng.tile.grid.AENetworkTile;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.util.DummyInvAdaptor;
import com.google.common.collect.ImmutableSet;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.io.IOException;

public class TileFluidLevelMaintainer extends AENetworkTile implements IStackWatcherHost, ICraftingRequester, IAEAppEngInventory {

    public static final int MAX_FLUID = 5;

    private final AppEngInternalAEInventory config = new AppEngInternalAEInventory(this, MAX_FLUID) {
        @Override
        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE;
        }

        @Override
        protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
            return Integer.MAX_VALUE;
        }
    };
    private final MultiCraftingTracker craftingTracker = new MultiCraftingTracker(this, MAX_FLUID);
    private final long[] request = new long[MAX_FLUID];
    private final long[] reportValue = new long[MAX_FLUID];
    private IStackWatcher watcher;
    private final IActionSource source;

    @Reflected
    public TileFluidLevelMaintainer() {
        getProxy().setIdlePowerUsage(2D);
        getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
        this.source = new MachineSource( this );
    }

    public void setConfig(int id, IAEItemStack item) {
        if (id < 0 || id > MAX_FLUID) {
            return;
        }
        this.config.setStackInSlot(id, item.getDefinition());
    }

    public void setConfig(int id, int size) {
        if (id < 0 || id > MAX_FLUID || this.config.getStackInSlot(id).isEmpty()) {
            return;
        }
        ItemStack packet = this.config.getStackInSlot(id);
        FluidStack copy = ItemFluidPacket.getFluidStack(packet);
        if (copy != null)
            copy.amount = size;
        this.config.setStackInSlot(id, ItemFluidPacket.newStack(copy));
    }

    public void setRequest(int id, long amount) {
        if (id < 0 || id > MAX_FLUID) {
            return;
        }
        this.request[id] = amount;
    }

    public IItemHandler getInventoryHandler() {
        return this.config;
    }

    public long[] getRequest() {
        return this.request;
    }

    @Override
    protected boolean readFromStream(ByteBuf data) throws IOException {
        boolean changed = super.readFromStream(data);
        for (int i = 0; i < config.getSlots(); i++) {
            ItemStack stack = ByteBufUtils.readItemStack(data);
            if (!ItemStack.areItemStacksEqual(stack, config.getStackInSlot(i))) {
                config.setStackInSlot(i, stack);
                changed = true;
            }
        }
        return changed;
    }

    @Override
    protected void writeToStream(ByteBuf data) throws IOException {
        super.writeToStream(data);
        for (int i = 0; i < config.getSlots(); i++) {
            ByteBufUtils.writeItemStack(data, config.getStackInSlot(i));
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        for (int i = 0; i < MAX_FLUID; i ++) {
            request[i] = data.getLong("req" + i);
            reportValue[i] = data.getLong("rep" + i);
        }
        config.readFromNBT(data, "configX");
        craftingTracker.readFromNBT(data);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        for (int i = 0; i < MAX_FLUID; i ++) {
            data.setLong("req" + i, request[i]);
            data.setLong("rep" + i, reportValue[i]);
        }
        config.writeToNBT(data, "configX");
        craftingTracker.writeToNBT(data);
        return data;
    }

    private IMEMonitor<IAEFluidStack> getFluidMonitor() {
        return getProxy().getNode().getGrid().<IStorageGrid>getCache(IStorageGrid.class)
                .getInventory(AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class));
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return this.craftingTracker.getRequestedJobs();
    }

    @Override
    public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack items, Actionable mode) {
        final IMEMonitor<IAEFluidStack> fluidGrid = getFluidMonitor();

        try {

            if (fluidGrid != null && this.getProxy().isActive()) {
                final IEnergyGrid energy = this.getProxy().getEnergy();
                final double power = items.getStackSize() / 1000D;

                if( energy.extractAEPower( power, mode, PowerMultiplier.CONFIG ) > power - 0.01 ) {
                    ItemStack inputStack = items.getCachedItemStack( items.getStackSize() );

                    FluidStack inputFluid = ItemFluidDrop.getFluidStack(inputStack);
                    IAEFluidStack remaining;

                    if( mode == Actionable.SIMULATE )
                    {
                        remaining = fluidGrid.injectItems(AEFluidStack.fromFluidStack(inputFluid), Actionable.SIMULATE, source );
                        items.setCachedItemStack( inputStack );
                    }
                    else
                    {
                        remaining = fluidGrid.injectItems(AEFluidStack.fromFluidStack(inputFluid), Actionable.MODULATE, source );
                        if( remaining == null || remaining.getStackSize() <= 0 )
                        {
                            ItemStack tmp = ItemFluidDrop.newAeStack(remaining) != null ? ItemFluidDrop.newAeStack(remaining).getDefinition() : null;
                            items.setCachedItemStack( tmp );
                        }
                    }

                    if( ItemFluidDrop.newStack(remaining != null ? remaining.getFluidStack() : null) == inputStack )
                    {
                        return items;
                    }

                    return ItemFluidDrop.newAeStack(remaining);
                }
            }

        } catch( final GridAccessException e ) {
            AELog.debug( e );
        }

        return items;
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        this.craftingTracker.jobStateChange( link );
    }

    @Override
    public void updateWatcher(IStackWatcher iWatcher) {
        this.watcher = iWatcher;
        configureWatchers();
    }

    public void configureWatchers() {
        if (this.watcher != null) {
            watcher.reset();
            for (int i = 0; i < MAX_FLUID; i ++ ) {
                if (this.config.getAEStackInSlot(i) != null && !this.config.getAEStackInSlot(i).getDefinition().isEmpty()) {
                    IAEItemStack packet = this.config.getAEStackInSlot(i);
                    watcher.add(ItemFluidDrop.newAeStack(ItemFluidPacket.getFluidStack(packet)));
                }
            }
        }
    }

    @Override
    public void onStackChange(IItemList<?> iItemList, IAEStack<?> fullStack, IAEStack<?> diffStack, IActionSource src, IStorageChannel<?> chan) {
        if (chan == AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class) && fullStack instanceof IAEFluidStack) {
            for (int i = 0; i < MAX_FLUID; i ++) {
                try {
                    IAEItemStack packet = this.config.getAEStackInSlot(i);
                    IAEFluidStack fluid = AEFluidStack.fromFluidStack(ItemFluidPacket.getFluidStack(packet));
                    if (fluid != null && fluid.getFluidStack() != null
                            && fluid.getFluidStack().isFluidEqual(((IAEFluidStack)diffStack).getFluidStack())) {
                        this.reportValue[i] = fullStack.getStackSize();
                        IAEItemStack drops = ItemFluidDrop.newAeStack(fluid);
                        if (drops != null && this.reportValue[i] < drops.getStackSize()) {
                            this.craftingTracker.handleCrafting(i, request[i], drops, DummyInvAdaptor.INSTANCE, this.world, this.getProxy().getGrid(), this.getProxy().getCrafting(), this.source );
                        }
                    }
                } catch (GridAccessException e) {
                    // Ignore
                }
            }
        }
    }

    @Override
    public void onChangeInventory(IItemHandler iItemHandler, int i, InvOperation invOperation, ItemStack itemStack, ItemStack itemStack1) {
        markForUpdate();
    }

}
