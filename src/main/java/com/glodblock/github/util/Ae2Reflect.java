package com.glodblock.github.util;

import appeng.api.definitions.IItemDefinition;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.implementations.ContainerExpandedProcessingPatternTerm;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.container.slot.SlotRestrictedInput;
import appeng.crafting.MECraftingInventory;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.recipes.game.DisassembleRecipe;
import appeng.util.inv.ItemSlot;
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
    private static final Field fContainerPatternTerm_craftingSlots;
    private static final Field fContainerPatternTerm_outputSlots;
    private static final Field fContainerPatternTerm_patternSlotIN;
    private static final Field fContainerPatternTerm_patternSlotOUT;
    private static final Field fContainerExPatternTerm_craftingSlots;
    private static final Field fContainerExPatternTerm_outputSlots;
    private static final Field fContainerExPatternTerm_patternSlotIN;
    private static final Field fContainerExPatternTerm_patternSlotOUT;
    private static final Field fInventory_container;
    private static final Field fCPU_inventory;
    private static final Field fCPU_machineSrc;

    static {
        try {
            mItemSlot_setExtractable = reflectMethod(ItemSlot.class, "setExtractable", boolean.class);
            mCPU_getGrid = reflectMethod(CraftingCPUCluster.class, "getGrid");
            mCPU_postChange = reflectMethod(CraftingCPUCluster.class, "postChange", IAEItemStack.class, IActionSource.class);
            mCPU_markDirty = reflectMethod(CraftingCPUCluster.class, "markDirty");
            fInventory_container = reflectField(InventoryCrafting.class, "eventHandler", "field_70465_c", "c");
            fDisassembleRecipe_nonCellMappings = reflectField(DisassembleRecipe.class, "nonCellMappings");
            fContainerPatternTerm_craftingSlots = reflectField(ContainerPatternTerm.class, "craftingSlots");
            fContainerPatternTerm_outputSlots = reflectField(ContainerPatternTerm.class, "outputSlots");
            fContainerPatternTerm_patternSlotIN = reflectField(ContainerPatternTerm.class, "patternSlotIN");
            fContainerPatternTerm_patternSlotOUT = reflectField(ContainerPatternTerm.class, "patternSlotOUT");
            fContainerExPatternTerm_craftingSlots = reflectField(ContainerExpandedProcessingPatternTerm.class, "gridSlots");
            fContainerExPatternTerm_outputSlots = reflectField(ContainerExpandedProcessingPatternTerm.class, "outputSlots");
            fContainerExPatternTerm_patternSlotIN = reflectField(ContainerExpandedProcessingPatternTerm.class, "patternSlotIN");
            fContainerExPatternTerm_patternSlotOUT = reflectField(ContainerExpandedProcessingPatternTerm.class, "patternSlotOUT");
            fCPU_inventory = Ae2Reflect.reflectField(CraftingCPUCluster.class, "inventory");
            fCPU_machineSrc = Ae2Reflect.reflectField(CraftingCPUCluster.class, "machineSrc");
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

    public static SlotFakeCraftingMatrix[] getCraftingSlots(ContainerPatternTerm cont) {
        return readField(cont, fContainerPatternTerm_craftingSlots);
    }

    public static OptionalSlotFake[] getOutputSlots(ContainerPatternTerm cont) {
        return readField(cont, fContainerPatternTerm_outputSlots);
    }

    public static SlotRestrictedInput getPatternSlotIn(ContainerPatternTerm cont) {
        return readField(cont, fContainerPatternTerm_patternSlotIN);
    }

    public static SlotRestrictedInput getPatternSlotOut(ContainerPatternTerm cont) {
        return readField(cont, fContainerPatternTerm_patternSlotOUT);
    }

    public static SlotFakeCraftingMatrix[] getExCraftingSlots(ContainerExpandedProcessingPatternTerm cont) {
        return readField(cont, fContainerExPatternTerm_craftingSlots);
    }

    public static OptionalSlotFake[] getExOutputSlots(ContainerExpandedProcessingPatternTerm cont) {
        return readField(cont, fContainerExPatternTerm_outputSlots);
    }

    public static SlotRestrictedInput getExPatternSlotIn(ContainerExpandedProcessingPatternTerm cont) {
        return readField(cont, fContainerExPatternTerm_patternSlotIN);
    }

    public static SlotRestrictedInput getExPatternSlotOut(ContainerExpandedProcessingPatternTerm cont) {
        return readField(cont, fContainerExPatternTerm_patternSlotOUT);
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

}