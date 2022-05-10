package com.glodblock.github.network;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import com.glodblock.github.client.gui.GuiFCBaseMonitor;
import com.glodblock.github.client.gui.GuiFluidCraftConfirm;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.util.LinkedList;
import java.util.List;

public class SPacketMEInventoryUpdate implements IMessage {

    private List<IAEItemStack> list;
    private byte ref;

    public SPacketMEInventoryUpdate() {
        ref = 0;
        list = new LinkedList<>();
    }

    public SPacketMEInventoryUpdate(byte b) {
        this.ref = b;
        list = new LinkedList<>();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        long amount = buf.readLong();
        ref = buf.readByte();
        list = new LinkedList<>();
        try {
            for (int i = 0; i < amount; i ++) {
                list.add(AEItemStack.loadItemStackFromPacket(buf));
            }
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(list.size());
        buf.writeByte(ref);
        try {
            for (IAEItemStack itemStack : list) {
                itemStack.writeToPacket(buf);
            }
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public void appendItem( final IAEItemStack is ) throws BufferOverflowException {
        list.add(is);
    }

    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    public static class Handler implements IMessageHandler<SPacketMEInventoryUpdate, IMessage> {

        @Override
        public IMessage onMessage(SPacketMEInventoryUpdate message, MessageContext ctx) {
            final GuiScreen gs = Minecraft.getMinecraft().currentScreen;
            if( gs instanceof GuiFCBaseMonitor)
            {
                ( (GuiFCBaseMonitor) gs ).postUpdate(message.list);
            }
            else if ( gs instanceof GuiFluidCraftConfirm) {
                ( (GuiFluidCraftConfirm) gs ).postUpdate( message.list, message.ref );
            }
            return null;
        }
    }
}
