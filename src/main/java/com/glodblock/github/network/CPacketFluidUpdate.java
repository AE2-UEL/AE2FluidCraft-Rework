package com.glodblock.github.network;

import appeng.api.storage.data.IAEFluidStack;
import appeng.util.item.AEFluidStack;
import com.glodblock.github.client.gui.container.ContainerFluidConfigurable;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.Container;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CPacketFluidUpdate implements IMessage {

    private Map<Integer, IAEFluidStack> list;

    public CPacketFluidUpdate() {
    }

    public CPacketFluidUpdate(Map<Integer, IAEFluidStack> data) {
        this.list = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        this.list = new HashMap<>();
        try {
            for (int i = 0; i < size; i ++) {
                int id = buf.readInt();
                boolean isNull = buf.readBoolean();
                if (!isNull)
                    list.put(id, null);
                else {
                    IAEFluidStack fluid = AEFluidStack.loadFluidStackFromPacket(buf);
                    list.put(id, fluid);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(list.size());
        try {
            for (Map.Entry<Integer, IAEFluidStack> fs : list.entrySet()) {
                buf.writeInt(fs.getKey());
                if (fs.getValue() == null)
                    buf.writeBoolean(false);
                else {
                    buf.writeBoolean(true);
                    fs.getValue().writeToPacket(buf);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class Handler implements IMessageHandler<CPacketFluidUpdate, IMessage> {

        @Override
        public IMessage onMessage(CPacketFluidUpdate message, MessageContext ctx) {
            final Container c = ctx.getServerHandler().playerEntity.openContainer;
            if( c instanceof ContainerFluidConfigurable)
            {
                for (Map.Entry<Integer, IAEFluidStack> e : message.list.entrySet() ) {
                    ( (ContainerFluidConfigurable) c ).setFluid(e.getKey(), e.getValue());
                }
            }
            return null;
        }

    }
}
