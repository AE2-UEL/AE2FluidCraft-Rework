package com.glodblock.github.network;

import appeng.api.storage.data.IAEFluidStack;
import appeng.util.item.AEFluidStack;
import com.glodblock.github.client.gui.GuiFluidIO;
import com.glodblock.github.client.gui.GuiFluidInterface;
import com.glodblock.github.client.gui.GuiIngredientBuffer;
import com.glodblock.github.client.gui.GuiLargeIngredientBuffer;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SPacketFluidUpdate implements IMessage {

    private Map<Integer, IAEFluidStack> list;

    public SPacketFluidUpdate() {
    }

    public SPacketFluidUpdate(Map<Integer, IAEFluidStack> data) {
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

    public static class Handler implements IMessageHandler<SPacketFluidUpdate, IMessage> {

        @Override
        public IMessage onMessage(SPacketFluidUpdate message, MessageContext ctx) {
            final GuiScreen gs = Minecraft.getMinecraft().currentScreen;
            if( gs instanceof GuiIngredientBuffer)
            {
                for (Map.Entry<Integer, IAEFluidStack> e : message.list.entrySet() ) {
                    ( (GuiIngredientBuffer) gs ).update(e.getKey(), e.getValue());
                }
            } else if (gs instanceof GuiLargeIngredientBuffer) {
                for (Map.Entry<Integer, IAEFluidStack> e : message.list.entrySet() ) {
                    ( (GuiLargeIngredientBuffer) gs ).update(e.getKey(), e.getValue());
                }
            } else if( gs instanceof GuiFluidIO)
            {
                for (Map.Entry<Integer, IAEFluidStack> e : message.list.entrySet() ) {
                    ( (GuiFluidIO) gs ).update(e.getKey(), e.getValue());
                }
            } else if (gs instanceof GuiFluidInterface) {
                for (Map.Entry<Integer, IAEFluidStack> e : message.list.entrySet() ) {
                    ( (GuiFluidInterface) gs ).update(e.getKey(), e.getValue());
                }
            }
            return null;
        }

    }
}
