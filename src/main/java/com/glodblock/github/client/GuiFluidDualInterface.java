package com.glodblock.github.client;

import appeng.api.AEApi;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.fluids.client.gui.GuiFluidInterface;
import appeng.fluids.helper.IFluidInterfaceHost;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.util.Ae2ReflectClient;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import java.io.IOException;

public class GuiFluidDualInterface extends GuiFluidInterface {

    private GuiTabButton switchInterface;
    private GuiTabButton priorityBtn;

    public GuiFluidDualInterface(final InventoryPlayer ip, final IFluidInterfaceHost te) {
        super(ip, te);
    }

    @Override
    public void initGui() {
        super.initGui();
        ItemStack icon = AEApi.instance().definitions().blocks().iface().maybeStack(1).orElse(ItemStack.EMPTY);
        switchInterface = new GuiTabButton(guiLeft + 133, guiTop, icon, icon.getDisplayName(), itemRender);
        buttonList.add(switchInterface);
        priorityBtn = Ae2ReflectClient.getPriorityButton(this);
    }

    @Override
    protected void actionPerformed(final GuiButton btn) throws IOException {
        if (btn == switchInterface) {
            InventoryHandler.switchGui(GuiType.DUAL_ITEM_INTERFACE);
        } else if (btn == priorityBtn) {
            InventoryHandler.switchGui(GuiType.PRIORITY);
        } else {
            super.actionPerformed(btn);
        }
    }

    @Override
    protected boolean drawUpgrades() {
        return false;
    }

}