package com.glodblock.github.client;

import appeng.client.gui.AEBaseGui;
import appeng.core.localization.GuiText;
import appeng.fluids.util.IAEFluidTank;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.container.ContainerIngredientBuffer;
import com.glodblock.github.client.render.FluidRenderUtils;
import com.glodblock.github.common.tile.TileIngredientBuffer;
import com.glodblock.github.handler.ButtonMouseHandler;
import com.glodblock.github.handler.TankMouseHandler;
import com.glodblock.github.util.MouseRegionManager;
import com.glodblock.github.util.NameConst;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GuiIngredientBuffer  extends AEBaseGui {

    private static final ResourceLocation TEX_BG = FluidCraft.resource("textures/gui/ingredient_buffer.png");
    private static final int TANK_X = 47, TANK_X_OFF = 22, TANK_Y = 18;
    private static final int TANK_WIDTH = 16, TANK_HEIGHT = 74;

    private final ContainerIngredientBuffer cont;
    private final MouseRegionManager mouseRegions = new MouseRegionManager(this);

    public GuiIngredientBuffer(InventoryPlayer ipl, TileIngredientBuffer tile) {
        super(new ContainerIngredientBuffer(ipl, tile));
        this.cont = (ContainerIngredientBuffer)inventorySlots;
        this.ySize = 222;
        for (int i = 0; i < 4; i++) {
            mouseRegions.addRegion(TANK_X + TANK_X_OFF * i, TANK_Y, TANK_WIDTH, TANK_HEIGHT,
                    new TankMouseHandler(cont.getTile().getFluidInventory(), i));
            mouseRegions.addRegion(TANK_X + 10 + 22 * i, TANK_Y + TANK_HEIGHT + 2, 7, 7,
                    ButtonMouseHandler.dumpTank(cont, i));
        }
    }

    @Override
    protected void mouseClicked(int xCoord, int yCoord, int btn) throws IOException {
        if (mouseRegions.onClick(xCoord, yCoord, btn)) {
            super.mouseClicked(xCoord, yCoord, btn);
        }
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEX_BG);
        drawTexturedModalRect(offsetX, offsetY, 0, 0, 176, ySize);
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        fontRenderer.drawString(getGuiDisplayName(I18n.format(NameConst.GUI_INGREDIENT_BUFFER)), 8, 6, 0x404040);
        fontRenderer.drawString(GuiText.inventory.getLocal(), 8, ySize - 94, 0x404040);
        GlStateManager.color(1F, 1F, 1F, 1F);

        IAEFluidTank fluidInv = cont.getTile().getFluidInventory();
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        for (int i = 0; i < 4; i++) {
            FluidRenderUtils.renderFluidIntoGui(tess, buf, TANK_X + i * TANK_X_OFF, TANK_Y, TANK_WIDTH, TANK_HEIGHT,
                    fluidInv.getFluidInSlot(i), fluidInv.getTankProperties()[i].getCapacity());
        }
        GlStateManager.color(1F, 1F, 1F, 1F);

        mouseRegions.render(mouseX, mouseY);
    }

}
