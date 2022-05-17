package com.glodblock.github.inventory;

import appeng.helpers.NonBlockingItems;
import appeng.util.inv.BlockingInventoryAdaptor;
import appeng.util.inv.ItemHandlerIterator;
import appeng.util.inv.ItemSlot;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Objects;

public class BlockingFluidInventoryAdaptor extends BlockingInventoryAdaptor {

    @Nullable
    private final IItemHandler invItems;
    @Nullable
    private final IFluidHandler invFluids;
    @Nullable
    private final String domain;

    public BlockingFluidInventoryAdaptor(@Nullable IItemHandler invItems, @Nullable IFluidHandler invFluids, @Nullable String domain) {
        this.invItems = invItems;
        this.invFluids = invFluids;
        this.domain = domain;
    }

    public static BlockingInventoryAdaptor getAdaptor(TileEntity te, EnumFacing d) {
        IItemHandler itemHandler = null;
        IFluidHandler fluidHandler = null;
        String domain = null;
        if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, d)) {
            itemHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, d);
            if (itemHandler != null) {
                domain =  Objects.requireNonNull(te.getBlockType().getRegistryName()).getNamespace();
            }
        }
        if (te != null && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, d)) {
            fluidHandler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, d);
            if (fluidHandler != null) {
                domain =  Objects.requireNonNull(te.getBlockType().getRegistryName()).getNamespace();
            }
        }
        return new BlockingFluidInventoryAdaptor(itemHandler, fluidHandler, domain);
    }

    @Override
    public boolean containsBlockingItems() {

        boolean itemPass = true;
        boolean fluidPass = true;

        if (invItems != null) {
            int slots = this.invItems.getSlots();
            for(int slot = 0; slot < slots; ++slot) {
                ItemStack is = this.invItems.getStackInSlot(slot);
                if (!is.isEmpty() && this.isBlockableItem(is)) {
                    itemPass = false;
                }
            }
        }

        if (invFluids != null) {
            for (IFluidTankProperties tank : invFluids.getTankProperties()) {
                if (tank != null && tank.getContents() != null && (tank.canFill() || tank.canDrain())) {
                    fluidPass = false;
                }
            }
        }

        return !(fluidPass && itemPass);
    }

    @Override
    public Iterator<ItemSlot> iterator() {
        return new ItemHandlerIterator(this.invItems);
    }

    boolean isBlockableItem(ItemStack stack) {
        Object2ObjectOpenHashMap map = NonBlockingItems.INSTANCE.getMap().get(this.domain);
        if (map.get(stack.getItem()) != null) {
            return !((IntSet)map.get(stack.getItem())).contains(stack.getItemDamage());
        } else {
            return true;
        }
    }
}
