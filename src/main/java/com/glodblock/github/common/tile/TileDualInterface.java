package com.glodblock.github.common.tile;

import appeng.api.config.Actionable;
import appeng.api.config.Upgrades;
import appeng.api.networking.GridFlags;
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
import appeng.helpers.IPriorityHost;
import appeng.tile.grid.AENetworkInvTileEntity;
import appeng.util.Platform;
import appeng.util.SettingsFrom;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.InvOperation;
import com.glodblock.github.client.container.ContainerItemDualInterface;
import com.glodblock.github.common.me.DualityDualInterface;
import com.glodblock.github.loader.FCBlocks;
import com.glodblock.github.util.FCUtil;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

public class TileDualInterface extends AENetworkInvTileEntity
        implements IGridTickable, IInventoryDestination, IInterfaceHost, IPriorityHost, IFluidInterfaceHost {

    public TileDualInterface() {
        super(FCUtil.getTileType(TileDualInterface.class, FCBlocks.DUAL_INTERFACE));
        getProxy().setIdlePowerUsage(4D);
        getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
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

    public void setSide(final Direction facing) {
        if (Platform.isClient()) {
            return;
        }

        Direction newForward;

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
            this.setOrientation(Direction.NORTH, Direction.UP);
        } else {
            Direction newUp = Direction.UP;
            if (newForward == Direction.UP || newForward == Direction.DOWN) {
                newUp = Direction.NORTH;
            }
            this.setOrientation(newForward, newUp);
        }

        this.configureNodeSides();
        this.markForUpdate();
        this.saveChanges();
    }

    private void configureNodeSides() {
        if (this.omniDirectional) {
            this.getProxy().setValidSides(EnumSet.allOf(Direction.class));
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
    public CompoundNBT write(final CompoundNBT data) {
        super.write(data);
        data.putBoolean("omniDirectional", this.omniDirectional);
        duality.writeToNBT(data);
        return data;
    }

    @Override
    public void read(BlockState blockState, CompoundNBT data) {
        super.read(blockState, data);
        this.omniDirectional = data.getBoolean("omniDirectional");
        duality.readFromNBT(data);
    }

    @Override
    protected boolean readFromStream(PacketBuffer data) throws IOException {
        final boolean c = super.readFromStream(data);
        boolean oldOmniDirectional = this.omniDirectional;
        this.omniDirectional = data.readBoolean();
        return oldOmniDirectional != this.omniDirectional || c;
    }

    @Override
    protected void writeToStream(PacketBuffer data) throws IOException {
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
    public EnumSet<Direction> getTargets() {
        if (this.omniDirectional) {
            return EnumSet.allOf(Direction.class);
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
    public boolean pushPattern(final ICraftingPatternDetails patternDetails, final CraftingInventory table) {
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

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        LazyOptional<T> capInst = duality.getCapability(capability, facing);
        return capInst.isPresent() ? capInst : super.getCapability(capability, facing);
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return new ItemStack(FCBlocks.DUAL_INTERFACE);
    }

    @Override
    public ContainerType<?> getContainerType() {
        return ContainerItemDualInterface.TYPE;
    }

    @Override
    public CompoundNBT downloadSettings(SettingsFrom from) {
        CompoundNBT pre = super.downloadSettings(from);
        CompoundNBT tag = pre == null ? new CompoundNBT() : pre;
        tag.put("pattern", this.duality.downloadSettings(from));
        return tag.isEmpty() ? null : tag;
    }

    @Override
    public void uploadSettings(SettingsFrom from, CompoundNBT compound) {
        super.uploadSettings(from, compound);
        if (compound.contains("pattern")) {
            this.duality.uploadSettings(from, compound.getCompound("pattern"));
        }
    }

}
