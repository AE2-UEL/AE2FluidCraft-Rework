package com.glodblock.github.client;

import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.render.StackSizeRenderer;
import appeng.container.AEBaseContainer;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.util.item.AEItemStack;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.button.GuiFCImgButton;
import com.glodblock.github.client.container.ContainerFluidPatternTerminal;
import com.glodblock.github.client.render.FluidRenderUtils;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.slot.SlotSingleItem;
import com.glodblock.github.network.CPacketFluidPatternTermBtns;
import com.glodblock.github.network.CPacketInventoryAction;
import com.glodblock.github.util.Ae2ReflectClient;
import com.glodblock.github.util.ModAndClassUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class GuiFluidPatternTerminal extends GuiPatternTerm {

    private final StackSizeRenderer stackSizeRenderer = Ae2ReflectClient.getStackSizeRenderer(this);
    private final ContainerFluidPatternTerminal container;
    private GuiTabButton craftingStatusBtn;
    private GuiFCImgButton craftingFluidBtn;
    private GuiFCImgButton combineEnableBtn;
    private GuiFCImgButton combineDisableBtn;
    private GuiFCImgButton fluidEnableBtn;
    private GuiFCImgButton fluidDisableBtn;

    public GuiFluidPatternTerminal(InventoryPlayer inventoryPlayer, ITerminalHost te) {
        super(inventoryPlayer, te);
        container = new ContainerFluidPatternTerminal(inventoryPlayer, te);
        container.setGui(this);
        this.inventorySlots = container;
        Ae2ReflectClient.setGuiContainer(this, container);
    }

    @Override
    public void initGui() {
        super.initGui();
        craftingStatusBtn = Ae2ReflectClient.getCraftingStatusButton(this);
        if (!ModAndClassUtil.NEE) {
            this.combineEnableBtn = new GuiFCImgButton( this.guiLeft + 84, this.guiTop + this.ySize - 163, "FORCE_COMBINE", "DO_COMBINE" );
            this.combineEnableBtn.setHalfSize( true );
            this.buttonList.add( this.combineEnableBtn );

            this.combineDisableBtn = new GuiFCImgButton( this.guiLeft + 84, this.guiTop + this.ySize - 163, "NOT_COMBINE", "DONT_COMBINE" );
            this.combineDisableBtn.setHalfSize( true );
            this.buttonList.add( this.combineDisableBtn );
        }

        this.fluidEnableBtn = new GuiFCImgButton( this.guiLeft + 74, this.guiTop + this.ySize - 153, "FLUID_FIRST", "FLUID" );
        this.fluidEnableBtn.setHalfSize( true );
        this.buttonList.add( this.fluidEnableBtn );

        this.fluidDisableBtn = new GuiFCImgButton( this.guiLeft + 74, this.guiTop + this.ySize - 153, "ORIGIN_ORDER", "ITEM" );
        this.fluidDisableBtn.setHalfSize( true );
        this.buttonList.add( this.fluidDisableBtn );

        this.craftingFluidBtn = new GuiFCImgButton(this.guiLeft + 110, this.guiTop + this.ySize - 32, "CRAFT_FLUID", "ENCODE");
        this.buttonList.add( this.craftingFluidBtn );
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        if (!this.container.isCraftingMode() && !ModAndClassUtil.NEE)
        {
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
        }
        else if (!ModAndClassUtil.NEE)
        {
            this.combineEnableBtn.visible = false;
            this.combineDisableBtn.visible = false;
        }
        if (this.container.fluidFirst)
        {
            this.fluidEnableBtn.visible = true;
            this.fluidDisableBtn.visible = false;
        }
        else
        {
            this.fluidEnableBtn.visible = false;
            this.fluidDisableBtn.visible = true;
        }
        this.craftingFluidBtn.visible = this.container.craftingMode;
        super.drawFG(offsetX, offsetY, mouseX, mouseY);
    }

    @Override
    public void drawSlot(Slot slot) {
        if (!(slot instanceof SlotFake && (FluidRenderUtils.renderFluidPacketIntoGuiSlot(
                slot, slot.getStack(), stackSizeRenderer, fontRenderer) || renderMEStyleSlot(slot, slot.getStack())))) {
                super.drawSlot(slot);
        }
    }

    private boolean renderMEStyleSlot(Slot slot, @Nonnull ItemStack stack) {
        if (slot instanceof SlotFake && !stack.isEmpty() && !(stack.getItem() instanceof ItemFluidPacket)) {
            super.drawSlot(new SlotSingleItem(slot));
            if (stack.getCount() > 1) {
                this.stackSizeRenderer.renderStackSize(fontRenderer, AEItemStack.fromItemStack(stack), slot.xPos, slot.yPos);
            }
            return true;
        }
        return false;
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        if (btn == craftingStatusBtn) {
            InventoryHandler.switchGui(GuiType.FLUID_PAT_TERM_CRAFTING_STATUS);
        } else if (this.combineDisableBtn == btn || this.combineEnableBtn == btn) {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns( "PatternTerminal.Combine", this.combineDisableBtn == btn ? "1" : "0" ));
        } else if (this.fluidDisableBtn == btn || this.fluidEnableBtn == btn) {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns( "PatternTerminal.Fluid", this.fluidDisableBtn == btn ? "1" : "0" ));
        } else if (this.craftingFluidBtn == btn) {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns( "PatternTerminal.Craft", "0" ));
        } else {
            super.actionPerformed(btn);
        }
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotIdx, int mouseButton, ClickType clickType) {
        if (mouseButton == 2 && !this.container.isCraftingMode()) {
            if (slot instanceof OptionalSlotFake || slot instanceof SlotFakeCraftingMatrix) {
                if (slot.getHasStack()) {
                    IAEItemStack stack = AEItemStack.fromItemStack(slot.getStack());
                    ((AEBaseContainer) this.inventorySlots).setTargetStack(stack);
                    for (int i = 0; i < this.inventorySlots.inventorySlots.size(); i ++) {
                        if (this.inventorySlots.inventorySlots.get(i).equals(slot)) {
                            FluidCraft.proxy.netHandler.sendToServer(new CPacketInventoryAction(1, i, 0, stack));
                            break;
                        }
                    }
                    return;
                }
            }
        }
        super.handleMouseClick(slot, slotIdx, mouseButton, clickType);
    }

}