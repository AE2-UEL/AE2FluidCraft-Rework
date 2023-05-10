package com.glodblock.github.common.part;

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
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.Reflected;
import appeng.items.parts.PartModels;
import appeng.parts.PartBasicState;
import appeng.parts.PartModel;
import appeng.util.Platform;
import appeng.util.SettingsFrom;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.InvOperation;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.component.DualityDualInterface;
import com.glodblock.github.interfaces.FCPriorityHost;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.loader.FCItems;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

public class PartDualInterface extends PartBasicState
        implements IGridTickable, IInventoryDestination, IInterfaceHost, IAEAppEngInventory, FCPriorityHost, IFluidInterfaceHost {

    @PartModels
    public static ResourceLocation[] MODELS = new ResourceLocation[] {
            new ResourceLocation(FluidCraft.MODID, "part/interface_base"),
            new ResourceLocation(FluidCraft.MODID, "part/interface_on"),
            new ResourceLocation(FluidCraft.MODID, "part/interface_off"),
            new ResourceLocation(FluidCraft.MODID, "part/interface_has_channel")
    };

    public static final PartModel MODELS_OFF = new PartModel(MODELS[0], MODELS[2]);
    public static final PartModel MODELS_ON = new PartModel(MODELS[0], MODELS[1]);
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODELS[0], MODELS[3]);

    private final DualityDualInterface<PartDualInterface> duality = new DualityDualInterface<>(getProxy(), this);

    @Reflected
    public PartDualInterface(final ItemStack is) {
        super(is);
    }

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkChannelsChanged c) {
        duality.onChannelStateChange(c);
    }

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkPowerStatusChange c) {
        duality.onPowerStateChange(c);
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(2, 2, 14, 14, 14, 16);
        bch.addBox(5, 5, 12, 11, 11, 14);
    }

    @Override
    public int getInstalledUpgrades(final Upgrades u) {
        return duality.getInstalledUpgrades(u);
    }

    @Override
    public void gridChanged() {
        duality.onGridChanged();
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        duality.readFromNBT(data);
    }

    @Override
    public void writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        duality.writeToNBT(data);
    }

    @Override
    public void addToWorld() {
        super.addToWorld();
        duality.initialize();
    }

    @Override
    public void getDrops(final List<ItemStack> drops, final boolean wrenched) {
        duality.addDrops(drops);
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 4;
    }

    @Override
    public IConfigManager getConfigManager() {
        return duality.getConfigManager();
    }

    @Override
    public IItemHandler getInventoryByName(final String name) {
        return duality.getItemInventoryByName(name);
    }

    @Override
    public boolean onPartActivate(final EntityPlayer p, final EnumHand hand, final Vec3d pos) {
        if (Platform.isServer()) {
            TileEntity tile = getTileEntity();
            InventoryHandler.openGui(p, tile.getWorld(), tile.getPos(), getSide().getFacing(), GuiType.DUAL_ITEM_INTERFACE);
        }
        return true;
    }

    @Override
    public boolean canInsert(final ItemStack stack) {
        return duality.canInsertItem(stack);
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
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc,
                                  final ItemStack removedStack, final ItemStack newStack) {
        duality.onItemInventoryChange(inv, slot, mc, removedStack, newStack);
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
        return EnumSet.of(this.getSide().getFacing());
    }

    @Override
    public TileEntity getTileEntity() {
        return super.getHost().getTile();
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

    @Override
    public boolean hasCapability(Capability<?> capabilityClass) {
        return duality.hasCapability(capabilityClass, getSide().getFacing());
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capabilityClass) {
        return duality.getCapability(capabilityClass, getSide().getFacing());
    }

    @Override
    public GuiType getGuiType() {
        return GuiType.DUAL_ITEM_INTERFACE;
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return new ItemStack(FCItems.PART_DUAL_INTERFACE, 1);
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return MODELS_ON;
        } else {
            return MODELS_OFF;
        }
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