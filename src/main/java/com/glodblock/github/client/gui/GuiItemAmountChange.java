package com.glodblock.github.client.gui;

import appeng.api.storage.ITerminalHost;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.ContainerItemAmountChange;
import com.glodblock.github.network.CPacketPatternValueSet;
import com.glodblock.github.network.CPacketSwitchGuis;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiItemAmountChange extends GuiFluidCraftAmount {

    public GuiItemAmountChange(final InventoryPlayer inventoryPlayer, final ITerminalHost te)
    {
        super(new ContainerItemAmountChange(inventoryPlayer, te));
    }

    @Override
    public void initGui()
    {
        super.initGui();
        this.next.displayString = I18n.format("ae2fc.gui.set");
    }

    @Override
    public void drawBG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
    {
        super.drawBG(offsetX, offsetY, mouseX, mouseY);
        this.next.displayString = I18n.format("ae2fc.gui.set");
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
                FluidCraft.proxy.netHandler.sendToServer( new CPacketPatternValueSet(this.originalGui, Integer.parseInt(this.amountToCraft.getText()), ((ContainerItemAmountChange) this.inventorySlots).getValueIndex()));
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
            this.addQty( this.getQty( btn ) );
        }
    }

}
