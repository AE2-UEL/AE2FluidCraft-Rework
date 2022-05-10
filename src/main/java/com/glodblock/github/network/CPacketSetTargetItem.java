package com.glodblock.github.network;

import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.Container;

import java.io.IOException;

public class CPacketSetTargetItem implements IMessage {

    private IAEItemStack stack;

    public CPacketSetTargetItem() {
    }

    public CPacketSetTargetItem(IAEItemStack stack) {
        this.stack = stack;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            stack.writeToPacket(buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            stack = AEItemStack.loadItemStackFromPacket(buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class Handler implements IMessageHandler<CPacketSetTargetItem, IMessage> {

        @Override
        public IMessage onMessage(CPacketSetTargetItem message, MessageContext ctx) {
            Container c = ctx.getServerHandler().playerEntity.openContainer;
            if (c instanceof AEBaseContainer) {
                ((AEBaseContainer) c).setTargetStack(message.stack);
            }
            return null;
        }

    }

}
