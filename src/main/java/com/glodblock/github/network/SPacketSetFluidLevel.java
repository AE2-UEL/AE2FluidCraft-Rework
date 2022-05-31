package com.glodblock.github.network;

import com.glodblock.github.client.GuiFluidLevelMaintainer;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class SPacketSetFluidLevel implements IMessage {

    private int index;
    private int size;

    public SPacketSetFluidLevel() {
        //NO-OP
    }

    public SPacketSetFluidLevel(int id, int value) {
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

    public static class Handler implements IMessageHandler<SPacketSetFluidLevel, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(SPacketSetFluidLevel message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                final GuiScreen gs = Minecraft.getMinecraft().currentScreen;
                if (gs instanceof GuiFluidLevelMaintainer) {
                    ((GuiFluidLevelMaintainer) gs).setMaintainNumber(message.index, message.size);
                }
            });
            return null;
        }

    }

}
