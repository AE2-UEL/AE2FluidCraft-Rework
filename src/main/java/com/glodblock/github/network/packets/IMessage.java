package com.glodblock.github.network.packets;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public interface IMessage<MSG> {

    void toBytes(PacketBuffer buf);

    MSG fromBytes(PacketBuffer buf);

    void onMessage(Supplier<NetworkEvent.Context> ctx);

    Class<MSG> getPacketClass();

}
