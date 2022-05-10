package com.glodblock.github.client.gui;

import appeng.api.storage.ITerminalHost;
import appeng.client.gui.implementations.GuiCraftingStatus;
import appeng.client.gui.widgets.GuiTabButton;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.Ae2Reflect;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public class GuiFluidPatternTerminalCraftingStatus extends GuiCraftingStatus {

    private GuiTabButton originalGuiBtn;

    public GuiFluidPatternTerminalCraftingStatus(InventoryPlayer inventoryPlayer, ITerminalHost te) {
        super(inventoryPlayer, te);
    }

    @Override
    public void initGui() {
        Ae2Reflect.rewriteIcon(this, new ItemStack(ItemAndBlockHolder.FLUID_TERMINAL, 1));
        super.initGui();
        originalGuiBtn = Ae2Reflect.getOriginalGuiButton(this);
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        if (btn == originalGuiBtn) {
            InventoryHandler.switchGui(GuiType.FLUID_PATTERN_TERMINAL);
        } else {
            super.actionPerformed(btn);
        }
    }

}
