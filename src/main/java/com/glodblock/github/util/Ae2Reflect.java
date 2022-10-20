package com.glodblock.github.util;

import appeng.api.definitions.IItemDefinition;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.implementations.ContainerExpandedProcessingPatternTerm;
import appeng.container.implementations.ContainerPatternEncoder;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.container.slot.SlotRestrictedInput;
import appeng.crafting.MECraftingInventory;
import appeng.helpers.DualityInterface;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.parts.reporting.AbstractPartEncoder;
import appeng.parts.reporting.PartExpandedProcessingPatternTerminal;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.recipes.game.DisassembleRecipe;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.inv.ItemSlot;
import appeng.util.inv.filter.IAEItemFilter;
import com.glodblock.github.inventory.ExAppEngInternalInventory;
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
    private static final Field fDisassembleRecipe_nonCellMappings;
    private static final Field fInventory_container;
    private static final Field fCPU_inventory;
    private static final Field fCPU_machineSrc;
    private static final Field fDualInterface_fluidPacket;
    private static final Field fAppEngInternalInventory_filter;

    static {
        try {
            mItemSlot_setExtractable = reflectMethod(ItemSlot.class, "setExtractable", boolean.class);
            mCPU_getGrid = reflectMethod(CraftingCPUCluster.class, "getGrid");
            mCPU_postChange = reflectMethod(CraftingCPUCluster.class, "postChange", IAEItemStack.class, IActionSource.class);
            mCPU_markDirty = reflectMethod(CraftingCPUCluster.class, "markDirty");
            fInventory_container = reflectField(InventoryCrafting.class, "eventHandler", "field_70465_c", "c");
            fDisassembleRecipe_nonCellMappings = reflectField(DisassembleRecipe.class, "nonCellMappings");
            fCPU_inventory = Ae2Reflect.reflectField(CraftingCPUCluster.class, "inventory");
            fCPU_machineSrc = Ae2Reflect.reflectField(CraftingCPUCluster.class, "machineSrc");
            fDualInterface_fluidPacket = Ae2Reflect.reflectField(DualityInterface.class, "fluidPacket");
            fAppEngInternalInventory_filter = Ae2Reflect.reflectField(AppEngInternalInventory.class, "filter");
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

    public static IAEItemFilter getInventoryFilter(AppEngInternalInventory owner) {
        return readField(owner, fAppEngInternalInventory_filter);
    }

}