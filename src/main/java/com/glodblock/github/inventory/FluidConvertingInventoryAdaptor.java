package com.glodblock.github.inventory;

import appeng.api.config.FuzzyMode;
import appeng.tile.networking.TileCableBus;
import appeng.util.InventoryAdaptor;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.ItemSlot;
import com.glodblock.github.common.Config;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.parts.PartFluidInterface;
import com.glodblock.github.common.tile.TileFluidInterface;
import com.glodblock.github.util.Ae2Reflect;
import com.glodblock.github.util.Util;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;

public class FluidConvertingInventoryAdaptor extends InventoryAdaptor {

    public static InventoryAdaptor wrap(TileEntity capProvider, EnumFacing face) {
        // sometimes i wish 1.7.10 has cap system.
        ForgeDirection f = Util.from(face);
        TileEntity inter = capProvider.getWorldObj().getTileEntity(capProvider.xCoord + f.offsetX, capProvider.yCoord + f.offsetY, capProvider.zCoord + f.offsetZ);
        if (!Config.noFluidPacket && !(inter instanceof TileFluidInterface || (inter instanceof TileCableBus && ((TileCableBus) inter).getPart(f.getOpposite()) instanceof PartFluidInterface)))
            return InventoryAdaptor.getAdaptor(capProvider, f);
        InventoryAdaptor item = InventoryAdaptor.getAdaptor(capProvider, f);
        IFluidHandler fluid = capProvider instanceof IFluidHandler ? (IFluidHandler) capProvider : null;
        return new FluidConvertingInventoryAdaptor(item, fluid, face);
    }

    private final InventoryAdaptor invItems;
    private final IFluidHandler invFluids;
    private final ForgeDirection side;

    public FluidConvertingInventoryAdaptor(@Nullable InventoryAdaptor invItems, @Nullable IFluidHandler invFluids, EnumFacing facing) {
        this.invItems = invItems;
        this.invFluids = invFluids;
        this.side = Util.from(facing);
    }

    @Override
    public ItemStack addItems(ItemStack toBeAdded) {
        if (toBeAdded.getItem() instanceof ItemFluidPacket) {
            if (invFluids != null) {
                FluidStack fluid = ItemFluidPacket.getFluidStack(toBeAdded);
                if (fluid != null) {
                    int filled = invFluids.fill(side, fluid, true);
                    if (filled > 0) {
                        fluid.amount -= filled;
                        return ItemFluidPacket.newStack(fluid);
                    }
                }
            }
            return toBeAdded;
        }
        return invItems != null ? invItems.addItems(toBeAdded) : toBeAdded;
    }

    @Override
    public ItemStack simulateAdd(ItemStack toBeSimulated) {
        if (toBeSimulated.getItem() instanceof ItemFluidPacket) {
            if (invFluids != null) {
                FluidStack fluid = ItemFluidPacket.getFluidStack(toBeSimulated);
                if (fluid != null) {
                    int filled = invFluids.fill(side, fluid, false);
                    if (filled > 0) {
                        fluid.amount -= filled;
                        return ItemFluidPacket.newStack(fluid);
                    }
                }
            }
            return toBeSimulated;
        }
        return invItems != null ? invItems.simulateAdd(toBeSimulated) : toBeSimulated;
    }

    @Override
    public ItemStack removeItems(int amount, ItemStack filter, IInventoryDestination destination) {
        return invItems != null ? invItems.removeItems(amount, filter, destination) : null;
    }

    @Override
    public ItemStack simulateRemove(int amount, ItemStack filter, IInventoryDestination destination) {
        return invItems != null ? invItems.simulateRemove(amount, filter, destination) : null;
    }

    @Override
    public ItemStack removeSimilarItems(int amount, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination) {
        return invItems != null ? invItems.removeSimilarItems(amount, filter, fuzzyMode, destination) : null;
    }

    @Override
    public ItemStack simulateSimilarRemove(int amount, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination) {
        return invItems != null ? invItems.simulateSimilarRemove(amount, filter, fuzzyMode, destination) : null;
    }

    @Override
    public boolean containsItems() {
        if (invFluids != null && invFluids.getTankInfo(side) != null) {
            for (FluidTankInfo tank : invFluids.getTankInfo(side)) {
                FluidStack fluid = tank.fluid;
                if (fluid != null && fluid.amount > 0) {
                    return true;
                }
            }
        }
        return invItems != null && invItems.containsItems();
    }

    public boolean hasSlots() {
        return (invFluids != null && invFluids.getTankInfo(side).length > 0)
            || (invItems != null);
    }

    @Override
    public Iterator<ItemSlot> iterator() {
        return new SlotIterator(
            invFluids != null ? invFluids.getTankInfo(side) : new FluidTankInfo[0],
            invItems != null ? invItems.iterator() : Collections.emptyIterator());
    }

    private static class SlotIterator implements Iterator<ItemSlot> {

        private final FluidTankInfo[] tanks;
        private final Iterator<ItemSlot> itemSlots;
        private int nextSlotIndex = 0;

        SlotIterator(FluidTankInfo[] tanks, Iterator<ItemSlot> itemSlots) {
            this.tanks = tanks;
            this.itemSlots = itemSlots;
        }

        @Override
        public boolean hasNext() {
            return nextSlotIndex < tanks.length || itemSlots.hasNext();
        }

        @Override
        public ItemSlot next() {
            if (nextSlotIndex < tanks.length) {
                FluidStack fluid = tanks[nextSlotIndex].fluid;
                ItemSlot slot = new ItemSlot();
                slot.setSlot(nextSlotIndex++);
                slot.setItemStack(fluid != null ? ItemFluidPacket.newStack(fluid) : null);
                Ae2Reflect.setItemSlotExtractable(slot, false);
                return slot;
            } else {
                ItemSlot slot = itemSlots.next();
                slot.setSlot(nextSlotIndex++);
                return slot;
            }
        }

    }

}
