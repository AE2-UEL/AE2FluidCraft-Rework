package com.glodblock.github.client.gui.container;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.PlayerSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.implementations.CraftingCPURecord;
import appeng.core.AELog;
import appeng.util.Platform;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.parts.PartFluidPatternTerminal;
import com.glodblock.github.common.parts.PartFluidPatternTerminalEx;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.network.SPacketMEInventoryUpdate;
import com.glodblock.github.util.Ae2Reflect;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.Util;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.Future;

public class ContainerFluidCraftConfirm extends AEBaseContainer {

    private final ArrayList<CraftingCPURecord> cpus = new ArrayList<CraftingCPURecord>();
    private Future<ICraftingJob> job;
    private ICraftingJob result;
    @GuiSync( 0 )
    public long bytesUsed;
    @GuiSync( 1 )
    public long cpuBytesAvail;
    @GuiSync( 2 )
    public int cpuCoProcessors;
    @GuiSync( 3 )
    public boolean autoStart = false;
    @GuiSync( 4 )
    public boolean simulation = true;
    @GuiSync( 5 )
    public int selectedCpu = -1;
    @GuiSync( 6 )
    public boolean noCPU = true;
    @GuiSync( 7 )
    public String myName = "";

    public ContainerFluidCraftConfirm(final InventoryPlayer ip, final ITerminalHost te )
    {
        super( ip, te );
    }

    public void cycleCpu( final boolean next )
    {
        if( next )
        {
            this.setSelectedCpu( this.getSelectedCpu() + 1 );
        }
        else
        {
            this.setSelectedCpu( this.getSelectedCpu() - 1 );
        }

        if( this.getSelectedCpu() < -1 )
        {
            this.setSelectedCpu( this.cpus.size() - 1 );
        }
        else if( this.getSelectedCpu() >= this.cpus.size() )
        {
            this.setSelectedCpu( -1 );
        }

        if( this.getSelectedCpu() == -1 )
        {
            this.setCpuAvailableBytes( 0 );
            this.setCpuCoProcessors( 0 );
            this.setName( "" );
        }
        else
        {
            this.setName(Ae2Reflect.getName(this.cpus.get(this.getSelectedCpu())));
            this.setCpuAvailableBytes(Ae2Reflect.getSize(this.cpus.get(this.getSelectedCpu())));
            this.setCpuCoProcessors(Ae2Reflect.getProcessors(this.cpus.get(this.getSelectedCpu())));
        }
    }

    @Override
    public void detectAndSendChanges()
    {
        if( Platform.isClient() )
        {
            return;
        }
        if (getGrid() == null)
            return;

        final ICraftingGrid cc = this.getGrid().getCache( ICraftingGrid.class );
        final ImmutableSet<ICraftingCPU> cpuSet = cc.getCpus();

        int matches = 0;
        boolean changed = false;
        for( final ICraftingCPU c : cpuSet )
        {
            boolean found = false;
            for( final CraftingCPURecord ccr : this.cpus )
            {
                if( Ae2Reflect.getCPU(ccr) == c )
                {
                    found = true;
                }
            }

            final boolean matched = this.cpuMatches( c );

            if( matched )
            {
                matches++;
            }

            if( found == !matched )
            {
                changed = true;
            }
        }

        if( changed || this.cpus.size() != matches )
        {
            this.cpus.clear();
            for( final ICraftingCPU c : cpuSet )
            {
                if( this.cpuMatches( c ) )
                {
                    this.cpus.add( new CraftingCPURecord( c.getAvailableStorage(), c.getCoProcessors(), c ) );
                }
            }

            this.sendCPUs();
        }

        this.setNoCPU( this.cpus.isEmpty() );

        super.detectAndSendChanges();

        if( this.getJob() != null && this.getJob().isDone() )
        {
            try
            {
                this.result = this.getJob().get();

                if( !this.result.isSimulation() )
                {
                    this.setSimulation( false );
                    if( this.isAutoStart() )
                    {
                        this.startJob();
                        return;
                    }
                }
                else
                {
                    this.setSimulation( true );
                }

                final SPacketMEInventoryUpdate a = new SPacketMEInventoryUpdate( (byte) 0 );
                final SPacketMEInventoryUpdate b = new SPacketMEInventoryUpdate( (byte) 1 );
                final SPacketMEInventoryUpdate c = this.result.isSimulation() ? new SPacketMEInventoryUpdate( (byte) 2 ) : null;

                final IItemList<IAEItemStack> plan = AEApi.instance().storage().createItemList();
                this.result.populatePlan( plan );

                this.setUsedBytes( this.result.getByteTotal() );

                for( final IAEItemStack out : plan )
                {

                    IAEItemStack o = out.copy();
                    o.reset();
                    o.setStackSize( out.getStackSize() );

                    final IAEItemStack p = out.copy();
                    p.reset();
                    p.setStackSize( out.getCountRequestable() );

                    final IStorageGrid sg = this.getGrid().getCache( IStorageGrid.class );
                    final IMEInventory<IAEItemStack> items = sg.getItemInventory();

                    IAEItemStack m = null;
                    if( c != null && this.result.isSimulation() )
                    {
                        m = o.copy();
                        o = items.extractItems( o, Actionable.SIMULATE, this.getActionSource() );

                        if( o == null )
                        {
                            o = m.copy();
                            o.setStackSize( 0 );
                        }

                        m.setStackSize( m.getStackSize() - o.getStackSize() );
                    }

                    if( o.getStackSize() > 0 )
                    {
                        a.appendItem( o );
                    }

                    if( p.getStackSize() > 0 )
                    {
                        b.appendItem( p );
                    }

                    if( c != null && m != null && m.getStackSize() > 0 )
                    {
                        c.appendItem( m );
                    }
                }

                for( final Object g : this.crafters )
                {
                    if( g instanceof EntityPlayer)
                    {
                        FluidCraft.proxy.netHandler.sendTo( a, (EntityPlayerMP) g );
                        FluidCraft.proxy.netHandler.sendTo( b, (EntityPlayerMP) g );
                        if( c != null )
                        {
                            FluidCraft.proxy.netHandler.sendTo( c, (EntityPlayerMP) g );
                        }
                    }
                }
            }
            catch( final Throwable e )
            {
                this.getPlayerInv().player.addChatMessage( new ChatComponentText( "Error: " + e.toString() ) );
                AELog.debug( e );
                this.setValidContainer( false );
                this.result = null;
            }

            this.setJob( null );
        }
        this.verifyPermissions( SecurityPermissions.CRAFT, false );
    }

    private IGrid getGrid()
    {
        final IActionHost h = ( (IActionHost) this.getTarget() );
        if (h == null || h.getActionableNode() == null)
            return null;
        return h.getActionableNode().getGrid();
    }

    private boolean cpuMatches( final ICraftingCPU c )
    {
        return c.getAvailableStorage() >= this.getUsedBytes() && !c.isBusy();
    }

    private void sendCPUs()
    {
        Collections.sort( this.cpus );

        if( this.getSelectedCpu() >= this.cpus.size() )
        {
            this.setSelectedCpu( -1 );
            this.setCpuAvailableBytes( 0 );
            this.setCpuCoProcessors( 0 );
            this.setName( "" );
        }
        else if( this.getSelectedCpu() != -1 )
        {
            this.setName(Ae2Reflect.getName(this.cpus.get(this.getSelectedCpu())));
            this.setCpuAvailableBytes(Ae2Reflect.getSize(this.cpus.get(this.getSelectedCpu())));
            this.setCpuCoProcessors(Ae2Reflect.getProcessors(this.cpus.get(this.getSelectedCpu())));
        }
    }

    public void startJob()
    {
        GuiType originalGui = null;

        final IActionHost ah = this.getActionHost();
        if( ah instanceof PartFluidPatternTerminal)
        {
            originalGui = GuiType.FLUID_PATTERN_TERMINAL;
        }
        if( ah instanceof PartFluidPatternTerminalEx)
        {
            originalGui = GuiType.FLUID_PATTERN_TERMINAL_EX;
        }

        if( this.result != null && !this.isSimulation() && getGrid() != null)
        {
            final ICraftingGrid cc = this.getGrid().getCache( ICraftingGrid.class );
            final ICraftingLink g = cc.submitJob( this.result, null, this.getSelectedCpu() == -1 ? null : Ae2Reflect.getCPU(this.cpus.get(this.getSelectedCpu())), true, this.getActionSrc() );
            this.setAutoStart( false );
            if( g != null && originalGui != null && this.getOpenContext() != null )
            {
                InventoryHandler.openGui(this.getInventoryPlayer().player, getWorld(), new BlockPos(this.getOpenContext().getTile()), Objects.requireNonNull(Util.from(this.getOpenContext().getSide())), originalGui);
            }
        }
    }

    private BaseActionSource getActionSrc()
    {
        return new PlayerSource( this.getPlayerInv().player, (IActionHost) this.getTarget() );
    }

    @Override
    public void removeCraftingFromCrafters( final ICrafting c )
    {
        super.removeCraftingFromCrafters( c );
        if( this.getJob() != null )
        {
            this.getJob().cancel( true );
            this.setJob( null );
        }
    }

    @Override
    public void onContainerClosed( final EntityPlayer par1EntityPlayer )
    {
        super.onContainerClosed( par1EntityPlayer );
        if( this.getJob() != null )
        {
            this.getJob().cancel( true );
            this.setJob( null );
        }
    }

    public World getWorld()
    {
        return this.getPlayerInv().player.worldObj;
    }

    public boolean isAutoStart()
    {
        return this.autoStart;
    }

    public void setAutoStart( final boolean autoStart )
    {
        this.autoStart = autoStart;
    }

    public long getUsedBytes()
    {
        return this.bytesUsed;
    }

    private void setUsedBytes( final long bytesUsed )
    {
        this.bytesUsed = bytesUsed;
    }

    public long getCpuAvailableBytes()
    {
        return this.cpuBytesAvail;
    }

    private void setCpuAvailableBytes( final long cpuBytesAvail )
    {
        this.cpuBytesAvail = cpuBytesAvail;
    }

    public int getCpuCoProcessors()
    {
        return this.cpuCoProcessors;
    }

    private void setCpuCoProcessors( final int cpuCoProcessors )
    {
        this.cpuCoProcessors = cpuCoProcessors;
    }

    public int getSelectedCpu()
    {
        return this.selectedCpu;
    }

    private void setSelectedCpu( final int selectedCpu )
    {
        this.selectedCpu = selectedCpu;
    }

    public String getName()
    {
        return this.myName;
    }

    private void setName( @Nonnull final String myName )
    {
        this.myName = myName;
    }

    public boolean hasNoCPU()
    {
        return this.noCPU;
    }

    private void setNoCPU( final boolean noCPU )
    {
        this.noCPU = noCPU;
    }

    public boolean isSimulation()
    {
        return this.simulation;
    }

    private void setSimulation( final boolean simulation )
    {
        this.simulation = simulation;
    }

    private Future<ICraftingJob> getJob()
    {
        return this.job;
    }

    public void setJob( final Future<ICraftingJob> job )
    {
        this.job = job;
    }
}
