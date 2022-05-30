package com.glodblock.github.network;

import com.glodblock.github.client.container.ContainerFluidLevelMaintainer;
import com.glodblock.github.common.tile.TileFluidLevelMaintainer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class CPacketUpdateFluidLevel implements IMessage {

    private int index;
    private int size;

    public CPacketUpdateFluidLevel() {
        //NO-OP
    }

    public CPacketUpdateFluidLevel(int id, int value) {
        this.index = id;
        this.size = value;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        index = buf.readInt();
        size = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(index);
        buf.writeInt(size);
    }

    public static class Handler implements IMessageHandler<CPacketUpdateFluidLevel, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(CPacketUpdateFluidLevel message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (player.openContainer instanceof ContainerFluidLevelMaintainer) {
                    TileFluidLevelMaintainer te = ((ContainerFluidLevelMaintainer) player.openContainer).getTile();
                    if (message.index > 10) {
                        te.setRequest(message.index - 10, message.size);
                    }
                    else {
                        te.setConfig(message.index, message.size);
                    }
                }
            });
            return null;
        }

    }

}
