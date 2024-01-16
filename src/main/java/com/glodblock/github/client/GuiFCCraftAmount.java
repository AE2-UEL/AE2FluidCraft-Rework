package com.glodblock.github.client;

import appeng.api.storage.ITerminalHost;
import appeng.client.gui.MathExpressionParser;
import appeng.client.gui.implementations.GuiCraftAmount;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.AEBaseContainer;
import appeng.helpers.WirelessTerminalGuiObject;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.part.PartExtendedFluidPatternTerminal;
import com.glodblock.github.common.part.PartFluidPatternTerminal;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.network.CPacketInventoryAction;
import com.glodblock.github.util.Ae2ReflectClient;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import java.io.IOException;

public class GuiFCCraftAmount extends GuiCraftAmount {

    private GuiTabButton originalGuiBtn;
    private GuiButton next;
    private GuiType originGui;

    public GuiFCCraftAmount(InventoryPlayer inventoryPlayer, ITerminalHost te) {
        super(inventoryPlayer, te);
    }

    @Override
    public void initGui() {
        super.initGui();
        this.originalGuiBtn = Ae2ReflectClient.getGuiCraftAmountBackButton(this);
        this.next = Ae2ReflectClient.getGuiCraftAmountNextButton(this);
        ItemStack icon = ItemStack.EMPTY;
        Object te = ((AEBaseContainer)this.inventorySlots).getTarget();
        if (te instanceof WirelessTerminalGuiObject) {
            ItemStack tool = ((WirelessTerminalGuiObject) te).getItemStack();
            if (tool.getItem() == FCItems.WIRELESS_FLUID_PATTERN_TERMINAL) {
                icon = new ItemStack(FCItems.WIRELESS_FLUID_PATTERN_TERMINAL, 1);
                this.originGui = GuiType.WIRELESS_FLUID_PATTERN_TERMINAL;
            }
        }
        if (te instanceof PartFluidPatternTerminal) {
            icon = new ItemStack(FCItems.PART_FLUID_PATTERN_TERMINAL, 1);
            this.originGui = GuiType.FLUID_PATTERN_TERMINAL;
        }
        if (te instanceof PartExtendedFluidPatternTerminal) {
            icon = new ItemStack(FCItems.PART_EXTENDED_FLUID_PATTERN_TERMINAL, 1);
            this.originGui = GuiType.FLUID_EXTENDED_PATTERN_TERMINAL;
        }
        if (!icon.isEmpty() && this.originGui != null) {
            this.buttonList.remove(this.originalGuiBtn);
            this.buttonList.add(this.originalGuiBtn = new GuiTabButton(this.guiLeft + 154, this.guiTop, icon, icon.getDisplayName(), this.itemRender));
        }
    }

    @Override
    protected void actionPerformed(GuiButton btn) throws IOException {
        if (btn == this.originalGuiBtn) {
            InventoryHandler.switchGui(this.originGui);
        } else if (btn == this.next) {
            String text = Ae2ReflectClient.getGuiCraftAmountTextBox(this).getText();
            double resultD = MathExpressionParser.parse(text);
            int result;
            if (resultD <= 0 || Double.isNaN(resultD)) {
                result = 1;
            } else {
                result = (int) MathExpressionParser.round(resultD, 0);
            }
            FluidCraft.proxy.netHandler.sendToServer(new CPacketInventoryAction(CPacketInventoryAction.Action.REQUEST_JOB, isShiftKeyDown() ? 1 : 0, result, null));
        } else {
            super.actionPerformed(btn);
        }
    }

}
