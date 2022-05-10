package com.glodblock.github.client.gui;

import appeng.api.config.ActionItems;
import appeng.api.config.ItemSubstitution;
import appeng.api.config.Settings;
import appeng.api.storage.ITerminalHost;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.slot.AppEngSlot;
import appeng.core.localization.GuiText;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.FCBasePartContainer;
import com.glodblock.github.network.CPacketFluidPatternTermBtns;
import com.glodblock.github.util.Ae2Reflect;
import com.glodblock.github.util.ModAndClassUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;

public class GuiBaseFluidPatternTerminal extends GuiFCBaseMonitor {

    private static final String SUBSITUTION_DISABLE = "0";
    private static final String SUBSITUTION_ENABLE = "1";

    private static final String CRAFTMODE_CRFTING = "1";
    private static final String CRAFTMODE_PROCESSING = "0";

    public final FCBasePartContainer container;

    private GuiTabButton tabCraftButton;
    private GuiTabButton tabProcessButton;
    private GuiImgButton substitutionsEnabledBtn;
    private GuiImgButton substitutionsDisabledBtn;
    private GuiImgButton encodeBtn;
    private GuiImgButton clearBtn;
    private GuiImgButton doubleBtn;

    public GuiBaseFluidPatternTerminal(final InventoryPlayer inventoryPlayer, final ITerminalHost te )
    {
        super( inventoryPlayer, te, new FCBasePartContainer( inventoryPlayer, te ) );
        this.container = (FCBasePartContainer) this.inventorySlots;
        Ae2Reflect.setReservedSpace(this, 81);
    }

    @Override
    protected void actionPerformed( final GuiButton btn )
    {
        super.actionPerformed( btn );

        if( this.tabCraftButton == btn || this.tabProcessButton == btn )
        {
            FluidCraft.proxy.netHandler.sendToServer( new CPacketFluidPatternTermBtns( "PatternTerminal.CraftMode", this.tabProcessButton == btn ? CRAFTMODE_CRFTING : CRAFTMODE_PROCESSING ) );
        }
        else if( this.encodeBtn == btn )
        {
            FluidCraft.proxy.netHandler.sendToServer( new CPacketFluidPatternTermBtns( "PatternTerminal.Encode", isShiftKeyDown() ? "2" : "1" ) );
        }
        else if( this.clearBtn == btn )
        {
            FluidCraft.proxy.netHandler.sendToServer( new CPacketFluidPatternTermBtns( "PatternTerminal.Clear", "1" ) );
        }
        else if( this.substitutionsEnabledBtn == btn || this.substitutionsDisabledBtn == btn )
        {
            FluidCraft.proxy.netHandler.sendToServer( new CPacketFluidPatternTermBtns( "PatternTerminal.Substitute", this.substitutionsEnabledBtn == btn ? SUBSITUTION_DISABLE : SUBSITUTION_ENABLE ) );
        }
        else if (ModAndClassUtil.isDoubleButton && doubleBtn == btn)
        {
            FluidCraft.proxy.netHandler.sendToServer( new CPacketFluidPatternTermBtns( "PatternTerminal.Double",  Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) ? "1": "0") );
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui()
    {
        super.initGui();
        this.tabCraftButton = new GuiTabButton( this.guiLeft + 173, this.guiTop + this.ySize - 177, new ItemStack( Blocks.crafting_table ), GuiText.CraftingPattern.getLocal(), itemRender );
        this.buttonList.add( this.tabCraftButton );

        this.tabProcessButton = new GuiTabButton( this.guiLeft + 173, this.guiTop + this.ySize - 177, new ItemStack( Blocks.furnace ), GuiText.ProcessingPattern.getLocal(), itemRender );
        this.buttonList.add( this.tabProcessButton );

        this.substitutionsEnabledBtn = new GuiImgButton( this.guiLeft + 84, this.guiTop + this.ySize - 163, Settings.ACTIONS, ItemSubstitution.ENABLED );
        this.substitutionsEnabledBtn.setHalfSize( true );
        this.buttonList.add( this.substitutionsEnabledBtn );

        this.substitutionsDisabledBtn = new GuiImgButton( this.guiLeft + 84, this.guiTop + this.ySize - 163, Settings.ACTIONS, ItemSubstitution.DISABLED );
        this.substitutionsDisabledBtn.setHalfSize( true );
        this.buttonList.add( this.substitutionsDisabledBtn );

        this.clearBtn = new GuiImgButton( this.guiLeft + 74, this.guiTop + this.ySize - 163, Settings.ACTIONS, ActionItems.CLOSE );
        this.clearBtn.setHalfSize( true );
        this.buttonList.add( this.clearBtn );

        this.encodeBtn = new GuiImgButton( this.guiLeft + 147, this.guiTop + this.ySize - 142, Settings.ACTIONS, ActionItems.ENCODE );
        this.buttonList.add( this.encodeBtn );

        if (ModAndClassUtil.isDoubleButton) {
            this.doubleBtn = new GuiImgButton( this.guiLeft + 74, this.guiTop + this.ySize - 153, Settings.ACTIONS, ActionItems.DOUBLE );
            this.doubleBtn.setHalfSize( true );
            this.buttonList.add( this.doubleBtn );
        }
    }

    @Override
    public void drawFG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
    {
        if( !this.container.isCraftingMode() )
        {
            this.tabCraftButton.visible = false;
            this.tabProcessButton.visible = true;
            if (ModAndClassUtil.isDoubleButton)
                this.doubleBtn.visible = true;
        }
        else
        {
            this.tabCraftButton.visible = true;
            this.tabProcessButton.visible = false;
            if (ModAndClassUtil.isDoubleButton)
                this.doubleBtn.visible = false;
        }

        if( this.container.substitute )
        {
            this.substitutionsEnabledBtn.visible = true;
            this.substitutionsDisabledBtn.visible = false;
        }
        else
        {
            this.substitutionsEnabledBtn.visible = false;
            this.substitutionsDisabledBtn.visible = true;
        }

        super.drawFG( offsetX, offsetY, mouseX, mouseY );
        this.fontRendererObj.drawString( GuiText.PatternTerminal.getLocal(), 8, this.ySize - 96 + 2 - Ae2Reflect.getReservedSpace(this), 4210752 );
    }

    @Override
    protected String getBackground()
    {
        if( this.container.isCraftingMode() )
        {
            return "guis/pattern.png";
        }
        return "guis/pattern2.png";
    }

    @Override
    protected void repositionSlot( final AppEngSlot s )
    {
        if( s.isPlayerSide() )
        {
            s.yDisplayPosition = s.getY() + this.ySize - 78 - 5;
        }
        else
        {
            s.yDisplayPosition = s.getY() + this.ySize - 78 - 3;
        }
    }

}
