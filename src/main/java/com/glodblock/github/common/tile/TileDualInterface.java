package com.glodblock.github.common.tile;

import appeng.api.config.Actionable;
import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.tile.grid.AENetworkInvTile;
import appeng.util.Platform;
import appeng.util.SettingsFrom;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.InvOperation;
import com.glodblock.github.common.component.DualityDualInterface;
import com.glodblock.github.interfaces.FCPriorityHost;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.loader.FCBlocks;
import com.google.common.collect.ImmutableSet;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

public class TileDualInterface extends AENetworkInvTile
        implements IGridTickable, IInventoryDestination, IInterfaceHost, FCPriorityHost, IFluidInterfaceHost {

    public TileDualInterface() {
        super();
    }

    private final DualityDualInterface<TileDualInterface> duality = new DualityDualInterface<>(getProxy(), this);

    // Indicates that this interface has no specific direction set
    private boolean omniDirectional = true;

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkChannelsChanged c) {
        duality.onChannelStateChange(c);
    }

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkPowerStatusChange c) {
        duality.onPowerStateChange(c);
    }

    public void setSide(final EnumFacing facing) {
        if (Platform.isClient()) {
            return;
        }

        EnumFacing newForward;

        if (!this.omniDirectional && this.getForward() == facing.getOpposite()) {
            newForward = facing;
        } else if (!this.omniDirectional
                && (this.getForward() == facing || this.getForward() == facing.getOpposite())) {
            newForward = facing;
            this.omniDirectional = true;
        } else if (this.omniDirectional) {
            newForward = facing.getOpposite();
            this.omniDirectional = false;
        } else {
            newForward = Platform.rotateAround(this.getForward(), facing);
        }

        if (this.omniDirectional) {
            this.setOrientation(EnumFacing.NORTH, EnumFacing.UP);
        } else {
            EnumFacing newUp = EnumFacing.UP;
            if (newForward == EnumFacing.UP || newForward == EnumFacing.DOWN) {
                newUp = EnumFacing.NORTH;
            }
            this.setOrientation(newForward, newUp);
        }

        this.configureNodeSides();
        this.markForUpdate();
        this.saveChanges();
    }

    private void configureNodeSides() {
        if (this.omniDirectional) {
            this.getProxy().setValidSides(EnumSet.allOf(EnumFacing.class));
        } else {
            this.getProxy().setValidSides(EnumSet.complementOf(EnumSet.of(this.getForward())));
        }
    }

    @Override
    public void getDrops(final World w, final BlockPos pos, final List<ItemStack> drops) {
        duality.addDrops(drops);
    }

    @Override
    public void gridChanged() {
        duality.onGridChanged();
    }

    @Override
    public void onReady() {
        this.configureNodeSides();
        super.onReady();
        duality.initialize();
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("omniDirectional", this.omniDirectional);
        duality.writeToNBT(data);
        return data;
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        this.omniDirectional = data.getBoolean("omniDirectional");
        duality.readFromNBT(data);
    }

    @Override
    protected boolean readFromStream(final ByteBuf data) throws IOException {
        final boolean c = super.readFromStream(data);
        boolean oldOmniDirectional = this.omniDirectional;
        this.omniDirectional = data.readBoolean();
        return oldOmniDirectional != this.omniDirectional || c;
    }

    @Override
    protected void writeToStream(final ByteBuf data) throws IOException {
        super.writeToStream(data);
        data.writeBoolean(this.omniDirectional);
    }

    @Override
    @Nonnull
    public AECableType getCableConnectionType(@Nonnull final AEPartLocation dir) {
        return AECableType.SMART;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this.getTileEntity());
    }

    @Override
    public boolean canInsert(final ItemStack stack) {
        return duality.canInsertItem(stack);
    }

    @Override
    public IItemHandler getInventoryByName(final String name) {
        return duality.getItemInventoryByName(name);
    }

    @Override
    @Nonnull
    public TickingRequest getTickingRequest(@Nonnull final IGridNode node) {
        return duality.getTickingRequest(node);
    }

    @Override
    @Nonnull
    public TickRateModulation tickingRequest(@Nonnull final IGridNode node, final int ticksSinceLastCall) {
        return duality.onTick(node, ticksSinceLastCall);
    }

    @Override
    @Nonnull
    public IItemHandler getInternalInventory() {
        return duality.getInternalItemInventory();
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc,
                                  final ItemStack removed, final ItemStack added) {
        duality.onItemInventoryChange(inv, slot, mc, removed, added);
    }

    @Override
    public DualityInterface getInterfaceDuality() {
        return duality.getItemInterface();
    }

    @Override
    public DualityFluidInterface getDualityFluidInterface() {
        return duality.getFluidInterface();
    }

    @Override
    public EnumSet<EnumFacing> getTargets() {
        if (this.omniDirectional) {
            return EnumSet.allOf(EnumFacing.class);
        }
        return EnumSet.of(this.getForward());
    }

    @Override
    public TileEntity getTileEntity() {
        return this;
    }

    @Override
    public IConfigManager getConfigManager() {
        return duality.getConfigManager();
    }

    @Override
    public boolean pushPattern(final ICraftingPatternDetails patternDetails, final InventoryCrafting table) {
        return duality.pushPattern(patternDetails, table);
    }

    @Override
    public boolean isBusy() {
        return duality.isCraftingBusy();
    }

    @Override
    public void provideCrafting(final ICraftingProviderHelper craftingTracker) {
        duality.provideCrafting(craftingTracker);
    }

    @Override
    public int getInstalledUpgrades(final Upgrades u) {
        return duality.getInstalledUpgrades(u);
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return duality.getRequestCraftingJobs();
    }

    @Override
    public IAEItemStack injectCraftedItems(final ICraftingLink link, final IAEItemStack items, final Actionable mode) {
        return duality.injectCraftedItems(link, items, mode);
    }

    @Override
    public void jobStateChange(final ICraftingLink link) {
        duality.onCraftingJobStateChange(link);
    }

    @Override
    public int getPriority() {
        return duality.getPriority();
    }

    @Override
    public void setPriority(final int newValue) {
        duality.setPriority(newValue);
    }

    /**
     * @return True if this interface is omni-directional.
     */
    public boolean isOmniDirectional() {
        return this.omniDirectional;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return duality.hasCapability(capability, facing) || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        T capInst = duality.getCapability(capability, facing);
        return capInst != null ? capInst : super.getCapability(capability, facing);
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return new ItemStack(FCBlocks.DUAL_INTERFACE);
    }

    @Override
    public GuiType getGuiType() {
        return GuiType.DUAL_ITEM_INTERFACE;
    }

    @Override
    public NBTTagCompound downloadSettings(SettingsFrom from) {
        NBTTagCompound pre = super.downloadSettings(from);
        NBTTagCompound tag = pre == null ? new NBTTagCompound() : pre;
        tag.setTag("pattern", this.duality.downloadSettings(from));
        return tag.isEmpty() ? null : tag;
    }

    @Override
    public void uploadSettings(SettingsFrom from, NBTTagCompound compound, EntityPlayer player) {
        super.uploadSettings(from, compound, player);
        if (compound.hasKey("pattern")) {
            this.duality.uploadSettings(compound.getCompoundTag("pattern"), player);
        }
    }

}
