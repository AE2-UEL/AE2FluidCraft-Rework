package com.glodblock.github.integration.jei;

import appeng.util.ReadableNumberConverter;
import com.glodblock.github.integration.mek.GasRenderUtil;
import mekanism.api.gas.GasStack;
import mekanism.client.jei.gas.GasStackRenderer;
import mezz.jei.api.gui.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CubicGasRender extends GasStackRenderer {

    public CubicGasRender(int capacityMb, boolean showCapacity, int width, int height, @Nullable IDrawable overlay) {
        super(capacityMb, showCapacity, width, height, overlay);
    }

    @Override
    public void render(@Nonnull Minecraft minecraft, final int xPosition, final int yPosition, @Nullable GasStack gasStack) {
        if (gasStack == null)
            return;

        GlStateManager.disableBlend();

        GasRenderUtil.renderGasIntoGuiCleanly(xPosition, yPosition, 16, 16, gasStack, gasStack.amount);

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5, 0.5, 1);

        String s = ReadableNumberConverter.INSTANCE.toWideReadableForm(gasStack.amount);

        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        fontRenderer.drawStringWithShadow(s, (xPosition + 6) * 2 - fontRenderer.getStringWidth(s) + 19, (yPosition + 11) * 2, 0xFFFFFF);

        GlStateManager.popMatrix();

        GlStateManager.enableBlend();
    }

}
