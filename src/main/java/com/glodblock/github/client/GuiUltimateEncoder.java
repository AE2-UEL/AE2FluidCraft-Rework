package com.glodblock.github.client;

import appeng.api.config.ActionItems;
import appeng.api.config.Settings;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.render.StackSizeRenderer;
import appeng.container.interfaces.IJEIGhostIngredients;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.util.item.AEItemStack;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.button.GuiFCImgButton;
import com.glodblock.github.client.container.ContainerUltimateEncoder;
import com.glodblock.github.client.render.FluidRenderUtils;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.tile.TileUltimateEncoder;
import com.glodblock.github.integration.jei.FluidPacketTarget;
import com.glodblock.github.integration.jei.ItemTarget;
import com.glodblock.github.inventory.slot.SlotSingleItem;
import com.glodblock.github.network.CPacketFluidPatternTermBtns;
import com.glodblock.github.network.CPacketInventoryAction;
import com.glodblock.github.util.Ae2ReflectClient;
import com.glodblock.github.util.NameConst;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiUltimateEncoder extends AEBaseGui implements IJEIGhostIngredients {

    private static final ResourceLocation TEX_BG = FluidCraft.resource("textures/gui/ultimate_encoder.png");
    private final ContainerUltimateEncoder container;
    private final StackSizeRenderer stackSizeRenderer = Ae2ReflectClient.getStackSizeRenderer(this);
    public final Map<IGhostIngredientHandler.Target<?>, Object> mapTargetSlot = new HashMap<>();
    private GuiImgButton encodeBtn;
    private GuiImgButton clearBtn;
    private GuiImgButton x2Btn;
    private GuiImgButton x3Btn;
    private GuiImgButton plusOneBtn;
    private GuiImgButton divTwoBtn;
    private GuiImgButton divThreeBtn;
    private GuiImgButton minusOneBtn;
    private GuiFCImgButton combineEnableBtn;
    private GuiFCImgButton combineDisableBtn;
    private GuiFCImgButton fluidEnableBtn;
    private GuiFCImgButton fluidDisableBtn;

    public GuiUltimateEncoder(InventoryPlayer ipl, TileUltimateEncoder tile) {
        super(new ContainerUltimateEncoder(ipl, tile));
        this.container = (ContainerUltimateEncoder) this.inventorySlots;
        this.ySize = 249;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.encodeBtn = new GuiImgButton(this.guiLeft + 137, this.guiTop + 111, Settings.ACTIONS, ActionItems.ENCODE);
        this.buttonList.add(this.encodeBtn);

        this.clearBtn = new GuiImgButton(this.guiLeft + 118, this.guiTop + 25, Settings.ACTIONS, ActionItems.CLOSE);
        this.clearBtn.setHalfSize(true);
        this.buttonList.add(this.clearBtn);

        this.x3Btn = new GuiImgButton(this.guiLeft + 115, this.guiTop + 61, Settings.ACTIONS, ActionItems.MULTIPLY_BY_THREE);
        this.x3Btn.setHalfSize(true);
        this.buttonList.add(this.x3Btn);

        this.x2Btn = new GuiImgButton(this.guiLeft + 115, this.guiTop + 79, Settings.ACTIONS, ActionItems.MULTIPLY_BY_TWO);
        this.x2Btn.setHalfSize(true);
        this.buttonList.add(this.x2Btn);

        this.plusOneBtn = new GuiImgButton(this.guiLeft + 115, this.guiTop + 97, Settings.ACTIONS, ActionItems.INCREASE_BY_ONE);
        this.plusOneBtn.setHalfSize(true);
        this.buttonList.add(this.plusOneBtn);

        this.divThreeBtn = new GuiImgButton(this.guiLeft + 124, this.guiTop + 61, Settings.ACTIONS, ActionItems.DIVIDE_BY_THREE);
        this.divThreeBtn.setHalfSize(true);
        this.buttonList.add(this.divThreeBtn);

        this.divTwoBtn = new GuiImgButton(this.guiLeft + 124, this.guiTop + 79, Settings.ACTIONS, ActionItems.DIVIDE_BY_TWO);
        this.divTwoBtn.setHalfSize(true);
        this.buttonList.add(this.divTwoBtn);

        this.minusOneBtn = new GuiImgButton(this.guiLeft + 124, this.guiTop + 97, Settings.ACTIONS, ActionItems.DECREASE_BY_ONE);
        this.minusOneBtn.setHalfSize(true);
        this.buttonList.add(this.minusOneBtn);

        this.combineEnableBtn = new GuiFCImgButton( this.guiLeft + 120, this.guiTop + 136, "FORCE_COMBINE", "DO_COMBINE" );
        this.combineEnableBtn.setHalfSize(true);
        this.buttonList.add(this.combineEnableBtn);

        this.combineDisableBtn = new GuiFCImgButton( this.guiLeft + 120, this.guiTop + 136, "NOT_COMBINE", "DONT_COMBINE" );
        this.combineDisableBtn.setHalfSize(true);
        this.buttonList.add(this.combineDisableBtn);

        this.fluidEnableBtn = new GuiFCImgButton( this.guiLeft + 120, this.guiTop + 118, "FLUID_FIRST", "FLUID" );
        this.fluidEnableBtn.setHalfSize(true);
        this.buttonList.add(this.fluidEnableBtn);

        this.fluidDisableBtn = new GuiFCImgButton( this.guiLeft + 120, this.guiTop + 118, "ORIGIN_ORDER", "ITEM" );
        this.fluidDisableBtn.setHalfSize(true);
        this.buttonList.add(this.fluidDisableBtn);
    }

    @Override
    protected void actionPerformed(@Nonnull GuiButton btn) {
        try {
            if (this.encodeBtn == btn) {
                if (isShiftKeyDown()) {
                    FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns("UltimateEncoder.Encode", "1"));
                } else {
                    FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns("UltimateEncoder.Encode", "0"));
                }
            }
            if (this.clearBtn == btn) {
                FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns("UltimateEncoder.Clear", "1"));
            }

            if (this.x2Btn == btn) {
                FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns("UltimateEncoder.MultiplyByTwo", "1"));
            }

            if (this.x3Btn == btn) {
                FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns("UltimateEncoder.MultiplyByThree", "1"));
            }

            if (this.divTwoBtn == btn) {
                FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns("UltimateEncoder.DivideByTwo", "1"));
            }

            if (this.divThreeBtn == btn) {
                FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns("UltimateEncoder.DivideByThree", "1"));
            }

            if (this.plusOneBtn == btn) {
                FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns("UltimateEncoder.IncreaseByOne", "1"));
            }

            if (this.minusOneBtn == btn) {
                FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns("UltimateEncoder.DecreaseByOne", "1"));
            }

            if (this.combineDisableBtn == btn || this.combineEnableBtn == btn) {
                FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns( "UltimateEncoder.Combine", this.combineDisableBtn == btn ? "1" : "0" ));
            }

            if (this.fluidDisableBtn == btn || this.fluidEnableBtn == btn) {
                FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns( "UltimateEncoder.Fluid", this.fluidDisableBtn == btn ? "1" : "0" ));
            }
            super.actionPerformed(btn);
        } catch (IOException ignore) {
            // NO-OP
        }
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotIdx, int mouseButton, ClickType clickType) {
        if (mouseButton == 2 ) {
            if (slot instanceof OptionalSlotFake || slot instanceof SlotFakeCraftingMatrix) {
                if (slot.getHasStack()) {
                    IAEItemStack stack = AEItemStack.fromItemStack(slot.getStack());
                    this.container.setTargetStack(stack);
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
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.fontRenderer.drawString(getGuiDisplayName(I18n.format(NameConst.GUI_ULTIMATE_ENCODER)), 8, 6, 0x404040);
        this.fontRenderer.drawString(GuiText.inventory.getLocal(), 8, ySize - 94, 0x404040);
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
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEX_BG);
        drawTexturedModalRect(offsetX, offsetY, 0, 0, 176, this.ySize);
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object ingredient) {
        if (FluidPacketTarget.covertFluid(ingredient) != null) {
            List<IGhostIngredientHandler.Target<?>> targets = new ArrayList<>();
            for (Slot slot : this.container.inventorySlots) {
                if (slot instanceof SlotFake) {
                    IGhostIngredientHandler.Target<?> target = new FluidPacketTarget(getGuiLeft(), getGuiTop(), slot);
                    targets.add(target);
                    this.mapTargetSlot.putIfAbsent(target, slot);
                }
            }
            return targets;
        } else if (ingredient instanceof ItemStack) {
            List<IGhostIngredientHandler.Target<?>> targets = new ArrayList<>();
            for (Slot slot : this.container.inventorySlots) {
                if (slot instanceof SlotFake) {
                    IGhostIngredientHandler.Target<?> target = new ItemTarget(getGuiLeft(), getGuiTop(), slot);
                    targets.add(target);
                    this.mapTargetSlot.putIfAbsent(target, slot);
                }
            }
            return targets;
        }
        return Collections.emptyList();
    }

    @Override
    public Map<IGhostIngredientHandler.Target<?>, Object> getFakeSlotTargetMap() {
        return this.mapTargetSlot;
    }

}
