package com.glodblock.github.client;

import appeng.api.AEApi;
import appeng.client.gui.implementations.GuiInterface;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.helpers.IInterfaceHost;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.button.GuiFCImgButton;
import com.glodblock.github.client.container.ContainerItemDualInterface;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.network.CPacketFluidPatternTermBtns;
import com.glodblock.github.util.Ae2ReflectClient;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import java.io.IOException;

public class GuiItemDualInterface extends GuiInterface {

    private final ContainerItemDualInterface container;
    private GuiTabButton switchInterface;
    private GuiTabButton priorityBtn;
    private GuiFCImgButton fluidPacketOffBtn;
    private GuiFCImgButton fluidPacketOnBtn;
    private GuiFCImgButton splittingBtn;

    public GuiItemDualInterface(final InventoryPlayer inventoryPlayer, final IInterfaceHost te) {
        super(inventoryPlayer, te);
        this.container = new ContainerItemDualInterface(inventoryPlayer, te);
        this.inventorySlots = container;
        Ae2ReflectClient.setInterfaceContainer(this, this.container);
    }

    @Override
    protected void addButtons() {
        super.addButtons();
        ItemStack icon = AEApi.instance().definitions().blocks().fluidIface().maybeStack(1).orElse(ItemStack.EMPTY);
        switchInterface = new GuiTabButton(guiLeft + 133, guiTop, icon, icon.getDisplayName(), itemRender);
        buttonList.add(switchInterface);
        fluidPacketOffBtn = new GuiFCImgButton(this.guiLeft - 18, this.guiTop + 44, "SEND_FLUID", "REAL_FLUID");
        buttonList.add(fluidPacketOffBtn);
        fluidPacketOnBtn = new GuiFCImgButton(this.guiLeft - 18, this.guiTop + 44, "SEND_PACKET", "FLUID_PACKET");
        buttonList.add(fluidPacketOnBtn);
        splittingBtn = new GuiFCImgButton(this.guiLeft - 18, this.guiTop + 62, "SPLITTING", "ALLOW");
        buttonList.add(splittingBtn);
        priorityBtn = Ae2ReflectClient.getPriorityButton(this);
    }

    @Override
    protected String getBackground() {
        return "guis/interface.png";
    }

    @Override
    protected void actionPerformed(final GuiButton btn) throws IOException {
        if (btn == switchInterface) {
            InventoryHandler.switchGui(GuiType.DUAL_FLUID_INTERFACE);
        } else if (btn == priorityBtn) {
            InventoryHandler.switchGui(GuiType.PRIORITY);
        } else if (btn == fluidPacketOffBtn || btn == fluidPacketOnBtn) {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns("DualInterface.FluidPacket", btn == fluidPacketOnBtn ? "0" : "1"));
        } else if (btn == splittingBtn) {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns("DualInterface.AllowSplitting", this.splittingBtn.getCurrentValue().equals("ALLOW") ? "0" : "1"));
        } else {
            super.actionPerformed(btn);
        }
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        if (this.container.fluidPacket) {
            this.fluidPacketOnBtn.visible = true;
            this.fluidPacketOffBtn.visible = false;
        }
        else {
            this.fluidPacketOnBtn.visible = false;
            this.fluidPacketOffBtn.visible = true;
        }

        this.splittingBtn.set(container.allowSplitting ? "ALLOW" : "PREVENT");

        super.drawFG(offsetX, offsetY, mouseX, mouseY);
    }

}