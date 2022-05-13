package com.glodblock.github.client;

import appeng.api.storage.ITerminalHost;
import appeng.client.gui.implementations.GuiCraftingStatus;
import appeng.client.gui.widgets.GuiTabButton;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.util.Ae2ReflectClient;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import java.io.IOException;

public class GuiFluidPatternTerminalCraftingStatus extends GuiCraftingStatus {

    private GuiTabButton originalGuiBtn;

    public GuiFluidPatternTerminalCraftingStatus(InventoryPlayer inventoryPlayer, ITerminalHost te) {
        super(inventoryPlayer, te);
    }

    @Override
    public void initGui() {
        super.initGui();
        originalGuiBtn = Ae2ReflectClient.getOriginalGuiButton(this);
    }

    @Override
    protected void actionPerformed(final GuiButton btn) throws IOException {
        if (btn == originalGuiBtn) {
            InventoryHandler.switchGui(GuiType.FLUID_PATTERN_TERMINAL);
        } else {
            super.actionPerformed(btn);
        }
    }

}