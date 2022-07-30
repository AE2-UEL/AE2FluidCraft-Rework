package com.glodblock.github.client.gui;

import appeng.api.config.*;
import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.gui.AEBaseMEGui;
import appeng.client.gui.widgets.*;
import appeng.client.me.InternalSlotME;
import appeng.client.me.ItemRepo;
import appeng.client.me.SlotDisconnected;
import appeng.client.me.SlotME;
import appeng.container.AEBaseContainer;
import appeng.container.slot.*;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.helpers.InventoryAction;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.FCBaseMonitorContain;
import com.glodblock.github.client.gui.container.FCBasePartContainer;
import com.glodblock.github.network.CPacketInventoryAction;
import com.glodblock.github.util.Ae2ReflectClient;
import com.glodblock.github.util.ModAndClassUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;

public class GuiFCBaseMonitor extends AEBaseMEGui implements ISortSource, IConfigManagerHost
{

    public static int craftingGridOffsetX;
    public static int craftingGridOffsetY;

    private static String memoryText = "";
    private final ItemRepo repo;
    private final int offsetX = 9;
    private final int lowerTextureOffset = 0;
    public IConfigManager configSrc;
    private final boolean viewCell;
    private final ItemStack[] myCurrentViewCells = new ItemStack[5];
    public FCBaseMonitorContain monitorableContainer;
    public GuiTabButton craftingStatusBtn;
    private GuiImgButton craftingStatusImgBtn;
    private FCGuiTextField searchField;
    private int perRow = 9;
    private int reservedSpace = 0;
    private boolean customSortOrder = true;
    private int rows = 0;
    private int maxRows = Integer.MAX_VALUE;
    private int standardSize;
    private GuiImgButton ViewBox;
    private GuiImgButton SortByBox;
    private GuiImgButton SortDirBox;
    private GuiImgButton searchBoxSettings;
    private GuiImgButton terminalStyleBox;
    private GuiImgButton searchStringSave;

    public GuiFCBaseMonitor( final InventoryPlayer inventoryPlayer, final ITerminalHost te, final FCBaseMonitorContain c )
    {

        super( c );

        final GuiScrollbar scrollbar = new GuiScrollbar();
        this.setScrollBar( scrollbar );
        this.repo = new ItemRepo( scrollbar, this );

        this.xSize = 195;
        this.ySize = 204;

        this.standardSize = this.xSize;

        this.configSrc = ( (IConfigurableObject) this.inventorySlots ).getConfigManager();
        ( this.monitorableContainer = (FCBaseMonitorContain) this.inventorySlots ).setGui( this );

        this.viewCell = te instanceof IViewCellStorage;

    }

    public void postUpdate( final List<IAEItemStack> list )
    {
        for( final IAEItemStack is : list )
        {
            this.repo.postUpdate( is );
        }

        this.repo.updateView();
        this.setScrollBar();
    }

    private void setScrollBar()
    {
        this.getScrollBar().setTop( 18 ).setLeft( 175 ).setHeight( this.rows * 18 - 2 );
        this.getScrollBar().setRange( 0, ( this.repo.size() + this.perRow - 1 ) / this.perRow - this.rows, Math.max( 1, this.rows / 6 ) );
    }

    @Override
    protected void actionPerformed( final GuiButton btn )
    {

        if(  btn == this.craftingStatusBtn || btn == this.craftingStatusImgBtn )
        {
            NetworkHandler.instance.sendToServer( new PacketSwitchGuis( GuiBridge.GUI_CRAFTING_STATUS ) );
        }

        if( btn instanceof GuiImgButton )
        {
            final boolean backwards = Mouse.isButtonDown( 1 );

            final GuiImgButton iBtn = (GuiImgButton) btn;
            if( iBtn.getSetting() != Settings.ACTIONS )
            {
                final Enum<?> cv = iBtn.getCurrentValue();
                final Enum<?> next = Platform.rotateEnum( cv, backwards, iBtn.getSetting().getPossibleValues() );

                if( btn == this.terminalStyleBox )
                {
                    AEConfig.instance.settings.putSetting( iBtn.getSetting(), next );
                }
                else if( btn == this.searchBoxSettings )
                {
                    AEConfig.instance.settings.putSetting( iBtn.getSetting(), next );
                }
                else if( ModAndClassUtil.isSaveText && btn == this.searchStringSave )
                {
                    AEConfig.instance.preserveSearchBar = next == YesNo.YES;
                }
                else
                {
                    try
                    {
                        NetworkHandler.instance.sendToServer( new PacketValueConfig( iBtn.getSetting().name(), next.name() ) );
                    }
                    catch( final IOException e )
                    {
                        AELog.debug( e );
                    }
                }

                iBtn.set( next );

                if( next.getClass() == SearchBoxMode.class || next.getClass() == TerminalStyle.class )
                {
                    this.reinitalize();
                }
            }
        }
    }

    private void reinitalize()
    {
        this.buttonList.clear();
        this.initGui();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui()
    {
        Keyboard.enableRepeatEvents( true );

        this.maxRows = this.getMaxRows();
        this.perRow = AEConfig.instance.getConfigManager().getSetting( Settings.TERMINAL_STYLE ) != TerminalStyle.FULL ? 9 : 9 + ( ( this.width - this.standardSize ) / 18 );

        final boolean hasNEI = IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.NEI );

        final int NEI = hasNEI ? 0 : 0;
        int top = hasNEI ? 22 : 0;

        final int magicNumber = 114 + 1;
        final int extraSpace = this.height - magicNumber - NEI - top - this.reservedSpace;

        this.rows = (int) Math.floor( extraSpace / 18.0 );
        if( this.rows > this.maxRows )
        {
            top += ( this.rows - this.maxRows ) * 18 / 2;
            this.rows = this.maxRows;
        }

        if( hasNEI )
        {
            this.rows--;
        }

        if( this.rows < 3 )
        {
            this.rows = 3;
        }

        this.getMeSlots().clear();
        for( int y = 0; y < this.rows; y++ )
        {
            for( int x = 0; x < this.perRow; x++ )
            {
                this.getMeSlots().add( new InternalSlotME( this.repo, x + y * this.perRow, this.offsetX + x * 18, 18 + y * 18 ) );
            }
        }

        if( AEConfig.instance.getConfigManager().getSetting( Settings.TERMINAL_STYLE ) != TerminalStyle.FULL )
        {
            this.xSize = this.standardSize + ( ( this.perRow - 9 ) * 18 );
        }
        else
        {
            this.xSize = this.standardSize;
        }

        super.initGui();
        // full size : 204
        // extra slots : 72
        // slot 18

        this.ySize = magicNumber + this.rows * 18 + this.reservedSpace;
        // this.guiTop = top;
        final int unusedSpace = this.height - this.ySize;
        this.guiTop = (int) Math.floor( unusedSpace / ( unusedSpace < 0 ? 3.8f : 2.0f ) );

        int offset = this.guiTop + 8;

        if( this.customSortOrder )
        {
            this.buttonList.add( this.SortByBox = new GuiImgButton( this.guiLeft - 18, offset, Settings.SORT_BY, this.configSrc.getSetting( Settings.SORT_BY ) ) );
            offset += 20;
        }

        this.buttonList.add( this.ViewBox = new GuiImgButton( this.guiLeft - 18, offset, Settings.VIEW_MODE, this.configSrc.getSetting( Settings.VIEW_MODE ) ) );
        offset += 20;

        this.buttonList.add( this.SortDirBox = new GuiImgButton( this.guiLeft - 18, offset, Settings.SORT_DIRECTION, this.configSrc.getSetting( Settings.SORT_DIRECTION ) ) );
        offset += 20;

        this.buttonList.add( this.searchBoxSettings = new GuiImgButton( this.guiLeft - 18, offset, Settings.SEARCH_MODE, AEConfig.instance.settings.getSetting( Settings.SEARCH_MODE ) ) );
        offset += 20;

        if (ModAndClassUtil.isSaveText) {
            this.buttonList.add( this.searchStringSave = new GuiImgButton( this.guiLeft - 18, offset, Settings.SAVE_SEARCH, AEConfig.instance.preserveSearchBar ? YesNo.YES : YesNo.NO ) );
            offset += 20;
        }

        this.buttonList.add( this.terminalStyleBox = new GuiImgButton( this.guiLeft - 18, offset, Settings.TERMINAL_STYLE, AEConfig.instance.settings.getSetting( Settings.TERMINAL_STYLE ) ) );
        offset += 20;

        this.searchField = new FCGuiTextField( this.fontRendererObj, this.guiLeft + Math.max( 80, this.offsetX ), this.guiTop + 4, 90, 12 );
        this.searchField.setEnableBackgroundDrawing( false );
        this.searchField.setMaxStringLength( 25 );
        this.searchField.setTextColor( 0xFFFFFF );
        this.searchField.setVisible( true );
        if (ModAndClassUtil.isSearchStringTooltip)
            searchField.setMessage(ButtonToolTips.SearchStringTooltip.getLocal());

        if( this.viewCell)
        {
            if (ModAndClassUtil.isCraftStatus && AEConfig.instance.getConfigManager().getSetting(Settings.CRAFTING_STATUS).equals(CraftingStatus.BUTTON)) {
                this.buttonList.add(this.craftingStatusImgBtn = new GuiImgButton(this.guiLeft - 18, offset, Settings.CRAFTING_STATUS, AEConfig.instance.settings.getSetting(Settings.CRAFTING_STATUS)));
            } else {
                this.buttonList.add(this.craftingStatusBtn = new GuiTabButton(this.guiLeft + 170, this.guiTop - 4, 2 + 11 * 16, GuiText.CraftingStatus.getLocal(), itemRender));
                this.craftingStatusBtn.setHideEdge( 13 ); // GuiTabButton implementation //
            }
        }

        // Enum setting = AEConfig.INSTANCE.getSetting( "Terminal", SearchBoxMode.class, SearchBoxMode.AUTOSEARCH );
        final Enum<?> setting = AEConfig.instance.settings.getSetting( Settings.SEARCH_MODE );
        this.searchField.setFocused( SearchBoxMode.AUTOSEARCH == setting || SearchBoxMode.NEI_AUTOSEARCH == setting );

        if (ModAndClassUtil.isSearchBar && (AEConfig.instance.preserveSearchBar || this.isSubGui()))
        {
            setSearchString(memoryText, false);
        }
        if( this.isSubGui() )
        {
            this.repo.updateView();
            this.setScrollBar();
        }

        craftingGridOffsetX = Integer.MAX_VALUE;
        craftingGridOffsetY = Integer.MAX_VALUE;

        for( final Object s : this.inventorySlots.inventorySlots )
        {
            if( s instanceof AppEngSlot)
            {
                if( ( (Slot) s ).xDisplayPosition < 195 )
                {
                    this.repositionSlot( (AppEngSlot) s );
                }
            }

            if( s instanceof SlotCraftingMatrix || s instanceof SlotFakeCraftingMatrix)
            {
                final Slot g = (Slot) s;
                if( g.xDisplayPosition > 0 && g.yDisplayPosition > 0 )
                {
                    craftingGridOffsetX = Math.min( craftingGridOffsetX, g.xDisplayPosition );
                    craftingGridOffsetY = Math.min( craftingGridOffsetY, g.yDisplayPosition );
                }
            }
        }

        craftingGridOffsetX -= 25;
        craftingGridOffsetY -= 6;
    }

    public void setSearchString(String memoryText, boolean updateView)
    {
        this.searchField.setText(memoryText);
        this.repo.setSearchString(memoryText);
        if (updateView)
        {
            this.repo.updateView();
            this.setScrollBar();
        }
    }

    @Override
    public void drawFG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
    {
        this.fontRendererObj.drawString( this.getGuiDisplayName( GuiText.Terminal.getLocal() ), 8, 6, 4210752 );
        this.fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752 );
    }

    @Override
    protected void mouseClicked( final int xCoord, final int yCoord, final int btn )
    {
        final Enum<?> searchMode = AEConfig.instance.settings.getSetting( Settings.SEARCH_MODE );

        if( searchMode != SearchBoxMode.AUTOSEARCH && searchMode != SearchBoxMode.NEI_AUTOSEARCH )
        {
            this.searchField.mouseClicked( xCoord, yCoord, btn );
        }

        if( btn == 1 && this.searchField.isMouseIn( xCoord, yCoord ) )
        {
            setSearchString("", true);
        }

        super.mouseClicked( xCoord, yCoord, btn );
    }

    @Override
    protected void handleMouseClick( final Slot slot, final int slotIdx, final int ctrlDown, final int mouseButton )
    {
        final EntityPlayer player = Minecraft.getMinecraft().thePlayer;

        if( slot instanceof SlotFake)
        {
            InventoryAction action = ctrlDown == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;

            if (Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU)) {
                if (action == InventoryAction.SPLIT_OR_PLACE_SINGLE) {
                    action = InventoryAction.MOVE_REGION;
                }
                else {
                    action = InventoryAction.PICKUP_SINGLE;
                }
            }

            if( Ae2ReflectClient.getDragClick(this).size() > 1 )
            {
                return;
            }

            final PacketInventoryAction p = new PacketInventoryAction( action, slotIdx, 0 );
            NetworkHandler.instance.sendToServer( p );

            return;
        }

        if( slot instanceof SlotPatternTerm)
        {
            if( mouseButton == 6 )
            {
                return; // prevent weird double clicks..
            }

            try
            {
                NetworkHandler.instance.sendToServer( ( (SlotPatternTerm) slot ).getRequest( isShiftKeyDown() ) );
            }
            catch( final IOException e )
            {
                AELog.debug( e );
            }
        }
        else if( slot instanceof SlotCraftingTerm )
        {
            if( mouseButton == 6 )
            {
                return; // prevent weird double clicks..
            }

            InventoryAction action;
            if( isShiftKeyDown() )
            {
                action = InventoryAction.CRAFT_SHIFT;
            }
            else
            {
                // Craft stack on right-click, craft single on left-click
                action = ( mouseButton == 1 ) ? InventoryAction.CRAFT_STACK : InventoryAction.CRAFT_ITEM;
            }

            final PacketInventoryAction p = new PacketInventoryAction( action, slotIdx, 0 );
            NetworkHandler.instance.sendToServer( p );

            return;
        }

        if( Keyboard.isKeyDown( Keyboard.KEY_SPACE ) )
        {
            if( this.enableSpaceClicking() && !( slot instanceof SlotPatternTerm ) )
            {
                IAEItemStack stack = null;
                if( slot instanceof SlotME )
                {
                    stack = ( (SlotME) slot ).getAEStack();
                }

                int slotNum = Ae2ReflectClient.getInventorySlots(this).size();

                if( !( slot instanceof SlotME ) && slot != null )
                {
                    slotNum = slot.slotNumber;
                }

                ( (AEBaseContainer) this.inventorySlots ).setTargetStack( stack );
                final PacketInventoryAction p = new PacketInventoryAction( InventoryAction.MOVE_REGION, slotNum, 0 );
                NetworkHandler.instance.sendToServer( p );
                return;
            }
        }

        if( slot instanceof SlotDisconnected)
        {
            if( Ae2ReflectClient.getDragClick(this).size() > 1 )
            {
                return;
            }

            InventoryAction action = null;

            switch( mouseButton )
            {
                case 0: // pickup / set-down.
                {
                    ItemStack heldStack = player.inventory.getItemStack();
                    if (slot.getStack() == null && heldStack != null)
                        action = InventoryAction.SPLIT_OR_PLACE_SINGLE;
                    else if (slot.getStack() != null && (heldStack == null || heldStack.stackSize <= 1))
                        action = InventoryAction.PICKUP_OR_SET_DOWN;
                }
                break;
                case 1:
                    action = ctrlDown == 1 ? InventoryAction.PICKUP_SINGLE : InventoryAction.SHIFT_CLICK;
                    break;

                case 3: // creative dupe:

                    if( player.capabilities.isCreativeMode )
                    {
                        action = InventoryAction.CREATIVE_DUPLICATE;
                    }

                    break;

                default:
                case 4: // drop item:
                case 6:
            }

            if( action != null )
            {
                final PacketInventoryAction p = new PacketInventoryAction( action, slot.getSlotIndex(), ( (SlotDisconnected) slot ).getSlot().getId() );
                NetworkHandler.instance.sendToServer( p );
            }

            return;
        }

        if( slot instanceof SlotME )
        {
            InventoryAction action = null;
            IAEItemStack stack = null;

            switch( mouseButton )
            {
                case 0: // pickup / set-down.
                    action = ctrlDown == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;
                    stack = ( (SlotME) slot ).getAEStack();

                    if( stack != null && action == InventoryAction.PICKUP_OR_SET_DOWN && stack.getStackSize() == 0 && player.inventory.getItemStack() == null )
                    {
                        action = InventoryAction.AUTO_CRAFT;
                    }

                    break;
                case 1:
                    action = ctrlDown == 1 ? InventoryAction.PICKUP_SINGLE : InventoryAction.SHIFT_CLICK;
                    stack = ( (SlotME) slot ).getAEStack();
                    break;

                case 3: // creative dupe:

                    stack = ( (SlotME) slot ).getAEStack();
                    if( stack != null && stack.isCraftable() )
                    {
                        action = InventoryAction.AUTO_CRAFT;
                    }
                    else if( player.capabilities.isCreativeMode )
                    {
                        final IAEItemStack slotItem = ( (SlotME) slot ).getAEStack();
                        if( slotItem != null )
                        {
                            action = InventoryAction.CREATIVE_DUPLICATE;
                        }
                    }
                    break;

                default:
                case 4: // drop item:
                case 6:
            }

            if (action == InventoryAction.AUTO_CRAFT) {
                ((AEBaseContainer) this.inventorySlots).setTargetStack( stack );
                FluidCraft.proxy.netHandler.sendToServer(new CPacketInventoryAction(action, Ae2ReflectClient.getInventorySlots(this).size(), 0, stack));
            }
            else if( action != null )
            {
                ( (AEBaseContainer) this.inventorySlots ).setTargetStack( stack );
                final PacketInventoryAction p = new PacketInventoryAction( action, Ae2ReflectClient.getInventorySlots(this).size(), 0 );
                NetworkHandler.instance.sendToServer( p );
            }

            return;
        }

        super.handleMouseClick( slot, slotIdx, ctrlDown, mouseButton );
    }

    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents( false );
        memoryText = this.searchField.getText();
    }

    public void bindTextureBack( final String file )
    {
        final ResourceLocation loc = new ResourceLocation( FluidCraft.MODID, "textures/" + file );
        this.mc.getTextureManager().bindTexture( loc );
    }

    @Override
    public void drawBG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
    {

        this.bindTextureBack( this.getBackground() );
        final int x_width = 195;
        this.drawTexturedModalRect( offsetX, offsetY, 0, 0, x_width, 18 );

        if( this.viewCell )
        {
            this.drawTexturedModalRect( offsetX + x_width, offsetY, x_width, 0, 46, 128 );
        }

        for( int x = 0; x < this.rows; x++ )
        {
            this.drawTexturedModalRect( offsetX, offsetY + 18 + x * 18, 0, 18, x_width, 18 );
        }

        this.drawTexturedModalRect( offsetX, offsetY + 16 + this.rows * 18 + this.lowerTextureOffset, 0, 106 - 18 - 18, x_width, 99 + this.reservedSpace - this.lowerTextureOffset );

        if( this.viewCell )
        {
            boolean update = false;

            for( int i = 0; i < 5; i++ )
            {
                if( this.myCurrentViewCells[i] != this.monitorableContainer.getCellViewSlot( i ).getStack() )
                {
                    update = true;
                    this.myCurrentViewCells[i] = this.monitorableContainer.getCellViewSlot( i ).getStack();
                }
            }

            if( update )
            {
                this.repo.setViewCell( this.myCurrentViewCells );
            }
        }

        if( this.searchField != null )
        {
            this.searchField.drawTextBox();
        }
    }

    protected String getBackground()
    {
        return "gui/terminal.png";
    }

    @Override
    protected boolean isPowered()
    {
        return this.repo.hasPower();
    }

    int getMaxRows()
    {
        return AEConfig.instance.getConfigManager().getSetting( Settings.TERMINAL_STYLE ) == TerminalStyle.SMALL ? 6 : Integer.MAX_VALUE;
    }

    protected void repositionSlot( final AppEngSlot s )
    {
        s.yDisplayPosition = s.getY() + this.ySize - 78 - 5;
    }

    @Override
    protected void keyTyped( final char character, final int key )
    {
        if( !this.checkHotbarKeys( key ) )
        {
            if( character == ' ' && this.searchField.getText().isEmpty() )
            {
                return;
            }

            if( this.searchField.textboxKeyTyped( character, key ) )
            {
                this.repo.setSearchString( this.searchField.getText() );
                this.repo.updateView();
                this.setScrollBar();
            }
            else
            {
                super.keyTyped( character, key );
            }
        }
    }

    @Override
    public void updateScreen()
    {
        this.repo.setPower( this.monitorableContainer.isPowered() );
        super.updateScreen();
    }

    @Override
    public Enum<?> getSortBy()
    {
        return this.configSrc.getSetting( Settings.SORT_BY );
    }

    @Override
    public Enum<?> getSortDir()
    {
        return this.configSrc.getSetting( Settings.SORT_DIRECTION );
    }

    @Override
    public Enum<?> getSortDisplay()
    {
        return this.configSrc.getSetting( Settings.VIEW_MODE );
    }

    @Override
    public void updateSetting( final IConfigManager manager, final Enum settingName, final Enum newValue )
    {
        if( this.SortByBox != null )
        {
            this.SortByBox.set( this.configSrc.getSetting( Settings.SORT_BY ) );
        }

        if( this.SortDirBox != null )
        {
            this.SortDirBox.set( this.configSrc.getSetting( Settings.SORT_DIRECTION ) );
        }

        if( this.ViewBox != null )
        {
            this.ViewBox.set( this.configSrc.getSetting( Settings.VIEW_MODE ) );
        }

        this.repo.updateView();
    }

    public int getReservedSpace()
    {
        return this.reservedSpace;
    }

    public void setReservedSpace( final int reservedSpace )
    {
        this.reservedSpace = reservedSpace;
    }

    public boolean isCustomSortOrder()
    {
        return this.customSortOrder;
    }

    public void setCustomSortOrder( final boolean customSortOrder )
    {
        this.customSortOrder = customSortOrder;
    }

    public int getStandardSize()
    {
        return this.standardSize;
    }

    public void setStandardSize( final int standardSize )
    {
        this.standardSize = standardSize;
    }

    @Override
    public void drawScreen( final int mouseX, final int mouseY, final float btn ) {
        super.drawScreen(mouseX, mouseY, btn);
        if (ModAndClassUtil.isSearchBar && AEConfig.instance.preserveSearchBar && searchField != null)
            handleTooltip(mouseX, mouseY, searchField.new TooltipProvider());
    }
    public boolean isOverSearchField(int x, int y)
    {
        return searchField.isMouseIn(x,y);
    }

    public boolean hideItemPanelSlot(int tx, int ty, int tw, int th)
    {

        if (this.viewCell) {
            int rw = 33;
            int rh = 14 + myCurrentViewCells.length * 18;

            if (rw <= 0 || rh <= 0 || tw <= 0 || th <= 0) {
                return false;
            }

            int rx = this.guiLeft + this.xSize;
            int ry = this.guiTop + 0;

            rw += rx;
            rh += ry;
            tw += tx;
            th += ty;

            //      overflow || intersect
            return (rw < rx || rw > tx) && (rh < ry || rh > ty) && (tw < tx || tw > rx) && (th < ty || th > ry);
        }

        return false;
    }

}
