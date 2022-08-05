package com.glodblock.github.client.render;

import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.RenderUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
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
        return type != ItemRenderType.FIRST_PERSON_MAP;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return type == ItemRenderType.ENTITY;
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

        if (type.equals(ItemRenderType.INVENTORY)) {
            RenderUtil.renderItemIcon(icon, 16.0D, 0.001D, 0.0F, 0.0F, -1.0F);
        } else {
            ItemRenderer.renderItemIn2D(Tessellator.instance, icon.getMaxU(), icon.getMinV(), icon.getMinU(), icon.getMaxV(), icon.getIconWidth(), icon.getIconHeight(), 0.0625F);
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
    }

}
