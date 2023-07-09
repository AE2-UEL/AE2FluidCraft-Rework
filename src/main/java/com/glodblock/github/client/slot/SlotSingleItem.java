package com.glodblock.github.client.slot;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SlotSingleItem extends Slot {

    private final Slot delegate;

    public SlotSingleItem(Slot delegate) {
        super(delegate.inventory, delegate.getSlotIndex(), delegate.xPos, delegate.yPos);
        this.delegate = delegate;
    }

    @Override
    @Nonnull
    public ItemStack getStack() {
        ItemStack stack = delegate.getStack();
        return stack.isEmpty() ? ItemStack.EMPTY : ItemHandlerHelper.copyStackWithSize(stack, 1);
    }

    // delegated

    @Override
    public void onSlotChange(@Nonnull ItemStack p_75220_1_, @Nonnull ItemStack p_75220_2_) {
        delegate.onSlotChange(p_75220_1_, p_75220_2_);
    }

    @Override
    @Nonnull
    public ItemStack onTake(@Nonnull PlayerEntity thePlayer, @Nonnull ItemStack stack) {
        return delegate.onTake(thePlayer, stack);
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return delegate.isItemValid(stack);
    }

    @Override
    public boolean getHasStack() {
        return delegate.getHasStack();
    }

    @Override
    public void putStack(@Nonnull ItemStack stack) {
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
    public int getItemStackLimit(@Nonnull ItemStack stack) {
        return delegate.getItemStackLimit(stack);
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    @Override
    public Pair<ResourceLocation, ResourceLocation> getBackground() {
        return this.delegate.getBackground();
    }

    @Override
    @Nonnull
    public ItemStack decrStackSize(int amount) {
        return delegate.decrStackSize(amount);
    }

    @Override
    public boolean canTakeStack(@Nonnull PlayerEntity playerIn) {
        return delegate.canTakeStack(playerIn);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean isEnabled() {
        return delegate.isEnabled();
    }

    @Override
    public int getSlotIndex() {
        return delegate.getSlotIndex();
    }

    @Override
    public boolean isSameInventory(@Nonnull Slot other) {
        return delegate.isSameInventory(other);
    }

    @Nonnull
    @Override
    public Slot setBackground(@Nonnull ResourceLocation rl1, @Nonnull ResourceLocation rl2) {
        return delegate.setBackground(rl1, rl2);
    }

}
