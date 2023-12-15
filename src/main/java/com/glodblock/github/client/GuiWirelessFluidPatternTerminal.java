package com.glodblock.github.client;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.render.StackSizeRenderer;
import appeng.container.AEBaseContainer;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.util.item.AEItemStack;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.button.GuiFCImgButton;
import com.glodblock.github.client.container.ContainerWirelessFluidPatternTerminal;
import com.glodblock.github.client.render.FluidRenderUtils;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.integration.jei.FluidPacketTarget;
import com.glodblock.github.integration.mek.GasRenderUtil;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.slot.SlotSingleItem;
import com.glodblock.github.network.CPacketFluidPatternTermBtns;
import com.glodblock.github.network.CPacketInventoryAction;
import com.glodblock.github.util.Ae2ReflectClient;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.UtilClient;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GuiWirelessFluidPatternTerminal extends GuiPatternTerm {

    private final StackSizeRenderer stackSizeRenderer = Ae2ReflectClient.getStackSizeRenderer(this);
    private final ContainerWirelessFluidPatternTerminal container;
    private GuiTabButton craftingStatusBtn;
    private GuiFCImgButton craftingFluidBtn;
    private GuiFCImgButton combineEnableBtn;
    private GuiFCImgButton combineDisableBtn;
    private GuiFCImgButton fluidEnableBtn;
    private GuiFCImgButton fluidDisableBtn;

    public GuiWirelessFluidPatternTerminal(InventoryPlayer inventoryPlayer, WirelessTerminalGuiObject te) {
        super(inventoryPlayer, te, new ContainerWirelessFluidPatternTerminal(inventoryPlayer, te));
        container = (ContainerWirelessFluidPatternTerminal) this.inventorySlots;
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

        this.craftingFluidBtn = new GuiFCImgButton(this.guiLeft + 110, this.guiTop + this.ySize - 115, "CRAFT_FLUID", "ENCODE");
        this.buttonList.add( this.craftingFluidBtn );
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.bindTexture("guis/wirelessupgrades.png");
        Gui.drawModalRectWithCustomSizedTexture(offsetX + 198, offsetY + 127, 0, 0, 32, 32, 32, 32);
        super.drawBG(offsetX, offsetY, mouseX, mouseY);
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
        if (slot instanceof SlotFake) {
            ItemStack stack = slot.getStack();
            if (FluidRenderUtils.renderFluidPacketIntoGuiSlot(slot, stack, stackSizeRenderer, fontRenderer)) {
                return;
            }
            if (ModAndClassUtil.GAS && GasRenderUtil.renderGasPacketIntoGuiSlot(slot, stack, stackSizeRenderer, fontRenderer)) {
                return;
            }
            renderMEStyleSlot(slot, slot.getStack());
        } else {
            super.drawSlot(slot);
        }
    }

    private void renderMEStyleSlot(Slot slot, @Nonnull ItemStack stack) {
        if (slot instanceof SlotFake && !stack.isEmpty() && !(stack.getItem() instanceof ItemFluidPacket)) {
            super.drawSlot(new SlotSingleItem(slot));
            if (stack.getCount() > 1) {
                this.stackSizeRenderer.renderStackSize(fontRenderer, AEItemStack.fromItemStack(stack), slot.xPos, slot.yPos);
            }
        }
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
                            FluidCraft.proxy.netHandler.sendToServer(new CPacketInventoryAction(CPacketInventoryAction.Action.CHANGE_AMOUNT, i, 0, stack));
                            break;
                        }
                    }
                    return;
                }
            }
        }
        if (UtilClient.shouldAutoCraft(slot, mouseButton, clickType)) {
            IAEItemStack stack = AEItemStack.fromItemStack(slot.getStack());
            ((AEBaseContainer) this.inventorySlots).setTargetStack(stack);
            FluidCraft.proxy.netHandler.sendToServer(new CPacketInventoryAction(CPacketInventoryAction.Action.AUTO_CRAFT, 0, 0, stack));
            return;
        }
        super.handleMouseClick(slot, slotIdx, mouseButton, clickType);
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object ingredient) {
        if (!this.container.isCraftingMode() && (FluidPacketTarget.covertFluid(ingredient) != null || FluidPacketTarget.covertGas(ingredient) != null)) {
            List<IGhostIngredientHandler.Target<?>> targets = new ArrayList<>();
            for (Slot slot : this.inventorySlots.inventorySlots) {
                if (slot instanceof SlotFake) {
                    IGhostIngredientHandler.Target<?> target = new FluidPacketTarget(getGuiLeft(), getGuiTop(), slot);
                    targets.add(target);
                    mapTargetSlot.putIfAbsent(target, slot);
                }
            }
            return targets;
        } else {
            return super.getPhantomTargets(ingredient);
        }
    }

}