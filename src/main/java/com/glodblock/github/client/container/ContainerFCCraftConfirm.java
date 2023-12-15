package com.glodblock.github.client.container;

import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.ITerminalHost;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.me.helpers.PlayerSource;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerFCCraftConfirm extends ContainerCraftConfirm {

    public ContainerFCCraftConfirm(InventoryPlayer ip, ITerminalHost te) {
        super(ip, te);
    }

    public IActionSource getActionSrc() {
        return new PlayerSource(this.getPlayerInv().player, (IActionHost)this.getTarget());
    }

    @Override
    public IActionHost getActionHost() {
        return super.getActionHost();
    }

}
