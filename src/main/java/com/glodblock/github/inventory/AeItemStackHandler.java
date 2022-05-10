package com.glodblock.github.inventory;

import appeng.api.storage.data.IAEItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class AeItemStackHandler implements IInventory {

    private final AeStackInventory<IAEItemStack> inv;

    public AeItemStackHandler(AeStackInventory<IAEItemStack> inv) {
        this.inv = inv;
    }

    public AeStackInventory<IAEItemStack> getAeInventory() {
        return inv;
    }

    @Override
    public int getSizeInventory() {
        return inv.getSlotCount();
    }

    public ItemStack getStackInSlot(int slot) {
        IAEItemStack stack = inv.getStack(slot);
        return stack != null ? stack.getItemStack() : null;
    }

    @Override
    public ItemStack decrStackSize(int p_70298_1_, int p_70298_2_) {
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int p_70304_1_) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int p_70299_1_, ItemStack p_70299_2_) {

    }

    @Override
    public String getInventoryName() {
        return "AeItemStackHandler";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer p_70300_1_) {
        return false;
    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

    }

    @Override
    public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
        return false;
    }

}
