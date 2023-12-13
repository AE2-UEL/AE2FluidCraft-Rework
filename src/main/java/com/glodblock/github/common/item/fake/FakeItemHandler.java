package com.glodblock.github.common.item.fake;

import appeng.api.storage.data.IAEItemStack;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public interface FakeItemHandler<V, A> {

    V getStack(ItemStack stack);

    V getStack(@Nullable IAEItemStack stack);

    A getAEStack(ItemStack stack);

    A getAEStack(@Nullable IAEItemStack stack);

    ItemStack packStack(V target);

    ItemStack displayStack(V target);

    IAEItemStack packAEStack(V target);

    IAEItemStack packAEStackLong(A target);

}
