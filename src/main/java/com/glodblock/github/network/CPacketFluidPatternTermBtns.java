package com.glodblock.github.network;

import com.glodblock.github.client.gui.container.ContainerFluidCraftConfirm;
import com.glodblock.github.client.gui.container.ContainerFluidPatternTerminal;
import com.glodblock.github.client.gui.container.ContainerFluidPatternTerminalEx;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.Container;

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
            String Name = message.Name;
            String Value = message.Value;
            final Container c = ctx.getServerHandler().playerEntity.openContainer;
            if( Name.startsWith( "PatternTerminal." ) && (c instanceof ContainerFluidPatternTerminal))
            {
                final ContainerFluidPatternTerminal cpt = (ContainerFluidPatternTerminal) c;
                switch (Name) {
                    case "PatternTerminal.CraftMode":
                        cpt.getPatternTerminal().setCraftingRecipe(Value.equals("1"));
                        break;
                    case "PatternTerminal.Encode":
                        if (Value.equals("2"))
                            cpt.encodeAndMoveToInventory();
                        else
                            cpt.encode();
                        break;
                    case "PatternTerminal.Clear":
                        cpt.clear();
                        break;
                    case "PatternTerminal.Substitute":
                        cpt.getPatternTerminal().setSubstitution(Value.equals("1"));
                        break;
                    case "PatternTerminal.Double":
                        cpt.doubleStacks(Value.equals("1"));
                        break;
                    case "PatternTerminal.Combine":
                        cpt.getPatternTerminal().setCombineMode(Value.equals("1"));
                        break;
                }
            } else if( Name.startsWith( "PatternTerminalEx." ) && (c instanceof ContainerFluidPatternTerminalEx))
            {
                final ContainerFluidPatternTerminalEx cpt = (ContainerFluidPatternTerminalEx) c;
                switch (Name) {
                    case "PatternTerminalEx.Encode":
                        if (Value.equals("2"))
                            cpt.encodeAndMoveToInventory();
                        else
                            cpt.encode();
                        break;
                    case "PatternTerminalEx.Clear":
                        cpt.clear();
                        break;
                    case "PatternTerminalEx.Substitute":
                        cpt.getPatternTerminal().setSubstitution(Value.equals("1"));
                        break;
                    case "PatternTerminalEx.Invert":
                        cpt.getPatternTerminal().setInverted(Value.equals("1"));
                        break;
                    case "PatternTerminalEx.Double":
                        cpt.doubleStacks(Value.equals("1"));
                        break;
                    case "PatternTerminalEx.Combine":
                        cpt.getPatternTerminal().setCombineMode(Value.equals("1"));
                        break;
                    case "PatternTerminalEx.ActivePage":
                        cpt.getPatternTerminal().setActivePage(Integer.parseInt(Value));
                        break;
                }
            } else if(Name.equals( "Terminal.Cpu" ) && c instanceof ContainerFluidCraftConfirm) {
                final ContainerFluidCraftConfirm qk = (ContainerFluidCraftConfirm) c;
                qk.cycleCpu( Value.equals( "Next" ) );
            }
            else if( Name.equals( "Terminal.Start" ) && c instanceof ContainerFluidCraftConfirm ) {
                final ContainerFluidCraftConfirm qk = (ContainerFluidCraftConfirm) c;
                qk.startJob();
            }
            return null;
        }

    }
}
