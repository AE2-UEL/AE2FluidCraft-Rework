package com.glodblock.github.common.item;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.items.misc.ItemEncodedPattern;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.util.FluidPatternDetails;
import com.glodblock.github.util.NameConst;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemFluidEncodedPattern extends ItemEncodedPattern {

    public ItemFluidEncodedPattern() {
        super();
        this.setUnlocalizedName(NameConst.ITEM_FLUID_ENCODED_PATTERN);
        this.setTextureName(FluidCraft.MODID + ":" + NameConst.ITEM_FLUID_ENCODED_PATTERN);
    }

    @Override
    public ICraftingPatternDetails getPatternForItem(ItemStack is, World w) {
        FluidPatternDetails pattern = new FluidPatternDetails(is);
        return pattern.readFromStack() ? pattern : null;
    }

    public ItemFluidEncodedPattern register() {
        GameRegistry.registerItem(this, NameConst.ITEM_FLUID_ENCODED_PATTERN, FluidCraft.MODID);
        return this;
    }

    public ItemStack stack() {
        return new ItemStack(this, 1);
    }

}
