package com.glodblock.github.coremod;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.crafting.MECraftingInventory;
import appeng.fluids.parts.PartFluidInterface;
import appeng.fluids.tile.TileFluidInterface;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.me.MachineSet;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.parts.misc.PartInterface;
import appeng.tile.misc.TileInterface;
import appeng.util.InventoryAdaptor;
import appeng.util.inv.BlockingInventoryAdaptor;
import appeng.util.item.AEItemStack;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.part.PartDualInterface;
import com.glodblock.github.common.tile.TileDualInterface;
import com.glodblock.github.handler.FluidConvertingItemHandler;
import com.glodblock.github.inventory.BlockingFluidInventoryAdaptor;
import com.glodblock.github.inventory.FluidConvertingInventoryAdaptor;
import com.glodblock.github.inventory.FluidConvertingInventoryCrafting;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.Ae2Reflect;
import com.glodblock.github.util.SetBackedMachineSet;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
public class CoreModHooks {

    public static InventoryCrafting wrapCraftingBuffer(InventoryCrafting inv) {
        return new FluidConvertingInventoryCrafting(Ae2Reflect.getCraftContainer(inv), inv.getWidth(), inv.getHeight());
    }

    public static IAEItemStack wrapFluidPacketStack(IAEItemStack stack) {
        if (stack.getItem() == FCItems.FLUID_PACKET) {
            IAEItemStack dropStack = ItemFluidDrop.newAeStack(ItemFluidPacket.getFluidStack(stack.getDefinition()));
            if (dropStack != null) {
                return dropStack;
            }
        }
        return stack;
    }

    @Nullable
    public static InventoryAdaptor wrapInventory(@Nullable TileEntity tile, EnumFacing face) {
        return tile != null ? FluidConvertingInventoryAdaptor.wrap(tile, face) : null;
    }

    @Nullable
    public static BlockingInventoryAdaptor wrapBlockInventory(@Nullable TileEntity tile, EnumFacing face) {
        return tile != null ? BlockingFluidInventoryAdaptor.getAdaptor(tile, face) : null;
    }

    public static void writeExtraNBTInterface(DualityInterface dual, NBTTagCompound nbt) {
        nbt.setBoolean("fluidPacket", Ae2Reflect.getFluidPacketMode(dual));
        nbt.setBoolean("allowSplitting", Ae2Reflect.getSplittingMode(dual));
    }

    public static void readExtraNBTInterface(DualityInterface dual, NBTTagCompound nbt) {
        Ae2Reflect.setFluidPacketMode(dual, nbt.getBoolean("fluidPacket"));
        Ae2Reflect.setSplittingMode(dual, !nbt.hasKey("allowSplitting") || nbt.getBoolean("allowSplitting"));
    }

    public static ItemStack removeFluidPackets(InventoryCrafting inv, int index) {
        ItemStack stack = inv.getStackInSlot(index);
        if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemFluidPacket) {
            FluidStack fluid = ItemFluidPacket.getFluidStack(stack);
            return ItemFluidDrop.newStack(fluid);
        }
        else {
            return stack;
        }
    }

    public static long getCraftingByteCost(IAEItemStack stack) {
        return stack.getItem() instanceof ItemFluidDrop
                ? (long)Math.ceil(stack.getStackSize() / 1000D) : stack.getStackSize();
    }

    public static boolean checkForItemHandler(ICapabilityProvider capProvider, Capability<?> capability, EnumFacing side) {
        return capProvider.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)
                || capProvider.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
    }

    public static IItemHandler wrapItemHandler(ICapabilityProvider capProvider, Capability<?> capability, EnumFacing side) {
        return FluidConvertingItemHandler.wrap(capProvider, side);
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
        if (c == TileInterface.class || c == TileFluidInterface.class) {
            return unionMachineSets(grid.getMachines(c), grid.getMachines(TileDualInterface.class));
        } else if (c == PartInterface.class || c == PartFluidInterface.class) {
            return unionMachineSets(grid.getMachines(c), grid.getMachines(PartDualInterface.class));
        } else {
            return grid.getMachines(c);
        }
    }

    public static Object wrapFluidPacket(ItemStack stack) {
        if (stack.getItem() instanceof ItemFluidPacket) {
            return ItemFluidPacket.getFluidStack(stack);
        } else if (stack.getItem() instanceof ItemFluidDrop) {
            return ItemFluidDrop.getFluidStack(stack);
        }
        return stack;
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

    public static ItemStack displayFluid(ItemStack drop) {
        if (!drop.isEmpty() && drop.getItem() instanceof ItemFluidDrop) {
            FluidStack fluid = ItemFluidDrop.getFluidStack(drop);
            return ItemFluidPacket.newDisplayStack(fluid);
        } else return drop;
    }

    public static IAEItemStack displayAEFluid(IAEItemStack drop) {
        if (!drop.getDefinition().isEmpty() && drop.getItem() instanceof ItemFluidDrop) {
            FluidStack fluid = ItemFluidDrop.getFluidStack(drop.getDefinition());
            return AEItemStack.fromItemStack(ItemFluidPacket.newDisplayStack(fluid));
        } else return drop;
    }

    public static IAEItemStack displayAEFluidAmount(IAEItemStack drop) {
        if (drop != null && !drop.getDefinition().isEmpty() && drop.getItem() instanceof ItemFluidDrop) {
            FluidStack fluid = ItemFluidDrop.getFluidStack(drop.getDefinition());
            AEItemStack stack = AEItemStack.fromItemStack(ItemFluidPacket.newDisplayStack(fluid));
            return stack == null ? null : stack.setStackSize(drop.getStackSize());
        } else return drop;
    }

    public static long getFluidSize(IAEItemStack aeStack) {
        if (aeStack.getDefinition() != null && !aeStack.getDefinition().isEmpty() && aeStack.getDefinition().getItem() instanceof ItemFluidDrop) {
            return (long) Math.max(aeStack.getStackSize() / 1000D, 1);
        }
        else return aeStack.getStackSize();
    }

    public static void storeFluidItem(CraftingCPUCluster instance) {
        Preconditions.checkState(Ae2Reflect.getCPUComplete(instance), "CPU should be complete to prevent re-insertion when dumping items");
        final IGrid g = Ae2Reflect.getGrid(instance);

        if (g == null) {
            return;
        }

        final IStorageGrid sg = g.getCache( IStorageGrid.class );
        final IMEInventory<IAEItemStack> ii = sg.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
        final IMEInventory<IAEFluidStack> jj = sg.getInventory(AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class));
        final MECraftingInventory inventory = Ae2Reflect.getCPUInventory(instance);

        for (IAEItemStack is : inventory.getItemList()) {
            Ae2Reflect.postCPUChange(instance, is, Ae2Reflect.getCPUSource(instance));

            if (is.getItem() instanceof ItemFluidDrop ) {
                IAEFluidStack drop = ItemFluidDrop.getAeFluidStack(is);
                IAEFluidStack fluidRemainder = jj.injectItems(drop, Actionable.MODULATE, Ae2Reflect.getCPUSource(instance));
                if (fluidRemainder != null) {
                    is.setStackSize(fluidRemainder.getStackSize());
                } else {
                    is.reset();
                }
            } else {
                IAEItemStack remainder = ii.injectItems(is.copy(), Actionable.MODULATE, Ae2Reflect.getCPUSource(instance));
                if (remainder != null) {
                    is.setStackSize(remainder.getStackSize());
                } else {
                    is.reset();
                }
            }
        }

        if (inventory.getItemList().isEmpty()) {
            Ae2Reflect.setCPUInventory(instance, new MECraftingInventory());
        }

        Ae2Reflect.markCPUDirty(instance);
    }

    public static void downloadExtraNBT(Object host, NBTTagCompound tag) {
        if (host instanceof IInterfaceHost) {
            DualityInterface dual = ((IInterfaceHost) host).getInterfaceDuality();
            NBTTagCompound extra = new NBTTagCompound();
            extra.setBoolean("fluidPacket", Ae2Reflect.getFluidPacketMode(dual));
            extra.setBoolean("allowSplitting", Ae2Reflect.getSplittingMode(dual));
            tag.setTag("extraNBTData", extra);
        }
    }

    public static void uploadExtraNBT(Object host, NBTTagCompound tag) {
        if (host instanceof IInterfaceHost && tag != null && tag.hasKey("extraNBTData")) {
            DualityInterface dual = ((IInterfaceHost) host).getInterfaceDuality();
            NBTTagCompound extra = tag.getCompoundTag("extraNBTData");
            Ae2Reflect.setFluidPacketMode(dual, extra.getBoolean("fluidPacket"));
            Ae2Reflect.setSplittingMode(dual, extra.getBoolean("allowSplitting"));
        }
    }

}