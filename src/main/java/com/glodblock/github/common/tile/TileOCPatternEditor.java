package com.glodblock.github.common.tile;

import appeng.items.misc.ItemEncodedPattern;
import appeng.tile.AEBaseInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

public class TileOCPatternEditor extends AEBaseInvTile {

    private final AppEngInternalInventory invItems = new AppEngInternalInventory(this, 16);

    @Override
    public IInventory getInternalInventory() {
        return invItems;
    }

    @Override
    public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added) {
        markForUpdate();
    }

    @Override
    public int[] getAccessibleSlotsBySide(ForgeDirection whichSide) {
        return new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    }

    @Override
    public boolean isItemValidForSlot( final int i, final ItemStack itemstack ) {
        return itemstack == null || itemstack.getItem() instanceof ItemEncodedPattern;
    }

}
