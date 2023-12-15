package com.glodblock.github.util;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.me.SlotME;
import appeng.helpers.InventoryAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;

public final class UtilClient {

    public static boolean shouldAutoCraft(Slot slot, int mouseButton, ClickType clickType) {
        if (slot instanceof SlotME) {
            IAEItemStack stack;
            InventoryAction action;
            final EntityPlayer player = Minecraft.getMinecraft().player;
            switch (clickType) {
                case PICKUP:
                    action = (mouseButton == 1) ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;
                    stack = ((SlotME) slot).getAEStack();
                    if (stack != null && action == InventoryAction.PICKUP_OR_SET_DOWN
                            && (stack.getStackSize() == 0 || GuiScreen.isAltKeyDown())
                            && player.inventory.getItemStack().isEmpty()) {
                        return true;
                    }
                    break;
                case CLONE:
                    stack = ((SlotME) slot).getAEStack();
                    if (stack != null && stack.isCraftable()) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

}
