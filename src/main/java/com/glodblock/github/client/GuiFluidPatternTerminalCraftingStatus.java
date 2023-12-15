package com.glodblock.github.client;

import appeng.api.storage.ITerminalHost;
import appeng.client.gui.implementations.GuiCraftingStatus;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.helpers.WirelessTerminalGuiObject;
import com.glodblock.github.common.part.PartExtendedFluidPatternTerminal;
import com.glodblock.github.common.part.PartFluidPatternTerminal;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.Ae2ReflectClient;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import java.io.IOException;

public class GuiFluidPatternTerminalCraftingStatus extends GuiCraftingStatus {

    private GuiTabButton originalGuiBtn;
    private final ITerminalHost part;

    public GuiFluidPatternTerminalCraftingStatus(InventoryPlayer inventoryPlayer, ITerminalHost te) {
        super(inventoryPlayer, te);
        this.part = te;
        if (te instanceof WirelessTerminalGuiObject) {
            ItemStack tool = ((WirelessTerminalGuiObject) te).getItemStack();
            if (tool.getItem() == FCItems.WIRELESS_FLUID_PATTERN_TERMINAL) {
                Ae2ReflectClient.setIconItem(this, new ItemStack(FCItems.WIRELESS_FLUID_PATTERN_TERMINAL));
            }
        }
        if (te instanceof PartFluidPatternTerminal) {
            Ae2ReflectClient.setIconItem(this, new ItemStack(FCItems.PART_FLUID_PATTERN_TERMINAL));
        }
        if (te instanceof PartExtendedFluidPatternTerminal) {
            Ae2ReflectClient.setIconItem(this, new ItemStack(FCItems.PART_EXTENDED_FLUID_PATTERN_TERMINAL));
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        originalGuiBtn = Ae2ReflectClient.getOriginalGuiButton(this);
    }

    @Override
    protected void actionPerformed(final GuiButton btn) throws IOException {
        if (btn == originalGuiBtn) {
            if (part instanceof WirelessTerminalGuiObject) {
                ItemStack tool = ((WirelessTerminalGuiObject) part).getItemStack();
                if (tool.getItem() == FCItems.WIRELESS_FLUID_PATTERN_TERMINAL) {
                    InventoryHandler.switchGui(GuiType.WIRELESS_FLUID_PATTERN_TERMINAL);
                }
            } else if (part instanceof PartFluidPatternTerminal)
                InventoryHandler.switchGui(GuiType.FLUID_PATTERN_TERMINAL);
            else if (part instanceof PartExtendedFluidPatternTerminal)
                InventoryHandler.switchGui(GuiType.FLUID_EXTENDED_PATTERN_TERMINAL);
        } else {
            super.actionPerformed(btn);
        }
    }

}