package com.glodblock.github.client.container;

import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.util.IConfigManager;
import appeng.container.SlotSemantic;
import appeng.container.guisync.GuiSync;
import appeng.container.implementations.ContainerTypeBuilder;
import appeng.container.implementations.UpgradeableContainer;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.FakeSlot;
import appeng.container.slot.RestrictedInputSlot;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.util.Platform;
import com.glodblock.github.interfaces.ConfigData;
import com.glodblock.github.coreutil.ExtendedInterface;
import com.glodblock.github.util.ConfigSet;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;

public class ContainerItemDualInterface extends UpgradeableContainer implements ConfigData {

    @GuiSync(3)
    public YesNo bMode;
    @GuiSync(4)
    public YesNo iTermMode;
    @GuiSync(95)
    public boolean fluidPacket = false;
    @GuiSync(96)
    public boolean allowSplitting = true;
    @GuiSync(97)
    public int blockModeEx = 0;
    private final ConfigSet exConfig = new ConfigSet();
    private final DualityInterface dualityInterfaceCopy;
    public static final ContainerType<ContainerItemDualInterface> TYPE = ContainerTypeBuilder
            .create(ContainerItemDualInterface::new, IInterfaceHost.class)
            .build("dual_item_interface");

    public ContainerItemDualInterface(int id, PlayerInventory ip, IInterfaceHost te) {
        super(TYPE, id, ip, te.getInterfaceDuality().getHost());
        this.bMode = YesNo.NO;
        this.iTermMode = YesNo.YES;
        this.dualityInterfaceCopy = te.getInterfaceDuality();
        int x;
        for (x = 0; x < 9; ++x) {
            this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.ENCODED_PATTERN, this.dualityInterfaceCopy.getPatterns(), x), SlotSemantic.ENCODED_PATTERN);
        }
        for (x = 0; x < 9; ++x) {
            this.addSlot(new FakeSlot(this.dualityInterfaceCopy.getConfig(), x), SlotSemantic.CONFIG);
        }
        for (x = 0; x < 9; ++x) {
            this.addSlot(new AppEngSlot(this.dualityInterfaceCopy.getStorage(), x), SlotSemantic.STORAGE);
        }
        this.exConfig
                .addConfig("fluidPacket", v -> {
                    this.fluidPacket = (boolean) v;
                    ((ExtendedInterface) dualityInterfaceCopy).setFluidPacketMode((boolean) v);
                    }, () -> this.fluidPacket)
                .addConfig("allowSplitting", v -> {
                    this.allowSplitting = (boolean) v;
                    ((ExtendedInterface) dualityInterfaceCopy).setSplittingMode((boolean) v);
                    }, () -> this.allowSplitting)
                .addConfig("blockModeEx", v -> {
                    this.blockModeEx = (int) v;
                    ((ExtendedInterface) dualityInterfaceCopy).setExtendedBlockMode((int) v);
                    }, () -> this.blockModeEx);
    }

    protected void setupConfig() {
        this.setupUpgrades();
    }

    public int availableUpgrades() {
        return 1;
    }

    @Override
    public void detectAndSendChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);
        super.detectAndSendChanges();
        if (Platform.isServer()) {
            ExtendedInterface eif = (ExtendedInterface) dualityInterfaceCopy;
            if (fluidPacket != eif.getFluidPacketMode()) {
                fluidPacket = eif.getFluidPacketMode();
            }
            if (allowSplitting != eif.getSplittingMode()) {
                allowSplitting = eif.getSplittingMode();
            }
            if (blockModeEx != eif.getExtendedBlockMode()) {
                blockModeEx = eif.getExtendedBlockMode();
            }
        }
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        this.setBlockingMode((YesNo)cm.getSetting(Settings.BLOCK));
        this.setInterfaceTerminalMode((YesNo)cm.getSetting(Settings.INTERFACE_TERMINAL));
    }

    public YesNo getBlockingMode() {
        return this.bMode;
    }

    private void setBlockingMode(YesNo bMode) {
        this.bMode = bMode;
    }

    public YesNo getInterfaceTerminalMode() {
        return this.iTermMode;
    }

    private void setInterfaceTerminalMode(YesNo iTermMode) {
        this.iTermMode = iTermMode;
    }

    @Override
    public void set(String id, Object value) {
        this.exConfig.setConfig(id, value);
    }

    @Override
    public Object get(String id) {
        return this.exConfig.getConfig(id);
    }
}
