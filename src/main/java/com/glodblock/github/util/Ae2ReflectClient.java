package com.glodblock.github.util;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.implementations.GuiCraftingStatus;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.render.AppEngRenderItem;
import com.glodblock.github.client.gui.GuiBaseFluidPatternTerminal;
import com.glodblock.github.client.gui.GuiFCBaseMonitor;
import com.glodblock.github.client.gui.container.ContainerFluidPatternTerminal;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class Ae2ReflectClient {

    private static final Field fAEBaseGui_stackSizeRenderer;
    private static final Field fGuiCraftingStatus_originalGuiBtn;
    private static final Field fGuiPatternTerm_container;
    private static final Field fGuiCPUStatus_icon;
    private static final Field fGuiMEMonitorable_monitorableContainer;
    private static final Field fGuiMEMonitorable_configSrc;
    private static final Field fGuiMEMonitorable_craftingStatusBtn;
    private static final Field fGuiContainer_guiLeft;
    private static final Field fGuiContainer_guiTop;
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
            fGui_drag = Ae2Reflect.reflectField(AEBaseGui.class, "drag_click");
            mGuiPatternTerm_inventorySlots = Ae2Reflect.reflectMethod(AEBaseGui.class, "getInventorySlots");
            mGuiPatternTerm_reservedSpace = Ae2Reflect.reflectMethod(GuiFCBaseMonitor.class, "getReservedSpace");
            mGuiPatternTerm_reservedSpaceSetter = Ae2Reflect.reflectMethod(GuiFCBaseMonitor.class, "setReservedSpace", int.class);
        } catch (NoSuchFieldException | NoSuchMethodException e) {
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
