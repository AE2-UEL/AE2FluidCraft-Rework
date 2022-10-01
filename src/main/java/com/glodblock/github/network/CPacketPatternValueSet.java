package com.glodblock.github.network;

import appeng.api.networking.IGridHost;
import appeng.container.ContainerOpenContext;
import com.glodblock.github.client.container.ContainerExtendedFluidPatternTerminal;
import com.glodblock.github.client.container.ContainerFluidPatternTerminal;
import com.glodblock.github.client.container.ContainerItemAmountChange;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CPacketPatternValueSet implements IMessage {

    private GuiType originGui;
    private int amount;
    private int valueIndex;

    public CPacketPatternValueSet() {
        //NO-OP
    }

    public CPacketPatternValueSet( GuiType originalGui, int amount, int valueIndex ){
        this.originGui = originalGui;
        this.amount = amount;
        this.valueIndex = valueIndex;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(originGui.ordinal());
        buf.writeInt(amount);
        buf.writeInt(valueIndex);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.originGui = GuiType.getByOrdinal(buf.readInt());
        this.amount = buf.readInt();
        this.valueIndex = buf.readInt();
    }

    public static class Handler implements IMessageHandler<CPacketPatternValueSet, IMessage> {

        @Override
        public IMessage onMessage(CPacketPatternValueSet message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().player;
            if (player.openContainer instanceof ContainerItemAmountChange) {
                ContainerItemAmountChange cpv = (ContainerItemAmountChange) player.openContainer;
                final Object target = cpv.getTarget();
                if (target instanceof IGridHost) {
                    final ContainerOpenContext context = cpv.getOpenContext();
                    if (context != null) {
                        final TileEntity te = context.getTile();
                        InventoryHandler.openGui(player, player.world, te.getPos(), context.getSide().getFacing(), message.originGui);
                        if (player.openContainer instanceof ContainerFluidPatternTerminal || player.openContainer instanceof ContainerExtendedFluidPatternTerminal) {
                            Slot slot = player.openContainer.getSlot(message.valueIndex);
                            if (slot.getHasStack()) {
                                ItemStack stack = slot.getStack().copy();
                                if (stack.getItem() instanceof ItemFluidPacket) {
                                    FluidStack fluidStack = ItemFluidPacket.getFluidStack(stack);
                                    if (fluidStack != null) {
                                        fluidStack.amount = message.amount;
                                    }
                                    slot.putStack(ItemFluidPacket.newStack(fluidStack));
                                } else {
                                    stack.setCount(message.amount);
                                    slot.putStack(stack);
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }

    }
}
