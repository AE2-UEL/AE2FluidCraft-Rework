package com.glodblock.github.integration.jei;

import appeng.util.ReadableNumberConverter;
import com.glodblock.github.client.render.FluidRenderUtils;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.plugins.vanilla.ingredients.fluid.FluidStackRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CubicFluidRender extends FluidStackRenderer {

    public CubicFluidRender(int capacityMb, boolean showCapacity, int width, int height, @Nullable IDrawable overlay) {
        super(capacityMb, showCapacity, width, height, overlay);
    }

    @Override
    public void render(@Nonnull Minecraft minecraft, final int xPosition, final int yPosition, @Nullable FluidStack fluidStack) {
        if (fluidStack == null)
            return;

        GlStateManager.disableBlend();

        FluidRenderUtils.renderFluidIntoGuiCleanly(xPosition, yPosition, 16, 16, fluidStack, fluidStack.amount);

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5, 0.5, 1);

        String s = ReadableNumberConverter.INSTANCE.toWideReadableForm(fluidStack.amount);

        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        fontRenderer.drawStringWithShadow(s, (xPosition + 6) * 2 - fontRenderer.getStringWidth(s) + 19, (yPosition + 11) * 2, 0xFFFFFF);

        GlStateManager.popMatrix();

        GlStateManager.enableBlend();
    }

}
