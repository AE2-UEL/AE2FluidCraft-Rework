package com.glodblock.github.network;

import com.glodblock.github.interfaces.TankDumpable;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (player.openContainer instanceof TankDumpable) {
                    ((TankDumpable)player.openContainer).dumpTank(message.index);
                }
            });
            return null;
        }

    }

}
