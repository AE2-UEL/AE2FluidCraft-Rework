package com.glodblock.github.client.gui;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.gui.widgets.GuiToggleButton;
import appeng.container.implementations.ContainerInterface;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.helpers.IInterfaceHost;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.network.CPacketSwitchGuis;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.NameConst;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Mouse;

public class GuiDualInterface extends GuiUpgradeable {

    private GuiTabButton priority;
    private GuiTabButton switcher;
    private GuiImgButton BlockMode;
    private GuiToggleButton interfaceMode;

    public GuiDualInterface(InventoryPlayer inventoryPlayer, IInterfaceHost te) {
        super(new ContainerInterface(inventoryPlayer, te));
        this.ySize = 211;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void addButtons()
    {
        this.priority = new GuiTabButton( this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.getLocal(), itemRender );
        this.buttonList.add( this.priority );

        this.switcher = new GuiTabButton( this.guiLeft + 132, this.guiTop, ItemAndBlockHolder.INTERFACE.stack(), StatCollector.translateToLocal("ae2fc.tooltip.switch_fluid_interface"), itemRender );
        this.buttonList.add( this.switcher );

        this.BlockMode = new GuiImgButton( this.guiLeft - 18, this.guiTop + 8, Settings.BLOCK, YesNo.NO );
        this.buttonList.add( this.BlockMode );

        this.interfaceMode = new GuiToggleButton( this.guiLeft - 18, this.guiTop + 26, 84, 85, GuiText.InterfaceTerminal.getLocal(), GuiText.InterfaceTerminalHint.getLocal() );
        this.buttonList.add( this.interfaceMode );
    }

    @Override
    public void drawFG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
    {
        if( this.BlockMode != null )
        {
            this.BlockMode.set( ( (ContainerInterface) this.cvb ).getBlockingMode() );
        }

        if( this.interfaceMode != null )
        {
            this.interfaceMode.setState( ( (ContainerInterface) this.cvb ).getInterfaceTerminalMode() == YesNo.YES );
        }

        this.fontRendererObj.drawString( StatCollector.translateToLocal(NameConst.GUI_FLUID_INTERFACE), 8, 6, 4210752 );
    }

    @Override
    protected String getBackground()
    {
        if (!ModAndClassUtil.isBigInterface)
            return "guis/interface.png";
        switch (((ContainerInterface) this.cvb).getPatternCapacityCardsInstalled())
        {
            case 1:
                return "guis/interface2.png";
            case 2:
                return "guis/interface3.png";
            case 3:
                return "guis/interface4.png";
        }
        return "guis/interface.png";
    }

    @Override
    protected void actionPerformed( final GuiButton btn )
    {
        super.actionPerformed( btn );

        final boolean backwards = Mouse.isButtonDown( 1 );

        if( btn == this.priority )
        {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketSwitchGuis(GuiType.DUAL_INTERFACE_PRIORITY));
        }

        if( btn == this.switcher )
        {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketSwitchGuis(GuiType.DUAL_INTERFACE_FLUID));
        }

        if( btn == this.interfaceMode )
        {
            NetworkHandler.instance.sendToServer( new PacketConfigButton( Settings.INTERFACE_TERMINAL, backwards ) );
        }

        if( btn == this.BlockMode )
        {
            NetworkHandler.instance.sendToServer( new PacketConfigButton( this.BlockMode.getSetting(), backwards ) );
        }
    }
}
