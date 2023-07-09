package com.glodblock.github.client.container;

import appeng.container.AEBaseContainer;
import appeng.container.SlotSemantic;
import appeng.container.implementations.ContainerTypeBuilder;
import appeng.container.slot.AppEngSlot;
import com.glodblock.github.common.tile.TileFluidPacketDecoder;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;

public class ContainerFluidPacketDecoder extends AEBaseContainer {

    public static final ContainerType<ContainerFluidPacketDecoder> TYPE = ContainerTypeBuilder
            .create(ContainerFluidPacketDecoder::new, TileFluidPacketDecoder.class)
            .build("fluid_packet_decoder");

    public ContainerFluidPacketDecoder(int id, PlayerInventory ip, TileFluidPacketDecoder tile) {
        super(TYPE, id, ip, tile);
        addSlot(new AppEngSlot(tile.getInventory(), 0), SlotSemantic.STORAGE);
        createPlayerInventorySlots(ip);
    }

}
