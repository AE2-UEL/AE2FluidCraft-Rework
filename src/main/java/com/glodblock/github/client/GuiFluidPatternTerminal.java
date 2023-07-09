package com.glodblock.github.client;

import appeng.api.config.ActionItems;
import appeng.client.gui.me.common.StackSizeRenderer;
import appeng.client.gui.me.items.ItemTerminalScreen;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.TabButton;
import appeng.container.SlotSemantic;
import appeng.container.slot.FakeSlot;
import appeng.core.localization.GuiText;
import appeng.util.item.AEItemStack;
import com.glodblock.github.client.button.BlitMap;
import com.glodblock.github.client.button.FCToggleButton;
import com.glodblock.github.client.container.ContainerFluidPatternTerminal;
import com.glodblock.github.client.slot.SlotSingleItem;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.network.NetworkManager;
import com.glodblock.github.network.packets.CPacketFluidCraftBtns;
import com.glodblock.github.util.Ae2ReflectClient;
import com.glodblock.github.util.FluidRenderUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;

public class GuiFluidPatternTerminal extends ItemTerminalScreen<ContainerFluidPatternTerminal> {
    private static final String MODES_TEXTURE = "guis/pattern_modes.png";
    private static final Blitter CRAFTING_MODE_BG = Blitter.texture(MODES_TEXTURE).src(0, 0, 126, 68);
    private static final Blitter PROCESSING_MODE_BG = Blitter.texture(MODES_TEXTURE).src(0, 70, 126, 68);
    private static final String SUBSTITUTION_DISABLE = "0";
    private static final String SUBSTITUTION_ENABLE = "1";
    private static final String CRAFTMODE_CRAFTING = "1";
    private static final String CRAFTMODE_PROCESSING = "0";
    private final StackSizeRenderer stackSizeRenderer = Ae2ReflectClient.getGuiStyle(this).getStackSizeRenderer();
    private final TabButton tabCraftButton;
    private final TabButton tabProcessButton;
    private final ActionButton substitutionsEnabledBtn;
    private final ActionButton substitutionsDisabledBtn;
    private final FCToggleButton combineBtn;
    private final FCToggleButton fluidBtn;

    public GuiFluidPatternTerminal(ContainerFluidPatternTerminal container, PlayerInventory playerInventory, ITextComponent title, ScreenStyle style) {
        super(container, playerInventory, title, style);
        this.tabCraftButton = new TabButton(
                new ItemStack(Blocks.CRAFTING_TABLE), GuiText.CraftingPattern.text(), this.itemRenderer,
                btn -> toggleCraftMode(CRAFTMODE_PROCESSING));
        widgets.add("craftingPatternMode", this.tabCraftButton);

        this.tabProcessButton = new TabButton(
                new ItemStack(Blocks.FURNACE), GuiText.ProcessingPattern.text(), this.itemRenderer,
                btn -> toggleCraftMode(CRAFTMODE_CRAFTING));
        widgets.add("processingPatternMode", this.tabProcessButton);

        this.substitutionsEnabledBtn = new ActionButton(
                ActionItems.ENABLE_SUBSTITUTION, act -> toggleSubstitutions(SUBSTITUTION_DISABLE));
        this.substitutionsEnabledBtn.setHalfSize(true);
        widgets.add("substitutionsEnabled", this.substitutionsEnabledBtn);

        this.substitutionsDisabledBtn = new ActionButton(
                ActionItems.DISABLE_SUBSTITUTION, act -> toggleSubstitutions(SUBSTITUTION_ENABLE));
        this.substitutionsDisabledBtn.setHalfSize(true);
        widgets.add("substitutionsDisabled", this.substitutionsDisabledBtn);

        this.combineBtn = new FCToggleButton(
                btn -> {
                    FCToggleButton fbtn = (FCToggleButton) btn;
                    NetworkManager.netHandler.sendToServer(new CPacketFluidCraftBtns("combine", fbtn.getActive() == 1));
                }, BlitMap.NOT_COMBINE, BlitMap.COMBINE
        );
        this.combineBtn.setHalfSize(true);
        widgets.add("combineBtn", this.combineBtn);

        this.fluidBtn = new FCToggleButton(
                btn -> {
                    FCToggleButton fbtn = (FCToggleButton) btn;
                    NetworkManager.netHandler.sendToServer(new CPacketFluidCraftBtns("fluidFirst", fbtn.getActive() == 0));
                }, BlitMap.FLUID_FIRST, BlitMap.ITEM_FIRST
        );
        this.fluidBtn.setHalfSize(true);
        widgets.add("fluidBtn", this.fluidBtn);

        ActionButton clearBtn = new ActionButton(ActionItems.CLOSE, act -> clear());
        clearBtn.setHalfSize(true);
        widgets.add("clearPattern", clearBtn);

        ActionButton encodeBtn = new ActionButton(ActionItems.ENCODE, act -> encode());
        widgets.add("encodePattern", encodeBtn);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        if (this.container.isCraftingMode()) {
            this.tabCraftButton.visible = true;
            this.tabProcessButton.visible = false;

            if (this.container.substitute) {
                this.substitutionsEnabledBtn.visible = true;
                this.substitutionsDisabledBtn.visible = false;
            } else {
                this.substitutionsEnabledBtn.visible = false;
                this.substitutionsDisabledBtn.visible = true;
            }
            this.fluidBtn.visible = false;
            this.combineBtn.visible = false;
        } else {
            this.tabCraftButton.visible = false;
            this.tabProcessButton.visible = true;
            this.substitutionsEnabledBtn.visible = false;
            this.substitutionsDisabledBtn.visible = false;
            this.fluidBtn.visible = true;
            this.combineBtn.visible = true;
        }

        setSlotsHidden(SlotSemantic.CRAFTING_RESULT, !this.container.isCraftingMode());
        setSlotsHidden(SlotSemantic.PROCESSING_RESULT, this.container.isCraftingMode());
    }

    private void toggleCraftMode(String mode) {
        NetworkManager.netHandler.sendToServer(new CPacketFluidCraftBtns("craft", mode.equals("1")));
    }

    private void toggleSubstitutions(String mode) {
        NetworkManager.netHandler.sendToServer(new CPacketFluidCraftBtns("substitute", mode.equals("1")));
    }

    private void encode() {
        NetworkManager.netHandler.sendToServer(new CPacketFluidCraftBtns("encode"));
    }

    private void clear() {
        NetworkManager.netHandler.sendToServer(new CPacketFluidCraftBtns("clear"));
    }

    @Override
    protected void moveItems(MatrixStack matrices, Slot slot) {
        if (!(slot instanceof FakeSlot && (FluidRenderUtils.renderFluidPacketIntoGuiSlot(
                slot, slot.getStack(), stackSizeRenderer, font) || renderMEStyleSlot(slot, slot.getStack(), matrices)))) {
            super.moveItems(matrices, slot);
        }
    }

    private boolean renderMEStyleSlot(Slot slot, @Nonnull ItemStack stack, MatrixStack matrices) {
        if (slot instanceof FakeSlot && !stack.isEmpty() && !(stack.getItem() instanceof ItemFluidPacket)) {
            super.moveItems(matrices, new SlotSingleItem(slot));
            if (stack.getCount() > 1) {
                this.stackSizeRenderer.renderStackSize(font, AEItemStack.fromItemStack(stack).getStackSize(), false, slot.xPos, slot.yPos);
            }
            return true;
        }
        return false;
    }

    @Override
    public void drawBG(MatrixStack matrixStack, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        super.drawBG(matrixStack, offsetX, offsetY, mouseX, mouseY, partialTicks);
        Blitter modeBg = this.container.isCraftingMode() ? CRAFTING_MODE_BG : PROCESSING_MODE_BG;
        modeBg.dest(this.guiLeft + 9, this.guiTop + this.ySize - 164).blit(matrixStack, this.getBlitOffset());
    }
}