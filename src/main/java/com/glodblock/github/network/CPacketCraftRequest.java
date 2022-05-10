package com.glodblock.github.network;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.core.AELog;
import com.glodblock.github.client.gui.container.ContainerFluidCraftConfirm;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.Util;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.Future;

public class CPacketCraftRequest implements IMessage {

    private long amount;
    private boolean heldShift;

    public CPacketCraftRequest() {
    }

    public CPacketCraftRequest( final int craftAmt, final boolean shift ) {
        amount = craftAmt;
        heldShift = shift;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(amount);
        buf.writeBoolean(heldShift);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        amount = buf.readLong();
        heldShift = buf.readBoolean();
    }

    public static class Handler implements IMessageHandler<CPacketCraftRequest, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(CPacketCraftRequest message, MessageContext ctx) {
            if( ctx.getServerHandler().playerEntity.openContainer instanceof ContainerCraftAmount)
            {
                EntityPlayerMP player = ctx.getServerHandler().playerEntity;
                final ContainerCraftAmount cca = (ContainerCraftAmount) ctx.getServerHandler().playerEntity.openContainer;
                final Object target = cca.getTarget();
                if( target instanceof IGridHost)
                {
                    final IGridHost gh = (IGridHost) target;
                    final IGridNode gn = gh.getGridNode( ForgeDirection.UNKNOWN );

                    if( gn == null )
                    {
                        return null;
                    }

                    final IGrid g = gn.getGrid();
                    if( g == null || cca.getItemToCraft() == null )
                    {
                        return null;
                    }

                    cca.getItemToCraft().setStackSize( message.amount );

                    Future<ICraftingJob> futureJob = null;
                    try
                    {
                        final ICraftingGrid cg = g.getCache( ICraftingGrid.class );
                        futureJob = cg.beginCraftingJob( cca.getWorld(), cca.getGrid(), cca.getActionSrc(), cca.getItemToCraft(), null );

                        final ContainerOpenContext context = cca.getOpenContext();
                        if( context != null )
                        {

                            final TileEntity te = context.getTile();
                            InventoryHandler.openGui(player, player.worldObj, new BlockPos(te), Objects.requireNonNull(Util.from(context.getSide())), GuiType.FLUID_CRAFTING_CONFIRM);

                            if( player.openContainer instanceof ContainerFluidCraftConfirm)
                            {
                                final ContainerFluidCraftConfirm ccc = (ContainerFluidCraftConfirm) player.openContainer;
                                ccc.setAutoStart( message.heldShift );
                                ccc.setJob( futureJob );
                                cca.detectAndSendChanges();
                            }
                        }
                    }
                    catch( final Throwable e )
                    {
                        if( futureJob != null )
                        {
                            futureJob.cancel( true );
                        }
                        AELog.debug( e );
                    }
                }
            }
            return null;
        }

    }

}
