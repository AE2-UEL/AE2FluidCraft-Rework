package com.glodblock.github.network;

import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class CPacketSwitchGuis implements IMessage {

    private GuiType guiType;

    public CPacketSwitchGuis(GuiType guiType) {
        this.guiType = guiType;
    }

    public CPacketSwitchGuis() {
        // NO-OP
    }

    @Override
    public void fromBytes(ByteBuf byteBuf) {
        guiType = GuiType.getByOrdinal(byteBuf.readByte());
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        byteBuf.writeByte(guiType != null ? guiType.ordinal() : 0);
    }

    public static class Handler implements IMessageHandler<CPacketSwitchGuis, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(CPacketSwitchGuis message, MessageContext ctx) {
            if (message.guiType == null) {
                return null;
            }
            EntityPlayerMP player = ctx.getServerHandler().player;
            Container cont = player.openContainer;
            if (!(cont instanceof AEBaseContainer)) {
                return null;
            }
            ContainerOpenContext context = ((AEBaseContainer)cont).getOpenContext();
            if (context == null) {
                return null;
            }
            TileEntity te = context.getTile();
            if (te == null) {
                return null;
            }
            player.getServerWorld().addScheduledTask(
                    () -> InventoryHandler.openGui(player, player.world, te.getPos(), context.getSide().getFacing(), message.guiType)
            );
            return null;
        }

    }
}