package com.glodblock.github.integration.jei;

import appeng.api.storage.data.IAEItemStack;
import appeng.container.slot.SlotFake;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.helpers.InventoryAction;
import appeng.util.item.AEItemStack;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;

public class ItemTarget implements IGhostIngredientHandler.Target<Object> {

    private final int left;
    private final int top;
    private final Slot slot;

    public ItemTarget(int guiLeft, int guiTop, Slot slot) {
        this.left = guiLeft;
        this.top = guiTop;
        this.slot = slot;
    }

    @Nonnull
    @Override
    public Rectangle getArea() {
        return new Rectangle(left + slot.xPos, top + slot.yPos, 16, 16);
    }

    @Override
    public void accept(@Nonnull Object ingredient) {
        if (!(ingredient instanceof ItemStack)) {
            return;
        }
        IAEItemStack stack = AEItemStack.fromItemStack((ItemStack) ingredient);
        if (stack == null) {
            return;
        }
        final PacketInventoryAction p;
        try {
            p = new PacketInventoryAction(InventoryAction.PLACE_JEI_GHOST_ITEM, (SlotFake) slot, stack);
            NetworkHandler.instance().sendToServer(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
