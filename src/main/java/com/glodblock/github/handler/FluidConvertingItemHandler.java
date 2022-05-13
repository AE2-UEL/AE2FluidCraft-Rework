package com.glodblock.github.handler;

import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
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
import java.util.Objects;

public class FluidConvertingItemHandler  implements IItemHandler {

    public static FluidConvertingItemHandler wrap(ICapabilityProvider capProvider, EnumFacing face) {
        // sometimes i wish i had the monadic version from 1.15
        return new FluidConvertingItemHandler(
                capProvider.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face)
                        ? Objects.requireNonNull(capProvider.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face))
                        : null,
                capProvider.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face)
                        ? Objects.requireNonNull(capProvider.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face))
                        : null);
    }

    @Nullable
    private final IItemHandler invItems;
    @Nullable
    private final IFluidHandler invFluids;

    private FluidConvertingItemHandler(@Nullable IItemHandler invItems, @Nullable IFluidHandler invFluids) {
        this.invItems = invItems;
        this.invFluids = invFluids;
    }

    @Override
    public int getSlots() {
        int slots = 0;
        if (invItems != null) {
            slots += invItems.getSlots();
        }
        if (invFluids != null) {
            slots += invFluids.getTankProperties().length;
        }
        return slots;
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        return slotOp(slot, IItemHandler::getStackInSlot,
                (fh, i) -> ItemFluidDrop.newStack(fh.getTankProperties()[i].getContents()));
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return slotOp(slot,
                (ih, i) -> (stack.getItem() instanceof ItemFluidDrop || stack.getItem() instanceof ItemFluidPacket)
                        ? stack : ih.insertItem(i, stack, simulate),
                (fh, i) -> {
                    if (stack.getItem() instanceof ItemFluidDrop) {
                        FluidStack toInsert = ItemFluidDrop.getFluidStack(stack);
                        if (toInsert != null && toInsert.amount > 0) {
                            FluidStack contained = fh.getTankProperties()[i].getContents();
                            if (contained == null || contained.amount == 0 || contained.isFluidEqual(toInsert)) {
                                toInsert.amount -= fh.fill(toInsert, !simulate);
                                return ItemFluidDrop.newStack(toInsert);
                            }
                        }
                    } else if (stack.getItem() instanceof ItemFluidPacket) {
                        FluidStack toInsert = ItemFluidPacket.getFluidStack(stack);
                        if (toInsert != null && toInsert.amount > 0) {
                            FluidStack contained = fh.getTankProperties()[i].getContents();
                            if (contained == null || contained.amount == 0 || contained.isFluidEqual(toInsert)) {
                                int insertable = fh.fill(toInsert, false); // only insert if the entire packet fits
                                if (insertable >= toInsert.amount) {
                                    if (!simulate) {
                                        fh.fill(toInsert, true);
                                    }
                                    return ItemStack.EMPTY;
                                }
                            }
                        }
                    }
                    return stack;
                });
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return slotOp(slot, (ih, i) -> ih.extractItem(i, slot, simulate), (fh, i) -> {
            FluidStack contained = fh.getTankProperties()[i].getContents();
            if (contained != null && contained.amount > 0) {
                return ItemFluidDrop.newStack(fh.drain(contained, !simulate));
            }
            return ItemStack.EMPTY;
        });
    }

    @Override
    public int getSlotLimit(int slot) {
        return slotOp(slot, IItemHandler::getSlotLimit, (fh, i) -> fh.getTankProperties()[i].getCapacity());
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return slotOp(slot, (ih, i) -> ih.isItemValid(i, stack),
                (fh, i) -> stack.getItem() instanceof ItemFluidDrop || stack.getItem() instanceof ItemFluidPacket);
    }

    private <T> T slotOp(int slot, Op<IItemHandler, T> itemConsumer, Op<IFluidHandler, T> fluidConsumer) {
        if (slot >= 0) {
            int fluidSlot = slot;
            if (invItems != null) {
                if (slot < invItems.getSlots()) {
                    return itemConsumer.apply(invItems, slot);
                } else {
                    fluidSlot -= invItems.getSlots();
                }
            }
            if (invFluids != null) {
                IFluidTankProperties[] tanks = invFluids.getTankProperties();
                if (fluidSlot < tanks.length) {
                    return fluidConsumer.apply(invFluids, fluidSlot);
                }
            }
        }
        throw new IndexOutOfBoundsException(String.format("Slot index %d out of bounds! |items| = %d, |fluids| = %d", slot,
                invItems != null ? invItems.getSlots() : 0, invFluids != null ? invFluids.getTankProperties().length : 0));
    }

    @FunctionalInterface
    private interface Op<C, T> {

        T apply(C collection, int index);

    }

}
