package com.glodblock.github.common.component;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.Upgrades;
import appeng.api.definitions.IMaterials;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.core.localization.PlayerMessages;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.fluids.util.AEFluidInventory;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.items.misc.ItemEncodedPattern;
import appeng.me.helpers.AENetworkProxy;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.SettingsFrom;
import appeng.util.inv.InvOperation;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class DualityDualInterface <H extends IInterfaceHost & IFluidInterfaceHost> implements ICapabilityProvider {

    private final DualityInterface itemDuality;
    private final DualityFluidInterface fluidDuality;

    public DualityDualInterface(AENetworkProxy networkProxy, H host) {
        this.itemDuality = new DualityInterface(networkProxy, host);
        this.fluidDuality = new DualityFluidInterface(networkProxy, host);
    }

    public DualityInterface getItemInterface() {
        return itemDuality;
    }

    public DualityFluidInterface getFluidInterface() {
        return fluidDuality;
    }

    public IConfigManager getConfigManager() {
        return itemDuality.getConfigManager(); // fluid interface has no meaningful config, so this is fine
    }

    public int getInstalledUpgrades(final Upgrades u) {
        return itemDuality.getInstalledUpgrades(u) + fluidDuality.getInstalledUpgrades(u);
    }

    public int getPriority() {
        return itemDuality.getPriority(); // both interfaces should always have the same priority
    }

    public void setPriority(final int newValue) {
        itemDuality.setPriority(newValue);
        fluidDuality.setPriority(newValue);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return itemDuality.hasCapability(capability, facing) || fluidDuality.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        T capInst = itemDuality.getCapability(capability, facing);
        return capInst != null ? capInst : fluidDuality.getCapability(capability, facing);
    }

    // dual behaviour

    public void initialize() {
        itemDuality.initialize();
    }

    public TickingRequest getTickingRequest(IGridNode node) {
        TickingRequest item = itemDuality.getTickingRequest(node), fluid = fluidDuality.getTickingRequest(node);
        return new TickingRequest(
                Math.min(item.minTickRate, fluid.minTickRate),
                Math.max(item.maxTickRate, fluid.maxTickRate),
                item.isSleeping && fluid.isSleeping, // might cause some unnecessary ticking, but oh well
                true);
    }

    public TickRateModulation onTick(IGridNode node, int ticksSinceLastCall) {
        TickRateModulation item = itemDuality.tickingRequest(node, ticksSinceLastCall);
        TickRateModulation fluid = fluidDuality.tickingRequest(node, ticksSinceLastCall);
        if (item.ordinal() >= fluid.ordinal()) { // return whichever is most urgent
            return item;
        } else {
            return fluid;
        }
    }

    public void onChannelStateChange(final MENetworkChannelsChanged c) {
        itemDuality.notifyNeighbors();
        fluidDuality.notifyNeighbors();
    }

    public void onPowerStateChange(final MENetworkPowerStatusChange c) {
        itemDuality.notifyNeighbors();
        fluidDuality.notifyNeighbors();
    }

    public void onGridChanged() {
        itemDuality.gridChanged();
        fluidDuality.gridChanged();
    }

    public void addDrops(List<ItemStack> drops) {
        itemDuality.addDrops(drops);
        fluidDuality.addDrops(drops);
    }

    public boolean canInsertItem(ItemStack stack) {
        return itemDuality.canInsert(stack);
    }

    public IItemHandler getItemInventoryByName(String name) {
        if (name.startsWith("item_")) {
            return itemDuality.getInventoryByName(name.replace("item_", ""));
        }
        if (name.startsWith("fluid_")) {
            return fluidDuality.getInventoryByName(name.replace("fluid_", ""));
        }
        return itemDuality.getInventoryByName(name);
    }

    public IItemHandler getInternalItemInventory() {
        return itemDuality.getInternalInventory();
    }

    public void onItemInventoryChange(IItemHandler inv, int slot, InvOperation op, ItemStack removed, ItemStack added) {
        itemDuality.onChangeInventory(inv, slot, op, removed, added);
    }

    // autocrafting

    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
        return itemDuality.pushPattern(patternDetails, table);
    }

    public boolean isCraftingBusy() {
        return itemDuality.isBusy();
    }

    public void provideCrafting(ICraftingProviderHelper craftingTracker) {
        itemDuality.provideCrafting(craftingTracker);
    }

    public ImmutableSet<ICraftingLink> getRequestCraftingJobs() {
        return itemDuality.getRequestedJobs();
    }

    public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack items, Actionable mode) {
        return itemDuality.injectCraftedItems(link, items, mode);
    }

    public void onCraftingJobStateChange(ICraftingLink link) {
        itemDuality.jobStateChange(link);
    }

    // serialization

    public void writeToNBT(final NBTTagCompound data) {
        NBTTagCompound itemIfaceTag = new NBTTagCompound(), fluidIfaceTag = new NBTTagCompound();
        itemDuality.writeToNBT(itemIfaceTag);
        fluidDuality.writeToNBT(fluidIfaceTag);
        data.setTag("itemDuality", itemIfaceTag);
        data.setTag("fluidDuality", fluidIfaceTag);
    }

    public void readFromNBT(final NBTTagCompound data) {
        itemDuality.readFromNBT(data.getCompoundTag("itemDuality"));
        fluidDuality.readFromNBT(data.getCompoundTag("fluidDuality"));
    }

    public NBTTagCompound downloadSettings(SettingsFrom from) {
        NBTTagCompound tag = new NBTTagCompound();
        if (from == SettingsFrom.MEMORY_CARD) {
            final IItemHandler inv = this.getItemInventoryByName("item_patterns");
            if (inv instanceof AppEngInternalInventory) {
                ((AppEngInternalInventory) inv).writeToNBT(tag, "item_patterns");
            }
            final IFluidHandler fluidInv = this.fluidDuality.getFluidInventoryByName("config");
            if (fluidInv instanceof AEFluidInventory) {
                ((AEFluidInventory) fluidInv).writeToNBT(tag, "fluid_config");
            }
        }
        return tag;
    }

    public void uploadSettings(NBTTagCompound compound, EntityPlayer player) {
        final IItemHandler inv = this.getItemInventoryByName("item_patterns");
        if (inv instanceof AppEngInternalInventory) {
            final AppEngInternalInventory target = (AppEngInternalInventory) inv;
            AppEngInternalInventory tmp = new AppEngInternalInventory(null, target.getSlots());
            tmp.readFromNBT(compound, "item_patterns");
            PlayerMainInvWrapper playerInv = new PlayerMainInvWrapper(player.inventory);
            final IMaterials materials = AEApi.instance().definitions().materials();
            int missingPatternsToEncode = 0;

            for (int i = 0; i < inv.getSlots(); i++) {
                if (target.getStackInSlot(i).getItem() instanceof ItemEncodedPattern) {
                    ItemStack blank = materials.blankPattern().maybeStack(target.getStackInSlot(i).getCount()).get();
                    if (!player.addItemStackToInventory(blank)) {
                        player.dropItem(blank, true);
                    }
                    target.setStackInSlot(i, ItemStack.EMPTY);
                }
            }


            for (int x = 0; x < tmp.getSlots(); x++) {
                if (!tmp.getStackInSlot(x).isEmpty()) {
                    boolean found = false;
                    for (int i = 0; i < playerInv.getSlots(); i++) {
                        if (materials.blankPattern().isSameAs(playerInv.getStackInSlot(i))) {
                            target.setStackInSlot(x, tmp.getStackInSlot(x));
                            playerInv.getStackInSlot(i).shrink(1);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        missingPatternsToEncode++;
                    }
                }
            }
            if (Platform.isServer() && missingPatternsToEncode > 0) {
                player.sendMessage(PlayerMessages.MissingPatternsToEncode.get());
            }
        }
        final IFluidHandler fluidInv = this.fluidDuality.getFluidInventoryByName("config");
        if (fluidInv instanceof AEFluidInventory) {
            ((AEFluidInventory) fluidInv).readFromNBT(compound, "fluid_config");
        }
    }

}
