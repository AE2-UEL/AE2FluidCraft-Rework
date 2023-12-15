package com.glodblock.github.client;

import appeng.api.storage.ITerminalHost;
import appeng.client.gui.implementations.GuiCraftConfirm;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.helpers.WirelessTerminalGuiObject;
import com.glodblock.github.client.container.ContainerFCCraftConfirm;
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

public class GuiFCCraftConfirm extends GuiCraftConfirm {

    private GuiButton cancel;
    private GuiType originGui;

    public GuiFCCraftConfirm(InventoryPlayer inventoryPlayer, ITerminalHost te) {
        super(inventoryPlayer, te);
        this.inventorySlots = new ContainerFCCraftConfirm(inventoryPlayer, te);
        ContainerCraftConfirm ccc = (ContainerCraftConfirm) this.inventorySlots;
        ccc.setGui(this);
        Ae2ReflectClient.writeCraftConfirmContainer(this, ccc);
    }

    @Override
    public void initGui() {
        super.initGui();
        this.cancel = Ae2ReflectClient.getCraftConfirmBackButton(this);
        Object te = ((AEBaseContainer)this.inventorySlots).getTarget();
        if (te instanceof WirelessTerminalGuiObject) {
            ItemStack tool = ((WirelessTerminalGuiObject) te).getItemStack();
            if (tool.getItem() == FCItems.WIRELESS_FLUID_PATTERN_TERMINAL) {
                this.originGui = GuiType.WIRELESS_FLUID_PATTERN_TERMINAL;
            }
        }
        if (te instanceof PartFluidPatternTerminal) {
            this.originGui = GuiType.FLUID_PATTERN_TERMINAL;
        }
        if (te instanceof PartExtendedFluidPatternTerminal) {
            this.originGui = GuiType.FLUID_EXTENDED_PATTERN_TERMINAL;
        }
    }

    @Override
    protected void actionPerformed(GuiButton btn) throws IOException {
        if (btn == this.cancel) {
            InventoryHandler.switchGui(this.originGui);
            return;
        }
        super.actionPerformed(btn);
    }

}
