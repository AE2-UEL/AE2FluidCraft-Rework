package com.glodblock.github.util;

import appeng.api.implementations.IUpgradeableHost;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.implementations.ContainerUpgradeable;
import appeng.container.implementations.CraftingCPURecord;
import appeng.crafting.MECraftingInventory;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.storage.MEInventoryHandler;
import appeng.me.storage.MEPassThrough;
import appeng.util.inv.ItemSlot;
import appeng.util.prioitylist.IPartitionList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class Ae2Reflect {

    private static final Field fInventory_containerUpgrade;
    private static final Field fAEPass_internal;
    private static final Field fAEInv_partitionList;
    private static final Field fCPU_cpu;
    private static final Field fCPU_myName;
    private static final Field fCPU_processors;
    private static final Field fCPU_size;
    private static final Field fCPU_inventory;
    private static final Field fCPU_machineSrc;
    private static final Method mItemSlot_setExtractable;
    private static final Method mCPU_getGrid;
    private static final Method mCPU_postChange;
    private static final Method mCPU_markDirty;

    static {
        try {
            fInventory_containerUpgrade = reflectField(ContainerUpgradeable.class, "upgradeable");
            fAEPass_internal = reflectField(MEPassThrough.class, "internal");
            fAEInv_partitionList = reflectField(MEInventoryHandler.class, "myPartitionList");
            fCPU_cpu = Ae2Reflect.reflectField(CraftingCPURecord.class, "cpu");
            fCPU_myName = Ae2Reflect.reflectField(CraftingCPURecord.class, "myName");
            fCPU_processors = Ae2Reflect.reflectField(CraftingCPURecord.class, "processors");
            fCPU_size = Ae2Reflect.reflectField(CraftingCPURecord.class, "size");
            fCPU_inventory = Ae2Reflect.reflectField(CraftingCPUCluster.class, "inventory");
            fCPU_machineSrc = Ae2Reflect.reflectField(CraftingCPUCluster.class, "machineSrc");
            mItemSlot_setExtractable = reflectMethod(ItemSlot.class, "setExtractable", boolean.class);
            mCPU_getGrid = reflectMethod(CraftingCPUCluster.class, "getGrid");
            mCPU_postChange = reflectMethod(CraftingCPUCluster.class, "postChange", IAEItemStack.class, BaseActionSource.class);
            mCPU_markDirty = reflectMethod(CraftingCPUCluster.class, "markDirty");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize AE2 reflection hacks!", e);
        }
    }

    public static Method reflectMethod(Class<?> owner, String name, Class<?>... paramTypes) throws NoSuchMethodException {
        Method m = owner.getDeclaredMethod(name, paramTypes);
        m.setAccessible(true);
        return m;
    }

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

    public static IPartitionList<?> getPartitionList(MEInventoryHandler<?> me) {
        return Ae2Reflect.readField(me, fAEInv_partitionList);
    }

    public static IMEInventory<?> getInternal(MEPassThrough<?> me) {
        return Ae2Reflect.readField(me, fAEPass_internal);
    }

    public static void setItemSlotExtractable(ItemSlot slot, boolean extractable) {
        try {
            mItemSlot_setExtractable.invoke(slot, extractable);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke method: " + mItemSlot_setExtractable, e);
        }
    }

    public static ICraftingCPU getCPU(CraftingCPURecord cpu) {
        return Ae2Reflect.readField(cpu, fCPU_cpu);
    }

    public static String getName(CraftingCPURecord cpu) {
        return Ae2Reflect.readField(cpu, fCPU_myName);
    }

    public static int getProcessors(CraftingCPURecord cpu) {
        return Ae2Reflect.readField(cpu, fCPU_processors);
    }

    public static long getSize(CraftingCPURecord cpu) {
        return Ae2Reflect.readField(cpu, fCPU_size);
    }

    public static IUpgradeableHost getUpgradeList(ContainerUpgradeable container) {
        return Ae2Reflect.readField(container, fInventory_containerUpgrade);
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

    public static MachineSource getCPUSource(CraftingCPUCluster cpu) {
        return Ae2Reflect.readField(cpu, fCPU_machineSrc);
    }

    public static void postCPUChange(CraftingCPUCluster cpu, IAEItemStack stack, MachineSource src) {
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

}
