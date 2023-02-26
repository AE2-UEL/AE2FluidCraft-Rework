package com.glodblock.github.interfaces;

import net.minecraft.item.ItemStack;

import java.util.HashMap;

public interface PatternConsumer {

    void acceptPattern(HashMap<Integer, ItemStack[]> inputs, ItemStack[] outputs, boolean compress);

}