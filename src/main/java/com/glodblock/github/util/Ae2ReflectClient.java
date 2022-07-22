package com.glodblock.github.util;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.implementations.GuiCraftingStatus;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.render.AppEngRenderItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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
    private static final Field fGuiCPUStatus_icon;
    private static final Field fGui_drag;
    private static final Method mGuiPatternTerm_inventorySlots;


    static {
        try {
            fAEBaseGui_stackSizeRenderer = Ae2Reflect.reflectField(AEBaseGui.class, "aeRenderItem");
            fGuiCraftingStatus_originalGuiBtn = Ae2Reflect.reflectField(GuiCraftingStatus.class, "originalGuiBtn");
            fGuiCPUStatus_icon = Ae2Reflect.reflectField(GuiCraftingStatus.class, "myIcon");
            fGui_drag = Ae2Reflect.reflectField(AEBaseGui.class, "drag_click");
            mGuiPatternTerm_inventorySlots = Ae2Reflect.reflectMethod(AEBaseGui.class, "getInventorySlots");
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

    public static AppEngRenderItem getStackSizeRenderer(AEBaseGui gui) {
        return Ae2Reflect.readField(gui, fAEBaseGui_stackSizeRenderer);
    }

    public static GuiTabButton getOriginalGuiButton(GuiCraftingStatus gui) {
        return Ae2Reflect.readField(gui, fGuiCraftingStatus_originalGuiBtn);
    }

}
