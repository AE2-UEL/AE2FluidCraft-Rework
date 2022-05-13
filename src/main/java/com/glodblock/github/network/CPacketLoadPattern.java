package com.glodblock.github.network;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import com.glodblock.github.interfaces.PatternConsumer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.io.IOException;

public class CPacketLoadPattern implements IMessage {

    private IAEItemStack[] crafting, output;

    public CPacketLoadPattern(IAEItemStack[] crafting, IAEItemStack[] output) {
        this.crafting = crafting;
        this.output = output;
    }

    public CPacketLoadPattern() {
        // NO-OP
    }

    @Override
    public void toBytes(ByteBuf buf) {
        writeStacksWithNulls(buf, crafting);
        writeStacksWithNulls(buf, output);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        crafting = readStacksWithNulls(buf);
        output = readStacksWithNulls(buf);
    }

    private static IAEItemStack[] readStacksWithNulls(ByteBuf buf) {
        IAEItemStack[] stacks = new IAEItemStack[buf.readByte()];
        int mask = buf.readInt();
        for (int i = 0; i < stacks.length; i++) {
            if ((mask & (1 << i)) != 0) {
                stacks[i] = AEItemStack.fromPacket(buf);
            }
        }
        return stacks;
    }

    private static void writeStacksWithNulls(ByteBuf buf, IAEItemStack[] stacks) {
        buf.writeByte(stacks.length);
        int mask = 0;
        for (int i = 0; i < stacks.length; i++) {
            if (stacks[i] != null) {
                mask |= 1 << i;
            }
        }
        buf.writeInt(mask);
        for (IAEItemStack stack : stacks) {
            try {
                if (stack != null) {
                    stack.writeToPacket(buf);
                }
            } catch (IOException e) {
                throw new IllegalStateException("Failed to write AE item stack!", e);
            }
        }
    }

    public static class Handler implements IMessageHandler<CPacketLoadPattern, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(CPacketLoadPattern message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (player.openContainer instanceof PatternConsumer) {
                    ((PatternConsumer)player.openContainer).acceptPattern(message.crafting, message.output);
                }
            });
            return null;
        }

    }

}
