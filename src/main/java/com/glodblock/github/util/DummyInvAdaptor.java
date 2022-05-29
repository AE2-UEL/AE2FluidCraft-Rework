package com.glodblock.github.util;

import appeng.api.config.FuzzyMode;
import appeng.util.InventoryAdaptor;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.ItemSlot;
import net.minecraft.item.ItemStack;

import java.util.Iterator;

/**
 * This a dummy InventoryAdaptor, it has infinity capacity and infinity any items.
 */
public class DummyInvAdaptor extends InventoryAdaptor {

    public static final DummyInvAdaptor INSTANCE = new DummyInvAdaptor();

    @Override
    public ItemStack removeItems(int i, ItemStack itemStack, IInventoryDestination iInventoryDestination) {
        return itemStack;
    }

    @Override
    public ItemStack simulateRemove(int i, ItemStack itemStack, IInventoryDestination iInventoryDestination) {
        return itemStack;
    }

    @Override
    public ItemStack removeSimilarItems(int i, ItemStack itemStack, FuzzyMode fuzzyMode, IInventoryDestination iInventoryDestination) {
        return itemStack;
    }

    @Override
    public ItemStack simulateSimilarRemove(int i, ItemStack itemStack, FuzzyMode fuzzyMode, IInventoryDestination iInventoryDestination) {
        return itemStack;
    }

    @Override
    public ItemStack addItems(ItemStack itemStack) {
        return null;
    }

    @Override
    public ItemStack simulateAdd(ItemStack itemStack) {
        return null;
    }

    @Override
    public boolean containsItems() {
        return true;
    }

    @Override
    public boolean hasSlots() {
        return false;
    }

    @Override
    public Iterator<ItemSlot> iterator() {
        return null;
    }
}
