package com.glodblock.github.client.gui;

import appeng.api.config.ActionItems;
import appeng.api.config.ItemSubstitution;
import appeng.api.config.PatternSlotConfig;
import appeng.api.config.Settings;
import appeng.api.storage.ITerminalHost;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.container.slot.AppEngSlot;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.FCBasePartContainerEx;
import com.glodblock.github.network.CPacketFluidPatternTermBtns;
import com.glodblock.github.util.Ae2ReflectClient;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.NameConst;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class GuiBaseFluidPatternTerminalEx extends GuiFCBaseMonitor {

    private static final String SUBSITUTION_DISABLE = "0";
    private static final String SUBSITUTION_ENABLE = "1";

    public FCBasePartContainerEx container;

    private GuiImgButton substitutionsEnabledBtn;
    private GuiImgButton substitutionsDisabledBtn;
    private GuiImgButton encodeBtn;
    private GuiImgButton invertBtn;
    private GuiImgButton clearBtn;
    private GuiImgButton doubleBtn;

    private GuiFCImgButton combineEnableBtn;
    private GuiFCImgButton combineDisableBtn;
    private final GuiScrollbar processingScrollBar = new GuiScrollbar();

    public GuiBaseFluidPatternTerminalEx(final InventoryPlayer inventoryPlayer, final ITerminalHost te )
    {
        super( inventoryPlayer, te, new FCBasePartContainerEx( inventoryPlayer, te ) );
        this.container = (FCBasePartContainerEx) this.inventorySlots;
        setReservedSpace(81);

        processingScrollBar.setHeight(70).setWidth(7).setLeft(6).setRange(0, 1, 1);
        processingScrollBar.setTexture(FluidCraft.MODID, "gui/pattern3.png", 242, 0);
    }

    @Override
    protected void actionPerformed( final GuiButton btn )
    {
        super.actionPerformed( btn );

        if( this.encodeBtn == btn )
        {
            FluidCraft.proxy.netHandler.sendToServer( new CPacketFluidPatternTermBtns( "PatternTerminalEx.Encode", isShiftKeyDown() ? "2" : "1" ) );
        }
        else if( this.clearBtn == btn )
        {
            FluidCraft.proxy.netHandler.sendToServer( new CPacketFluidPatternTermBtns( "PatternTerminalEx.Clear", "1" ) );
        }
        else if( this.substitutionsEnabledBtn == btn || this.substitutionsDisabledBtn == btn )
        {
            FluidCraft.proxy.netHandler.sendToServer( new CPacketFluidPatternTermBtns( "PatternTerminalEx.Substitute", this.substitutionsEnabledBtn == btn ? SUBSITUTION_DISABLE : SUBSITUTION_ENABLE ) );
        }
        else if( this.invertBtn == btn )
        {
            FluidCraft.proxy.netHandler.sendToServer( new CPacketFluidPatternTermBtns( "PatternTerminalEx.Invert", container.inverted ? "0" : "1" ) );
        }
        else if( this.combineDisableBtn == btn || this.combineEnableBtn == btn )
        {
            FluidCraft.proxy.netHandler.sendToServer( new CPacketFluidPatternTermBtns( "PatternTerminalEx.Combine", this.combineDisableBtn == btn ? "1" : "0" ) );
        }
        else if (ModAndClassUtil.isDoubleButton && doubleBtn == btn)
        {
            FluidCraft.proxy.netHandler.sendToServer( new CPacketFluidPatternTermBtns( "PatternTerminalEx.Double",  Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) ? "1": "0") );
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui()
    {
        super.initGui();

        this.substitutionsEnabledBtn = new GuiImgButton( this.guiLeft + 97, this.guiTop + this.ySize - 163, Settings.ACTIONS, ItemSubstitution.ENABLED );
        this.substitutionsEnabledBtn.setHalfSize( true );
        this.buttonList.add( this.substitutionsEnabledBtn );

        this.substitutionsDisabledBtn = new GuiImgButton( this.guiLeft + 97, this.guiTop + this.ySize - 163, Settings.ACTIONS, ItemSubstitution.DISABLED );
        this.substitutionsDisabledBtn.setHalfSize( true );
        this.buttonList.add( this.substitutionsDisabledBtn );

        invertBtn = new GuiImgButton( this.guiLeft + 87, this.guiTop + this.ySize - 153, Settings.ACTIONS, container.inverted ? PatternSlotConfig.C_4_16 : PatternSlotConfig.C_16_4);
        invertBtn.setHalfSize( true );
        this.buttonList.add( this.invertBtn );

        this.clearBtn = new GuiImgButton( this.guiLeft + 87, this.guiTop + this.ySize - 163, Settings.ACTIONS, ActionItems.CLOSE );
        this.clearBtn.setHalfSize( true );
        this.buttonList.add( this.clearBtn );

        this.encodeBtn = new GuiImgButton( this.guiLeft + 147, this.guiTop + this.ySize - 142, Settings.ACTIONS, ActionItems.ENCODE );
        this.buttonList.add( this.encodeBtn );

        if (ModAndClassUtil.isDoubleButton) {
            this.doubleBtn = new GuiImgButton( this.guiLeft + 97, this.guiTop + this.ySize - 153, Settings.ACTIONS, ActionItems.DOUBLE );
            this.doubleBtn.setHalfSize( true );
            this.buttonList.add( this.doubleBtn );
        }

        this.combineEnableBtn = new GuiFCImgButton( this.guiLeft + 87, this.guiTop + this.ySize - 143, "FORCE_COMBINE", "DO_COMBINE" );
        this.combineEnableBtn.setHalfSize( true );
        this.buttonList.add( this.combineEnableBtn );

        this.combineDisableBtn = new GuiFCImgButton( this.guiLeft + 87, this.guiTop + this.ySize - 143, "NOT_COMBINE", "DONT_COMBINE" );
        this.combineDisableBtn.setHalfSize( true );
        this.buttonList.add( this.combineDisableBtn );

        processingScrollBar.setTop(this.ySize - 164);
    }

    @Override
    public void drawFG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
    {
        if (ModAndClassUtil.isDoubleButton)
            this.doubleBtn.visible = true;

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

        if ( this.container.combine )
        {
            this.combineEnableBtn.visible = true;
            this.combineDisableBtn.visible = false;
        }
        else
        {
            this.combineEnableBtn.visible = false;
            this.combineDisableBtn.visible = true;
        }

        super.drawFG( offsetX, offsetY, mouseX, mouseY );
        this.fontRendererObj.drawString( StatCollector.translateToLocal(NameConst.GUI_FLUID_PATTERN_TERMINAL_EX), 8, this.ySize - 96 + 2 - getReservedSpace(), 4210752 );
        this.processingScrollBar.draw(this);
    }

    @Override
    protected String getBackground()
    {
        return container.inverted ? "gui/pattern4.png" : "gui/pattern3.png";
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

    @Override
    public void drawScreen( final int mouseX, final int mouseY, final float btn )
    {

        if (container.substitute) {
            substitutionsEnabledBtn.visible = true;
            substitutionsDisabledBtn.visible = false;
        } else {
            substitutionsEnabledBtn.visible = false;
            substitutionsDisabledBtn.visible = true;
        }

        final int offset = container.inverted? 18 * -3: 0;

        substitutionsEnabledBtn.xPosition  = this.guiLeft + 97 + offset;
        substitutionsDisabledBtn.xPosition = this.guiLeft + 97 + offset;
        doubleBtn.xPosition = this.guiLeft + 97 + offset;
        clearBtn.xPosition  = this.guiLeft + 87 + offset;
        invertBtn.xPosition = this.guiLeft + 87 + offset;
        combineEnableBtn.xPosition = this.guiLeft + 87 + offset;
        combineDisableBtn.xPosition = this.guiLeft + 87 + offset;

        processingScrollBar.setCurrentScroll(container.activePage);

        super.drawScreen(mouseX, mouseY, btn);
    }

    @Override
    protected void mouseClicked( final int xCoord, final int yCoord, final int btn )
    {
        final int currentScroll = this.processingScrollBar.getCurrentScroll();
        this.processingScrollBar.click(this, xCoord - this.guiLeft, yCoord - this.guiTop);
        super.mouseClicked(xCoord, yCoord, btn);

        if (currentScroll != this.processingScrollBar.getCurrentScroll()) {
            changeActivePage();
        }
    }

    @Override
    protected void mouseClickMove( final int x, final int y, final int c, final long d )
    {
        final int currentScroll = this.processingScrollBar.getCurrentScroll();
        this.processingScrollBar.click(this, x - this.guiLeft, y - this.guiTop );
        super.mouseClickMove(x, y, c, d);

        if (currentScroll != this.processingScrollBar.getCurrentScroll()) {
            changeActivePage();
        }
    }

    @Override
    public void handleMouseInput()
    {
        super.handleMouseInput();

        final int wheel = Mouse.getEventDWheel();

        if (wheel != 0) {
            final int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
            final int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight;

            if (this.processingScrollBar.contains(x - this.guiLeft, y - this.guiTop)) {
                final int currentScroll = this.processingScrollBar.getCurrentScroll();
                this.processingScrollBar.wheel(wheel);

                if (currentScroll != this.processingScrollBar.getCurrentScroll()) {
                    changeActivePage();
                }

            }
        }

    }

    private void changeActivePage()
    {
        FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns("PatternTerminalEx.ActivePage", String.valueOf(this.processingScrollBar.getCurrentScroll())));
    }

}
