package com.glodblock.github.client.render;

import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.RenderUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class ItemDropRender implements IItemRenderer {

    private final Map<String, Integer> colourCache = new HashMap<>();

    public ItemDropRender() {
        MinecraftForgeClient.registerItemRenderer(ItemAndBlockHolder.DROP, this);
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return type != ItemRenderType.FIRST_PERSON_MAP;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return type == ItemRenderType.ENTITY;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        IIcon shape = ItemAndBlockHolder.DROP.shape;
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        if (type.equals(ItemRenderType.ENTITY)) {
            GL11.glRotated(90.0D, 0.0D, 1.0D, 0.0D);
            GL11.glTranslated(-0.5D, -0.6D, 0.0D);
        }

        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationItemsTexture);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        FluidStack fluid = ItemFluidDrop.getFluidStack(item);
        if (fluid != null) {
            int RGB = getColour(fluid);
            GL11.glColor3f((RGB >> 16 & 0xFF) / 255.0F, (RGB >> 8 & 0xFF) / 255.0F, (RGB & 0xFF) / 255.0F);
        }

        if (type.equals(ItemRenderType.INVENTORY)) {
            RenderUtil.renderItemIcon(shape, 16.0D, 0.001D, 0.0F, 0.0F, -1.0F);
        } else {
            ItemRenderer.renderItemIn2D(Tessellator.instance, shape.getMaxU(), shape.getMinV(), shape.getMinU(), shape.getMaxV(), shape.getIconWidth(), shape.getIconHeight(), 0.0625F);
        }

        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public int getColour(FluidStack fluidStack) {
        Fluid fluid = fluidStack.getFluid();
        int colour = fluid.getColor(fluidStack);
        return colour != 0xFFFFFF ? colour : getColour(fluid);
    }

    public int getColour(Fluid fluid) {
        Integer cached = colourCache.get(fluid.getName());
        if (cached != null) {
            return cached;
        }
        int colour = fluid.getColor();
        if (colour == 0xFFFFFF) {
            TextureAtlasSprite sprite;
            try {
                sprite = Minecraft.getMinecraft().getTextureMapBlocks()
                    .getTextureExtry(fluid.getStillIcon().getIconName());
            } catch (NullPointerException npe) {
                colourCache.put(fluid.getName(), colour);
                return colour;
            }

            if (sprite != null && sprite.getFrameCount() > 0) {
                int[][] image = sprite.getFrameTextureData(0);
                int r = 0, g = 0, b = 0, count = 0;
                for (int[] row : image) {
                    for (int pixel : row) {
                        if (((pixel >> 24) & 0xFF) > 127) { // is alpha above 50%?
                            r += (pixel >> 16) & 0xFF;
                            g += (pixel >> 8) & 0xFF;
                            b += pixel & 0xFF;
                            ++count;
                        }
                    }
                }
                if (count > 0) {
                    // probably shouldn't need to mask each component by 0xFF
                    colour = ((r / count) << 16) | ((g / count) << 8) | (b / count);
                }
            }
        }
        colourCache.put(fluid.getName(), colour);
        return colour;
    }
}
