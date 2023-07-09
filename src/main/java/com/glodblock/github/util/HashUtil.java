package com.glodblock.github.util;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.Hash;
import net.minecraft.fluid.Fluid;

import java.util.Objects;

public class HashUtil {

    public static final Hash.Strategy<Fluid> FLUID = new Hash.Strategy<Fluid>() {
        @Override
        public int hashCode(Fluid o) {
            return Objects.requireNonNull(o.getRegistryName()).hashCode();
        }

        @Override
        public boolean equals(Fluid a, Fluid b) {
            return a == b || (a != null && b != null && Objects.equals(a.getRegistryName(), b.getRegistryName()));
        }
    };

    public static final Hash.Strategy<Class<?>> CLASS = new Hash.Strategy<Class<?>>() {
        @Override
        public int hashCode(Class<?> o) {
            return o.getName().hashCode();
        }

        @Override
        public boolean equals(Class<?> a, Class<?> b) {
            return a == b || (a != null && b != null && Objects.equals(a.getName(), b.getName()));
        }
    };

}
