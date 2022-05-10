package com.glodblock.github.client.gui.container;

import appeng.api.AEApi;
import appeng.api.config.*;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.api.implementations.tiles.IMEChest;
import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.parts.IPart;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotRestrictedInput;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.me.helpers.ChannelPowerSrc;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.network.SPacketMEInventoryUpdate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.BufferOverflowException;

public class FCBaseMonitorContain extends AEBaseContainer implements IConfigManagerHost, IConfigurableObject, IMEMonitorHandlerReceiver<IAEItemStack>
{

    private final SlotRestrictedInput[] cellView = new SlotRestrictedInput[5];
    private final IMEMonitor<IAEItemStack> monitor;
    private final IItemList<IAEItemStack> items = AEApi.instance().storage().createItemList();
    private final IConfigManager clientCM;
    private final ITerminalHost host;
    @GuiSync( 99 )
    public boolean canAccessViewCells = false;
    @GuiSync( 98 )
    public boolean hasPower = false;
    private IConfigManagerHost gui;
    private IConfigManager serverCM;
    private IGridNode networkNode;

    public FCBaseMonitorContain(final InventoryPlayer ip, final ITerminalHost monitorable )
    {
        this( ip, monitorable, true );
    }

    protected FCBaseMonitorContain( final InventoryPlayer ip, final ITerminalHost monitorable, final boolean bindInventory )
    {
        super( ip, monitorable instanceof TileEntity ? (TileEntity) monitorable : null, monitorable instanceof IPart ? (IPart) monitorable : null );

        this.host = monitorable;
        this.clientCM = new ConfigManager( this );

        this.clientCM.registerSetting( Settings.SORT_BY, SortOrder.NAME );
        this.clientCM.registerSetting( Settings.VIEW_MODE, ViewItems.ALL );
        this.clientCM.registerSetting( Settings.SORT_DIRECTION, SortDir.ASCENDING );

        if( Platform.isServer() )
        {
            this.serverCM = monitorable.getConfigManager();

            this.monitor = monitorable.getItemInventory();
            if( this.monitor != null )
            {
                this.monitor.addListener( this, null );

                this.setCellInventory( this.monitor );

                if( monitorable instanceof IPortableCell)
                {
                    this.setPowerSource( (IEnergySource) monitorable );
                }
                else if( monitorable instanceof IMEChest)
                {
                    this.setPowerSource( (IEnergySource) monitorable );
                }
                else if( monitorable instanceof IGridHost)
                {
                    final IGridNode node = ( (IGridHost) monitorable ).getGridNode( ForgeDirection.UNKNOWN );
                    if( node != null )
                    {
                        this.networkNode = node;
                        final IGrid g = node.getGrid();
                        if( g != null )
                        {
                            this.setPowerSource( new ChannelPowerSrc( this.networkNode, (IEnergySource) g.getCache( IEnergyGrid.class ) ) );
                        }
                    }
                }
            }
            else
            {
                this.setValidContainer( false );
            }
        }
        else
        {
            this.monitor = null;
        }

        this.canAccessViewCells = false;
        if( monitorable instanceof IViewCellStorage)
        {
            for( int y = 0; y < 5; y++ )
            {
                this.cellView[y] = new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.VIEW_CELL, ( (IViewCellStorage) monitorable ).getViewCellStorage(), y, 206, y * 18 + 8, this.getInventoryPlayer() );
                this.cellView[y].setAllowEdit( this.canAccessViewCells );
                this.addSlotToContainer( this.cellView[y] );
            }
        }

        if( bindInventory )
        {
            this.bindPlayerInventory( ip, 0, 0 );
        }
    }

    public IGridNode getNetworkNode()
    {
        return this.networkNode;
    }

    @Override
    public void detectAndSendChanges()
    {
        if( Platform.isServer() )
        {
            if( this.monitor != this.host.getItemInventory() )
            {
                this.setValidContainer( false );
            }

            for( final Settings set : this.serverCM.getSettings() )
            {
                final Enum<?> sideLocal = this.serverCM.getSetting( set );
                final Enum<?> sideRemote = this.clientCM.getSetting( set );

                if( sideLocal != sideRemote )
                {
                    this.clientCM.putSetting( set, sideLocal );
                    for( final Object crafter : this.crafters )
                    {
                        try
                        {
                            NetworkHandler.instance.sendTo( new PacketValueConfig( set.name(), sideLocal.name() ), (EntityPlayerMP) crafter );
                        }
                        catch( final IOException e )
                        {
                            AELog.debug( e );
                        }
                    }
                }
            }

            if( !this.items.isEmpty() )
            {
                final IItemList<IAEItemStack> monitorCache = this.monitor.getStorageList();

                final SPacketMEInventoryUpdate piu = new SPacketMEInventoryUpdate();

                for( final IAEItemStack is : this.items )
                {
                    final IAEItemStack send = monitorCache.findPrecise( is );
                    if( send == null )
                    {
                        is.setStackSize( 0 );
                        piu.appendItem( is );
                    }
                    else
                    {
                        piu.appendItem( send );
                    }
                }

                if( !piu.isEmpty() )
                {
                    this.items.resetStatus();

                    for( final Object c : this.crafters )
                    {
                        if( c instanceof EntityPlayer)
                        {
                            FluidCraft.proxy.netHandler.sendTo(piu, (EntityPlayerMP) c);
                        }
                    }
                }
            }

            this.updatePowerStatus();

            final boolean oldAccessible = this.canAccessViewCells;
            this.canAccessViewCells =
                this.host instanceof WirelessTerminalGuiObject
                    || this.hasAccess( SecurityPermissions.BUILD, false );
            if( this.canAccessViewCells != oldAccessible )
            {
                for( int y = 0; y < 5; y++ )
                {
                    if( this.cellView[y] != null )
                    {
                        this.cellView[y].setAllowEdit( this.canAccessViewCells );
                    }
                }
            }

            super.detectAndSendChanges();
        }
    }

    protected void updatePowerStatus()
    {
        try
        {
            if( this.networkNode != null )
            {
                this.setPowered( this.networkNode.isActive() );
            }
            else if( this.getPowerSource() instanceof IEnergyGrid )
            {
                this.setPowered( ( (IEnergyGrid) this.getPowerSource() ).isNetworkPowered() );
            }
            else
            {
                this.setPowered( this.getPowerSource().extractAEPower( 1, Actionable.SIMULATE, PowerMultiplier.CONFIG ) > 0.8 );
            }
        }
        catch( final Throwable t )
        {
            // :P
        }
    }

    @Override
    public void onUpdate( final String field, final Object oldValue, final Object newValue )
    {
        if( field.equals( "canAccessViewCells" ) )
        {
            for( int y = 0; y < 5; y++ )
            {
                if( this.cellView[y] != null )
                {
                    this.cellView[y].setAllowEdit( this.canAccessViewCells );
                }
            }
        }

        super.onUpdate( field, oldValue, newValue );
    }

    @Override
    public void addCraftingToCrafters( final ICrafting c )
    {
        super.addCraftingToCrafters( c );
        this.queueInventory( c );
    }

    private void queueInventory( final ICrafting c )
    {
        if( Platform.isServer() && c instanceof EntityPlayer && this.monitor != null )
        {
            SPacketMEInventoryUpdate piu = new SPacketMEInventoryUpdate();
            final IItemList<IAEItemStack> monitorCache = this.monitor.getStorageList();

            for( final IAEItemStack send : monitorCache )
            {
                try
                {
                    piu.appendItem( send );
                }
                catch( final BufferOverflowException boe )
                {
                    FluidCraft.proxy.netHandler.sendTo( piu, (EntityPlayerMP) c );

                    piu = new SPacketMEInventoryUpdate();
                    piu.appendItem( send );
                }
            }
            FluidCraft.proxy.netHandler.sendTo( piu, (EntityPlayerMP) c );
        }
    }

    @Override
    public void removeCraftingFromCrafters( final ICrafting c )
    {
        super.removeCraftingFromCrafters( c );

        if( this.crafters.isEmpty() && this.monitor != null )
        {
            this.monitor.removeListener( this );
        }
    }

    @Override
    public void onContainerClosed( final EntityPlayer player )
    {
        super.onContainerClosed( player );
        if( this.monitor != null )
        {
            this.monitor.removeListener( this );
        }
    }

    @Override
    public boolean isValid( final Object verificationToken )
    {
        return true;
    }

    @Override
    public void postChange(final IBaseMonitor<IAEItemStack> monitor, final Iterable<IAEItemStack> change, final BaseActionSource source )
    {
        for( final IAEItemStack is : change )
        {
            this.items.add( is );
        }
    }

    @Override
    public void onListUpdate()
    {
        for( final Object c : this.crafters )
        {
            if( c instanceof ICrafting )
            {
                final ICrafting cr = (ICrafting) c;
                this.queueInventory( cr );
            }
        }
    }

    @Override
    public void updateSetting( final IConfigManager manager, final Enum settingName, final Enum newValue )
    {
        if( this.getGui() != null )
        {
            this.getGui().updateSetting( manager, settingName, newValue );
        }
    }

    @Override
    public IConfigManager getConfigManager()
    {
        if( Platform.isServer() )
        {
            return this.serverCM;
        }
        return this.clientCM;
    }

    public ItemStack[] getViewCells()
    {
        final ItemStack[] list = new ItemStack[this.cellView.length];

        for( int x = 0; x < this.cellView.length; x++ )
        {
            list[x] = this.cellView[x].getStack();
        }

        return list;
    }

    public SlotRestrictedInput getCellViewSlot( final int index )
    {
        return this.cellView[index];
    }

    public boolean isPowered()
    {
        return this.hasPower;
    }

    private void setPowered( final boolean isPowered )
    {
        this.hasPower = isPowered;
    }

    private IConfigManagerHost getGui()
    {
        return this.gui;
    }

    public void setGui( @Nonnull final IConfigManagerHost gui )
    {
        this.gui = gui;
    }
}
