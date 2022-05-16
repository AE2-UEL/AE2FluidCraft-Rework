package com.glodblock.github.inventory;

import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import com.glodblock.github.util.ObjectArrayIterator;
import com.glodblock.github.util.Util;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

public class AeStackInventoryImpl <T extends IAEStack<T>> implements AeStackInventory<T> {

    private final StorageChannel channel;
    private final T[] inv;
    private final IAEAppEngInventory owner;

    @SuppressWarnings("unchecked")
    public AeStackInventoryImpl(StorageChannel channel, int slotCount, @Nullable IAEAppEngInventory owner) {
        this.channel = channel;
        this.inv = (T[])new IAEStack[slotCount];
        this.owner = owner;
    }

    public AeStackInventoryImpl(StorageChannel channel, int slotCount) {
        this(channel, slotCount, null);
    }

    @Override
    public int getSlotCount() {
        return inv.length;
    }

    @Override
    @Nullable
    public T getStack(int slot) {
        return inv[slot];
    }

    @Override
    public void setStack(int slot, @Nullable T stack) {
        inv[slot] = stack;
        if (owner != null) {
            owner.saveChanges();
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new ObjectArrayIterator<>(inv);
    }

    @Override
    public Stream<T> stream() {
        return Arrays.stream(inv);
    }

    public void writeToNbt(NBTTagCompound tag) {
        NBTTagList stacksTag = new NBTTagList();
        for (T stack : inv) {
            if (stack == null) {
                stacksTag.appendTag(new NBTTagCompound());
            } else {
                NBTTagCompound stackTag = new NBTTagCompound();
                stack.writeToNBT(stackTag);
                stacksTag.appendTag(stackTag);
            }
        }
        tag.setTag("Contents", stacksTag);
    }

    public void writeToNbt(NBTTagCompound parentTag, String key) {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNbt(tag);
        parentTag.setTag(key, tag);
    }

    @SuppressWarnings("unchecked")
    public void readFromNbt(NBTTagCompound tag) {
        NBTTagList stacksTag = tag.getTagList("Contents", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < inv.length; i++) {
            NBTTagCompound stackTag = stacksTag.getCompoundTagAt(i);
            if (channel == StorageChannel.ITEMS) {
                inv[i] = stackTag == null ? null : (T) AEItemStack.loadItemStackFromNBT(stackTag);
            }
            else {
                inv[i] = stackTag == null ? null : (T) Util.loadFluidStackFromNBT(stackTag);
            }
        }
    }

    public void readFromNbt(NBTTagCompound parentTag, String key) {
        readFromNbt(parentTag.getCompoundTag(key));
    }

}
