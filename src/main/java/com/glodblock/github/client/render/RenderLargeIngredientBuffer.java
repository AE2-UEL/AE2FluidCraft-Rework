package com.glodblock.github.client.render;

import com.glodblock.github.common.tile.TileLargeIngredientBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.opengl.GL11;

public class RenderLargeIngredientBuffer extends TileEntitySpecialRenderer<TileLargeIngredientBuffer> {

    private static final double d = 0.45D;

    @Override
    public void render(TileLargeIngredientBuffer tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);
        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        IItemHandler inv = tile.getInternalInventory();
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                GlStateManager.pushMatrix();
                float scale = stack.getItem() instanceof ItemBlock ? 0.36F : 0.64F;
                GlStateManager.scale(scale, scale, scale);
                GlStateManager.rotate((getWorld().getTotalWorldTime() + partialTicks) * 6F, 0F, 1F, 0F);
                RenderItem renderer = Minecraft.getMinecraft().getRenderItem();
                renderer.renderItem(stack, renderer.getItemModelWithOverrides(stack, getWorld(), null));
                GlStateManager.popMatrix();
                break;
            }
        }

        for (IFluidTankProperties tank : tile.getFluidInventory().getTankProperties()) {
            TextureAtlasSprite fluidSprite = FluidRenderUtils.prepareRender(tank.getContents());
            if (fluidSprite != null) {
                Tessellator tess = Tessellator.getInstance();
                BufferBuilder buf = tess.getBuffer();
                // not necessarily the most efficient way to draw a cube, but probably the least tedious
                GlStateManager.pushMatrix();
                drawFace(tess, buf, fluidSprite);
                for (int i = 0; i < 3; i++) {
                    GlStateManager.rotate(90F, 1F, 0F, 0F);
                    drawFace(tess, buf, fluidSprite);
                }
                GlStateManager.rotate(90F, 0F, 0F, 1F);
                drawFace(tess, buf, fluidSprite);
                GlStateManager.rotate(180F, 0F, 0F, 1F);
                drawFace(tess, buf, fluidSprite);
                GlStateManager.popMatrix();
                break;
            }
        }

        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.popMatrix();
    }

    private static void drawFace(Tessellator tess, BufferBuilder buf, TextureAtlasSprite sprite) {
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(-d, d, -d).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
        buf.pos(-d, d, d).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
        buf.pos(d, d, d).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
        buf.pos(d, d, -d).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();
        tess.draw();
    }

}
