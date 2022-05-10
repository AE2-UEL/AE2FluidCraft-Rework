package com.glodblock.github.network;

import com.glodblock.github.client.gui.container.ContainerFluidPatternTerminal;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.nei.object.OrderStack;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

public class CPacketTransferRecipe implements IMessage {

    private List<OrderStack<?>> inputs;
    private List<OrderStack<?>> outputs;
    private boolean isCraft;
    private final static int MAX_INDEX = 16;

    public CPacketTransferRecipe(){
    }

    public CPacketTransferRecipe(List<OrderStack<?>> IN, List<OrderStack<?>> OUT, boolean craft) {
        this.inputs = IN;
        this.outputs = OUT;
        this.isCraft = craft;
    }

    //I should use GZIP to compress the message, but i'm too lazy.
    //NBT to ByteBuf has a compress stream
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(isCraft);
        NBTTagCompound nbt_m = new NBTTagCompound();
        NBTTagCompound nbt_i = new NBTTagCompound();
        NBTTagCompound nbt_o = new NBTTagCompound();
        for (OrderStack<?> stack : inputs) {
            stack.writeToNBT(nbt_i);
        }
        for (OrderStack<?> stack : outputs) {
            stack.writeToNBT(nbt_o);
        }
        nbt_m.setTag("i", nbt_i);
        nbt_m.setTag("o", nbt_o);
        ByteBufUtils.writeTag(buf, nbt_m);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        isCraft = buf.readBoolean();
        inputs = new LinkedList<>();
        outputs = new LinkedList<>();
        NBTTagCompound nbt_m = ByteBufUtils.readTag(buf);
        NBTTagCompound nbt_i = nbt_m.getCompoundTag("i");
        NBTTagCompound nbt_o = nbt_m.getCompoundTag("o");
        for (int i = 0; i < MAX_INDEX; i ++) {
            OrderStack<?> tmp = OrderStack.readFromNBT(nbt_i, null, i);
            if (tmp != null) inputs.add(tmp);
        }
        for (int i = 0; i < MAX_INDEX; i ++) {
            OrderStack<?> tmp = OrderStack.readFromNBT(nbt_o, null, i);
            if (tmp != null) outputs.add(tmp);
        }
    }

    public static class Handler implements IMessageHandler<CPacketTransferRecipe, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(CPacketTransferRecipe message, MessageContext ctx) {
            Container c = ctx.getServerHandler().playerEntity.openContainer;
            if (c instanceof ContainerFluidPatternTerminal) {
                ContainerFluidPatternTerminal cf = (ContainerFluidPatternTerminal) c;
                cf.getPatternTerminal().setCraftingRecipe(message.isCraft);
                IInventory inputSlot = cf.getInventoryByName("crafting");
                IInventory outputSlot = cf.getInventoryByName("output");
                for (int i = 0; i < inputSlot.getSizeInventory(); i ++) {
                    inputSlot.setInventorySlotContents(i, null);
                }
                for (int i = 0; i < outputSlot.getSizeInventory(); i ++) {
                    outputSlot.setInventorySlotContents(i, null);
                }
                for (OrderStack<?> stack : message.inputs) {
                    if (stack != null) {
                        int index = stack.getIndex();
                        ItemStack stack1;
                        if (stack.getStack() instanceof ItemStack) {
                            stack1 = ((ItemStack) stack.getStack()).copy();
                        }
                        else if (stack.getStack() instanceof FluidStack) {
                            stack1 = ItemFluidPacket.newStack((FluidStack) stack.getStack());
                        }
                        else throw new UnsupportedOperationException("Trying to get an unsupported item!");
                        if (index < inputSlot.getSizeInventory())
                            inputSlot.setInventorySlotContents(index, stack1);
                    }
                }
                for (OrderStack<?> stack : message.outputs) {
                    if (stack != null) {
                        int index = stack.getIndex();
                        ItemStack stack1;
                        if (stack.getStack() instanceof ItemStack) {
                            stack1 = ((ItemStack) stack.getStack()).copy();
                        }
                        else if (stack.getStack() instanceof FluidStack) {
                            stack1 = ItemFluidPacket.newStack((FluidStack) stack.getStack());
                        }
                        else throw new UnsupportedOperationException("Trying to get an unsupported item!");
                        if (index < outputSlot.getSizeInventory())
                            outputSlot.setInventorySlotContents(index, stack1);
                    }
                }
                c.onCraftMatrixChanged(inputSlot);
                c.onCraftMatrixChanged(outputSlot);
            }
            return null;
        }
    }

}
