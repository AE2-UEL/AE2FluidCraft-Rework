package com.glodblock.github.handler;

import appeng.api.storage.data.IAEItemStack;
import com.glodblock.github.interfaces.AeStackInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class AeItemStackHandler implements IItemHandler {

    private final AeStackInventory<IAEItemStack> inv;

    public AeItemStackHandler(AeStackInventory<IAEItemStack> inv) {
        this.inv = inv;
    }

    public AeStackInventory<IAEItemStack> getAeInventory() {
        return inv;
    }

    @Override
    public int getSlots() {
        return inv.getSlotCount();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        IAEItemStack stack = inv.getStack(slot);
        return stack != null ? stack.createItemStack() : ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

}
