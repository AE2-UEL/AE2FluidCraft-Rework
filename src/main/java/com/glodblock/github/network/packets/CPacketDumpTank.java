package com.glodblock.github.network.packets;

import com.glodblock.github.interfaces.TankDumpable;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class CPacketDumpTank implements IMessage<CPacketDumpTank> {

    private int index;

    public CPacketDumpTank(int index) {
        this.index = index;
    }

    public CPacketDumpTank() {
        // NO-OP
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeShort(index);
    }

    @Override
    public CPacketDumpTank fromBytes(PacketBuffer buf) {
        CPacketDumpTank dup = new CPacketDumpTank();
        dup.index = buf.readShort();
        return dup;
    }

    @Override
    public void onMessage(Supplier<NetworkEvent.Context> ctx) {
        ServerPlayerEntity player = ctx.get().getSender();
        if (player != null) {
            ctx.get().enqueueWork(() -> {
                if (player.openContainer instanceof TankDumpable) {
                    ((TankDumpable)player.openContainer).dumpTank(this.index);
                }
            });
        }
    }

    @Override
    public Class<CPacketDumpTank> getPacketClass() {
        return CPacketDumpTank.class;
    }

}