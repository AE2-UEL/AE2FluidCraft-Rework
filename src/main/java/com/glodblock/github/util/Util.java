package com.glodblock.github.util;

import appeng.api.AEApi;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.AEFluidStack;
import appeng.util.item.AEItemStack;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class Util {

    public static IStorageChannel<IAEFluidStack> getFluidChannel() {
        return AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
    }

    public static IStorageChannel<IAEItemStack> getItemChannel() {
        return AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
    }

    public static FluidStack getFluidFromItem(ItemStack stack) {
        if (!stack.isEmpty() && stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            if (stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) != null) {
                IFluidTankProperties[] tanks = Objects.requireNonNull(stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)).getTankProperties();
                for (IFluidTankProperties tank : tanks) {
                    if (tank != null && tank.getContents() != null) {
                        return tank.getContents().copy();
                    }
                }
            }
        }
        return null;
    }

    public static ItemStack getEmptiedContainer(ItemStack stack) {
        if (!stack.isEmpty() && stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            ItemStack dummy = stack.copy();
            IFluidHandlerItem fh = dummy.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fh != null) {
                fh.drain(Integer.MAX_VALUE, true);
                return fh.getContainer();
            }
        }
        return stack;
    }

    public static void writeFluidInventoryToBuffer(@Nonnull AEFluidInventory inv, ByteBuf data) throws IOException {
        int fluidMask = 0;
        for (int i = 0; i < inv.getSlots(); i++) {
            if (inv.getFluidInSlot(i) != null) {
                fluidMask |= 1 << i;
            }
        }
        data.writeByte(fluidMask);
        for (int i = 0; i < inv.getSlots(); i++) {
            IAEFluidStack fluid = inv.getFluidInSlot(i);
            if (fluid != null) {
                fluid.writeToPacket(data);
            }
        }
    }

    public static boolean readFluidInventoryToBuffer(@Nonnull AEFluidInventory inv, ByteBuf data) throws IOException {
        boolean changed = false;
        int fluidMask = data.readByte();
        for (int i = 0; i < inv.getSlots(); i++) {
            if ((fluidMask & (1 << i)) != 0) {
                IAEFluidStack fluid = AEFluidStack.fromPacket(data);
                if (fluid != null) { // this shouldn't happen, but better safe than sorry
                    IAEFluidStack origFluid = inv.getFluidInSlot(i);
                    if (!fluid.equals(origFluid) || fluid.getStackSize() != origFluid.getStackSize()) {
                        inv.setFluidInSlot(i, fluid);
                        changed = true;
                    }
                }
            } else if (inv.getFluidInSlot(i) != null) {
                inv.setFluidInSlot(i, null);
                changed = true;
            }
        }
        return changed;
    }

    public static void fuzzyTransferItems(int slot, ItemStack[] inputs, ItemStack[] des, IItemList<IAEItemStack> storage) {
        if (slot < des.length && inputs.length > 0) {
            if (storage != null) {
                IAEItemStack select = AEItemStack.fromItemStack(inputs[0]);
                for (ItemStack item : inputs) {
                    IAEItemStack result = storage.findPrecise(AEItemStack.fromItemStack(item));
                    if (result != null) {
                        select = AEItemStack.fromItemStack(item);
                        break;
                    }
                }
                if (select != null) {
                    des[slot] = select.createItemStack();
                }
            } else {
                des[slot] = inputs[0];
            }
        }
    }

    public static void clearItemInventory(IItemHandlerModifiable inv) {
        for (int i = 0; i < inv.getSlots(); i ++) {
            inv.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    public static ItemStack[] compress(ItemStack[] list) {
        List<ItemStack> comp = new LinkedList<>();
        for (ItemStack item : list) {
            if (item == null) continue;
            ItemStack currentStack = item.copy();
            if (currentStack.isEmpty() || currentStack.getCount() == 0) continue;
            boolean find = false;
            for (ItemStack storedStack : comp) {
                if (storedStack.isEmpty()) continue;
                boolean areItemStackEqual = storedStack.isItemEqual(currentStack) && ItemStack.areItemStackTagsEqual(storedStack, currentStack);
                if (areItemStackEqual && (storedStack.getCount() + currentStack.getCount()) <= storedStack.getMaxStackSize()) {
                    find = true;
                    storedStack.setCount(storedStack.getCount() + currentStack.getCount());
                }
            }
            if (!find) {
                comp.add(item.copy());
            }
        }
        return comp.stream().filter(Objects::nonNull).toArray(ItemStack[]::new);
    }

    public static int findMax(Collection<Integer> list) {
        int a = Integer.MIN_VALUE;
        for (int x : list) {
            a = Math.max(x, a);
        }
        return a;
    }

    public static void writeNBTToBytes(ByteBuf buf, NBTTagCompound nbt) {
        PacketBuffer pb = new PacketBuffer(buf);
        try {
            pb.writeCompoundTag(nbt);
        } catch (EncoderException ignore) {
        }
    }

    public static NBTTagCompound readNBTFromBytes(ByteBuf from)
    {
        PacketBuffer pb = new PacketBuffer(from);
        try {
            return pb.readCompoundTag();
        } catch (IOException e) {
            return new NBTTagCompound();
        }
    }


}
