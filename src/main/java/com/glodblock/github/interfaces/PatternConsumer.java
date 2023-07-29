package com.glodblock.github.interfaces;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface PatternConsumer {

    void acceptPattern(Int2ObjectMap<ItemStack[]> inputs, List<ItemStack> outputs, boolean compress);

}