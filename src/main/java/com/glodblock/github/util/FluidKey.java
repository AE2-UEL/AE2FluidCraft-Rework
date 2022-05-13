package com.glodblock.github.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.Objects;

public class FluidKey {

    private final Fluid fluid;
    @Nullable
    private final NBTTagCompound tag;

    public FluidKey(Fluid fluid, @Nullable NBTTagCompound tag) {
        this.fluid = fluid;
        this.tag = tag;
    }

    public FluidKey(FluidStack fluid) {
        this(fluid.getFluid(), fluid.tag);
    }

    @Override
    public int hashCode() {
        return fluid.getName().hashCode() ^ (tag != null ? Integer.rotateLeft(tag.hashCode(), 17) : 0x4e6f5467); // NoTg
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FluidKey)) {
            return false;
        }
        return fluid.getName().equals(((FluidKey)obj).fluid.getName()) && Objects.equals(tag, ((FluidKey)obj).tag);
    }

}