package com.glodblock.github.common.parts;

import appeng.api.config.RedstoneMode;
import appeng.api.config.Upgrades;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.client.texture.CableBusTextures;
import appeng.me.GridAccessException;
import appeng.parts.automation.PartUpgradeable;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.Platform;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.Util;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.Objects;

public abstract class PartSharedFluidBus extends PartUpgradeable implements IGridTickable
{

    private final AppEngInternalAEInventory config = new AppEngInternalAEInventory( this, 9 );
    private boolean lastRedstone;

    public PartSharedFluidBus( ItemStack is )
    {
        super( is );
    }

    public abstract IIcon getFaceIcon();

    @Override
    public void upgradesChanged()
    {
        this.updateState();
    }

    @Override
    public void onNeighborChanged()
    {
        this.updateState();
        if( this.lastRedstone != this.getHost().hasRedstone( this.getSide() ) )
        {
            this.lastRedstone = !this.lastRedstone;
            if( this.lastRedstone && this.getRSMode() == RedstoneMode.SIGNAL_PULSE )
            {
                this.doBusWork();
            }
        }
    }

    private void updateState()
    {
        try
        {
            if( !this.isSleeping() )
            {
                this.getProxy().getTick().wakeDevice( this.getProxy().getNode() );
            }
            else
            {
                this.getProxy().getTick().sleepDevice( this.getProxy().getNode() );
            }
        }
        catch( final GridAccessException e )
        {
            // :P
        }
    }

    @Override
    public boolean onPartActivate(final EntityPlayer player, final Vec3 pos)
    {
        if( Platform.isServer() )
        {
            InventoryHandler.openGui(player, this.getHost().getTile().getWorldObj(), new BlockPos(this.getHost().getTile()), Objects.requireNonNull(Util.from(this.getSide())), GuiType.FLUID_BUS_IO);
        }

        return true;
    }

    @Override
    public void getBoxes( IPartCollisionHelper bch )
    {
        bch.addBox( 6, 6, 11, 10, 10, 13 );
        bch.addBox( 5, 5, 13, 11, 11, 14 );
        bch.addBox( 4, 4, 14, 12, 12, 16 );
    }

    protected TileEntity getConnectedTE()
    {
        TileEntity self = this.getHost().getTile();
        return this.getTileEntity( self, (new BlockPos(self)).getOffSet( this.getSide() ) );
    }

    private TileEntity getTileEntity( final TileEntity self, final BlockPos pos )
    {
        final World w = self.getWorldObj();

        if( w.getChunkProvider().chunkExists( pos.getX() >> 4, pos.getZ() >> 4 ) )
        {
            return w.getTileEntity( pos.getX(), pos.getY(), pos.getZ() );
        }

        return null;
    }

    protected int calculateAmountToSend()
    {
        double amount = 1000D;
        switch( this.getInstalledUpgrades( Upgrades.SPEED ) )
        {
            case 4:
                amount = amount * 1.5;
            case 3:
                amount = amount * 2;
            case 2:
                amount = amount * 4;
            case 1:
                amount = amount * 8;
            case 0:
            default:
                return (int) Math.floor( amount );
        }
    }

    @Override
    public void readFromNBT( NBTTagCompound extra )
    {
        super.readFromNBT( extra );
        this.config.readFromNBT( extra, "config" );
    }

    @Override
    public void writeToNBT( NBTTagCompound extra )
    {
        super.writeToNBT( extra );
        this.config.writeToNBT( extra, "config" );
    }

    @Override
    public IInventory getInventoryByName(String name) {
        if (name.equals("config")) {
            return this.config;
        }
        return super.getInventoryByName(name);
    }

    public void setFluidInSlot(int id, IAEFluidStack fluid) {
        ItemStack tmp = ItemFluidPacket.newStack(fluid == null ? null : fluid.getFluidStack());
        if (tmp != null) {
            tmp.setStackDisplayName(fluid.getFluidStack().getLocalizedName());
        }
        this.config.setInventorySlotContents(id, tmp);
    }

    protected StorageChannel getChannel()
    {
        return StorageChannel.FLUIDS;
    }

    protected abstract TickRateModulation doBusWork();

    protected abstract boolean canDoBusWork();

    @Override
    public int cableConnectionRenderTo()
    {
        return 5;
    }

}
