package com.glodblock.github.network;

import appeng.container.ContainerOpenContext;
import appeng.container.slot.SlotFake;
import com.glodblock.github.client.container.ContainerExtendedFluidPatternTerminal;
import com.glodblock.github.client.container.ContainerFluidPatternTerminal;
import com.glodblock.github.client.container.ContainerItemAmountChange;
import com.glodblock.github.client.container.ContainerUltimateEncoder;
import com.glodblock.github.client.container.ContainerWirelessFluidPatternTerminal;
import com.glodblock.github.common.item.fake.FakeFluids;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.integration.mek.FCGasItems;
import com.glodblock.github.integration.mek.FakeGases;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.Ae2Reflect;
import com.glodblock.github.util.ModAndClassUtil;
import io.netty.buffer.ByteBuf;
import mekanism.api.gas.GasStack;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
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
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (player.openContainer instanceof ContainerItemAmountChange) {
                    ContainerItemAmountChange cpv = (ContainerItemAmountChange) player.openContainer;
                    final ContainerOpenContext context = cpv.getOpenContext();
                    if (context != null) {
                        InventoryHandler.openGui(
                                player,
                                player.world,
                                new BlockPos(Ae2Reflect.getContextX(context), Ae2Reflect.getContextY(context), Ae2Reflect.getContextZ(context)),
                                context.getSide().getFacing(),
                                message.originGui
                        );
                        if (player.openContainer instanceof ContainerFluidPatternTerminal
                                || player.openContainer instanceof ContainerExtendedFluidPatternTerminal
                                || player.openContainer instanceof ContainerUltimateEncoder
                                || player.openContainer instanceof ContainerWirelessFluidPatternTerminal) {
                            Slot slot = player.openContainer.getSlot(message.valueIndex);
                            if (slot instanceof SlotFake) {
                                if (slot.getHasStack()) {
                                    ItemStack stack = slot.getStack().copy();
                                    if (stack.getItem() == FCItems.FLUID_PACKET) {
                                        FluidStack fluidStack = FakeItemRegister.getStack(stack);
                                        if (fluidStack != null) {
                                            fluidStack.amount = message.amount;
                                        }
                                        slot.putStack(FakeFluids.packFluid2Packet(fluidStack));
                                    } else if (ModAndClassUtil.GAS && stack.getItem() == FCGasItems.GAS_PACKET) {
                                        GasStack gasStack = FakeItemRegister.getStack(stack);
                                        if (gasStack != null) {
                                            gasStack.amount = message.amount;
                                        }
                                        slot.putStack(FakeGases.packGas2Packet(gasStack));
                                    } else {
                                        stack.setCount(message.amount);
                                        slot.putStack(stack);
                                    }
                                }
                            }
                        }
                    }
                }
            });
            return null;
        }

    }
}
