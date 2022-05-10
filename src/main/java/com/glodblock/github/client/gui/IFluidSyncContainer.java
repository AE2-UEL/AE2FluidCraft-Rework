package com.glodblock.github.client.gui;

import appeng.api.storage.data.IAEFluidStack;

import java.util.Map;

public interface IFluidSyncContainer {
    void receiveFluidSlots( final Map<Integer, IAEFluidStack> fluids );
}
