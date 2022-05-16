package com.glodblock.github.network;

import com.glodblock.github.client.gui.TankDumpable;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

import javax.annotation.Nullable;

public class CPacketDumpTank implements IMessage {

    private int index;

    public CPacketDumpTank(int index) {
        this.index = index;
    }

    public CPacketDumpTank() {
        // NO-OP
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeShort(index);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        index = buf.readShort();
    }

    public static class Handler implements IMessageHandler<CPacketDumpTank, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(CPacketDumpTank message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player.openContainer instanceof TankDumpable) {
                ((TankDumpable)player.openContainer).dumpTank(message.index);
            }
            return null;
        }

    }

}
