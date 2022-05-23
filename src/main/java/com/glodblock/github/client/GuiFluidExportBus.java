package com.glodblock.github.client;

import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.core.localization.GuiText;
import appeng.fluids.client.gui.widgets.GuiFluidSlot;
import appeng.fluids.client.gui.widgets.GuiOptionalFluidSlot;
import appeng.fluids.container.ContainerFluidIO;
import appeng.fluids.parts.PartSharedFluidBus;
import appeng.fluids.util.IAEFluidTank;
import com.glodblock.github.client.container.ContainerFluidExportBus;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiFluidExportBus extends GuiUpgradeable {

    private final PartSharedFluidBus bus;

    public GuiFluidExportBus(InventoryPlayer inventoryPlayer, PartSharedFluidBus te) {
        super(new ContainerFluidExportBus(inventoryPlayer, te));
        this.bus = te;
    }

    public void initGui() {
        super.initGui();
        ContainerFluidIO container = (ContainerFluidIO)this.inventorySlots;
        IAEFluidTank inv = this.bus.getConfig();
        this.guiSlots.add(new GuiFluidSlot(inv, 0, 0, 80, 40));
        this.guiSlots.add(new GuiOptionalFluidSlot(inv, container, 1, 1, 1, 80, 40, -1, 0));
        this.guiSlots.add(new GuiOptionalFluidSlot(inv, container, 2, 2, 1, 80, 40, 1, 0));
        this.guiSlots.add(new GuiOptionalFluidSlot(inv, container, 3, 3, 1, 80, 40, 0, -1));
        this.guiSlots.add(new GuiOptionalFluidSlot(inv, container, 4, 4, 1, 80, 40, 0, 1));
        this.guiSlots.add(new GuiOptionalFluidSlot(inv, container, 5, 5, 2, 80, 40, -1, -1));
        this.guiSlots.add(new GuiOptionalFluidSlot(inv, container, 6, 6, 2, 80, 40, 1, -1));
        this.guiSlots.add(new GuiOptionalFluidSlot(inv, container, 7, 7, 2, 80, 40, -1, 1));
        this.guiSlots.add(new GuiOptionalFluidSlot(inv, container, 8, 8, 2, 80, 40, 1, 1));
    }

    protected GuiText getName() {
        return GuiText.ExportBusFluids;
    }
}
