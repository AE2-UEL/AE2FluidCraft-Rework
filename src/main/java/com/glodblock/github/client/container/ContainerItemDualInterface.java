package com.glodblock.github.client.container;

import appeng.container.guisync.GuiSync;
import appeng.container.implementations.ContainerInterface;
import appeng.container.slot.SlotRestrictedInput;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.util.Platform;
import com.glodblock.github.util.Ae2Reflect;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.IItemHandler;

public class ContainerItemDualInterface extends ContainerInterface {

    @GuiSync(95)
    public boolean fluidPacket = false;
    @GuiSync(96)
    public boolean allowSplitting = true;
    @GuiSync(97)
    public int blockModeEx = 0;
    private final DualityInterface dualityInterfaceCopy;

    public ContainerItemDualInterface(InventoryPlayer ip, IInterfaceHost te) {
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

    @Override
    protected void setupUpgrades() {
        IItemHandler upgrades = this.getUpgradeable().getInventoryByName("item_upgrades");
        if (this.availableUpgrades() > 0) {
            this.addSlotToContainer((new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 0, 187, 8, this.getInventoryPlayer())).setNotDraggable());
        }

        if (this.availableUpgrades() > 1) {
            this.addSlotToContainer((new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 1, 187, 26, this.getInventoryPlayer())).setNotDraggable());
        }

        if (this.availableUpgrades() > 2) {
            this.addSlotToContainer((new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 2, 187, 44, this.getInventoryPlayer())).setNotDraggable());
        }

        if (this.availableUpgrades() > 3) {
            this.addSlotToContainer((new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 3, 187, 62, this.getInventoryPlayer())).setNotDraggable());
        }
    }

}
