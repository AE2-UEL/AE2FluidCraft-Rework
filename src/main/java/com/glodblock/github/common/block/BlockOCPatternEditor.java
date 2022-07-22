package com.glodblock.github.common.block;

import appeng.block.AEBaseItemBlock;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.common.tile.TileOCPatternEditor;
import com.glodblock.github.crossmod.opencomputers.OCDriverInit;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.NameConst;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BlockOCPatternEditor extends FCBaseBlock {

    public BlockOCPatternEditor() {
        super(Material.iron, NameConst.BLOCK_OC_PATTERN_EDITOR);
        setTileEntity(TileOCPatternEditor.class);
        setFullBlock(true);
        setOpaque(true);
    }

    @Override
    public boolean onActivated(World world, int x, int y, int z, EntityPlayer player, int facing, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) {
            return false;
        }
        TileOCPatternEditor tile = getTileEntity(world, x, y, z);
        if (tile != null) {
            if (!world.isRemote) {
                InventoryHandler.openGui(player, world, new BlockPos(x, y, z), EnumFacing.getFront(facing), GuiType.OC_PATTERN_EDITOR);
            }
            return true;
        }
        return false;
    }

    public BlockOCPatternEditor register() {
        if (ModAndClassUtil.OC) {
            GameRegistry.registerBlock(this, AEBaseItemBlock.class, NameConst.BLOCK_OC_PATTERN_EDITOR);
            GameRegistry.registerTileEntity(TileOCPatternEditor.class, NameConst.BLOCK_OC_PATTERN_EDITOR);
            OCDriverInit.run();
            setCreativeTab(FluidCraftingTabs.INSTANCE);
            return this;
        }
        return null;
    }

    public ItemStack stack(int size) {
        return new ItemStack(this, size);
    }

    public ItemStack stack() {
        return new ItemStack(this, 1);
    }

}
