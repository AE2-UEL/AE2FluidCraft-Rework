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
    @GuiSync(96)
    public boolean allowSplitting = true;
    @GuiSync(97)
    public int blockModeEx = 0;
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
            allowSplitting = Ae2Reflect.getSplittingMode(dualityInterfaceCopy);
            blockModeEx = Ae2Reflect.getExtendedBlockMode(dualityInterfaceCopy);
        }
    }

    public void setFluidPacketInTile(boolean value) {
        this.fluidPacket = value;
        Ae2Reflect.setFluidPacketMode(dualityInterfaceCopy, value);
    }

    public void setAllowSplittingInTile(boolean value) {
        this.allowSplitting = value;
        Ae2Reflect.setSplittingMode(dualityInterfaceCopy, value);
    }

    public void setExtendedBlockMode(int value) {
        this.blockModeEx = value;
        Ae2Reflect.setExtendedBlockMode(dualityInterfaceCopy, value);
    }

}
