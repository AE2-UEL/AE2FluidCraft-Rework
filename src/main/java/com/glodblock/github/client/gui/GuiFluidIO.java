package com.glodblock.github.client.gui;

import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.core.localization.GuiText;
import com.glodblock.github.client.gui.container.ContainerFluidIO;
import com.glodblock.github.common.parts.PartFluidImportBus;
import com.glodblock.github.common.parts.PartSharedFluidBus;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiFluidIO extends GuiUpgradeable
{
    private final PartSharedFluidBus bus;

    public GuiFluidIO(InventoryPlayer inventoryPlayer, PartSharedFluidBus te )
    {
        super( new ContainerFluidIO( inventoryPlayer, te ) );
        this.bus = te;
    }


    @Override
    protected GuiText getName()
    {
        return this.bus instanceof PartFluidImportBus ? GuiText.ImportBus : GuiText.ExportBus;
    }

    public void update(int id, IAEFluidStack stack) {
        ((ContainerFluidIO)this.cvb).getBus().setFluidInSlot(id, stack);
    }

}
