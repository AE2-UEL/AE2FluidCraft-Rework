package com.glodblock.github.interfaces;

import appeng.api.storage.data.IAEItemStack;

import javax.annotation.Nullable;

public interface SlotFluid {

    @Nullable
    IAEItemStack getAeStack();

    void setAeStack(@Nullable IAEItemStack stack, boolean sync);

}
