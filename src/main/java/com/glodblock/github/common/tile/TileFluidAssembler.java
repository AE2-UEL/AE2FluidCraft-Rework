package com.glodblock.github.common.tile;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Upgrades;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.helpers.Reflected;
import appeng.me.GridAccessException;
import appeng.me.helpers.MachineSource;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.inv.InvOperation;
import appeng.util.item.AEItemStack;
import com.glodblock.github.common.item.ItemFluidCraftEncodedPattern;
import com.glodblock.github.util.FluidCraftingPatternDetails;
import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

public class TileFluidAssembler extends AENetworkInvTile implements ICraftingProvider, ITickable {

    public final AppEngInternalInventory invPatterns = new AppEngInternalInventory(this, 36, 1);
    public final AppEngInternalInventory upgrade = new AppEngInternalInventory(this, 2, 3);
    public final AppEngInternalInventory gridInv = new AppEngInternalInventory(this, 9, 1);
    public final AppEngInternalInventory output = new AppEngInternalInventory(this, 1);
    private List<ICraftingPatternDetails> craftingList = null;
    private int progress = 0;
    private static final double powerUsage = 0.5D;
    public static final int TIME = 20;
    private ICraftingPatternDetails myPlan = null;
    private final IActionSource mySrc = new MachineSource(this);
    private List<ItemStack> waitingToSend = new ArrayList<>();

    @Reflected
    public TileFluidAssembler() {
        getProxy().setIdlePowerUsage(0.0D);
    }

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkChannelsChanged c) {
        this.notifyNeighbors();
    }

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkPowerStatusChange c) {
        this.notifyNeighbors();
    }

    private void addToCraftingList(final ItemStack is) {
        if (is.isEmpty()) {
            return;
        }

        if (is.getItem() instanceof ItemFluidCraftEncodedPattern) {
            final ICraftingPatternDetails details = ((ItemFluidCraftEncodedPattern) is.getItem()).getPatternForItem(is, this.getWorld());

            if (details != null) {
                if (this.craftingList == null) {
                    this.craftingList = new ArrayList<>();
                }
                this.craftingList.add(details);
            }
        }
    }

    private void updateCraftingList() {
        final Boolean[] accountedFor = new Boolean[this.invPatterns.getSlots()];
        Arrays.fill(accountedFor, false);

        if (!this.getProxy().isReady()) {
            return;
        }

        if (this.craftingList != null) {
            final Iterator<ICraftingPatternDetails> i = this.craftingList.iterator();
            while (i.hasNext()) {
                final ICraftingPatternDetails details = i.next();
                boolean found = false;

                for (int x = 0; x < accountedFor.length; x++) {
                    final ItemStack is = this.invPatterns.getStackInSlot(x);
                    if (details.getPattern() == is) {
                        accountedFor[x] = found = true;
                    }
                }
                if (!found) {
                    i.remove();
                }
            }
        }

        for (int x = 0; x < accountedFor.length; x++) {
            if (!accountedFor[x]) {
                this.addToCraftingList(this.invPatterns.getStackInSlot(x));
            }
        }
        try {
            this.getProxy().getGrid().postEvent(new MENetworkCraftingPatternChange(this, this.getProxy().getNode()));
        } catch (GridAccessException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onReady() {
        super.onReady();
        updateCraftingList();
    }

    @Nonnull
    @Override
    public IItemHandler getInternalInventory() {
        return invPatterns;
    }

    @Override
    public boolean canBeRotated() {
        return false;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T)invPatterns;
        }
        return null;
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added) {
        if (inv == this.invPatterns && (!removed.isEmpty() || !added.isEmpty())) {
            updateCraftingList();
        }
        markForUpdate();
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        this.upgrade.writeToNBT(data, "upgrade");
        this.gridInv.writeToNBT(data, "gridInv");
        this.output.writeToNBT(data, "output");
        data.setInteger("progress", this.progress);
        if (this.myPlan != null) {
            final ItemStack pattern = this.myPlan.getPattern();
            final NBTTagCompound tmp = new NBTTagCompound();
            pattern.writeToNBT(tmp);
            data.setTag("myPlan", tmp);
        }
        final NBTTagList waitingToSend = new NBTTagList();
        if (!this.waitingToSend.isEmpty()) {
            for (final ItemStack is : this.waitingToSend) {
                final NBTTagCompound item = new NBTTagCompound();
                is.writeToNBT(item);
                waitingToSend.appendTag(item);
            }
        }
        data.setTag("waitingToSend", waitingToSend);
        return data;
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        this.upgrade.readFromNBT(data, "upgrade");
        this.gridInv.readFromNBT(data, "gridInv");
        this.output.readFromNBT(data, "output");
        this.progress = data.getInteger("progress");
        if (data.hasKey("myPlan")) {
            ItemStack pattern = new ItemStack(data.getCompoundTag("myPlan"));
            this.myPlan = FluidCraftingPatternDetails.GetFluidPattern(pattern, this.getWorld());
        }
        this.waitingToSend = new ArrayList<>();
        final NBTTagList waitingList = data.getTagList("waitingToSend", 10);
        for (int x = 0; x < waitingList.tagCount(); x++) {
            final NBTTagCompound c = waitingList.getCompoundTagAt(x);
            this.waitingToSend.add(new ItemStack(c));
        }
        updateCraftingList();
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Nonnull
    @Override
    public AECableType getCableConnectionType(@Nonnull AEPartLocation aePartLocation) {
        return AECableType.SMART;
    }

    @Override
    public void provideCrafting(ICraftingProviderHelper craftingTracker) {
        if (this.getProxy().isActive() && this.craftingList != null) {
            for (ICraftingPatternDetails details : this.craftingList) {
                craftingTracker.addCraftingOption(this, details);
            }
        }
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting inventoryCrafting) {
        if (!this.getProxy().isActive() || this.myPlan != null
                || !this.craftingList.contains(patternDetails) || !(patternDetails instanceof FluidCraftingPatternDetails)) {
            return false;
        }
        FluidCraftingPatternDetails fluidPattern = (FluidCraftingPatternDetails) patternDetails;
        this.myPlan = patternDetails;
        for (int x = 0; x < 9; x ++) {
            IAEItemStack item = fluidPattern.getOriginInputs()[x];
            if (item == null) {
                this.gridInv.setStackInSlot(x, ItemStack.EMPTY);
            } else {
                this.gridInv.setStackInSlot(x, item.getDefinition());
            }
        }
        if (fluidPattern.getOutputs()[0] != null) {
            this.output.setStackInSlot(0, fluidPattern.getOutputs()[0].getDefinition());
        } else {
            this.output.setStackInSlot(0, ItemStack.EMPTY);
        }
        return true;
    }

    @Override
    public boolean isBusy() {
        return this.myPlan != null || !this.waitingToSend.isEmpty();
    }

    public int getSpeed() {
        ItemStack card = this.upgrade.getStackInSlot(0);
        if (!card.isEmpty() && card.getItem() instanceof IUpgradeModule) {
            Upgrades type = ((IUpgradeModule) card.getItem()).getType(card);
            if (type == Upgrades.SPEED) {
                return card.getCount() + 1;
            }
        }
        return 1;
    }

    public int getPatternCap() {
        ItemStack card = this.upgrade.getStackInSlot(1);
        if (!card.isEmpty() && card.getItem() instanceof IUpgradeModule) {
            Upgrades type = ((IUpgradeModule) card.getItem()).getType(card);
            if (type == Upgrades.PATTERN_EXPANSION) {
                return card.getCount();
            }
        }
        return 0;
    }

    public void update() {
        if (!this.world.isRemote && this.world.getTotalWorldTime() % 5L == 0L) {
            if (this.waitingToSend.isEmpty()) {
                int speed = getSpeed();
                if (this.myPlan != null && drainEnergy(powerUsage * speed)) {
                    this.progress += speed;
                    if (this.progress >= TIME) {
                        this.progress = 0;
                        IAEItemStack[] outputs = this.myPlan.getOutputs();
                        for (IAEItemStack item : outputs) {
                            this.waitingToSend.add(item.getDefinition());
                        }
                        for (int x = 0; x < this.gridInv.getSlots(); x ++) {
                            this.gridInv.setStackInSlot(x, ItemStack.EMPTY);
                        }
                        for (int x = 0; x < this.output.getSlots(); x ++) {
                            this.output.setStackInSlot(x, ItemStack.EMPTY);
                        }
                        this.myPlan = null;
                    }
                }
            }
            if (!this.waitingToSend.isEmpty() && this.getProxy().isActive()) {
                List<ItemStack> rst = new ArrayList<>();
                try {
                    IMEInventory<IAEItemStack> des = this.getProxy().getStorage().getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
                    final IEnergySource src = this.getProxy().getEnergy();
                    for (ItemStack item : this.waitingToSend) {
                        if (item != null && !item.isEmpty()) {
                            IAEItemStack remaining = Platform.poweredInsert(src, des, Objects.requireNonNull(AEItemStack.fromItemStack(item)), this.mySrc);
                            if (remaining != null) {
                                rst.add(remaining.getDefinition());
                            }
                        }
                    }
                    this.waitingToSend = rst;
                } catch (GridAccessException ignore) {
                }
            }
        }
    }

    private boolean drainEnergy(double energy) {
        double drain = 0;
        try {
            IGrid grid = this.getProxy().getGrid();
            if (grid != null) {
                IEnergyGrid energyGrid = grid.getCache(IEnergyGrid.class);
                drain = energyGrid.extractAEPower(energy, Actionable.MODULATE, PowerMultiplier.CONFIG);
            }
        } catch (GridAccessException ignore) {
        }
        return drain > 0;
    }

    @Override
    public void getDrops(final World w, final BlockPos pos, final List<ItemStack> drops) {
        if (!this.waitingToSend.isEmpty()) {
            drops.addAll(this.waitingToSend);
        }
        for (ItemStack item : this.invPatterns) {
            if (item != null && !item.isEmpty())
                drops.add(item);
        }
        for (ItemStack item : this.upgrade) {
            if (item != null && !item.isEmpty())
                drops.add(item);
        }
    }

    public void dropExcessPatterns() {
        List<ItemStack> dropList = new ArrayList<>();
        for (int invSlot = 0; invSlot < this.invPatterns.getSlots(); invSlot++) {
            if (invSlot > 8 + getPatternCap() * 9) {
                ItemStack is = this.invPatterns.getStackInSlot(invSlot);
                if (is.isEmpty()) {
                    continue;
                }
                dropList.add(this.invPatterns.extractItem(invSlot, Integer.MAX_VALUE, false));
            }
        }
        if (dropList.size() > 0) {
            World world = this.getLocation().getWorld();
            BlockPos blockPos = this.getLocation().getPos();
            Platform.spawnDrops(world, blockPos, dropList);
        }
    }

    public int getProgress() {
        return this.progress;
    }

    public void notifyNeighbors() {
        if (this.getProxy().isActive()) {
            try {
                this.getProxy().getGrid().postEvent(new MENetworkCraftingPatternChange(this, this.getProxy().getNode()));
                this.getProxy().getTick().wakeDevice(this.getProxy().getNode());
            } catch (final GridAccessException ignore) {
            }
        }
        Platform.notifyBlocksOfNeighbors(this.getWorld(), this.getPos());
    }

    @Override
    protected void writeToStream(ByteBuf data) throws IOException {
        super.writeToStream(data);
        data.writeInt(this.progress);
    }

    @Override
    protected boolean readFromStream(ByteBuf data) throws IOException {
        boolean changed = super.readFromStream(data);
        int newPro = data.readInt();
        if (this.progress != newPro) {
            this.progress = newPro;
            changed = true;
        }
        return changed;
    }
}
