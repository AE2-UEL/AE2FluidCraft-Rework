package com.glodblock.github.network;

import com.glodblock.github.client.GuiItemAmountChange;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class SPacketSetItemAmount implements IMessage {

    private int amount;

    public SPacketSetItemAmount() {
        //NO-OP
    }

    public SPacketSetItemAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        amount = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(amount);
    }

    public static class Handler implements IMessageHandler<SPacketSetItemAmount, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(SPacketSetItemAmount message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                final GuiScreen gs = Minecraft.getMinecraft().currentScreen;
                if (gs instanceof GuiItemAmountChange) {
                    ((GuiItemAmountChange) gs).setAmount(message.amount);
                }
            });
            return null;
        }
    }

}
