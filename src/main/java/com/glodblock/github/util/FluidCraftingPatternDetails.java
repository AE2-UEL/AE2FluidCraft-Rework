package com.glodblock.github.util;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.ContainerNull;
import appeng.helpers.PatternHelper;
import appeng.util.item.AEItemStack;
import com.glodblock.github.common.item.ItemFluidDrop;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;

public class FluidCraftingPatternDetails implements ICraftingPatternDetails, Comparable<ICraftingPatternDetails> {

    private final IAEItemStack[] containerInputs = new IAEItemStack[9];
    private final IAEItemStack[] remainingInputs = new IAEItemStack[9];
    private final IAEItemStack[] containerOutputs = new IAEItemStack[1];
    private final IAEItemStack[] fluidInputs = new IAEItemStack[9];
    private final ItemStack pattern;
    private final boolean canSubstitute;
    private final boolean isNecessary;
    private int priority = 0;

    public static FluidCraftingPatternDetails GetFluidPattern(ItemStack pattern, World w) {
        try {
            new PatternHelper(pattern, w);
        } catch (Throwable t) {
            return null;
        }
        try {
            return new FluidCraftingPatternDetails(pattern, w);
        } catch (Throwable t) {
            return null;
        }
    }

    public FluidCraftingPatternDetails(ItemStack pattern, World w) {
        NBTTagCompound encodedValue = pattern.getTagCompound();
        this.pattern = pattern;
        if (encodedValue == null) {
            throw new IllegalArgumentException("No pattern here!");
        } else {
            if (!encodedValue.getBoolean("crafting")) {
                throw new IllegalArgumentException("Not Crafting pattern!");
            }
            NBTTagList inTag = encodedValue.getTagList("in", 10);
            this.canSubstitute = encodedValue.getBoolean("substitute");
            InventoryCrafting crafting = new InventoryCrafting(new ContainerNull(), 3, 3);
            for(int x = 0; x < inTag.tagCount(); x++) {
                NBTTagCompound resultItemTag = inTag.getCompoundTagAt(x);
                ItemStack gs = new ItemStack(resultItemTag);
                if (resultItemTag.hasKey("stackSize")) {
                    gs.setCount(resultItemTag.getInteger("stackSize"));
                }
                crafting.setInventorySlotContents(x, gs);
                this.containerInputs[x] = AEItemStack.fromItemStack(gs);
            }
            IRecipe standardRecipe = CraftingManager.findMatchingRecipe(crafting, w);
            if (standardRecipe == null) {
                throw new IllegalStateException("No pattern here!");
            }
            ItemStack outputItem = standardRecipe.getCraftingResult(crafting);
            List<ItemStack> remain = standardRecipe.getRemainingItems(crafting);
            for (int x = 0; x < remain.size(); x++) {
                this.remainingInputs[x] = AEItemStack.fromItemStack(remain.get(x));
            }
            this.containerOutputs[0] = AEItemStack.fromItemStack(outputItem);
        }
        for (int x = 0; x < 9; x++) {
            IAEItemStack filledContainer = this.containerInputs[x];
            IAEItemStack emptyContainer = this.remainingInputs[x];
            if (filledContainer != null && emptyContainer != null && Util.getFluidFromItem(filledContainer.getDefinition()) != null) {
                ItemStack drained = Util.getEmptiedContainer(filledContainer.getDefinition());
                if (emptyContainer.equals(drained)) {
                    this.fluidInputs[x] = ItemFluidDrop.newAeStack(Util.getFluidFromItem(filledContainer.getDefinition()));
                    continue;
                }
            }
            this.fluidInputs[x] = filledContainer;
        }
        this.isNecessary = Arrays.stream(this.fluidInputs).anyMatch(t -> t != null && t.getItem() instanceof ItemFluidDrop);
    }

    @Override
    public ItemStack getPattern() {
        return this.pattern;
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
        return this.fluidInputs;
    }

    @Override
    public IAEItemStack[] getCondensedInputs() {
        return FluidPatternDetails.condenseStacks(this.fluidInputs);
    }

    @Override
    public IAEItemStack[] getCondensedOutputs() {
        return this.containerOutputs;
    }

    @Override
    public IAEItemStack[] getOutputs() {
        return this.containerOutputs;
    }

    public IAEItemStack[] getOriginInputs() {
        return this.containerInputs;
    }

    @Override
    public boolean canSubstitute() {
        return canSubstitute;
    }

    @Override
    public ItemStack getOutput(InventoryCrafting inventoryCrafting, World world) {
        return null;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int i) {
        this.priority = i;
    }

    @Override
    public int compareTo(ICraftingPatternDetails o) {
        return Integer.compare(o.getPriority(), this.priority);
    }

    public boolean isNecessary() {
        return this.isNecessary;
    }
}
