package com.glodblock.github.util;

import appeng.api.definitions.IItemDefinition;
import appeng.api.networking.IGrid;
import appeng.container.implementations.InterfaceTerminalContainer;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.helpers.DualityInterface;
import appeng.me.helpers.AENetworkProxy;
import appeng.recipes.game.DisassembleRecipe;
import appeng.util.inv.ItemSlot;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.IItemHandler;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

public class Ae2Reflect {

    private static final Field fDualInterface_gridProxy;
    private static final Field fDualityFluidInterface_gridProxy;
    private static final Field fDisassembleRecipe_nonCellMappings;
    private static final Field fInterfaceTerminalContainer_total;
    private static final Field fInterfaceTerminalContainer_forceFullUpdate;
    private static final Field fSpecialRecipeSerializer_field_222176_t;
    private static final Field fCraftingInventory_eventHandler;
    private static final Method mItemSlot_setExtractable;
    private static final Method mInterfaceTerminalContainer_visitInterfaceHosts;
    private static final Constructor<?> cInterfaceTerminalContainer_VisitorState;
    private static final Constructor<?> cInterfaceTerminalContainer_InvTracker;

    static {
        try {
            fDualInterface_gridProxy = reflectField(DualityInterface.class, "gridProxy");
            fDualityFluidInterface_gridProxy = reflectField(DualityFluidInterface.class, "gridProxy");
            fDisassembleRecipe_nonCellMappings = reflectField(DisassembleRecipe.class, "nonCellMappings");
            fInterfaceTerminalContainer_total = reflectField(Class.forName("appeng.container.implementations.InterfaceTerminalContainer$VisitorState"), "total");
            fInterfaceTerminalContainer_forceFullUpdate = reflectField(Class.forName("appeng.container.implementations.InterfaceTerminalContainer$VisitorState"), "forceFullUpdate");
            fSpecialRecipeSerializer_field_222176_t = reflectField(SpecialRecipeSerializer.class, "field_222176_t");
            fCraftingInventory_eventHandler = reflectField(CraftingInventory.class, "eventHandler", "field_70465_c");
            mItemSlot_setExtractable = reflectMethod(ItemSlot.class, "setExtractable", boolean.class);
            mInterfaceTerminalContainer_visitInterfaceHosts = reflectMethod(InterfaceTerminalContainer.class, "visitInterfaceHosts",
                    IGrid.class, Class.class, Class.forName("appeng.container.implementations.InterfaceTerminalContainer$VisitorState"));
            cInterfaceTerminalContainer_VisitorState = Class
                    .forName("appeng.container.implementations.InterfaceTerminalContainer$VisitorState")
                    .getDeclaredConstructor();
            cInterfaceTerminalContainer_VisitorState.setAccessible(true);
            cInterfaceTerminalContainer_InvTracker = Class
                    .forName("appeng.container.implementations.InterfaceTerminalContainer$InvTracker")
                    .getDeclaredConstructor(DualityInterface.class, IItemHandler.class, ITextComponent.class);
            cInterfaceTerminalContainer_InvTracker.setAccessible(true);
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

    public static AENetworkProxy getInterfaceProxy(DualityInterface owner) {
        return readField(owner, fDualInterface_gridProxy);
    }

    public static AENetworkProxy getInterfaceProxy(DualityFluidInterface owner) {
        return readField(owner, fDualityFluidInterface_gridProxy);
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

    public static Object genVisitorState() {
        try {
            return cInterfaceTerminalContainer_VisitorState.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke constructor: " + cInterfaceTerminalContainer_VisitorState, e);
        }
    }

    public static Object genInvTracker(DualityInterface dual, IItemHandler patterns, ITextComponent name) {
        try {
            return cInterfaceTerminalContainer_InvTracker.newInstance(dual, patterns, name);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke constructor: " + cInterfaceTerminalContainer_VisitorState, e);
        }
    }

    public static void visitInterfaceHost(InterfaceTerminalContainer owner, IGrid grid, Class<?> machineClass, Object state) {
        try {
            mInterfaceTerminalContainer_visitInterfaceHosts.invoke(owner, grid, machineClass, state);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke method: " + mInterfaceTerminalContainer_visitInterfaceHosts, e);
        }
    }

    public static int getVisitorStateTotal(Object owner) {
        return readField(owner, fInterfaceTerminalContainer_total);
    }

    public static boolean getVisitorStateUpdate(Object owner) {
        return readField(owner, fInterfaceTerminalContainer_forceFullUpdate);
    }

    public static <T extends IRecipe<?>> Function<ResourceLocation, T> getRecipeFactory(SpecialRecipeSerializer<T> own) {
        return readField(own, fSpecialRecipeSerializer_field_222176_t);
    }

    public static Container getContainer(CraftingInventory own) {
        return readField(own, fCraftingInventory_eventHandler);
    }

}
