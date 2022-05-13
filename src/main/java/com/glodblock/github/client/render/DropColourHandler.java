package com.glodblock.github.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class DropColourHandler {

    private final Map<String, Integer> colourCache = new HashMap<>();

    @SubscribeEvent
    public void onTextureMapStitch(TextureStitchEvent event) {
        if (event.getMap() == Minecraft.getMinecraft().getTextureMapBlocks()) {
            colourCache.clear();
        }
    }

    public int getColour(FluidStack fluidStack) {
        Fluid fluid = fluidStack.getFluid();
        int colour = fluid.getColor(fluidStack);
        return colour != -1 ? colour : getColour(fluid);
    }

    public int getColour(Fluid fluid) {
        Integer cached = colourCache.get(fluid.getName());
        if (cached != null) {
            return cached;
        }
        int colour = fluid.getColor();
        if (colour == -1) {
            TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks()
                    .getTextureExtry(fluid.getStill().toString());
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
