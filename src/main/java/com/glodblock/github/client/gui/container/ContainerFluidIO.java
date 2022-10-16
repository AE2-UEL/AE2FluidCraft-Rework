package com.glodblock.github.client.gui.container;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.util.IConfigManager;
import appeng.tile.inventory.AppEngInternalAEInventory;
import com.glodblock.github.common.parts.PartFluidExportBus;
import com.glodblock.github.common.parts.PartSharedFluidBus;
import com.glodblock.github.util.Ae2Reflect;
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

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        super.loadSettingsFromHost(cm);
        if (Ae2Reflect.getUpgradeableHost(this) instanceof PartFluidExportBus) {
            this.setCraftingMode((YesNo) cm.getSetting(Settings.CRAFT_ONLY));
        }
    }
}
