package com.glodblock.github.network;

import com.glodblock.github.interfaces.PatternConsumer;
import com.glodblock.github.util.Util;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.util.HashMap;

public class CPacketLoadPattern implements IMessage {

    private ItemStack[] output;
    private HashMap<Integer, ItemStack[]> crafting;
    private boolean compress;
    private static final int SLOT_SIZE = 16;

    public CPacketLoadPattern(HashMap<Integer, ItemStack[]> crafting, ItemStack[] output, boolean compress) {
        this.crafting = crafting;
        this.output = output;
        this.compress = compress;
    }

    public CPacketLoadPattern() {
        // NO-OP
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(compress);
        NBTTagCompound msg = new NBTTagCompound();
        for (int index : crafting.keySet()) {
            writeItemArray(msg, crafting.get(index), index + "#");
        }
        writeItemArray(msg, output, "o");
        Util.writeNBTToBytes(buf, msg);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        crafting = new HashMap<>();
        compress = buf.readBoolean();
        NBTTagCompound msg = Util.readNBTFromBytes(buf);
        for (int i = 0; i < SLOT_SIZE; i ++) {
            if (msg.hasKey(i + "#")) {
                crafting.put(i, readItemArray(msg, i + "#"));
            }
        }
        output = readItemArray(msg, "o");
    }

    private void writeItemArray(NBTTagCompound nbt, ItemStack[] itemList, String key) {
        NBTTagCompound dict = new NBTTagCompound();
        dict.setShort("l", (short) (itemList == null ? 0 : itemList.length));
        if (itemList != null) {
            int cnt = 0;
            for (ItemStack item : itemList) {
                NBTTagCompound itemTag = new NBTTagCompound();
                if (item != null) {
                    item.writeToNBT(itemTag);
                    dict.setTag(cnt + "#", itemTag);
                    cnt ++;
                }
            }
            dict.setShort("l", (short) cnt);
        }
        nbt.setTag(key, dict);
    }

    private ItemStack[] readItemArray(NBTTagCompound nbt, String key) {
        NBTTagCompound dict = nbt.getCompoundTag(key);
        short len = dict.getShort("l");
        if (len == 0) {
            return new ItemStack[0];
        } else {
            ItemStack[] itemList = new ItemStack[len];
            for (int i = 0; i < len; i ++) {
                itemList[i] = new ItemStack(dict.getCompoundTag(i + "#"));
            }
            return itemList;
        }
    }

    public static class Handler implements IMessageHandler<CPacketLoadPattern, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(CPacketLoadPattern message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (player.openContainer instanceof PatternConsumer) {
                    ((PatternConsumer) player.openContainer).acceptPattern(message.crafting, message.output, message.compress);
                }
            });
            return null;
        }

    }

}
