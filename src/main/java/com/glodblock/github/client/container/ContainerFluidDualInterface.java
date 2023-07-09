package com.glodblock.github.client.container;

import appeng.api.config.SecurityPermissions;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.IConfigManager;
import appeng.container.implementations.ContainerTypeBuilder;
import appeng.fluids.container.FluidConfigurableContainer;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.FluidSyncHelper;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.fluids.util.IAEFluidTank;
import appeng.util.Platform;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.IContainerListener;

import java.util.Collections;
import java.util.Map;

public class ContainerFluidDualInterface extends FluidConfigurableContainer {

    private final DualityFluidInterface myDuality;
    private final FluidSyncHelper tankSync;
    public static final ContainerType<ContainerFluidDualInterface> TYPE = ContainerTypeBuilder
            .create(ContainerFluidDualInterface::new, IFluidInterfaceHost.class)
            .build("dual_fluid_interface");

    public ContainerFluidDualInterface(int id, PlayerInventory ip, IFluidInterfaceHost te) {
        super(TYPE, id, ip, te.getDualityFluidInterface().getHost());
        this.myDuality = te.getDualityFluidInterface();
        this.tankSync = new FluidSyncHelper(this.myDuality.getTanks(), 6);
    }

    protected void setupConfig() {
        // NO-OP
    }

    protected void loadSettingsFromHost(IConfigManager cm) {
        // NO-OP
    }

    public IAEFluidTank getTanks() {
        return this.myDuality.getTanks();
    }

    public IAEFluidTank getFluidConfigInventory() {
        return this.myDuality.getConfig();
    }

    @Override
    public void detectAndSendChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);
        super.detectAndSendChanges();
        if (Platform.isServer()) {
            this.tankSync.sendDiff(this.listeners);
        }
        standardDetectAndSendChanges();
    }

    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        this.tankSync.sendFull(Collections.singleton(listener));
    }

    public void receiveFluidSlots(Map<Integer, IAEFluidStack> fluids) {
        super.receiveFluidSlots(fluids);
        this.tankSync.readPacket(fluids);
    }

    protected boolean supportCapacity() {
        return false;
    }

    public int availableUpgrades() {
        return 0;
    }

    public boolean hasToolbox() {
        return false;
    }

}
