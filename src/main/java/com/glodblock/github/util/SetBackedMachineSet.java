package com.glodblock.github.util;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

public class SetBackedMachineSet implements IMachineSet {

    private final Class<? extends IGridHost> machineClass;
    private final Set<IGridNode> backingSet;

    public SetBackedMachineSet(Class<? extends IGridHost> machineClass, Set<IGridNode> backingSet) {
        this.machineClass = machineClass;
        this.backingSet = backingSet;
    }

    @Nonnull
    @Override
    public Class<? extends IGridHost> getMachineClass() {
        return machineClass;
    }

    @Override
    public int size() {
        return backingSet.size();
    }

    @Override
    public boolean isEmpty() {
        return backingSet.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        //noinspection SuspiciousMethodCalls
        return backingSet.contains(o);
    }

    @Override
    @Nonnull
    public Iterator<IGridNode> iterator() {
        return backingSet.iterator();
    }

    @Override
    public void forEach(Consumer<? super IGridNode> action) {
        backingSet.forEach(action);
    }

    @Override
    public Spliterator<IGridNode> spliterator() {
        return backingSet.spliterator();
    }

}