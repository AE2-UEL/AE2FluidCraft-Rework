package com.glodblock.github.common.item;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.items.misc.ItemEncodedPattern;
import com.glodblock.github.interfaces.HasCustomModel;
import com.glodblock.github.util.FluidPatternDetails;
import com.glodblock.github.util.NameConst;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ItemFluidEncodedPattern extends ItemEncodedPattern implements HasCustomModel {

    @Override
    protected void getCheckedSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> itemStacks) {
        // NO-OP
    }

    @Nullable
    @Override
    public ICraftingPatternDetails getPatternForItem(ItemStack is, World w) {
        FluidPatternDetails pattern = new FluidPatternDetails(is);
        return pattern.readFromStack() ? pattern : null;
    }

    @Override
    public ResourceLocation getCustomModelPath() {
        return NameConst.MODEL_DENSE_ENCODED_PATTERN;
    }

}