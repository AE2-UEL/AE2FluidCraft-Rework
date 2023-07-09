package com.glodblock.github.network.packets;

import com.glodblock.github.interfaces.PatternConsumer;
import com.glodblock.github.util.FCUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class CPacketLoadPattern implements IMessage<CPacketLoadPattern> {

    private ItemStack[] output;
    private Int2ObjectMap<ItemStack[]> crafting;
    private boolean compress;
    private static final int SLOT_SIZE = 16;

    public CPacketLoadPattern(Int2ObjectMap<ItemStack[]> crafting, ItemStack[] output, boolean compress) {
        this.crafting = crafting;
        this.output = output;
        this.compress = compress;
    }

    public CPacketLoadPattern() {
        // NO-OP
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeBoolean(compress);
        CompoundNBT msg = new CompoundNBT();
        for (int index : crafting.keySet()) {
            writeItemArray(msg, crafting.get(index), index + "#");
        }
        writeItemArray(msg, output, "o");
        FCUtil.writeNBTToBytes(buf, msg);
    }

    @Override
    public CPacketLoadPattern fromBytes(PacketBuffer buf) {
        CPacketLoadPattern dup = new CPacketLoadPattern();
        dup.crafting = new Int2ObjectArrayMap<>();
        dup.compress = buf.readBoolean();
        CompoundNBT msg = FCUtil.readNBTFromBytes(buf);
        for (int i = 0; i < SLOT_SIZE; i ++) {
            if (msg.contains(i + "#")) {
                dup.crafting.put(i, readItemArray(msg, i + "#"));
            }
        }
        dup.output = readItemArray(msg, "o");
        return dup;
    }

    @Override
    public void onMessage(Supplier<NetworkEvent.Context> ctx) {
        ServerPlayerEntity player = ctx.get().getSender();
        if (player != null) {
            ctx.get().enqueueWork(() -> {
                if (player.openContainer instanceof PatternConsumer) {
                    ((PatternConsumer) player.openContainer).acceptPattern(this.crafting, this.output, this.compress);
                }
            });
        }
    }

    private void writeItemArray(CompoundNBT nbt, ItemStack[] itemList, String key) {
        CompoundNBT dict = new CompoundNBT();
        dict.putShort("l", (short) (itemList == null ? 0 : itemList.length));
        if (itemList != null) {
            int cnt = 0;
            for (ItemStack item : itemList) {
                CompoundNBT itemTag = new CompoundNBT();
                if (item != null) {
                    item.write(itemTag);
                    dict.put(cnt + "#", itemTag);
                    cnt ++;
                }
            }
            dict.putShort("l", (short) cnt);
        }
        nbt.put(key, dict);
    }

    private ItemStack[] readItemArray(CompoundNBT nbt, String key) {
        CompoundNBT dict = nbt.getCompound(key);
        short len = dict.getShort("l");
        if (len == 0) {
            return new ItemStack[0];
        } else {
            ItemStack[] itemList = new ItemStack[len];
            for (int i = 0; i < len; i ++) {
                itemList[i] = ItemStack.read(dict.getCompound(i + "#"));
            }
            return itemList;
        }
    }

    @Override
    public Class<CPacketLoadPattern> getPacketClass() {
        return CPacketLoadPattern.class;
    }
}
