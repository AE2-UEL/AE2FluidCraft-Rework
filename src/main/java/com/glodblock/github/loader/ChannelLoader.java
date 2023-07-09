package com.glodblock.github.loader;

import com.glodblock.github.network.NetworkManager;
import com.glodblock.github.network.packets.CPacketDumpTank;
import com.glodblock.github.network.packets.CPacketFluidCraftBtns;
import com.glodblock.github.network.packets.CPacketLoadPattern;
import com.glodblock.github.network.packets.IMessage;
import com.glodblock.github.util.ModAndClassUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ChannelLoader {

    static int id = 0;

    public static void load() {
        registerPacket(new CPacketDumpTank());
        registerPacket(new CPacketFluidCraftBtns());
        if (ModAndClassUtil.JEI || ModAndClassUtil.REI) {
            registerPacket(new CPacketLoadPattern());
        }
    }

    private static <MSG extends IMessage<MSG>> void registerPacket(MSG msg) {
        NetworkManager.netHandler.registerMessage(
                id ++,
                msg.getPacketClass(),
                ChannelLoader::encoder,
                msg::fromBytes,
                ChannelLoader::handle
                );
    }

    public static <MSG extends IMessage<MSG>> void encoder(MSG msg, PacketBuffer p) {
        msg.toBytes(p);
    }

    public static <MSG extends IMessage<MSG>> void handle(MSG msg, Supplier<NetworkEvent.Context> ctx) {
        msg.onMessage(ctx);
    }

}
