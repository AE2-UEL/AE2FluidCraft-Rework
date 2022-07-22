package com.glodblock.github.common.tabs;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.loader.ItemAndBlockHolder;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class FluidCraftingTabs extends CreativeTabs {

    public static final FluidCraftingTabs INSTANCE = new FluidCraftingTabs(FluidCraft.MODID);

    public FluidCraftingTabs(String name){
        super(name);
    }

    @Override
    public Item getTabIconItem() {
        return ItemAndBlockHolder.DISCRETIZER.stack().getItem();
    }
}
