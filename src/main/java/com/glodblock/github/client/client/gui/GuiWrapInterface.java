package com.glodblock.github.client.client.gui;

import appeng.client.gui.implementations.GuiInterface;
import appeng.helpers.IInterfaceHost;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.button.GuiFCImgButton;
import com.glodblock.github.client.container.ContainerWrapInterface;
import com.glodblock.github.network.CPacketFluidPatternTermBtns;
import com.glodblock.github.util.Ae2ReflectClient;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import java.io.IOException;

public class GuiWrapInterface extends GuiInterface {
    //fuck ae2
    private final ContainerWrapInterface container;

    private GuiFCImgButton fluidPacketOffBtn;
    private GuiFCImgButton fluidPacketOnBtn;

    public GuiWrapInterface(final InventoryPlayer inventoryPlayer, final IInterfaceHost te) {
        super(inventoryPlayer, te);
        this.container = new ContainerWrapInterface(inventoryPlayer, te);
        this.inventorySlots = container;
        Ae2ReflectClient.setInterfaceContainer(this, this.container);
    }

    @Override
    protected void addButtons() {
        super.addButtons();
        fluidPacketOffBtn = new GuiFCImgButton(this.guiLeft - 18, this.guiTop + 44, "SEND_FLUID", "REAL_FLUID");
        buttonList.add(fluidPacketOffBtn);
        fluidPacketOnBtn = new GuiFCImgButton(this.guiLeft - 18, this.guiTop + 44, "SEND_PACKET", "FLUID_PACKET");
        buttonList.add(fluidPacketOnBtn);
    }

    @Override
    protected void actionPerformed(final GuiButton btn) throws IOException {
        if (btn == fluidPacketOffBtn || btn == fluidPacketOnBtn) {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns("WrapDualInterface.FluidPacket", btn == fluidPacketOnBtn ? "0" : "1"));
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
        super.drawFG(offsetX, offsetY, mouseX, mouseY);
    }

}
