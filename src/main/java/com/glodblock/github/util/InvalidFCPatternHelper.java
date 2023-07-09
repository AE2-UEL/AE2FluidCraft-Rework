package com.glodblock.github.util;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

public class InvalidFCPatternHelper {
    private final List<PatternIngredient> outputs = new ArrayList<>();
    private final List<PatternIngredient> inputs = new ArrayList<>();

    public InvalidFCPatternHelper(ItemStack is) {
        CompoundNBT encodedValue = is.getTag();
        if (encodedValue == null) {
            throw new IllegalArgumentException("No pattern here!");
        } else {
            ListNBT inTag = encodedValue.getList("in", 10);
            ListNBT outTag = encodedValue.getList("out", 10);

            int i;
            for(i = 0; i < outTag.size(); ++i) {
                this.outputs.add(new PatternIngredient(outTag.getCompound(i)));
            }

            for(i = 0; i < inTag.size(); ++i) {
                CompoundNBT in = inTag.getCompound(i);
                if (!in.isEmpty()) {
                    this.inputs.add(new PatternIngredient(in));
                }
            }
        }
    }

    public List<PatternIngredient> getOutputs() {
        return this.outputs;
    }

    public List<PatternIngredient> getInputs() {
        return this.inputs;
    }

    public static class PatternIngredient {

        private final IAEItemStack stack;
        private String id;
        private int count;
        private int damage;

        public PatternIngredient(CompoundNBT tag) {
            this.stack = AEItemStack.fromNBT(tag);
            if (this.isValid()) {
                CompoundNBT is = tag.getCompound("is");
                this.id = is.getString("id");
                this.count = (int) this.stack.getStackSize();
                this.damage = Math.max(0, tag.getShort("Damage"));
            }
        }

        public boolean isValid() {
            return this.stack != null;
        }

        public ITextComponent getName() {
            return this.isValid() ? Platform.getItemDisplayName(this.stack) : new StringTextComponent(this.id + '@' + this.getDamage());
        }

        public int getDamage() {
            return this.isValid() ? this.stack.getItemDamage() : this.damage;
        }

        public int getCount() {
            return this.isValid() ? (int) this.stack.getStackSize() : this.count;
        }

        public ITextComponent getFormattedToolTip() {
            IFormattableTextComponent result = (new StringTextComponent(this.getCount() + " ")).appendSibling(this.getName());
            if (!this.isValid()) {
                result.mergeStyle(TextFormatting.RED);
            }
            return result;
        }
    }
}