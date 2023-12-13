package com.glodblock.github.util;

import appeng.api.definitions.IItemDefinition;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.implementations.ContainerPatternEncoder;
import appeng.crafting.MECraftingInventory;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.helpers.DualityInterface;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.AENetworkProxy;
import appeng.parts.reporting.AbstractPartEncoder;
import appeng.recipes.game.DisassembleRecipe;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.inv.ItemSlot;
import appeng.util.inv.filter.IAEItemFilter;
import com.the9grounds.aeadditions.tileentity.TileEntityGasInterface;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

public class Ae2Reflect {

    private static final Method mItemSlot_setExtractable;
    private static final Method mCPU_getGrid;
    private static final Method mCPU_postChange;
    private static final Method mCPU_markDirty;
    private static final Method mContain_getPart;
    private static final Field fDisassembleRecipe_nonCellMappings;
    private static final Field fInventory_container;
    private static final Field fCPU_inventory;
    private static final Field fCPU_machineSrc;
    private static final Field fCPU_isComplete;
    private static final Field fDualInterface_fluidPacket;
    private static final Field fDualInterface_allowSplitting;
    private static final Field fDualInterface_blockModeEx;
    private static final Field fDualInterface_gridProxy;
    private static final Field fDualityFluidInterface_gridProxy;
    private static final Field fAppEngInternalInventory_filter;
    private static final Field fTileEntityGasInterface_node;

    static {
        try {
            mItemSlot_setExtractable = reflectMethod(ItemSlot.class, "setExtractable", boolean.class);
            mCPU_getGrid = reflectMethod(CraftingCPUCluster.class, "getGrid");
            mCPU_postChange = reflectMethod(CraftingCPUCluster.class, "postChange", IAEItemStack.class, IActionSource.class);
            mCPU_markDirty = reflectMethod(CraftingCPUCluster.class, "markDirty");
            mContain_getPart = reflectMethod(ContainerPatternEncoder.class, new String[]{"getPatternTerminal", "getPart"});
            fInventory_container = reflectField(InventoryCrafting.class, "eventHandler", "field_70465_c", "c");
            fDisassembleRecipe_nonCellMappings = reflectField(DisassembleRecipe.class, "nonCellMappings");
            fCPU_inventory = reflectField(CraftingCPUCluster.class, "inventory");
            fCPU_machineSrc = reflectField(CraftingCPUCluster.class, "machineSrc");
            fCPU_isComplete = reflectField(CraftingCPUCluster.class, "isComplete");
            fDualInterface_fluidPacket = reflectField(DualityInterface.class, "fluidPacket");
            fDualInterface_allowSplitting = reflectField(DualityInterface.class, "allowSplitting");
            fDualInterface_blockModeEx = reflectField(DualityInterface.class, "blockModeEx");
            fDualInterface_gridProxy = reflectField(DualityInterface.class, "gridProxy");
            fDualityFluidInterface_gridProxy = reflectField(DualityFluidInterface.class, "gridProxy");
            fAppEngInternalInventory_filter = reflectField(AppEngInternalInventory.class, "filter");
            if (ModAndClassUtil.GAS) {
                fTileEntityGasInterface_node = reflectField(TileEntityGasInterface.class, "node");
            } else {
                fTileEntityGasInterface_node = null;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize AE2 reflection hacks!", e);
        }
    }

    public static Method reflectMethod(Class<?> owner, String name, Class<?>... paramTypes) throws NoSuchMethodException {
        return reflectMethod(owner, new String[]{name}, paramTypes);
    }

    @SuppressWarnings("all")
    public static Method reflectMethod(Class<?> owner, String[] names, Class<?>... paramTypes) throws NoSuchMethodException {
        Method m = null;
        for (String name : names) {
            try {
                m = owner.getDeclaredMethod(name, paramTypes);
                if (m != null) break;
            }
            catch (NoSuchMethodException ignore) {
            }
        }
        if (m == null) throw new NoSuchMethodException("Can't find field from " + Arrays.toString(names));
        m.setAccessible(true);
        return m;
    }

    @SuppressWarnings("all")
    public static Field reflectField(Class<?> owner, String ...names) throws NoSuchFieldException {
        Field f = null;
        for (String name : names) {
            try {
                f = owner.getDeclaredField(name);
                if (f != null) break;
            }
            catch (NoSuchFieldException ignore) {
            }
        }
        if (f == null) throw new NoSuchFieldException("Can't find field from " + Arrays.toString(names));
        f.setAccessible(true);
        return f;
    }

    @SuppressWarnings("unchecked")
    public static <T> T readField(Object owner, Field field) {
        try {
            return (T)field.get(owner);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read field: " + field);
        }
    }

    public static void writeField(Object owner, Field field, Object value) {
        try {
            field.set(owner, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write field: " + field);
        }
    }

    public static Container getCraftContainer(InventoryCrafting inv) {
        return Ae2Reflect.readField(inv, fInventory_container);
    }

    public static void setItemSlotExtractable(ItemSlot slot, boolean extractable) {
        try {
            mItemSlot_setExtractable.invoke(slot, extractable);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke method: " + mItemSlot_setExtractable, e);
        }
    }

    public static Map<IItemDefinition, IItemDefinition> getDisassemblyNonCellMap(DisassembleRecipe recipe) {
        return readField(recipe, fDisassembleRecipe_nonCellMappings);
    }

    public static IGrid getGrid(CraftingCPUCluster cpu) {
        try {
            return (IGrid) mCPU_getGrid.invoke(cpu);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke method: " + mCPU_getGrid, e);
        }
    }

    public static MECraftingInventory getCPUInventory(CraftingCPUCluster cpu) {
        return Ae2Reflect.readField(cpu, fCPU_inventory);
    }

    public static void setCPUInventory(CraftingCPUCluster cpu, MECraftingInventory value) {
        Ae2Reflect.writeField(cpu, fCPU_inventory, value);
    }

    public static IActionSource getCPUSource(CraftingCPUCluster cpu) {
        return Ae2Reflect.readField(cpu, fCPU_machineSrc);
    }

    public static boolean getCPUComplete(CraftingCPUCluster cpu) {
        return Ae2Reflect.readField(cpu, fCPU_isComplete);
    }

    public static AbstractPartEncoder getPart(ContainerPatternEncoder owner) {
        try {
            return (AbstractPartEncoder) mContain_getPart.invoke(owner);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke method: " + mContain_getPart, e);
        }
    }

    public static void postCPUChange(CraftingCPUCluster cpu, IAEItemStack stack, IActionSource src) {
        try {
            mCPU_postChange.invoke(cpu, stack, src);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke method: " + mCPU_postChange, e);
        }
    }

    public static void markCPUDirty(CraftingCPUCluster cpu) {
        try {
            mCPU_markDirty.invoke(cpu);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke method: " + mCPU_markDirty, e);
        }
    }

    public static boolean getFluidPacketMode(DualityInterface owner) {
        return readField(owner, fDualInterface_fluidPacket);
    }

    public static void setFluidPacketMode(DualityInterface owner, boolean value) {
        writeField(owner, fDualInterface_fluidPacket, value);
    }

    public static boolean getSplittingMode(DualityInterface owner) {
        return readField(owner, fDualInterface_allowSplitting);
    }

    public static void setSplittingMode(DualityInterface owner, boolean value) {
        writeField(owner, fDualInterface_allowSplitting, value);
    }

    public static int getExtendedBlockMode(DualityInterface owner) {
        return readField(owner, fDualInterface_blockModeEx);
    }

    public static void setExtendedBlockMode(DualityInterface owner, int value) {
        writeField(owner, fDualInterface_blockModeEx, value);
    }

    public static AENetworkProxy getInterfaceProxy(DualityInterface owner) {
        return readField(owner, fDualInterface_gridProxy);
    }

    public static AENetworkProxy getInterfaceProxy(DualityFluidInterface owner) {
        return readField(owner, fDualityFluidInterface_gridProxy);
    }

    public static IAEItemFilter getInventoryFilter(AppEngInternalInventory owner) {
        return readField(owner, fAppEngInternalInventory_filter);
    }

    public static IGridNode getGasInterfaceGrid(Object owner) {
        return readField(owner, fTileEntityGasInterface_node);
    }

}