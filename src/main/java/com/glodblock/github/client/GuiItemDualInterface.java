package com.glodblock.github.client;

import appeng.api.AEApi;
import appeng.api.config.YesNo;
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
    private GuiFCImgButton fluidPacketBtn;
    private GuiFCImgButton splittingBtn;
    private GuiFCImgButton blockingBtn;

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
        fluidPacketBtn = new GuiFCImgButton(this.guiLeft - 18, this.guiTop + 44, "SEND_MODE", "REAL_FLUID");
        buttonList.add(fluidPacketBtn);
        splittingBtn = new GuiFCImgButton(this.guiLeft - 18, this.guiTop + 62, "SPLITTING", "ALLOW");
        buttonList.add(splittingBtn);
        blockingBtn = new GuiFCImgButton(this.guiLeft - 18, this.guiTop + 80, "BLOCK", "ALL");
        buttonList.add(blockingBtn);
        priorityBtn = Ae2ReflectClient.getPriorityButton(this);
    }

    @Override
    protected void actionPerformed(final GuiButton btn) throws IOException {
        if (btn == switchInterface) {
            InventoryHandler.switchGui(GuiType.DUAL_FLUID_INTERFACE);
        } else if (btn == priorityBtn) {
            InventoryHandler.switchGui(GuiType.PRIORITY);
        } else if (btn == fluidPacketBtn) {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns("DualInterface.FluidPacket", this.fluidPacketBtn.getCurrentValue().equals("FLUID_PACKET") ? "0" : "1"));
        } else if (btn == splittingBtn) {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns("DualInterface.AllowSplitting", this.splittingBtn.getCurrentValue().equals("ALLOW") ? "0" : "1"));
        } else if (btn == blockingBtn) {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns("DualInterface.ExtendedBlockMode", this.blockingBtn.getCurrentValue().equals("ALL") ? "1" : this.blockingBtn.getCurrentValue().equals("ITEM") ? "2" : "0"));
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