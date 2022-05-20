package com.glodblock.github.client;

import appeng.api.storage.ITerminalHost;
import appeng.client.gui.implementations.GuiCraftingStatus;
import appeng.client.gui.widgets.GuiTabButton;
import com.glodblock.github.common.part.PartExtendedFluidPatternTerminal;
import com.glodblock.github.common.part.PartFluidPatternTerminal;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.util.Ae2ReflectClient;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import java.io.IOException;

public class GuiFluidPatternTerminalCraftingStatus extends GuiCraftingStatus {

    private GuiTabButton originalGuiBtn;
    private final ITerminalHost part;

    public GuiFluidPatternTerminalCraftingStatus(InventoryPlayer inventoryPlayer, ITerminalHost te) {
        super(inventoryPlayer, te);
        this.part = te;
    }

    @Override
    public void initGui() {
        super.initGui();
        originalGuiBtn = Ae2ReflectClient.getOriginalGuiButton(this);
    }

    @Override
    protected void actionPerformed(final GuiButton btn) throws IOException {
        if (btn == originalGuiBtn) {
            if (part instanceof PartFluidPatternTerminal)
                InventoryHandler.switchGui(GuiType.FLUID_PATTERN_TERMINAL);
            else if (part instanceof PartExtendedFluidPatternTerminal)
                InventoryHandler.switchGui(GuiType.FLUID_EXTENDED_PATTERN_TERMINAL);
        } else {
            super.actionPerformed(btn);
        }
    }

}