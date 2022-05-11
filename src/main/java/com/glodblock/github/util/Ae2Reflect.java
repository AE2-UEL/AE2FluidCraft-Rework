package com.glodblock.github.util;

import appeng.api.implementations.IUpgradeableHost;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.storage.IMEInventory;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.implementations.GuiCraftingStatus;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.render.AppEngRenderItem;
import appeng.container.implementations.ContainerUpgradeable;
import appeng.container.implementations.CraftingCPURecord;
import appeng.helpers.MultiCraftingTracker;
import appeng.me.storage.MEInventoryHandler;
import appeng.me.storage.MEPassThrough;
import appeng.util.inv.ItemSlot;
import appeng.util.prioitylist.IPartitionList;
import com.glodblock.github.client.gui.GuiBaseFluidPatternTerminal;
import com.glodblock.github.client.gui.GuiFCBaseMonitor;
import com.glodblock.github.client.gui.container.ContainerFluidPatternTerminal;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Ae2Reflect {

    private static final Field fInventory_container;
    private static final Field fInventory_width;
    private static final Field fInventory_stackList;
    private static final Field fAEContainer_upgradeable;
    private static final Field fAEPass_internal;
    private static final Field fAEInv_partitionList;
    private static final Method mItemSlot_setExtractable;

    static {
        try {
            fInventory_container = reflectField(InventoryCrafting.class, "eventHandler", "field_70465_c", "c");
            fInventory_width = reflectField(InventoryCrafting.class, "inventoryWidth", "field_70464_b", "b");
            fInventory_stackList = reflectField(InventoryCrafting.class, "stackList", "field_70466_a", "a");
            fAEContainer_upgradeable = reflectField(ContainerUpgradeable.class, "upgradeable");
            fAEPass_internal = reflectField(MEPassThrough.class, "internal");
            fAEInv_partitionList = reflectField(MEInventoryHandler.class, "myPartitionList");
            mItemSlot_setExtractable = reflectMethod(ItemSlot.class, "setExtractable", boolean.class);
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

    public static IUpgradeableHost getUpgrade(ContainerUpgradeable container) {
        return Ae2Reflect.readField(container, fAEContainer_upgradeable);
    }

    public static Container getCraftContainer(InventoryCrafting inv) {
        return Ae2Reflect.readField(inv, fInventory_container);
    }

    public static int getCraftWidth(InventoryCrafting inv) {
        return Ae2Reflect.readField(inv, fInventory_width);
    }

    public static ItemStack[] getCraftStackList(InventoryCrafting inv) {
        return Ae2Reflect.readField(inv, fInventory_stackList);
    }

    public static void setItemSlotExtractable(ItemSlot slot, boolean extractable) {
        try {
            mItemSlot_setExtractable.invoke(slot, extractable);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke method: " + mItemSlot_setExtractable, e);
        }
    }

    private static final Field fAEBaseGui_stackSizeRenderer;
    private static final Field fGuiCraftingStatus_originalGuiBtn;
    private static final Field fGuiPatternTerm_container;
    private static final Field fGuiCPUStatus_icon;
    private static final Field fGuiMEMonitorable_monitorableContainer;
    private static final Field fGuiMEMonitorable_configSrc;
    private static final Field fGuiMEMonitorable_craftingStatusBtn;
    private static final Field fGuiContainer_guiLeft;
    private static final Field fGuiContainer_guiTop;
    private static final Field fCPU_cpu;
    private static final Field fCPU_myName;
    private static final Field fCPU_processors;
    private static final Field fCPU_size;
    private static final Field fGui_drag;
    private static final Method mGuiPatternTerm_inventorySlots;
    private static final Method mGuiPatternTerm_reservedSpace;
    private static final Method mGuiPatternTerm_reservedSpaceSetter;

    static {
        try {
            fAEBaseGui_stackSizeRenderer = Ae2Reflect.reflectField(AEBaseGui.class, "aeRenderItem");
            fGuiCraftingStatus_originalGuiBtn = Ae2Reflect.reflectField(GuiCraftingStatus.class, "originalGuiBtn");
            fGuiPatternTerm_container = Ae2Reflect.reflectField(GuiBaseFluidPatternTerminal.class, "container");
            fGuiMEMonitorable_monitorableContainer = Ae2Reflect.reflectField(GuiFCBaseMonitor.class, "monitorableContainer");
            fGuiMEMonitorable_configSrc = Ae2Reflect.reflectField(GuiFCBaseMonitor.class, "configSrc");
            fGuiMEMonitorable_craftingStatusBtn = Ae2Reflect.reflectField(GuiFCBaseMonitor.class, "craftingStatusBtn");
            fGuiCPUStatus_icon = Ae2Reflect.reflectField(GuiCraftingStatus.class, "myIcon");
            fGuiContainer_guiLeft = Ae2Reflect.reflectField(GuiContainer.class, "guiLeft", "field_147003_i", "i");
            fGuiContainer_guiTop = Ae2Reflect.reflectField(GuiContainer.class, "guiTop", "field_147009_r", "r");
            fCPU_cpu = Ae2Reflect.reflectField(CraftingCPURecord.class, "cpu");
            fCPU_myName = Ae2Reflect.reflectField(CraftingCPURecord.class, "myName");
            fCPU_processors = Ae2Reflect.reflectField(CraftingCPURecord.class, "processors");
            fCPU_size = Ae2Reflect.reflectField(CraftingCPURecord.class, "size");
            fGui_drag = Ae2Reflect.reflectField(AEBaseGui.class, "drag_click");
            mGuiPatternTerm_inventorySlots = Ae2Reflect.reflectMethod(AEBaseGui.class, "getInventorySlots");
            mGuiPatternTerm_reservedSpace = Ae2Reflect.reflectMethod(GuiFCBaseMonitor.class, "getReservedSpace");
            mGuiPatternTerm_reservedSpaceSetter = Ae2Reflect.reflectMethod(GuiFCBaseMonitor.class, "setReservedSpace", int.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize AE2 reflection hacks!", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Slot> getInventorySlots(AEBaseGui gui) {
        try {
            return (List<Slot>) mGuiPatternTerm_inventorySlots.invoke(gui);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke method: " + mGuiPatternTerm_inventorySlots, e);
        }
    }

    public static void rewriteIcon(GuiCraftingStatus gui, ItemStack icon) {
        Ae2Reflect.writeField(gui, fGuiCPUStatus_icon, icon);
    }

    public static Set<Slot> getDragClick(AEBaseGui gui) {
        return Ae2Reflect.readField(gui, fGui_drag);
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

    public static int getGuiLeft(GuiContainer gui) {
        return Ae2Reflect.readField(gui, fGuiContainer_guiLeft);
    }

    public static int getGuiTop(GuiContainer gui) {
        return Ae2Reflect.readField(gui, fGuiContainer_guiTop);
    }

    public static void setReservedSpace(GuiFCBaseMonitor gui, int size) {
        try {
            mGuiPatternTerm_reservedSpaceSetter.invoke(gui, size);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke method: " + mGuiPatternTerm_reservedSpaceSetter, e);
        }
    }

    public static int getReservedSpace(GuiFCBaseMonitor gui) {
        try {
            return (int) mGuiPatternTerm_reservedSpace.invoke(gui);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke method: " + mGuiPatternTerm_reservedSpace, e);
        }
    }

    public static AppEngRenderItem getStackSizeRenderer(AEBaseGui gui) {
        return Ae2Reflect.readField(gui, fAEBaseGui_stackSizeRenderer);
    }

    public static GuiTabButton getOriginalGuiButton(GuiCraftingStatus gui) {
        return Ae2Reflect.readField(gui, fGuiCraftingStatus_originalGuiBtn);
    }

    public static void setGuiContainer(GuiBaseFluidPatternTerminal instance, ContainerFluidPatternTerminal container) {
        Ae2Reflect.writeField(instance, fGuiPatternTerm_container, container);
        Ae2Reflect.writeField(instance, fGuiMEMonitorable_monitorableContainer, container);
        Ae2Reflect.writeField(instance, fGuiMEMonitorable_configSrc, container.getConfigManager());
    }

    public static GuiTabButton getCraftingStatusButton(GuiFCBaseMonitor gui) {
        return Ae2Reflect.readField(gui, fGuiMEMonitorable_craftingStatusBtn);
    }

}
