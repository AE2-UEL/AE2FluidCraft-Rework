package com.glodblock.github.common.block;

import appeng.block.AEBaseItemBlock;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.common.tile.TileLargeIngredientBuffer;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.NameConst;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BlockLargeIngredientBuffer extends FCBaseBlock {

    public BlockLargeIngredientBuffer() {
        super(Material.iron, NameConst.BLOCK_LARGE_INGREDIENT_BUFFER);
        setTileEntity(TileLargeIngredientBuffer.class);
        setOpaque(false);
        setFullBlock(false);
        this.lightOpacity = 4;
    }

    @Override
    public boolean onActivated(World world, int x, int y, int z, EntityPlayer player, int facing, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) {
            return false;
        }
        TileLargeIngredientBuffer tile = getTileEntity(world, x, y, z);
        if (tile != null) {
            if (!world.isRemote) {
                InventoryHandler.openGui(player, world, new BlockPos(x, y, z), EnumFacing.getFront(facing), GuiType.LARGE_INGREDIENT_BUFFER);
            }
            return true;
        }
        return false;
    }

    public BlockLargeIngredientBuffer register() {
        GameRegistry.registerBlock(this, AEBaseItemBlock.class, NameConst.BLOCK_LARGE_INGREDIENT_BUFFER);
        GameRegistry.registerTileEntity(TileLargeIngredientBuffer.class, NameConst.BLOCK_LARGE_INGREDIENT_BUFFER);
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
