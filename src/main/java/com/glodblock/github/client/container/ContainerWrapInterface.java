package com.glodblock.github.client.container;

import appeng.container.guisync.GuiSync;
import appeng.container.implementations.ContainerInterface;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.util.Platform;
import com.glodblock.github.util.Ae2Reflect;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerWrapInterface extends ContainerInterface {

    @GuiSync(95)
    public boolean fluidPacket = false;
    private final DualityInterface dualityInterfaceCopy;

    public ContainerWrapInterface(InventoryPlayer ip, IInterfaceHost te) {
        super(ip, te);
        this.dualityInterfaceCopy = te.getInterfaceDuality();
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (Platform.isServer()) {
            fluidPacket = Ae2Reflect.getFluidPacketMode(dualityInterfaceCopy);
        }
    }

    public void setFluidPacketInTile(boolean value) {
        this.fluidPacket = value;
        Ae2Reflect.setFluidPacketMode(dualityInterfaceCopy, value);
    }

}
