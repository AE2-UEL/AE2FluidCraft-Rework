package com.glodblock.github.client.container;

import appeng.api.config.Upgrades;
import appeng.container.slot.SlotRestrictedInput;
import appeng.fluids.container.ContainerFluidInterface;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.util.Platform;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.IItemHandler;

public class ContainerFluidDualInterface extends ContainerFluidInterface {
    private final DualityFluidInterface dualityInterfaceCopy;
    public ContainerFluidDualInterface(InventoryPlayer ip, IFluidInterfaceHost te) {
        super(ip, te);
        this.dualityInterfaceCopy = te.getDualityFluidInterface();
    }

    @Override
    protected void setupUpgrades() {
        IItemHandler upgrades = this.getUpgradeable().getInventoryByName("fluid_upgrades");
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

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (Platform.isServer()) {
            if (this.capacityUpgrades != this.dualityInterfaceCopy.getInstalledUpgrades(Upgrades.CAPACITY)) {
                this.capacityUpgrades = this.dualityInterfaceCopy.getInstalledUpgrades(Upgrades.CAPACITY);
            }
        }
        standardDetectAndSendChanges();
    }

}
