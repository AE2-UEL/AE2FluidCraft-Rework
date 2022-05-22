package com.glodblock.github.inventory;

import appeng.api.config.FuzzyMode;
import appeng.util.InventoryAdaptor;
import appeng.util.inv.AdaptorItemHandler;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.ItemSlot;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.util.Ae2Reflect;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

public class FluidConvertingInventoryAdaptor extends InventoryAdaptor {

    public static FluidConvertingInventoryAdaptor wrap(ICapabilityProvider capProvider, EnumFacing face) {
        // sometimes i wish i had the monadic version from 1.15
        return new FluidConvertingInventoryAdaptor(
                capProvider.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face)
                        ? Objects.requireNonNull(capProvider.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face))
                        : null,
                capProvider.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face)
                        ? Objects.requireNonNull(capProvider.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face))
                        : null);
    }

    @Nullable
    private final InventoryAdaptor invItems;
    @Nullable
    private final IFluidHandler invFluids;

    public FluidConvertingInventoryAdaptor(@Nullable IItemHandler invItems, @Nullable IFluidHandler invFluids) {
        this.invItems = invItems != null ? new AdaptorItemHandler(invItems) : null;
        this.invFluids = invFluids;
    }

    @Override
    public ItemStack addItems(ItemStack toBeAdded) {
        if (toBeAdded.getItem() instanceof ItemFluidPacket || toBeAdded.getItem() instanceof ItemFluidDrop) {
            if (invFluids != null) {
                FluidStack fluid;
                if(toBeAdded.getItem() instanceof ItemFluidPacket)
                    fluid = ItemFluidPacket.getFluidStack(toBeAdded);
                else
                    fluid = ItemFluidDrop.getFluidStack(toBeAdded);
                if (fluid != null) {
                    int filled = invFluids.fill(fluid, true);
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
        if (toBeSimulated.getItem() instanceof ItemFluidPacket || toBeSimulated.getItem() instanceof ItemFluidDrop) {
            if (invFluids != null) {
                FluidStack fluid;
                if(toBeSimulated.getItem() instanceof ItemFluidPacket)
                    fluid = ItemFluidPacket.getFluidStack(toBeSimulated);
                else
                    fluid = ItemFluidDrop.getFluidStack(toBeSimulated);
                if (fluid != null) {
                    int filled = invFluids.fill(fluid, false);
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
        return invItems != null ? invItems.removeItems(amount, filter, destination) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack simulateRemove(int amount, ItemStack filter, IInventoryDestination destination) {
        return invItems != null ? invItems.simulateRemove(amount, filter, destination) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeSimilarItems(int amount, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination) {
        return invItems != null ? invItems.removeSimilarItems(amount, filter, fuzzyMode, destination) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack simulateSimilarRemove(int amount, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination) {
        return invItems != null ? invItems.simulateSimilarRemove(amount, filter, fuzzyMode, destination) : ItemStack.EMPTY;
    }

    @Override
    public boolean containsItems() {
        if (invFluids != null) {
            for (IFluidTankProperties tank : invFluids.getTankProperties()) {
                FluidStack fluid = tank.getContents();
                if (fluid != null && fluid.amount > 0) {
                    return true;
                }
            }
        }
        return invItems != null && invItems.containsItems();
    }

    @Override
    public boolean hasSlots() {
        return (invFluids != null && invFluids.getTankProperties().length > 0)
                || (invItems != null && invItems.hasSlots());
    }

    @Override
    @Nonnull
    public Iterator<ItemSlot> iterator() {
        return new SlotIterator(
                invFluids != null ? invFluids.getTankProperties() : new IFluidTankProperties[0],
                invItems != null ? invItems.iterator() : Collections.emptyIterator());
    }

    private static class SlotIterator implements Iterator<ItemSlot> {

        private final IFluidTankProperties[] tanks;
        private final Iterator<ItemSlot> itemSlots;
        private int nextSlotIndex = 0;

        SlotIterator(IFluidTankProperties[] tanks, Iterator<ItemSlot> itemSlots) {
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
                FluidStack fluid = tanks[nextSlotIndex].getContents();
                ItemSlot slot = new ItemSlot();
                slot.setSlot(nextSlotIndex++);
                slot.setItemStack(fluid != null ? ItemFluidPacket.newStack(fluid) : ItemStack.EMPTY);
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
