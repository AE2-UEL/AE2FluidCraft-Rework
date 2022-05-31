package com.glodblock.github.client.gui.container;

import appeng.tile.inventory.AppEngInternalAEInventory;
import com.glodblock.github.common.parts.PartSharedFluidBus;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerFluidIO extends ContainerFluidConfigurable
{
    private final PartSharedFluidBus bus;

    public ContainerFluidIO(InventoryPlayer ip, PartSharedFluidBus te )
    {
        super( ip, te );
        this.bus = te;
    }

    public PartSharedFluidBus getBus() {
        return this.bus;
    }

    @Override
    public AppEngInternalAEInventory getFakeFluidInv() {
        return (AppEngInternalAEInventory) this.bus.getInventoryByName("config");
    }
}
