package com.glodblock.github.client;

import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.implementations.GuiExpandedProcessingPatternTerm;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.render.StackSizeRenderer;
import appeng.container.AEBaseContainer;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.util.item.AEItemStack;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.button.GuiFCImgButton;
import com.glodblock.github.client.container.ContainerExtendedFluidPatternTerminal;
import com.glodblock.github.client.render.FluidRenderUtils;
import com.glodblock.github.integration.jei.FluidPacketTarget;
import com.glodblock.github.integration.mek.FCGasItems;
import com.glodblock.github.integration.mek.GasRenderUtil;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.slot.SlotSingleItem;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.network.CPacketFluidPatternTermBtns;
import com.glodblock.github.network.CPacketInventoryAction;
import com.glodblock.github.util.Ae2ReflectClient;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.UtilClient;
import mezz.jei.api.gui.IGhostIngredientHandler.Target;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GuiExtendedFluidPatternTerminal extends GuiExpandedProcessingPatternTerm {

    private final StackSizeRenderer stackSizeRenderer = Ae2ReflectClient.getStackSizeRenderer(this);
    private final ContainerExtendedFluidPatternTerminal container;
    private GuiTabButton craftingStatusBtn;
    private GuiFCImgButton combineEnableBtn;
    private GuiFCImgButton combineDisableBtn;
    private GuiFCImgButton fluidEnableBtn;
    private GuiFCImgButton fluidDisableBtn;

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

        this.fluidEnableBtn = new GuiFCImgButton( this.guiLeft + 74, this.guiTop + this.ySize - 143, "FLUID_FIRST", "FLUID" );
        this.fluidEnableBtn.setHalfSize( true );
        this.buttonList.add( this.fluidEnableBtn );

        this.fluidDisableBtn = new GuiFCImgButton( this.guiLeft + 74, this.guiTop + this.ySize - 143, "ORIGIN_ORDER", "ITEM" );
        this.fluidDisableBtn.setHalfSize( true );
        this.buttonList.add( this.fluidDisableBtn );
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
        if (slot instanceof SlotFake && !stack.isEmpty() && !(stack.getItem() == FCItems.FLUID_PACKET || (ModAndClassUtil.GAS && stack.getItem() == FCGasItems.GAS_PACKET))) {
            super.drawSlot(new SlotSingleItem(slot));
            if (stack.getCount() > 1) {
                this.stackSizeRenderer.renderStackSize(fontRenderer, AEItemStack.fromItemStack(stack), slot.xPos, slot.yPos);
            }
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
        super.drawFG(offsetX, offsetY, mouseX, mouseY);
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        if (btn == craftingStatusBtn) {
            InventoryHandler.switchGui(GuiType.FLUID_PAT_TERM_CRAFTING_STATUS);
        } else if (this.combineDisableBtn == btn || this.combineEnableBtn == btn) {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns( "PatternTerminal.Combine", this.combineDisableBtn == btn ? "1" : "0" ));
        } else if (this.fluidDisableBtn == btn || this.fluidEnableBtn == btn) {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns( "PatternTerminal.Fluid", this.fluidDisableBtn == btn ? "1" : "0" ));
        } else {
            super.actionPerformed(btn);
        }
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotIdx, int mouseButton, ClickType clickType) {
        if (mouseButton == 2 ) {
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
    public List<Target<?>> getPhantomTargets(Object ingredient) {
        if (FluidPacketTarget.covertFluid(ingredient) != null || FluidPacketTarget.covertGas(ingredient) != null) {
            List<Target<?>> targets = new ArrayList<>();
            for (Slot slot : this.inventorySlots.inventorySlots) {
                if (slot instanceof SlotFake) {
                    Target<?> target = new FluidPacketTarget(getGuiLeft(), getGuiTop(), slot);
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
