package com.glodblock.github.integration.builder;

import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.util.FCUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RecipeTransferBuilder {

    private static final int MAX_ITEMS = 16;

    private final Int2ObjectArrayMap<ItemStack[]> in;
    private final int bound;
    private final ItemStack[] out;
    private final IRecipeLayout recipe;
    private List<ItemStack[]> itemsIn;
    private List<FluidStack> fluidIn;
    private List<ItemStack> itemOut;
    private List<FluidStack> fluidOut;
    private boolean noNull = true;
    private boolean fluidFirst = false;

    public RecipeTransferBuilder(int maxInput, int maxOutput, IRecipeLayout recipe) {
        this.in = new Int2ObjectArrayMap<>();
        this.bound = maxInput;
        this.out = new ItemStack[maxOutput];
        this.recipe = recipe;
        this.itemsIn = new ArrayList<>();
        this.itemOut = new ArrayList<>();
        this.fluidIn = new ArrayList<>();
        this.fluidOut = new ArrayList<>();
        this.split();
    }

    private void split() {
        for (int index : FCUtil.sort(this.recipe.getItemStacks().getGuiIngredients().keySet())) {
            IGuiIngredient<ItemStack> ing = this.recipe.getItemStacks().getGuiIngredients().get(index);
            if (ing == null) {
                continue;
            }
            if (ing.isInput()) {
                List<ItemStack> holder;
                if (ing.getAllIngredients().size() < MAX_ITEMS - 1) {
                    holder = ing.getAllIngredients();
                } else {
                    holder = ing.getAllIngredients().subList(0, MAX_ITEMS - 1);
                }
                // Put displayed item at first check
                if (ing.getDisplayedIngredient() != null) {
                    holder.add(0, ing.getDisplayedIngredient());
                }
                this.itemsIn.add(holder.toArray(new ItemStack[0]));
            } else {
                this.itemOut.add(ing.getDisplayedIngredient());
            }
        }
        for (int index : FCUtil.sort(this.recipe.getFluidStacks().getGuiIngredients().keySet())) {
            IGuiIngredient<FluidStack> ing = this.recipe.getFluidStacks().getGuiIngredients().get(index);
            if (ing == null) {
                continue;
            }
            if (ing.isInput()) {
                this.fluidIn.add(ing.getDisplayedIngredient());
            } else {
                this.fluidOut.add(ing.getDisplayedIngredient());
            }
        }
    }

    private void setItemIn(int offset) {
        int bound = Math.min(this.bound, this.itemsIn.size() + offset);
        for (int index = offset; index < bound; index ++) {
            int i = index - offset;
            if (this.itemsIn.get(i) != null && this.itemsIn.get(i).length > 0) {
                this.in.put(index, this.itemsIn.get(i));
            }
        }
    }

    private void setFluidIn(int offset) {
        int bound = Math.min(this.bound, this.fluidIn.size() + offset);
        for (int index = offset; index < bound; index ++) {
            int i = index - offset;
            if (this.fluidIn.get(i) != null) {
                this.in.put(index, new ItemStack[] {ItemFluidPacket.newStack(this.fluidIn.get(i))});
            }
        }
    }

    private void setOutputs() {
        for (int index = 0; index < this.out.length; index ++) {
            if (index < this.itemOut.size()) {
                this.out[index] = this.itemOut.get(index);
            } else if (index - this.itemOut.size() < this.fluidOut.size()) {
                this.out[index] = ItemFluidPacket.newStack(this.fluidOut.get(index - this.itemOut.size()));
            }
        }
    }

    public RecipeTransferBuilder clearEmptySlot(boolean val) {
        this.noNull = val;
        return this;
    }

    public RecipeTransferBuilder putFluidFirst(boolean val) {
        this.fluidFirst = val;
        return this;
    }

    public RecipeTransferBuilder build() {
        if (this.noNull) {
            this.itemsIn = this.itemsIn.stream().filter(o -> o != null && o.length > 0).collect(Collectors.toList());
            this.itemOut = this.itemOut.stream().filter(Objects::nonNull).collect(Collectors.toList());
            this.fluidIn = this.fluidIn.stream().filter(Objects::nonNull).collect(Collectors.toList());
            this.fluidOut = this.fluidOut.stream().filter(Objects::nonNull).collect(Collectors.toList());
        }
        if (this.fluidFirst) {
            this.setFluidIn(0);
            this.setItemIn(this.fluidIn.size());
        } else {
            this.setItemIn(0);
            this.setFluidIn(this.itemsIn.size());
        }
        this.setOutputs();
        return this;
    }

    public ItemStack[] getOutput() {
        return this.out;
    }

    public Int2ObjectMap<ItemStack[]> getInput() {
        return this.in;
    }

}
