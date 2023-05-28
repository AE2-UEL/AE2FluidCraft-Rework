package com.glodblock.github.client.client.gui;

import appeng.api.config.YesNo;
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

    private GuiFCImgButton fluidPacketBtn;
    private GuiFCImgButton splittingBtn;
    private GuiFCImgButton blockingBtn;

    public GuiWrapInterface(final InventoryPlayer inventoryPlayer, final IInterfaceHost te) {
        super(inventoryPlayer, te);
        this.container = new ContainerWrapInterface(inventoryPlayer, te);
        this.inventorySlots = container;
        Ae2ReflectClient.setInterfaceContainer(this, this.container);
    }

    @Override
    protected void addButtons() {
        super.addButtons();
        fluidPacketBtn = new GuiFCImgButton(this.guiLeft - 18, this.guiTop + 44, "SEND_MODE", "REAL_FLUID");
        buttonList.add(fluidPacketBtn);
        splittingBtn = new GuiFCImgButton(this.guiLeft - 18, this.guiTop + 62, "SPLITTING", "ALLOW");
        buttonList.add(splittingBtn);
        blockingBtn = new GuiFCImgButton(this.guiLeft - 18, this.guiTop + 80, "BLOCK", "ALL");
        buttonList.add(blockingBtn);
    }

    @Override
    protected void actionPerformed(final GuiButton btn) throws IOException {
        if (btn == fluidPacketBtn) {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns("WrapDualInterface.FluidPacket", this.fluidPacketBtn.getCurrentValue().equals("FLUID_PACKET") ? "0" : "1"));
        } else if (btn == splittingBtn) {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns("WrapDualInterface.AllowSplitting", this.splittingBtn.getCurrentValue().equals("ALLOW") ? "0" : "1"));
        } else if (btn == blockingBtn) {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns("WrapDualInterface.ExtendedBlockMode", this.blockingBtn.getCurrentValue().equals("ALL") ? "1" : this.blockingBtn.getCurrentValue().equals("ITEM") ? "2" : "0"));
        }  else {
            super.actionPerformed(btn);
        }
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.fluidPacketBtn.set(this.container.fluidPacket ? "FLUID_PACKET" : "REAL_FLUID");
        this.splittingBtn.set(this.container.allowSplitting ? "ALLOW" : "PREVENT");
        this.blockingBtn.set(this.container.blockModeEx == 0 ? "ALL" : this.container.blockModeEx == 1 ? "ITEM" : "FLUID");
        this.blockingBtn.visible = this.container.getBlockingMode() == YesNo.YES;
        super.drawFG(offsetX, offsetY, mouseX, mouseY);
    }

}
