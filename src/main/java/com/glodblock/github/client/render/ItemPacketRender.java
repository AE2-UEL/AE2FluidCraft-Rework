package com.glodblock.github.client.render;

import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.RenderUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ItemPacketRender implements IItemRenderer {

    public ItemPacketRender() {
        MinecraftForgeClient.registerItemRenderer(ItemAndBlockHolder.PACKET, this);
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return type == ItemRenderType.INVENTORY;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return false;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        if (item == null || item.getItem() == null || !(item.getItem() instanceof ItemFluidPacket))
            return;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        FluidStack fluid = ItemFluidPacket.getFluidStack(item);
        int RGB = 0xFFFFFF;
        if (fluid != null) {
            RGB = fluid.getFluid().getColor(fluid);
        }
        IIcon icon = fluid == null ? FluidRegistry.WATER.getStillIcon() : fluid.getFluid().getStillIcon();

        if (RGB != 0xFFFFFF) {
            GL11.glColor3f((RGB >> 16 & 0xFF) / 255.0F, (RGB >> 8 & 0xFF) / 255.0F, (RGB & 0xFF) / 255.0F);
        }

        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        // draw a simple rectangle for the inventory icon
        final float x_min = icon.getMinU();
        final float x_max = icon.getMaxU();
        final float y_min = icon.getMinV();
        final float y_max = icon.getMaxV();
        tess.addVertexWithUV( 0, 16, 0, x_min, y_max);
        tess.addVertexWithUV(16, 16, 0, x_max, y_max);
        tess.addVertexWithUV(16,  0, 0, x_max, y_min);
        tess.addVertexWithUV( 0,  0, 0, x_min, y_min);
        tess.draw();

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
    }

}
