package com.glodblock.github.client;

import appeng.api.storage.ITerminalHost;
import appeng.client.gui.implementations.GuiExpandedProcessingPatternTerm;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.render.StackSizeRenderer;
import appeng.container.slot.SlotFake;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.button.GuiFCImgButton;
import com.glodblock.github.client.container.ContainerExtendedFluidPatternTerminal;
import com.glodblock.github.client.render.FluidRenderUtils;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.network.CPacketFluidPatternTermBtns;
import com.glodblock.github.util.Ae2ReflectClient;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;

public class GuiExtendedFluidPatternTerminal extends GuiExpandedProcessingPatternTerm {

    private final StackSizeRenderer stackSizeRenderer = Ae2ReflectClient.getStackSizeRenderer(this);
    private final ContainerExtendedFluidPatternTerminal container;
    private GuiTabButton craftingStatusBtn;
    private GuiFCImgButton combineEnableBtn;
    private GuiFCImgButton combineDisableBtn;

    public GuiExtendedFluidPatternTerminal(InventoryPlayer inventoryPlayer, ITerminalHost te) {
        super(inventoryPlayer, te);
        container = new ContainerExtendedFluidPatternTerminal(inventoryPlayer, te);
        container.setGui(this);
        this.inventorySlots = container;
        Ae2ReflectClient.setGuiExContainer(this, container);
    }

    @Override
    public void initGui() {
        super.initGui();
        craftingStatusBtn = Ae2ReflectClient.getCraftingStatusButton(this);
        this.combineEnableBtn = new GuiFCImgButton( this.guiLeft + 74, this.guiTop + this.ySize - 153, "FORCE_COMBINE", "DO_COMBINE" );
        this.combineEnableBtn.setHalfSize( true );
        this.buttonList.add( this.combineEnableBtn );

        this.combineDisableBtn = new GuiFCImgButton( this.guiLeft + 74, this.guiTop + this.ySize - 153, "NOT_COMBINE", "DONT_COMBINE" );
        this.combineDisableBtn.setHalfSize( true );
        this.buttonList.add( this.combineDisableBtn );
    }

    @Override
    public void drawSlot(Slot slot) {
        if (!(slot instanceof SlotFake && FluidRenderUtils.renderFluidPacketIntoGuiSlot(
                slot, slot.getStack(), stackSizeRenderer, fontRenderer))) {
            super.drawSlot(slot);
        }
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        if ( this.container.combine )
        {
            this.combineEnableBtn.visible = true;
            this.combineDisableBtn.visible = false;
        }
        else
        {
            this.combineEnableBtn.visible = false;
            this.combineDisableBtn.visible = true;
        }
        super.drawFG(offsetX, offsetY, mouseX, mouseY);
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        if (btn == craftingStatusBtn) {
            InventoryHandler.switchGui(GuiType.FLUID_PAT_TERM_CRAFTING_STATUS);
        } else if (this.combineDisableBtn == btn || this.combineEnableBtn == btn) {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns( "PatternTerminal.Combine", this.combineDisableBtn == btn ? "1" : "0" ));
        } else {
            super.actionPerformed(btn);
        }
    }

}
