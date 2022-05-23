package com.glodblock.github.client.container;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.util.IConfigManager;
import appeng.fluids.container.ContainerFluidIO;
import appeng.fluids.parts.PartSharedFluidBus;
import com.glodblock.github.common.part.PartFluidExportBus;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerFluidExportBus extends ContainerFluidIO {

    public ContainerFluidExportBus(InventoryPlayer ip, PartSharedFluidBus te) {
        super(ip, te);
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        super.loadSettingsFromHost(cm);
        if (this.getUpgradeable() instanceof PartFluidExportBus) {
            this.setCraftingMode((YesNo)cm.getSetting(Settings.CRAFT_ONLY));
        }
    }

}
