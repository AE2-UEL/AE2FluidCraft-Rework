package com.glodblock.github.util;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class FluidCraftingPatternDetails implements ICraftingPatternDetails, Comparable<FluidPatternDetails> {

    @Override
    public ItemStack getPattern() {
        return null;
    }

    @Override
    public boolean isValidItemForSlot(int i, ItemStack itemStack, World world) {
        return false;
    }

    @Override
    public boolean isCraftable() {
        return false;
    }

    @Override
    public IAEItemStack[] getInputs() {
        return new IAEItemStack[0];
    }

    @Override
    public IAEItemStack[] getCondensedInputs() {
        return new IAEItemStack[0];
    }

    @Override
    public IAEItemStack[] getCondensedOutputs() {
        return new IAEItemStack[0];
    }

    @Override
    public IAEItemStack[] getOutputs() {
        return new IAEItemStack[0];
    }

    @Override
    public boolean canSubstitute() {
        return false;
    }

    @Override
    public ItemStack getOutput(InventoryCrafting inventoryCrafting, World world) {
        return null;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void setPriority(int i) {

    }

    @Override
    public int compareTo(FluidPatternDetails o) {
        return 0;
    }
}
