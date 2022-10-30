package com.glodblock.github.network;

import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.util.item.AEItemStack;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.container.ContainerItemAmountChange;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.io.IOException;

public class CPacketInventoryAction implements IMessage {

    private int action;
    private int slot;
    private long id;
    private IAEItemStack stack;
    private boolean isEmpty;

    public CPacketInventoryAction() {
        //NO-OP
    }

    public CPacketInventoryAction( final int action, final int slot, final int id, IAEItemStack stack ) {
        this.action = action;
        this.slot = slot;
        this.id = id;
        this.stack = stack;
        this.isEmpty = stack == null || stack.getDefinition().isEmpty();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(action);
        buf.writeInt(slot);
        buf.writeLong(id);
        buf.writeBoolean(isEmpty);
        if (!isEmpty) {
            try {
                stack.writeToPacket(buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        action = buf.readInt();
        slot = buf.readInt();
        id = buf.readLong();
        isEmpty = buf.readBoolean();
        if (!isEmpty) {
            stack = AEItemStack.fromPacket(buf);
        }
    }

    public static class Handler implements IMessageHandler<CPacketInventoryAction, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(CPacketInventoryAction message, MessageContext ctx) {
            final EntityPlayerMP sender = ctx.getServerHandler().player;
            if( sender.openContainer instanceof AEBaseContainer) {
                final AEBaseContainer baseContainer = (AEBaseContainer) sender.openContainer;
                if (message.action == 1)
                {
                    final ContainerOpenContext context = baseContainer.getOpenContext();
                    if( context != null )
                    {
                        final TileEntity te = context.getTile();
                        InventoryHandler.openGui( sender, te.getWorld(), te.getPos(), baseContainer.getOpenContext().getSide().getFacing(), GuiType.ITEM_AMOUNT_SET );
                        FluidCraft.proxy.netHandler.sendTo(new SPacketSetItemAmount((int) message.stack.getStackSize()), sender);
                        if( sender.openContainer instanceof ContainerItemAmountChange)
                        {
                            final ContainerItemAmountChange iac = (ContainerItemAmountChange) sender.openContainer;
                            if( message.stack != null )
                            {
                                iac.getPatternValue().putStack( message.stack.getDefinition() );
                                iac.setValueIndex( message.slot );
                                iac.setInitValue( message.stack.getStackSize() );
                            }
                            iac.detectAndSendChanges();
                        }
                    }
                }
            }
            return null;
        }
    }

}
