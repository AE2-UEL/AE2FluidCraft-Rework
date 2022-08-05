package com.glodblock.github.common.block;

import appeng.block.AEBaseItemBlock;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.common.tile.TileFluidDiscretizer;
import com.glodblock.github.util.NameConst;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;

public class BlockFluidDiscretizer extends FCBaseBlock {

    public BlockFluidDiscretizer() {
        super(Material.iron, NameConst.BLOCK_FLUID_DISCRETIZER);
        setFullBlock(true);
        setOpaque(true);
        setTileEntity(TileFluidDiscretizer.class);
    }

    public BlockFluidDiscretizer register() {
        GameRegistry.registerBlock(this, AEBaseItemBlock.class, NameConst.BLOCK_FLUID_DISCRETIZER);
        GameRegistry.registerTileEntity(TileFluidDiscretizer.class, NameConst.BLOCK_FLUID_DISCRETIZER);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    public ItemStack stack(int size) {
        return new ItemStack(this, size);
    }

    public ItemStack stack() {
        return new ItemStack(this, 1);
    }

}
