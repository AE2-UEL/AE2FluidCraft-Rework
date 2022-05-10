package com.glodblock.github.coremod.hooker;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.MachineSet;
import appeng.tile.misc.TileInterface;
import appeng.util.InventoryAdaptor;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.FluidConvertingInventoryAdaptor;
import com.glodblock.github.inventory.FluidConvertingInventoryCrafting;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.Ae2Reflect;
import com.glodblock.github.util.SetBackedMachineSet;
import com.glodblock.github.util.Util;
import com.google.common.collect.Sets;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class CoreModHooks {

    public static InventoryCrafting wrapCraftingBuffer(InventoryCrafting inv) {
        return new FluidConvertingInventoryCrafting(Ae2Reflect.getCraftContainer(inv), Ae2Reflect.getCraftWidth(inv), inv.getSizeInventory()/Ae2Reflect.getCraftWidth(inv));
    }

    public static IAEItemStack wrapFluidPacketStack(IAEItemStack stack) {
        if (stack.getItem() == ItemAndBlockHolder.PACKET) {
            IAEItemStack dropStack = ItemFluidDrop.newAeStack(ItemFluidPacket.getFluidStack(stack.getItemStack()));
            if (dropStack != null) {
                return dropStack;
            }
        }
        return stack;
    }

    @Nullable
    public static InventoryAdaptor wrapInventory(@Nullable TileEntity tile, ForgeDirection face) {
        return tile != null ? FluidConvertingInventoryAdaptor.wrap(tile, Util.from(face)) : null;
    }

    public static long getCraftingByteCost(IAEItemStack stack) {
        return stack.getItem() instanceof ItemFluidDrop
            ? (long)Math.ceil(stack.getStackSize() / 1000D) : stack.getStackSize();
    }

    public static IAEItemStack[] flattenFluidPackets(IAEItemStack[] stacks) {
        for (int i = 0; i < stacks.length; i++) {
            if (stacks[i].getItem() instanceof ItemFluidPacket) {
                stacks[i] = ItemFluidDrop.newAeStack(ItemFluidPacket.getFluidStack(stacks[i]));
            }
        }
        return stacks;
    }

    /*public static IMachineSet getMachines(IGrid grid, Class<? extends IGridHost> c) {
        if (c == TileInterface.class) {
            return unionMachineSets(grid.getMachines(c), grid.getMachines(TileDualInterface.class));
        } else if (c == PartInterface.class) {
            return unionMachineSets(grid.getMachines(c), grid.getMachines(PartDualInterface.class));
        } else {
            return grid.getMachines(c);
        }
    }*/ //unfinished

    private static IMachineSet unionMachineSets(IMachineSet a, IMachineSet b) {
        if (a.isEmpty()) {
            return b;
        } else if (b.isEmpty()) {
            return a;
        } else if (a instanceof MachineSet && b instanceof MachineSet) {
            return new SetBackedMachineSet(TileInterface.class, Sets.union((MachineSet)a, (MachineSet)b));
        } else {
            Set<IGridNode> union = new HashSet<>();
            a.forEach(union::add);
            b.forEach(union::add);
            return new SetBackedMachineSet(TileInterface.class, union);
        }
    }

}
