package com.glodblock.github.inventory.slot;

import com.glodblock.github.util.Util;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class SlotSingleItem extends Slot {

    private final Slot delegate;

    public SlotSingleItem(Slot delegate) {
        super(delegate.inventory, delegate.getSlotIndex(), delegate.xDisplayPosition, delegate.yDisplayPosition);
        this.delegate = delegate;
    }

    @Override
    public ItemStack getStack() {
        ItemStack stack = delegate.getStack();
        return stack == null ? null : Util.copyStackWithSize(stack, 1);
    }

    // delegated

    @Override
    public void onSlotChange(ItemStack p_75220_1_, ItemStack p_75220_2_) {
        delegate.onSlotChange(p_75220_1_, p_75220_2_);
    }

    @Override
    public void onPickupFromSlot(EntityPlayer thePlayer, ItemStack stack) {
        delegate.onPickupFromSlot(thePlayer, stack);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return delegate.isItemValid(stack);
    }

    @Override
    public boolean getHasStack() {
        return delegate.getHasStack();
    }

    @Override
    public void putStack(ItemStack stack) {
        delegate.putStack(stack);
    }

    @Override
    public void onSlotChanged() {
        delegate.onSlotChanged();
    }

    @Override
    public int getSlotStackLimit() {
        return delegate.getSlotStackLimit();
    }

    @Override
    public ItemStack decrStackSize(int amount) {
        return delegate.decrStackSize(amount);
    }

    @Override
    public boolean isSlotInInventory(IInventory inv, int slotIn) {
        return delegate.isSlotInInventory(inv, slotIn);
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        return delegate.canTakeStack(playerIn);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean func_111238_b() {
        return delegate.func_111238_b();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ResourceLocation getBackgroundIconTexture() {
        return delegate.getBackgroundIconTexture();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setBackgroundIconTexture(ResourceLocation texture) {
        delegate.setBackgroundIconTexture(texture);
    }

    @Override
    public int getSlotIndex() {
        return delegate.getSlotIndex();
    }

}
