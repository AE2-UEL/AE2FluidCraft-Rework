package com.glodblock.github.common.part;

import appeng.api.config.*;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.core.sync.GuiBridge;
import appeng.fluids.parts.PartSharedFluidBus;
import appeng.helpers.MultiCraftingTracker;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.me.helpers.MachineSource;
import appeng.parts.PartModel;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.inventory.FluidConvertingInventoryAdaptor;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;

public class PartFluidExportBus extends PartSharedFluidBus implements ICraftingRequester
{
    public static final ResourceLocation MODEL_BASE = new ResourceLocation( AppEng.MOD_ID, "part/fluid_export_bus_base" );
    @PartModels
    public static final IPartModel MODELS_OFF = new PartModel( MODEL_BASE, new ResourceLocation( AppEng.MOD_ID, "part/fluid_export_bus_off" ) );
    @PartModels
    public static final IPartModel MODELS_ON = new PartModel( MODEL_BASE, new ResourceLocation( AppEng.MOD_ID, "part/fluid_export_bus_on" ) );
    @PartModels
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel( MODEL_BASE, new ResourceLocation( AppEng.MOD_ID, "part/fluid_export_bus_has_channel" ) );

    private final IActionSource source;
    private final MultiCraftingTracker craftingTracker = new MultiCraftingTracker( this, 9 );

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
    public void readFromNBT( final NBTTagCompound extra )
    {
        super.readFromNBT( extra );
        this.craftingTracker.readFromNBT( extra );
    }

    @Override
    public void writeToNBT( final NBTTagCompound extra )
    {
        super.writeToNBT( extra );
        this.craftingTracker.writeToNBT( extra );
    }

    @Override
    public boolean onPartActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
        if (Platform.isServer()) {
            Platform.openGUI(player, this.getHost().getTile(), this.getSide(), GuiBridge.GUI_BUS_FLUID);
        }
        return true;
    }

    @Override
    @Nonnull
    public TickingRequest getTickingRequest(@Nonnull IGridNode node )
    {
        return new TickingRequest( TickRates.FluidExportBus.getMin(), TickRates.FluidExportBus.getMax(), this.isSleeping(), false );
    }

    @Override
    @Nonnull
    public TickRateModulation tickingRequest(@Nonnull IGridNode node, int ticksSinceLastCall )
    {
        return this.canDoBusWork() ? this.doBusWork() : TickRateModulation.IDLE;
    }

    @Override
    protected boolean canDoBusWork()
    {
        return this.getProxy().isActive();
    }

    private boolean craftOnly() {
        return this.getConfigManager().getSetting(Settings.CRAFT_ONLY) == YesNo.YES;
    }

    @Override
    protected TickRateModulation doBusWork()
    {
        if( !this.canDoBusWork() )
        {
            return TickRateModulation.IDLE;
        }

        final TileEntity te = this.getConnectedTE();

        if( te != null && te.hasCapability( CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, this.getSide().getFacing().getOpposite() ) )
        {
            try
            {
                final InventoryAdaptor destination = this.getHandler();
                final ICraftingGrid cg = this.getProxy().getCrafting();
                final IFluidHandler fh = te.getCapability( CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, this.getSide().getFacing().getOpposite() );
                final IMEMonitor<IAEFluidStack> inv = this.getProxy().getStorage().getInventory( this.getChannel() );

                if( fh != null )
                {
                    for( int i = 0; i < this.getConfig().getSlots(); i++ )
                    {
                        IAEFluidStack fluid = this.getConfig().getFluidInSlot( i );

                        if( fluid != null )
                        {

                            final IAEFluidStack toExtract = fluid.copy();

                            toExtract.setStackSize( this.calculateAmountToSend() );

                            if( this.craftOnly() )
                            {
                                this.craftingTracker.handleCrafting( i, toExtract.getStackSize(), ItemFluidDrop.newAeStack(toExtract), destination, this.getTile().getWorld(), this.getProxy().getGrid(), cg, this.source );
                            }

                            final IAEFluidStack out = inv.extractItems( toExtract, Actionable.SIMULATE, this.source );

                            if( out != null )
                            {
                                int wasInserted = fh.fill( out.getFluidStack(), true );

                                if( wasInserted > 0 )
                                {
                                    toExtract.setStackSize( wasInserted );
                                    inv.extractItems( toExtract, Actionable.MODULATE, this.source );

                                    return TickRateModulation.FASTER;
                                }
                            }

                            if( this.isCraftingEnabled() ) {
                                this.craftingTracker.handleCrafting( i, toExtract.getStackSize(), ItemFluidDrop.newAeStack(toExtract), destination, this.getTile().getWorld(), this.getProxy().getGrid(), cg, this.source );
                            }

                        }
                    }

                    return TickRateModulation.SLOWER;
                }
            }
            catch( GridAccessException e )
            {
                // Ignore
            }
        }

        return TickRateModulation.SLEEP;
    }

    private boolean isCraftingEnabled()
    {
        return this.getInstalledUpgrades( Upgrades.CRAFTING ) > 0;
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
    public RedstoneMode getRSMode()
    {
        return (RedstoneMode) this.getConfigManager().getSetting( Settings.REDSTONE_CONTROLLED );
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels()
    {
        if( this.isActive() && this.isPowered() )
        {
            return MODELS_HAS_CHANNEL;
        }
        else if( this.isPowered() )
        {
            return MODELS_ON;
        }
        else
        {
            return MODELS_OFF;
        }
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return this.craftingTracker.getRequestedJobs();
    }

    protected InventoryAdaptor getHandler() {
        TileEntity self = this.getHost().getTile();
        TileEntity target = this.getTileEntity(self, self.getPos().offset(this.getSide().getFacing()));
        return target != null ? FluidConvertingInventoryAdaptor.wrap(target, this.getSide().getFacing().getOpposite()) : null;
    }

    private TileEntity getTileEntity(TileEntity self, BlockPos pos) {
        World w = self.getWorld();
        return w.getChunkProvider().getLoadedChunk(pos.getX() >> 4, pos.getZ() >> 4) != null ? w.getTileEntity(pos) : null;
    }

    @Override
    public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack items, Actionable mode) {
        final InventoryAdaptor d = this.getHandler();

        try
        {
            if( d != null && this.getProxy().isActive() )
            {
                final IEnergyGrid energy = this.getProxy().getEnergy();
                final double power = items.getStackSize();

                if( energy.extractAEPower( power, mode, PowerMultiplier.CONFIG ) > power - 0.01 )
                {
                    ItemStack inputStack = items.getCachedItemStack( items.getStackSize() );

                    ItemStack remaining;

                    if( mode == Actionable.SIMULATE )
                    {
                        remaining = d.simulateAdd( inputStack );
                        items.setCachedItemStack( inputStack );
                    }
                    else
                    {
                        remaining = d.addItems( inputStack );
                        if( !remaining.isEmpty() )
                        {
                            items.setCachedItemStack( remaining );
                        }
                    }

                    if( remaining == inputStack )
                    {
                        return items;
                    }

                    return AEItemStack.fromItemStack( remaining );
                }
            }
        }
        catch( final GridAccessException e )
        {
            AELog.debug( e );
        }

        return items;
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        this.craftingTracker.jobStateChange( link );
    }
}