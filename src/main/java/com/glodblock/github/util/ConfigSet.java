package com.glodblock.github.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ConfigSet {

    private final Object2ObjectMap<String, Consumer<?>> MAP_IN = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<String, Supplier<?>> MAP_OUT = new Object2ObjectOpenHashMap<>();

    public ConfigSet addConfig(String id, Consumer<?> in, Supplier<?> out) {
        MAP_IN.put(id, in);
        MAP_OUT.put(id, out);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> void setConfig(String id, T value) {
        if (MAP_IN.containsKey(id)) {
            ((Consumer<T>) MAP_IN.get(id)).accept(value);
        } else {
            throw new IllegalArgumentException("This config id doesn't exist: " + id);
        }
    }

    public Object getConfig(String id) {
        if (MAP_OUT.containsKey(id)) {
            return MAP_OUT.get(id).get();
        } else {
            throw new IllegalArgumentException("This config id doesn't exist: " + id);
        }
    }

}
