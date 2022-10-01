package com.glodblock.github.inventory;

import appeng.tile.inventory.AppEngInternalInventory;
import com.glodblock.github.util.Ae2Reflect;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

public class ExAppEngInternalInventory extends AppEngInternalInventory {

    public ExAppEngInternalInventory(AppEngInternalInventory inv) {
        super(inv.getTileEntity(), inv.getSlots(), inv.getSlotLimit(0), Ae2Reflect.getInventoryFilter(inv));
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagList nbtTagList = new NBTTagList();
        for (int i = 0; i < stacks.size(); i++)
        {
            if (!stacks.get(i).isEmpty())
            {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setInteger("Slot", i);
                stacks.get(i).writeToNBT(itemTag);
                itemTag.setInteger("Count", stacks.get(i).getCount());
                nbtTagList.appendTag(itemTag);
            }
        }
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("Items", nbtTagList);
        nbt.setInteger("Size", stacks.size());
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        setSize(nbt.hasKey("Size", Constants.NBT.TAG_INT) ? nbt.getInteger("Size") : stacks.size());
        NBTTagList tagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound itemTags = tagList.getCompoundTagAt(i);
            int slot = itemTags.getInteger("Slot");

            if (slot >= 0 && slot < stacks.size())
            {
                ItemStack tmp = new ItemStack(itemTags);
                tmp.setCount(itemTags.getInteger("Count"));
                stacks.set(slot, tmp);
            }
        }
        onLoad();
    }

}
