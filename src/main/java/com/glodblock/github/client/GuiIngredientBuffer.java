package com.glodblock.github.client;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.fluids.util.IAEFluidTank;
import com.glodblock.github.client.container.ContainerIngredientBuffer;
import com.glodblock.github.handler.ButtonMouseHandler;
import com.glodblock.github.handler.TankMouseHandler;
import com.glodblock.github.util.FluidRenderUtils;
import com.glodblock.github.util.MouseRegionManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.text.ITextComponent;

public class GuiIngredientBuffer extends AEBaseScreen<ContainerIngredientBuffer> {

    private static final int TANK_X = 47, TANK_X_OFF = 22, TANK_Y = 18;
    private static final int TANK_WIDTH = 16, TANK_HEIGHT = 74;
    private final MouseRegionManager mouseRegions = new MouseRegionManager(this);

    public GuiIngredientBuffer(ContainerIngredientBuffer container, PlayerInventory playerInventory, ITextComponent title, ScreenStyle style) {
        super(container, playerInventory, title, style);
        for (int i = 0; i < 4; i++) {
            mouseRegions.addRegion(TANK_X + TANK_X_OFF * i, TANK_Y, TANK_WIDTH, TANK_HEIGHT,
                    new TankMouseHandler(container.getTile().getFluidInventory(), i));
            mouseRegions.addRegion(TANK_X + 10 + 22 * i, TANK_Y + TANK_HEIGHT + 2, 7, 7,
                    ButtonMouseHandler.dumpTank(container, i));
        }
    }

    @Override
    public boolean mouseClicked(double xCoord, double yCoord, int btn) {
        if (mouseRegions.onClick(xCoord, yCoord, btn)) {
            return super.mouseClicked(xCoord, yCoord, btn);
        }
        return true;
    }

    @Override
    public void drawFG(MatrixStack matrixStack, int offsetX, int offsetY, int mouseX, int mouseY) {
        GlStateManager.color4f(1F, 1F, 1F, 1F);
        IAEFluidTank fluidInv = container.getTile().getFluidInventory();
        assert minecraft != null;
        minecraft.getTextureManager().bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        for (int i = 0; i < 4; i++) {
            FluidRenderUtils.renderFluidIntoGui(tess, buf, TANK_X + i * TANK_X_OFF, TANK_Y, TANK_WIDTH, TANK_HEIGHT,
                    fluidInv.getFluidInSlot(i), fluidInv.getTankCapacity(i));
        }
        GlStateManager.color4f(1F, 1F, 1F, 1F);
        mouseRegions.render(matrixStack, mouseX, mouseY);
    }

}
