package com.glodblock.github.client.render;

import com.glodblock.github.common.tile.TileSimpleBuffer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class RenderIngredientBuffer extends TileEntityRenderer<TileSimpleBuffer> {

    public RenderIngredientBuffer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(@Nonnull TileSimpleBuffer tile, float partialTicks, @Nonnull MatrixStack matrixStackIn, @Nonnull IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA.param,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.param
        );
        GlStateManager.enableLighting();
        matrixStackIn.push();
        matrixStackIn.translate(0.5D, 0.25D, 0.5D);
        renderDispatcher.textureManager.bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        IItemHandler inv = tile.getInternalInventory();
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                matrixStackIn.push();
                matrixStackIn.rotate(Vector3f.YP.rotation((renderDispatcher.world.getGameTime() + partialTicks) / 20.0F));
                itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.GROUND, combinedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn);
                matrixStackIn.pop();
                break;
            }
        }

        GlStateManager.color4f(1F, 1F, 1F, 1F);
        GlStateManager.disableLighting();
        matrixStackIn.pop();
    }

}
