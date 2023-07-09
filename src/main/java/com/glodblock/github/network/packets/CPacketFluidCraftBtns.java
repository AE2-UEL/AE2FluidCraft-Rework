package com.glodblock.github.network.packets;

import com.glodblock.github.interfaces.ConfigData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class CPacketFluidCraftBtns implements IMessage<CPacketFluidCraftBtns> {

    private String id;
    private int value;
    private OType type;

    public CPacketFluidCraftBtns(String id) {
        this.id = id;
        this.value = -1;
        this.type = OType.VOID;
    }

    public CPacketFluidCraftBtns(String id, boolean value) {
        this.id = id;
        this.value = value ? 1 : 0;
        this.type = OType.BOOLEAN;
    }

    public CPacketFluidCraftBtns(String id, int value) {
        this.id = id;
        this.value = value;
        this.type = OType.INT;
    }

    public CPacketFluidCraftBtns() {
        // NO-OP
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeString(id);
        buf.writeVarInt(value);
        buf.writeByte(type.ordinal());
    }

    @Override
    public CPacketFluidCraftBtns fromBytes(PacketBuffer buf) {
        CPacketFluidCraftBtns dup = new CPacketFluidCraftBtns();
        dup.id = buf.readString(32767);
        dup.value = buf.readVarInt();
        dup.type = OType.values()[buf.readByte()];
        return dup;
    }

    @Override
    public void onMessage(Supplier<NetworkEvent.Context> ctx) {
        ServerPlayerEntity player = ctx.get().getSender();
        if (player != null) {
            ctx.get().enqueueWork(() -> {
               if (player.openContainer instanceof ConfigData) {
                   switch (type) {
                       case INT:
                           ((ConfigData) player.openContainer).set(id, value);
                           break;
                       case BOOLEAN:
                           ((ConfigData) player.openContainer).set(id, value == 1);
                           break;
                       case VOID:
                           ((ConfigData) player.openContainer).set(id, null);
                   }
               }
            });
        }
    }

    @Override
    public Class<CPacketFluidCraftBtns> getPacketClass() {
        return CPacketFluidCraftBtns.class;
    }

    enum OType {
        INT,
        BOOLEAN,
        VOID
    }

}
