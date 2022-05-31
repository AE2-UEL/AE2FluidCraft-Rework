package com.glodblock.github.common.parts;

import appeng.api.config.*;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.client.texture.CableBusTextures;
import appeng.me.GridAccessException;
import appeng.util.item.AEFluidStack;
import com.glodblock.github.client.textures.FCPartsTexture;
import com.glodblock.github.common.item.ItemFluidPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.IFluidHandler;

public class PartFluidExportBus extends PartSharedFluidBus {

    private final BaseActionSource source;

    public PartFluidExportBus( ItemStack is )
    {
        super( is );
        this.getConfigManager().registerSetting( Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE );
        this.getConfigManager().registerSetting( Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );
        this.getConfigManager().registerSetting( Settings.CRAFT_ONLY, YesNo.NO );
        this.getConfigManager().registerSetting( Settings.SCHEDULING_MODE, SchedulingMode.DEFAULT );
        this.source = new MachineSource( this );
    }

    @Override
    public IIcon getFaceIcon() {
        return FCPartsTexture.PartFluidExportBus.getIcon();
    }

    @Override
    public TickingRequest getTickingRequest( IGridNode node )
    {
        return new TickingRequest( 5, 40, this.isSleeping(), false );
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall )
    {
        return this.canDoBusWork() ? this.doBusWork() : TickRateModulation.IDLE;
    }

    @Override
    protected boolean canDoBusWork()
    {
        return this.getProxy().isActive();
    }

    @Override
    protected TickRateModulation doBusWork()
    {
        if( !this.canDoBusWork() )
        {
            return TickRateModulation.IDLE;
        }

        final TileEntity te = this.getConnectedTE();

        if(te instanceof IFluidHandler)
        {
            try
            {
                final IFluidHandler fh = (IFluidHandler) te;
                final IMEMonitor<IAEFluidStack> inv = this.getProxy().getStorage().getFluidInventory();

                for( int i = 0; i < this.getInventoryByName("config").getSizeInventory(); i++ )
                {
                    IAEFluidStack fluid = AEFluidStack.create(ItemFluidPacket.getFluidStack(this.getInventoryByName("config").getStackInSlot(i)));
                    if( fluid != null )
                    {
                        final IAEFluidStack toExtract = fluid.copy();

                        toExtract.setStackSize( this.calculateAmountToSend() );

                        final IAEFluidStack out = inv.extractItems( toExtract, Actionable.SIMULATE, this.source );

                        if( out != null )
                        {
                            int wasInserted = fh.fill( this.getSide().getOpposite(), out.getFluidStack(), true );

                            if( wasInserted > 0 )
                            {
                                toExtract.setStackSize( wasInserted );
                                inv.extractItems( toExtract, Actionable.MODULATE, this.source );

                                return TickRateModulation.FASTER;
                            }
                        }
                    }
                }

                return TickRateModulation.SLOWER;
            }
            catch( GridAccessException e )
            {
                // Ignore
            }
        }

        return TickRateModulation.SLEEP;
    }

    @Override
    public void getBoxes( final IPartCollisionHelper bch )
    {
        bch.addBox( 4, 4, 12, 12, 12, 14 );
        bch.addBox( 5, 5, 14, 11, 11, 15 );
        bch.addBox( 6, 6, 15, 10, 10, 16 );
        bch.addBox( 6, 6, 11, 10, 10, 12 );
    }

    @Override
    @SideOnly( Side.CLIENT )
    public void renderInventory(final IPartRenderHelper rh, final RenderBlocks renderer )
    {
        rh.setTexture( CableBusTextures.PartExportSides.getIcon(), CableBusTextures.PartExportSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(), this.getFaceIcon(), CableBusTextures.PartExportSides.getIcon(), CableBusTextures.PartExportSides.getIcon() );

        rh.setBounds( 4, 4, 12, 12, 12, 14 );
        rh.renderInventoryBox( renderer );

        rh.setBounds( 5, 5, 14, 11, 11, 15 );
        rh.renderInventoryBox( renderer );

        rh.setBounds( 6, 6, 15, 10, 10, 16 );
        rh.renderInventoryBox( renderer );
    }

    @Override
    @SideOnly( Side.CLIENT )
    public void renderStatic( final int x, final int y, final int z, final IPartRenderHelper rh, final RenderBlocks renderer )
    {
        this.setRenderCache( rh.useSimplifiedRendering( x, y, z, this, this.getRenderCache() ) );
        rh.setTexture( CableBusTextures.PartExportSides.getIcon(), CableBusTextures.PartExportSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(), this.getFaceIcon(), CableBusTextures.PartExportSides.getIcon(), CableBusTextures.PartExportSides.getIcon() );

        rh.setBounds( 4, 4, 12, 12, 12, 14 );
        rh.renderBlock( x, y, z, renderer );

        rh.setBounds( 5, 5, 14, 11, 11, 15 );
        rh.renderBlock( x, y, z, renderer );

        rh.setBounds( 6, 6, 15, 10, 10, 16 );
        rh.renderBlock( x, y, z, renderer );

        rh.setTexture( CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorBack.getIcon(), this.getFaceIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon() );

        rh.setBounds( 6, 6, 11, 10, 10, 12 );
        rh.renderBlock( x, y, z, renderer );

        this.renderLights( x, y, z, rh, renderer );
    }

    @Override
    public RedstoneMode getRSMode()
    {
        return (RedstoneMode) this.getConfigManager().getSetting( Settings.REDSTONE_CONTROLLED );
    }

}
