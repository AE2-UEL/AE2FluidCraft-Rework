package com.glodblock.github.util;

import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.Api;
import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.AEFluidStack;
import appeng.util.item.AEItemStack;
import com.glodblock.github.FluidCraft;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class FCUtil {

    private static final Object2ReferenceMap<Class<?>, TileEntityType<? extends TileEntity>> TILE_CACHE = new Object2ReferenceOpenCustomHashMap<>(HashUtil.CLASS);
    public static final IStorageChannel<IAEItemStack> ITEM = Api.instance().storage().getStorageChannel(IItemStorageChannel.class);
    public static final IStorageChannel<IAEFluidStack> FLUID = Api.instance().storage().getStorageChannel(IFluidStorageChannel.class);

    @Nonnull
    public static FluidStack getFluidFromItem(ItemStack stack) {
        if (!stack.isEmpty() && stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null).resolve().isPresent()) {
            IFluidHandlerItem tanks = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null).resolve().get();
            for (int i = 0; i < tanks.getTanks(); i ++) {
                FluidStack fluid = tanks.getFluidInTank(i);
                if (!fluid.isEmpty()) {
                    return fluid.copy();
                }
            }
        }
        return FluidStack.EMPTY;
    }

    public static void writeFluidInventoryToBuffer(@Nonnull AEFluidInventory inv, PacketBuffer data) {
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

    public static boolean readFluidInventoryToBuffer(@Nonnull AEFluidInventory inv, PacketBuffer data) {
        boolean changed = false;
        int fluidMask = data.readByte();
        for (int i = 0; i < inv.getSlots(); i++) {
            if ((fluidMask & (1 << i)) != 0) {
                IAEFluidStack fluid = AEFluidStack.fromPacket(data);
                IAEFluidStack origFluid = inv.getFluidInSlot(i);
                if (!fluid.equals(origFluid) || fluid.getStackSize() != origFluid.getStackSize()) {
                    inv.setFluidInSlot(i, fluid);
                    changed = true;
                }
            } else if (inv.getFluidInSlot(i) != null) {
                inv.setFluidInSlot(i, null);
                changed = true;
            }
        }
        return changed;
    }

    public static void clearItemInventory(IItemHandlerModifiable inv) {
        for (int i = 0; i < inv.getSlots(); i ++) {
            inv.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    public static int findMax(Collection<Integer> list) {
        int a = Integer.MIN_VALUE;
        for (int x : list) {
            a = Math.max(x, a);
        }
        return a;
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

    @SuppressWarnings("all")
    public static <T extends TileEntity> TileEntityType<T> getTileType(Class<T> clazz, Block block) {
        if (block == null) {
            return (TileEntityType<T>) TILE_CACHE.get(clazz);
        }
        return (TileEntityType<T>) TILE_CACHE.computeIfAbsent(
                clazz,
                k -> TileEntityType.Builder.create(
                        () -> {
                            try {
                                return clazz.newInstance();
                            } catch (InstantiationException|IllegalAccessException e) {
                                FluidCraft.log.error("Fail to bulid TileEntityType: " + clazz.getName());
                                e.printStackTrace();
                                return null;
                            }
                        }, block).build(null)
                );
    }

    public static void writeNBTToBytes(ByteBuf buf, CompoundNBT nbt) {
        PacketBuffer pb = new PacketBuffer(buf);
        try {
            pb.writeCompoundTag(nbt);
        } catch (EncoderException ignore) {
        }
    }

    public static CompoundNBT readNBTFromBytes(ByteBuf from)
    {
        PacketBuffer pb = new PacketBuffer(from);
        return pb.readCompoundTag();
    }

}
