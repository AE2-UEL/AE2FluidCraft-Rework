package com.glodblock.github.util;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;

import java.util.*;

public class FluidPatternDetails implements ICraftingPatternDetails, Comparable<ICraftingPatternDetails> {

    private final ItemStack patternStack;
    private final IAEItemStack patternStackAe;
    private final IAEItemStack[] inputs;
    private final IAEItemStack[] outputs;
    private final List<IAEItemStack> inputsCond;
    private final List<IAEItemStack> outputsCond;
    private int priority = 0;

    private FluidPatternDetails(ItemStack pattern) {
        CompoundNBT encodedValue = pattern.getTag();
        this.patternStack = pattern;
        this.patternStackAe = Objects.requireNonNull(AEItemStack.fromItemStack(pattern)); // s2g
        if (encodedValue == null) {
            throw new IllegalArgumentException("No pattern here!");
        } else {
            ListNBT inTag = encodedValue.getList("in", 10);
            ListNBT outTag = encodedValue.getList("out", 10);
            this.inputs = new IAEItemStack[inTag.size()];
            this.outputs = new IAEItemStack[outTag.size()];
            for(int x = 0; x < inTag.size(); x++) {
                CompoundNBT ingredient = inTag.getCompound(x);
                if (!ingredient.isEmpty()) {
                    IAEItemStack stack = AEItemStack.fromNBT(ingredient);
                    this.inputs[x] = stack;
                }
            }
            for(int x = 0; x < outTag.size(); x++) {
                CompoundNBT ingredient = outTag.getCompound(x);
                if (!ingredient.isEmpty()) {
                    IAEItemStack stack = AEItemStack.fromNBT(ingredient);
                    this.outputs[x] = stack;
                }
            }
            this.inputsCond = condenseStacks(inputs);
            this.outputsCond = condenseStacks(outputs);
        }
    }

    public static FluidPatternDetails fromPattern(ItemStack pattern) {
        try {
            return new FluidPatternDetails(pattern);
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    public ItemStack getPattern() {
        return patternStack;
    }

    @Override
    public boolean isValidItemForSlot(int i, ItemStack itemStack, World world) {
        throw new IllegalStateException("Not a crafting recipe!");
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public boolean isCraftable() {
        return false;
    }

    @Override
    public List<IAEItemStack> getInputs() {
        return this.inputsCond;
    }

    @Override
    public List<IAEItemStack> getOutputs() {
        return this.outputsCond;
    }

    @Override
    public IAEItemStack[] getSparseInputs() {
        return this.inputs;
    }

    @Override
    public IAEItemStack[] getSparseOutputs() {
        return this.outputs;
    }

    @Override
    public boolean canSubstitute() {
        return false;
    }

    @Override
    public List<IAEItemStack> getSubstituteInputs(int i) {
        return Collections.emptyList();
    }

    @Override
    public ItemStack getOutput(CraftingInventory craftingInventory, World world) {
        throw new IllegalStateException("Not a crafting recipe!");
    }

    public static List<IAEItemStack> condenseStacks(IAEItemStack[] stacks) {
        Map<IAEItemStack, IAEItemStack> accMap = new HashMap<>();
        for (IAEItemStack stack : stacks) {
            if (stack != null) {
                IAEItemStack acc = accMap.get(stack);
                if (acc == null) {
                    accMap.put(stack, stack.copy());
                } else {
                    acc.add(stack);
                }
            }
        }
        return new ArrayList<>(accMap.values());
    }

    @Override
    public int hashCode() {
        return patternStackAe.hashCode();
    }

    @Override
    public int compareTo(ICraftingPatternDetails o) {
        return Integer.compare(o.getPriority(), this.priority);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FluidPatternDetails && patternStackAe.equals(((FluidPatternDetails)obj).patternStackAe);
    }

}