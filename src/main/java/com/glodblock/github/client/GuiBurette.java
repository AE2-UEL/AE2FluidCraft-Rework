package com.glodblock.github.client;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiNumberBox;
import appeng.core.localization.GuiText;
import appeng.fluids.util.IAEFluidTank;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.container.ContainerBurette;
import com.glodblock.github.client.render.FluidRenderUtils;
import com.glodblock.github.common.tile.TileBurette;
import com.glodblock.github.handler.ButtonMouseHandler;
import com.glodblock.github.handler.TankMouseHandler;
import com.glodblock.github.network.CPacketTransposeFluid;
import com.glodblock.github.util.MouseRegionManager;
import com.glodblock.github.util.NameConst;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GuiBurette extends AEBaseGui {

    private static final ResourceLocation TEX_BG = FluidCraft.resource("textures/gui/burette.png");
    private static final int TANK_X = 52, TANK_Y = 17;
    private static final int TANK_WIDTH = 16, TANK_HEIGHT = 32;

    private final ContainerBurette cont;
    private GuiNumberBox amountField;
    private final MouseRegionManager mouseRegions = new MouseRegionManager(this);

    public GuiBurette(InventoryPlayer ipl, TileBurette tile) {
        super(new ContainerBurette(ipl, tile));
        this.cont = (ContainerBurette)inventorySlots;

        mouseRegions.addRegion(TANK_X, TANK_Y, TANK_WIDTH, TANK_HEIGHT,
                new TankMouseHandler(cont.getTile().getFluidInventory(), 0));
        mouseRegions.addRegion(70, 16, 7, 7, ButtonMouseHandler.dumpTank(cont, 0));
        mouseRegions.addRegion(73, 42, 8, 16, createTransposeButton(true));
        mouseRegions.addRegion(81, 42, 8, 16, createTransposeButton(false));
    }

    @Override
    public void initGui() {
        super.initGui();
        amountField = new GuiNumberBox(fontRenderer, guiLeft + 95, guiTop + 46, 28, fontRenderer.FONT_HEIGHT, Integer.class);
        amountField.setEnableBackgroundDrawing(false);
        amountField.setMaxStringLength(4);
        amountField.setTextColor(0xffffff);
        amountField.setVisible(true);
        amountField.setFocused(true);
        amountField.setText("1000");
    }

    private ButtonMouseHandler createTransposeButton(boolean into) {
        return new ButtonMouseHandler(into ? NameConst.TT_TRANSPOSE_IN : NameConst.TT_TRANSPOSE_OUT, () -> {
            try {
                int amount = Integer.parseInt(amountField.getText());
                if (cont.canTranferFluid(into)) {
                    FluidCraft.proxy.netHandler.sendToServer(new CPacketTransposeFluid(amount, into));
                }
            } catch (NumberFormatException e) {
                // NO-OP
            }
        });
    }

    @Override
    protected void mouseClicked(int xCoord, int yCoord, int btn) throws IOException {
        if (mouseRegions.onClick(xCoord, yCoord, btn)) {
            amountField.mouseClicked(xCoord, yCoord, btn);
            super.mouseClicked(xCoord, yCoord, btn);
        }
    }

    @Override
    protected void keyTyped(final char character, final int key) throws IOException {
        if (!checkHotbarKeys(key)) {
            if ((key == 211 || key == 205 || key == 203 || key == 14 || Character.isDigit(character))
                    && amountField.textboxKeyTyped(character, key)) {
                try {
                    int amount = Integer.parseInt(amountField.getText());
                    if (amount < 0) {
                        amountField.setText("1");
                    } else {
                        amountField.setText(Long.toString(amount));
                    }
                } catch (final NumberFormatException e) {
                    // NO-OP
                }
            } else {
                super.keyTyped(character, key);
            }
        }
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEX_BG);
        drawTexturedModalRect(offsetX, offsetY, 0, 0, 176, ySize);
        amountField.drawTextBox();
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        fontRenderer.drawString(getGuiDisplayName(I18n.format(NameConst.GUI_BURETTE)), 8, 6, 0x404040);
        fontRenderer.drawString(GuiText.inventory.getLocal(), 8, ySize - 94, 0x404040);
        GlStateManager.color(1F, 1F, 1F, 1F);

        IAEFluidTank fluidInv = cont.getTile().getFluidInventory();
        FluidRenderUtils.renderFluidIntoGuiCleanly(TANK_X, TANK_Y, TANK_WIDTH, TANK_HEIGHT,
                fluidInv.getFluidInSlot(0), fluidInv.getTankProperties()[0].getCapacity());

        mouseRegions.render(mouseX, mouseY);
    }

}