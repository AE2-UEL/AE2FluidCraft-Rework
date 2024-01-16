package com.glodblock.github.client;

import appeng.api.storage.ITerminalHost;
import appeng.client.gui.MathExpressionParser;
import appeng.client.gui.implementations.GuiCraftAmount;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.AEBaseContainer;
import appeng.helpers.WirelessTerminalGuiObject;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.container.ContainerItemAmountChange;
import com.glodblock.github.common.part.PartExtendedFluidPatternTerminal;
import com.glodblock.github.common.part.PartFluidPatternTerminal;
import com.glodblock.github.common.tile.TileUltimateEncoder;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.loader.FCBlocks;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.network.CPacketPatternValueSet;
import com.glodblock.github.network.CPacketSwitchGuis;
import com.glodblock.github.util.Ae2ReflectClient;
import com.glodblock.github.util.NameConst;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public class GuiItemAmountChange extends GuiCraftAmount {

    private GuiType originalGui;
    private GuiTabButton originalGuiBtn;
    private GuiButton next;
    private GuiTextField amountToCraft;
    private GuiButton plus1;
    private GuiButton plus10;
    private GuiButton plus100;
    private GuiButton plus1000;
    private GuiButton minus1;
    private GuiButton minus10;
    private GuiButton minus100;
    private GuiButton minus1000;

    public GuiItemAmountChange(InventoryPlayer inventoryPlayer, ITerminalHost te) {
        super(inventoryPlayer, te);
        this.inventorySlots = new ContainerItemAmountChange(inventoryPlayer, te);
    }

    @Override
    public void initGui() {
        super.initGui();
        ItemStack myIcon = null;
        Object target = ((AEBaseContainer)this.inventorySlots).getTarget();
        this.originalGuiBtn = Ae2ReflectClient.getGuiCraftAmountBackButton(this);
        this.buttonList.remove(this.originalGuiBtn);

        if (target instanceof WirelessTerminalGuiObject) {
            ItemStack tool = ((WirelessTerminalGuiObject) target).getItemStack();
            if (tool.getItem() == FCItems.WIRELESS_FLUID_PATTERN_TERMINAL) {
                myIcon = new ItemStack(FCItems.WIRELESS_FLUID_PATTERN_TERMINAL);
                this.originalGui = GuiType.WIRELESS_FLUID_PATTERN_TERMINAL;
            }
        }

        if (target instanceof PartFluidPatternTerminal) {
            myIcon = new ItemStack(FCItems.PART_FLUID_PATTERN_TERMINAL);
            this.originalGui = GuiType.FLUID_PATTERN_TERMINAL;
        }

        if (target instanceof PartExtendedFluidPatternTerminal) {
            myIcon = new ItemStack(FCItems.PART_EXTENDED_FLUID_PATTERN_TERMINAL);
            this.originalGui = GuiType.FLUID_EXTENDED_PATTERN_TERMINAL;
        }

        if (target instanceof TileUltimateEncoder) {
            myIcon = new ItemStack(FCBlocks.ULTIMATE_ENCODER);
            this.originalGui = GuiType.ULTIMATE_ENCODER;
        }

        if (this.originalGui != null && myIcon != null) {
            this.buttonList.add(this.originalGuiBtn = new GuiTabButton(this.guiLeft + 154, this.guiTop, myIcon, myIcon.getDisplayName(), this.itemRender));
        }

        next = Ae2ReflectClient.getGuiCraftAmountNextButton(this);
        amountToCraft = Ae2ReflectClient.getGuiCraftAmountTextBox(this);
        plus1 = Ae2ReflectClient.getGuiCraftAmountAddButton(this, 1);
        plus10 = Ae2ReflectClient.getGuiCraftAmountAddButton(this, 2);
        plus100 = Ae2ReflectClient.getGuiCraftAmountAddButton(this, 3);
        plus1000 = Ae2ReflectClient.getGuiCraftAmountAddButton(this, 4);
        minus1 = Ae2ReflectClient.getGuiCraftAmountAddButton(this, -1);
        minus10 = Ae2ReflectClient.getGuiCraftAmountAddButton(this, -2);
        minus100 = Ae2ReflectClient.getGuiCraftAmountAddButton(this, -3);
        minus1000 = Ae2ReflectClient.getGuiCraftAmountAddButton(this, -4);
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.fontRenderer.drawString(I18n.format(NameConst.GUI_ITEM_AMOUNT_SET), 8, 6, 4210752);
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        super.drawBG(offsetX, offsetY, mouseX, mouseY);
        this.next.displayString = I18n.format(NameConst.GUI_ITEM_AMOUNT_SET_CONFIRM);
    }

    @Override
    protected void actionPerformed( final GuiButton btn ) {
        try
        {
            if( btn == this.originalGuiBtn )
            {
                FluidCraft.proxy.netHandler.sendToServer( new CPacketSwitchGuis( this.originalGui ) );
            }

            if( btn == this.next )
            {
                String text = Ae2ReflectClient.getGuiCraftAmountTextBox(this).getText();
                double resultD = MathExpressionParser.parse(text);
                int result;
                if (resultD <= 0 || Double.isNaN(resultD)) {
                    result = 1;
                } else {
                    result = (int) MathExpressionParser.round(resultD, 0);
                }
                FluidCraft.proxy.netHandler.sendToServer( new CPacketPatternValueSet(this.originalGui, result, ((ContainerItemAmountChange) this.inventorySlots).getValueIndex()));
            }
        }
        catch( final NumberFormatException e )
        {
            // nope..
            this.amountToCraft.setText( "1" );
        }

        final boolean isPlus = btn == this.plus1 || btn == this.plus10 || btn == this.plus100 || btn == this.plus1000;
        final boolean isMinus = btn == this.minus1 || btn == this.minus10 || btn == this.minus100 || btn == this.minus1000;

        if( isPlus || isMinus )
        {
            Ae2ReflectClient.setGuiCraftAmountAddQty(this, this.getQty( btn ));
        }
    }

    public void setAmount(int amount) {
        this.amountToCraft.setText(String.valueOf(amount));
    }

}
