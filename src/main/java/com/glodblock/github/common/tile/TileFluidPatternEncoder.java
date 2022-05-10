package com.glodblock.github.common.tile;

import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.tile.AEBaseTile;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import com.glodblock.github.inventory.AeStackInventory;
import com.glodblock.github.inventory.AeStackInventoryImpl;
import com.glodblock.github.util.BlockPos;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.List;

public class TileFluidPatternEncoder extends AEBaseTile implements IAEAppEngInventory {

    private final AppEngInternalInventory patternInv = new AppEngInternalInventory(this, 2);
    private final AeStackInventoryImpl<IAEItemStack> crafting = new AeStackInventoryImpl<>(StorageChannel.ITEMS, 9, this);
    private final AeStackInventoryImpl<IAEItemStack> output = new AeStackInventoryImpl<>(StorageChannel.ITEMS, 3, this);

    public IInventory getInventory() {
        return patternInv;
    }

    public AeStackInventory<IAEItemStack> getCraftingSlots() {
        return crafting;
    }

    public AeStackInventory<IAEItemStack> getOutputSlots() {
        return output;
    }

    @Override
    public boolean canBeRotated() {
        return false;
    }

    @Override
    public void getDrops(final World w, final int x, final int y, final int z, final List<ItemStack> drops ) {
        getDrops(w, new BlockPos(x, y, z), drops);
    }

    public void getDrops(World world, BlockPos pos, List<ItemStack> drops) {
        for (ItemStack stack : patternInv) {
            if (stack != null) {
                drops.add(stack);
            }
        }
    }

    @TileEvent( TileEventType.WORLD_NBT_READ )
    public void readFromNBTEvent(NBTTagCompound data) {
        patternInv.readFromNBT(data, "Inventory");
        crafting.readFromNbt(data, "CraftingSlots");
        output.readFromNbt(data, "OutputSlots");
    }

    @TileEvent( TileEventType.WORLD_NBT_WRITE )
    public NBTTagCompound writeToNBTEvent(NBTTagCompound data) {
        patternInv.writeToNBT(data, "Inventory");
        crafting.writeToNbt(data, "CraftingSlots");
        output.writeToNbt(data, "OutputSlots");
        return data;
    }

    @Override
    public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack) {

    }

}
