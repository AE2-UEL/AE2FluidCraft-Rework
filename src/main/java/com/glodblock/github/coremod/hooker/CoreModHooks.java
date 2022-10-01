package com.glodblock.github.coremod.hooker;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.crafting.MECraftingInventory;
import appeng.me.MachineSet;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.parts.misc.PartInterface;
import appeng.tile.misc.TileInterface;
import appeng.util.InventoryAdaptor;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.parts.PartFluidInterface;
import com.glodblock.github.common.tile.TileFluidInterface;
import com.glodblock.github.inventory.FluidConvertingInventoryAdaptor;
import com.glodblock.github.inventory.FluidConvertingInventoryCrafting;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.Ae2Reflect;
import com.glodblock.github.util.SetBackedMachineSet;
import com.glodblock.github.util.Util;
import com.google.common.collect.Sets;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class CoreModHooks {

    public static InventoryCrafting wrapCraftingBuffer(InventoryCrafting inv) {
        return new FluidConvertingInventoryCrafting(inv.eventHandler, inv.inventoryWidth, inv.getSizeInventory()/inv.inventoryWidth);
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

    public static ItemStack removeFluidPackets(InventoryCrafting inv, int index) {
        ItemStack stack = inv.getStackInSlot(index);
        if (stack != null && stack.getItem() instanceof ItemFluidPacket) {
            FluidStack fluid = ItemFluidPacket.getFluidStack(stack);
            return ItemFluidDrop.newStack(fluid);
        }
        else {
            return stack;
        }
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

    public static IMachineSet getMachines(IGrid grid, Class<? extends IGridHost> c) {
        if (c == TileInterface.class) {
            return unionMachineSets(grid.getMachines(c), grid.getMachines(TileFluidInterface.class));
        } else if (c == PartInterface.class) {
            return unionMachineSets(grid.getMachines(c), grid.getMachines(PartFluidInterface.class));
        } else {
            return grid.getMachines(c);
        }
    }

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

    public static ItemStack displayFluid(IAEItemStack aeStack) {
        if (aeStack.getItemStack() != null && aeStack.getItemStack().getItem() instanceof ItemFluidDrop) {
            FluidStack fluid = ItemFluidDrop.getFluidStack(aeStack.getItemStack());
            return ItemFluidPacket.newDisplayStack(fluid);
        }
        else return aeStack.getItemStack();
    }

    public static long getFluidSize(IAEItemStack aeStack) {
        if (aeStack.getItemStack() != null && aeStack.getItemStack().getItem() instanceof ItemFluidDrop) {
            return (long) Math.max(aeStack.getStackSize() / 1000D, 1);
        }
        else return aeStack.getStackSize();
    }

    public static void storeFluidItem(CraftingCPUCluster instance) {
        final IGrid g = Ae2Reflect.getGrid(instance);

        if( g == null )
        {
            return;
        }

        final IStorageGrid sg = g.getCache( IStorageGrid.class );
        final IMEInventory<IAEItemStack> ii = sg.getItemInventory();
        final IMEInventory<IAEFluidStack> jj = sg.getFluidInventory();
        final MECraftingInventory inventory = Ae2Reflect.getCPUInventory(instance);

        for( IAEItemStack is : inventory.getItemList() )
        {
            is = inventory.extractItems( is.copy(), Actionable.MODULATE, Ae2Reflect.getCPUSource(instance) );

            if( is != null )
            {
                Ae2Reflect.postCPUChange(instance, is, Ae2Reflect.getCPUSource(instance));
                if (is.getItem() instanceof ItemFluidDrop) {
                    IAEFluidStack fluidDrop = ItemFluidDrop.getAeFluidStack(is);
                    fluidDrop = jj.injectItems(fluidDrop, Actionable.MODULATE, Ae2Reflect.getCPUSource(instance));
                    if (fluidDrop == null) {
                        is = null;
                    } else {
                        is.setStackSize(fluidDrop.getStackSize());
                    }
                } else {
                    is = ii.injectItems(is, Actionable.MODULATE, Ae2Reflect.getCPUSource(instance));
                }
            }

            if( is != null )
            {
                inventory.injectItems( is, Actionable.MODULATE, Ae2Reflect.getCPUSource(instance) );
            }
        }

        if( inventory.getItemList().isEmpty() )
        {
            Ae2Reflect.setCPUInventory(instance, new MECraftingInventory());
        }

        Ae2Reflect.markCPUDirty(instance);
    }

}
