package com.glodblock.github.client;

import appeng.client.gui.implementations.GuiPriority;
import appeng.client.gui.widgets.GuiTabButton;
import com.glodblock.github.interfaces.FCPriorityHost;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.util.Ae2ReflectClient;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import java.io.IOException;

public class GuiFCPriority extends GuiPriority {

    private final GuiType originalGui;
    private GuiTabButton originalGuiBtn;

    public GuiFCPriority(final InventoryPlayer inventoryPlayer, final FCPriorityHost te) {
        super(inventoryPlayer, te);
        this.originalGui = te.getGuiType();
    }

    @Override
    public void initGui() {
        super.initGui();
        originalGuiBtn = Ae2ReflectClient.getOriginalGuiButton(this);
    }

    @Override
    protected void actionPerformed(final GuiButton btn) throws IOException {
        if (btn == originalGuiBtn) {
            InventoryHandler.switchGui(originalGui);
        } else {
            super.actionPerformed(btn);
        }
    }

    protected String getBackground() {
        return "guis/priority.png";
    }

}
