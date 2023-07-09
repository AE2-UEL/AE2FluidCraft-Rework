package com.glodblock.github.common.tile;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.util.AEFluidStack;
import appeng.me.GridAccessException;
import appeng.me.helpers.MachineSource;
import appeng.tile.grid.AENetworkTileEntity;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.loader.FCBlocks;
import com.glodblock.github.util.FCUtil;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

public class TileFluidPacketDecoder extends AENetworkTileEntity implements IGridTickable, IAEAppEngInventory {

    private final AppEngInternalInventory inventory = new AppEngInternalInventory(this, 1);
    private final IActionSource ownActionSource = new MachineSource(this);

    public TileFluidPacketDecoder() {
        super(FCUtil.getTileType(TileFluidPacketDecoder.class, FCBlocks.FLUID_PACKET_DECODER));
        getProxy().setIdlePowerUsage(1.5D);
        getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    public IItemHandlerModifiable getInventory() {
        return inventory;
    }

    @Override
    public boolean canBeRotated() {
        return false;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return LazyOptional.of(() -> (T)inventory);
        } else {
            return LazyOptional.empty();
        }
    }

    @Override
    @Nonnull
    public TickingRequest getTickingRequest(@Nonnull IGridNode node) {
        return new TickingRequest(5, 120, false, true);
    }

    @Override
    @Nonnull
    public TickRateModulation tickingRequest(@Nonnull IGridNode node, int ticksSinceLastCall) {
        ItemStack stack = inventory.getStackInSlot(0);
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemFluidPacket)) {
            return TickRateModulation.SLEEP;
        }
        FluidStack fluid = ItemFluidPacket.getFluidStack(stack);
        if (fluid.isEmpty()) {
            inventory.setStackInSlot(0, ItemStack.EMPTY);
            return TickRateModulation.SLEEP;
        }
        IAEFluidStack aeFluid = AEFluidStack.fromFluidStack(fluid);
        IEnergyGrid energyGrid = node.getGrid().getCache(IEnergyGrid.class);
        IMEMonitor<IAEFluidStack> fluidGrid = node.getGrid().<IStorageGrid>getCache(IStorageGrid.class)
                .getInventory(FCUtil.FLUID);
        IAEFluidStack remaining = Platform.poweredInsert(energyGrid, fluidGrid, aeFluid, ownActionSource);
        if (remaining != null) {
            if (remaining.getStackSize() == aeFluid.getStackSize()) {
                return TickRateModulation.SLOWER;
            }
            inventory.setStackInSlot(0, ItemFluidPacket.newStack(remaining.getFluidStack()));
            return TickRateModulation.FASTER;
        } else {
            inventory.setStackInSlot(0, ItemStack.EMPTY);
            return TickRateModulation.SLEEP;
        }
    }

    @Override
    public void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack) {
        try {
            getProxy().getTick().alertDevice(getProxy().getNode());
        } catch (GridAccessException e) {
            // NO-OP
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT data) {
        super.write(data);
        inventory.writeToNBT(data, "Inventory");
        return data;
    }

    @Override
    public void read(BlockState blockState, CompoundNBT data) {
        super.read(blockState, data);
        inventory.readFromNBT(data, "Inventory");
    }

}
