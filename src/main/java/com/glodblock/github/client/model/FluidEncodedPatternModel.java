package com.glodblock.github.client.model;

import appeng.items.misc.EncodedPatternItem;
import com.glodblock.github.util.Ae2ReflectClient;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public class FluidEncodedPatternModel implements IModelGeometry<FluidEncodedPatternModel> {
    private final ResourceLocation baseModel;

    public FluidEncodedPatternModel(ResourceLocation baseModel) {
        this.baseModel = baseModel;
    }

    public static final IItemColor PATTERN_ITEM_COLOR_HANDLER = (stack, tintIndex) -> {
        EncodedPatternItem iep = (EncodedPatternItem) stack.getItem();
        ItemStack output = iep.getOutput(stack);
        if (!output.isEmpty() && Screen.hasShiftDown()) {
            return Minecraft.getInstance().getItemColors().getColor(output, tintIndex);
        }
        return 0xFFFFFF;
    };

    public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return modelGetter.apply(this.baseModel).getTextures(modelGetter, missingTextureErrors);
    }

    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
        IBakedModel baseModel = bakery.getBakedModel(this.baseModel, modelTransform, spriteGetter);
        return Ae2ReflectClient.bakeEncodedPatternModel(baseModel);
    }

    public static class Loader implements IModelLoader<FluidEncodedPatternModel> {

        @Override
        public void onResourceManagerReload(@Nonnull IResourceManager iResourceManager) {
            // NO-OP
        }

        @Nonnull
        @Override
        public FluidEncodedPatternModel read(@Nonnull JsonDeserializationContext jsonDeserializationContext, @Nonnull JsonObject jsonObject) {
            jsonObject.remove("loader");
            ResourceLocation baseModel = new ResourceLocation(JSONUtils.getString(jsonObject, "baseModel"));
            return new FluidEncodedPatternModel(baseModel);
        }
    }
}
