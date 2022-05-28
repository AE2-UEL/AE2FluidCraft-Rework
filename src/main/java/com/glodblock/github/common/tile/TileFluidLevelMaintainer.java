package com.glodblock.github.common.tile;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.helpers.MultiCraftingTracker;
import appeng.helpers.Reflected;
import appeng.tile.grid.AENetworkTile;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import com.google.common.collect.ImmutableSet;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class TileFluidLevelMaintainer extends AENetworkTile implements IStackWatcherHost, ICraftingRequester, IAEAppEngInventory {

    public static final int MAX_FLUID = 5;

    private final AppEngInternalAEInventory config = new AppEngInternalAEInventory(this, MAX_FLUID);
    private final MultiCraftingTracker craftingTracker = new MultiCraftingTracker(this, MAX_FLUID);
    private IStackWatcher watcher;

    @Reflected
    public TileFluidLevelMaintainer() {
        getProxy().setIdlePowerUsage(2D);
        getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return null;
    }

    @Override
    public IAEItemStack injectCraftedItems(ICraftingLink iCraftingLink, IAEItemStack iaeItemStack, Actionable actionable) {
        return null;
    }

    @Override
    public void jobStateChange(ICraftingLink iCraftingLink) {

    }

    @Override
    public void updateWatcher(IStackWatcher iStackWatcher) {

    }

    @Override
    public void onStackChange(IItemList<?> iItemList, IAEStack<?> iaeStack, IAEStack<?> iaeStack1, IActionSource iActionSource, IStorageChannel<?> iStorageChannel) {

    }

    @Override
    public void onChangeInventory(IItemHandler iItemHandler, int i, InvOperation invOperation, ItemStack itemStack, ItemStack itemStack1) {
        markForUpdate();
    }
}
