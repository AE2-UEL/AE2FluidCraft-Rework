package com.glodblock.github.client.model;

import appeng.items.misc.ItemEncodedPattern;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.util.Ae2ReflectClient;
import com.glodblock.github.util.NameConst;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.common.model.IModelState;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

public class LargeItemEncodedPatternModel implements IModel {

    private static final ResourceLocation BASE_MODEL = FluidCraft.resource("item/large_item_encoded_pattern");

    @Override
    @Nonnull
    public Collection<ResourceLocation> getDependencies() {
        return Collections.singletonList(BASE_MODEL);
    }

    public static final IItemColor PATTERN_ITEM_COLOR_HANDLER = (stack, tintIndex) -> {
        ItemEncodedPattern iep = (ItemEncodedPattern) stack.getItem();
        ItemStack output = iep.getOutput(stack);
        if (!output.isEmpty() && isShiftKeyDown()) {
            return Minecraft.getMinecraft().getItemColors().colorMultiplier(output, tintIndex);
        }
        return 0xFFFFFF;
    };

    private static boolean isShiftKeyDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
    }

    // adapted from ae2's ItemEncodedPatternModel#bake
    @Override
    @Nonnull
    public IBakedModel bake(@Nonnull IModelState state, @Nonnull VertexFormat format, @Nonnull Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        IBakedModel baseModel;
        try {
            baseModel = ModelLoaderRegistry.getModel(BASE_MODEL).bake(state, format, bakedTextureGetter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Ae2ReflectClient.bakeEncodedPatternModel(baseModel, PerspectiveMapWrapper.getTransforms(state));
    }

    public static class Loader implements ICustomModelLoader {

        @Override
        public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {
            // NO-OP
        }

        @Override
        public boolean accepts(ResourceLocation modelLocation) {
            // modelLocation will probably be a ModelResourceLocation, so using compareTo lets us bypass the
            // ModelResourceLocation equality behaviour and fall back to that of ResourceLocation
            return modelLocation.compareTo(NameConst.MODEL_LARGE_ITEM_ENCODED_PATTERN) == 0;
        }

        @Override
        @Nonnull
        public IModel loadModel(@Nonnull ResourceLocation modelLocation) {
            return new LargeItemEncodedPatternModel();
        }

    }

}
