package com.glodblock.github.client.container;

import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotNormal;
import com.glodblock.github.common.tile.TileFluidPacketDecoder;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerFluidPacketDecoder extends AEBaseContainer {

    public ContainerFluidPacketDecoder(InventoryPlayer ipl, TileFluidPacketDecoder tile) {
        super(ipl, tile);
        addSlotToContainer(new SlotNormal(tile.getInventory(), 0, 80, 35));
        bindPlayerInventory(ipl, 0, 84);
    }

}