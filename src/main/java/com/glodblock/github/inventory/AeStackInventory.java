package com.glodblock.github.inventory;

import appeng.api.storage.data.IAEStack;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public interface AeStackInventory<T extends IAEStack<T>> extends Iterable<T> {

    int getSlotCount();

    @Nullable
    T getStack(int slot);

    void setStack(int slot, @Nullable T stack);

    Stream<T> stream();

}
