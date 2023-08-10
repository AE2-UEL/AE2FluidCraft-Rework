package com.glodblock.github.util;

import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.me.common.StackSizeRenderer;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidRenderUtils {

    private final static Object2ObjectMap<Fluid, TextureAtlasSprite> cache = new Object2ObjectOpenHashMap<>();

    public static void resetCache() {
        cache.clear();
    }

    @Nullable
    public static TextureAtlasSprite prepareRender(@Nullable Fluid fluid) {
        if (fluid == null || fluid == Fluids.EMPTY) {
            return null;
        }
        return cache.computeIfAbsent(
                fluid,
                f -> Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE)
                        .apply(f.getAttributes().getStillTexture())
        );
    }

    @Nullable
    public static TextureAtlasSprite prepareRender(@Nonnull FluidStack fluidStack) {
        if (!fluidStack.isEmpty()) {
            return prepareRender(fluidStack.getFluid());
        }
        return null;
    }

    private static void doRenderFluid(Tessellator tess, BufferBuilder buf, int color, int x, int y, int width, int height,
                                      TextureAtlasSprite sprite, double fraction) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA.param,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.param
        );
        int fluidHeight = Math.round(height * (float)Math.min(1D, Math.max(0D, fraction)));
        double x2 = x + width;
        while (fluidHeight > 0) {
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
            float y1 = y + height - fluidHeight, y2 = y1 + Math.min(fluidHeight, width);
            float u1 = sprite.getMinU(), v1 = sprite.getMinV(), u2 = sprite.getMaxU(), v2 = sprite.getMaxV();
            if (fluidHeight < width) {
                v2 = v1 + (v2 - v1) * (fluidHeight / (float)width);
                fluidHeight = 0;
            } else {
                //noinspection SuspiciousNameCombination
                fluidHeight -= width;
            }
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;
            int a = (color >> 24) & 0xFF;
            buf.pos(x, y1, 0D).color(r, g, b, a).tex(u1, v1).endVertex();
            buf.pos(x, y2, 0D).color(r, g, b, a).tex(u1, v2).endVertex();
            buf.pos(x2, y2, 0D).color(r, g, b, a).tex(u2, v2).endVertex();
            buf.pos(x2, y1, 0D).color(r, g, b, a).tex(u2, v1).endVertex();
            tess.draw();
        }
    }

    public static void renderFluidIntoGuiCleanly(int x, int y, int width, int height,
                                                 @Nonnull FluidStack fluidStack, int capacity) {
        Minecraft.getInstance().getTextureManager().bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
        Tessellator tess = Tessellator.getInstance();
        renderFluidIntoGui(tess, tess.getBuffer(), x, y, width, height, fluidStack, capacity);
    }

    public static boolean renderFluidIntoGuiSlot(Slot slot, @Nonnull FluidStack fluid,
                                                 StackSizeRenderer stackSizeRenderer, FontRenderer fontRenderer) {
        if (fluid.isEmpty()) {
            return false;
        }
        renderFluidIntoGuiCleanly(slot.xPos, slot.yPos, 16, 16, fluid, fluid.getAmount());
        stackSizeRenderer.renderStackSize(fontRenderer, fluid.getAmount(), false, slot.xPos, slot.yPos);
        return true;
    }

    public static void renderFluidIntoGui(Tessellator tess, BufferBuilder buf, int x, int y, int width, int height,
                                          @Nullable IAEFluidStack aeFluidStack, int capacity) {
        if (aeFluidStack != null) {
            TextureAtlasSprite sprite = FluidRenderUtils.prepareRender(aeFluidStack.getFluidStack());
            if (sprite != null) {
                doRenderFluid(tess, buf, aeFluidStack.getFluid().getAttributes().getColor(), x, y, width, height, sprite, aeFluidStack.getStackSize() / (double)capacity);
            }
        }
    }

    public static void renderFluidIntoGui(Tessellator tess, BufferBuilder buf, int x, int y, int width, int height,
                                          @Nonnull FluidStack fluidStack, int capacity) {
        if (!fluidStack.isEmpty()) {
            TextureAtlasSprite sprite = FluidRenderUtils.prepareRender(fluidStack);
            if (sprite != null) {
                doRenderFluid(tess, buf, fluidStack.getFluid().getAttributes().getColor(), x, y, width, height, sprite, fluidStack.getAmount() / (double)capacity);
            }
        }
    }

    public static boolean renderFluidPacketIntoGuiSlot(Slot slot, ItemStack stack,
                                                       StackSizeRenderer stackSizeRenderer, FontRenderer fontRenderer) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemFluidPacket
                && renderFluidIntoGuiSlot(slot, ItemFluidPacket.getFluidStack(stack), stackSizeRenderer, fontRenderer);
    }

}
