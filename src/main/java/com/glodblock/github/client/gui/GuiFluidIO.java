package com.glodblock.github.client.gui;

import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.core.localization.GuiText;
import com.glodblock.github.client.gui.container.ContainerFluidIO;
import com.glodblock.github.common.parts.PartFluidImportBus;
import com.glodblock.github.common.parts.PartSharedFluidBus;
import com.glodblock.github.util.NameConst;
import net.minecraft.client.resources.I18n;
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
    public void drawFG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
    {
        this.fontRendererObj.drawString( this.getGuiDisplayName(this.bus instanceof PartFluidImportBus ? I18n.format(NameConst.GUI_FLUID_IMPORT) : I18n.format(NameConst.GUI_FLUID_EXPORT)), 8, 6, 4210752 );
        this.fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752 );

        if( this.redstoneMode != null )
        {
            this.redstoneMode.set( this.cvb.getRedStoneMode() );
        }

        if( this.fuzzyMode != null )
        {
            this.fuzzyMode.set( this.cvb.getFuzzyMode() );
        }

        if( this.craftMode != null )
        {
            this.craftMode.set( this.cvb.getCraftingMode() );
        }

        if( this.schedulingMode != null )
        {
            this.schedulingMode.set( this.cvb.getSchedulingMode() );
        }
    }

    @Override
    protected GuiText getName()
    {
        return this.bus instanceof PartFluidImportBus ? GuiText.ImportBus : GuiText.ExportBus;
    }

    public void update(int id, IAEFluidStack stack) {
        ((ContainerFluidIO) this.cvb).getBus().setFluidInSlot(id, stack);
    }

}
