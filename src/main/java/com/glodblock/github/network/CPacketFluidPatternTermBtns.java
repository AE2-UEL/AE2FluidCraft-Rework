package com.glodblock.github.network;

import com.glodblock.github.client.container.ContainerExtendedFluidPatternTerminal;
import com.glodblock.github.client.container.ContainerFluidPatternTerminal;
import com.glodblock.github.common.part.PartExtendedFluidPatternTerminal;
import com.glodblock.github.common.part.PartFluidPatternTerminal;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CPacketFluidPatternTermBtns implements IMessage {

    private String Name = "";
    private String Value = "";

    public CPacketFluidPatternTermBtns( final String name, final String value ) {
        Name = name;
        Value = value;
    }

    public CPacketFluidPatternTermBtns() {
        // NO-OP
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int leName = buf.readInt();
        int leVal = buf.readInt();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < leName; i ++) {
            sb.append(buf.readChar());
        }
        Name = sb.toString();
        sb = new StringBuilder();
        for (int i = 0; i < leVal; i ++) {
            sb.append(buf.readChar());
        }
        Value = sb.toString();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(Name.length());
        buf.writeInt(Value.length());
        for (int i = 0; i < Name.length(); i ++) {
            buf.writeChar(Name.charAt(i));
        }
        for (int i = 0; i < Value.length(); i ++) {
            buf.writeChar(Value.charAt(i));
        }
    }

    public static class Handler implements IMessageHandler<CPacketFluidPatternTermBtns, IMessage> {

        @Override
        public IMessage onMessage(CPacketFluidPatternTermBtns message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            String Name = message.Name;
            String Value = message.Value;
            final Container c = player.openContainer;
            if (c instanceof ContainerFluidPatternTerminal) {
                final ContainerFluidPatternTerminal cpt = (ContainerFluidPatternTerminal) c;
                switch (Name) {
                    case "PatternTerminal.Combine":
                        ((PartFluidPatternTerminal) cpt.getPatternTerminal()).setCombineMode(Value.equals("1"));
                        break;
                }
            } else if (c instanceof ContainerExtendedFluidPatternTerminal) {
                final ContainerExtendedFluidPatternTerminal cpt = (ContainerExtendedFluidPatternTerminal) c;
                switch (Name) {
                    case "PatternTerminal.Combine":
                        ((PartExtendedFluidPatternTerminal) cpt.getExpandedPatternTerminal()).setCombineMode(Value.equals("1"));
                        break;
                }
            }
            return null;
        }
    }

}
