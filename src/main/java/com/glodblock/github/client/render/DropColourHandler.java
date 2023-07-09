package com.glodblock.github.client.render;

import com.glodblock.github.util.FluidRenderUtils;
import com.glodblock.github.util.HashUtil;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;

public class DropColourHandler {

    public static final DropColourHandler INSTANCE = new DropColourHandler();
    private final Object2IntMap<Fluid> colourCache = new Object2IntLinkedOpenCustomHashMap<>(HashUtil.FLUID);

    @SubscribeEvent
    public void onTextureMapStitch(TextureStitchEvent event) {
        if (event.getMap().getTextureLocation().equals(PlayerContainer.LOCATION_BLOCKS_TEXTURE)) {
            colourCache.clear();
        }
    }

    public int getColour(FluidStack fluidStack) {
        Fluid fluid = fluidStack.getFluid();
        int colour = fluid.getAttributes().getColor(fluidStack);
        return colour != 0xFFFFFFFF ? colour : getColour(fluid);
    }

    public int getColour(Fluid fluid) {
        boolean isCached = colourCache.containsKey(fluid);
        if (isCached) {
            return colourCache.getOrDefault(fluid, 0xFFFFFFFF);
        }
        int colour = fluid.getAttributes().getColor();
        if (colour == 0xFFFFFFFF) {
            TextureAtlasSprite sprite = FluidRenderUtils.prepareRender(fluid);
            if (sprite != null && sprite.getFrameCount() > 0) {
                int r = 0, g = 0, b = 0, count = 0;
                for (int row = 0; row < sprite.getHeight(); row ++)
                    for (int col = 0; col < sprite.getWidth(); col ++) {
                        int pixel = sprite.getPixelRGBA(0, row, col);
                        if (((pixel >>> 24) & 0xFF) > 127) { // is alpha above 50%?
                            r += (pixel) & 0xFF;
                            g += (pixel >>> 8) & 0xFF;
                            b += (pixel >>> 16) & 0xFF;
                            ++count;
                        }
                    }
                if (count > 0) {
                    colour = ((r / count) << 16) | ((g / count) << 8) | (b / count);
                }
            }
        }
        colourCache.put(fluid, colour);
        return colour;
    }

}
