package com.glodblock.github.network;

import com.glodblock.github.client.container.ContainerBurette;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class CPacketTransposeFluid implements IMessage {

    private int amount;
    private boolean into;

    public CPacketTransposeFluid(int amount, boolean into) {
        this.amount = amount;
        this.into = into;
    }

    public CPacketTransposeFluid() {
        // NO-OP
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(amount).writeBoolean(into);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        amount = buf.readInt();
        into = buf.readBoolean();
    }

    public static class Handler implements IMessageHandler<CPacketTransposeFluid, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(CPacketTransposeFluid message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (player.openContainer instanceof ContainerBurette) {
                    ((ContainerBurette)player.openContainer).tryTransferFluid(message.amount, message.into);
                }
            });
            return null;
        }

    }

}