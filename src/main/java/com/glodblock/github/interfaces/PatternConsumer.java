package com.glodblock.github.interfaces;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;

public interface PatternConsumer {

    void acceptPattern(Int2ObjectMap<ItemStack[]> inputs, ItemStack[] outputs, boolean compress);

}