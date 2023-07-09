package com.glodblock.github.client.model;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.*;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.VanillaResourceType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static net.minecraftforge.fluids.FluidAttributes.BUCKET_VOLUME;

// Adapted from https://github.com/CoFH/ThermalInnovation/blob/1.16.5/src/main/java/cofh/thermal/innovation/client/model/FluidReservoirItemModel.java
public class FluidPacketModel implements IModelGeometry<FluidPacketModel> {

    // minimal Z offset to prevent depth-fighting
    private static final float NORTH_Z_FLUID = 7.498F / 16F;
    private static final float SOUTH_Z_FLUID = 8.502F / 16F;

    @Nonnull
    private final FluidStack fluidStack;
    private final boolean isDisplay;

    public FluidPacketModel(@Nonnull FluidStack fluidStack, boolean display) {
        this.fluidStack = fluidStack;
        this.isDisplay = display;
    }

    public FluidPacketModel set(@Nonnull FluidStack fluidStack, boolean display) {
        return new FluidPacketModel(fluidStack, display);
    }

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {

        RenderMaterial particleLocation = owner.isTexturePresent("particle") ? owner.resolveTexture("particle") : null;
        RenderMaterial fluidMaskLocation = owner.resolveTexture("layer0");
        RenderMaterial background = owner.resolveTexture("layer1");

        IModelTransform transformsFromModel = owner.getCombinedTransform();
        Fluid fluid = fluidStack.getFluid();

        TextureAtlasSprite fluidSprite;
        if (fluidStack.isEmpty()) {
            fluidSprite = spriteGetter.apply(ForgeHooksClient.getBlockMaterial(Fluids.WATER.getAttributes().getStillTexture()));
        } else {
            fluidSprite = spriteGetter.apply(ForgeHooksClient.getBlockMaterial(fluid.getAttributes().getStillTexture()));
        }
        ImmutableMap<ItemCameraTransforms.TransformType, TransformationMatrix> transformMap = PerspectiveMapWrapper.getTransforms(new ModelTransformComposition(transformsFromModel, modelTransform));

        TextureAtlasSprite particleSprite = particleLocation != null ? spriteGetter.apply(particleLocation) : null;
        if (particleSprite == null) particleSprite = fluidSprite;
        if (particleSprite == null) particleSprite = spriteGetter.apply(background);

        TransformationMatrix transform = modelTransform.getRotation();
        ItemMultiLayerBakedModel.Builder builder = ItemMultiLayerBakedModel.builder(owner, particleSprite, new ContainedFluidOverrideHandler(bakery, owner, this), transformMap);

        if (!isDisplay) {
            builder.addQuads(ItemLayerModel.getLayerRenderType(false), ItemLayerModel.getQuadsForSprite(0, spriteGetter.apply(background), transform));
        }

        if (fluidSprite != null) {
            TextureAtlasSprite templateSprite = spriteGetter.apply(fluidMaskLocation);
            int luminosity = fluid.getAttributes().getLuminosity(fluidStack);
            int color = fluid.getAttributes().getColor(fluidStack);
            if (isDisplay) {
                builder.addQuads(ItemLayerModel.getLayerRenderType(luminosity > 0), ItemTextureQuadConverter.genQuad(transform, 0F, 0F, 16F, 16F, NORTH_Z_FLUID, fluidSprite, Direction.NORTH, color, 2, luminosity));
                builder.addQuads(ItemLayerModel.getLayerRenderType(luminosity > 0), ItemTextureQuadConverter.genQuad(transform, 0F, 0F, 16F, 16F, SOUTH_Z_FLUID, fluidSprite, Direction.SOUTH, color, 2, luminosity));
            } else if (templateSprite != null) {
                builder.addQuads(ItemLayerModel.getLayerRenderType(luminosity > 0), ItemTextureQuadConverter.convertTexture(transform, templateSprite, fluidSprite, NORTH_Z_FLUID, Direction.NORTH, color, 2, luminosity));
                builder.addQuads(ItemLayerModel.getLayerRenderType(luminosity > 0), ItemTextureQuadConverter.convertTexture(transform, templateSprite, fluidSprite, SOUTH_Z_FLUID, Direction.SOUTH, color, 2, luminosity));
            }
        }
        builder.setParticle(particleSprite);
        return builder.build();
    }

    @Override
    public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        Set<RenderMaterial> texs = Sets.newHashSet();
        if (owner.isTexturePresent("particle")) {
            texs.add(owner.resolveTexture("particle"));
        }
        if (owner.isTexturePresent("fluid_mask")) {
            texs.add(owner.resolveTexture("fluid_mask"));
        }
        if (owner.isTexturePresent("background")) {
            texs.add(owner.resolveTexture("background"));
        }
        return texs;
    }

    public static class Loader implements IModelLoader<FluidPacketModel> {

        @Override
        public IResourceType getResourceType() {
            return VanillaResourceType.MODELS;
        }

        @Override
        public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {
            // NO-OP
        }

        @Override
        public void onResourceManagerReload(@Nonnull IResourceManager resourceManager, @Nonnull Predicate<IResourceType> resourcePredicate) {
            // NO-OP
        }

        @Nonnull
        @Override
        public FluidPacketModel read(@Nonnull JsonDeserializationContext deserializationContext, JsonObject modelContents) {
            FluidStack stack = FluidStack.EMPTY;
            if (modelContents.has("fluid")) {
                ResourceLocation fluidName = new ResourceLocation(modelContents.get("fluid").getAsString());
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidName);
                if (fluid != null) {
                    stack = new FluidStack(fluid, BUCKET_VOLUME);
                }
            }
            // create new model with correct liquid
            return new FluidPacketModel(stack, false);
        }

    }

    private static final class ContainedFluidOverrideHandler extends ItemOverrideList {

        private final Object2ObjectMap<Pair<ResourceLocation, Boolean>, IBakedModel> cache = new Object2ObjectOpenHashMap<>(); // contains all the baked models since they'll never change
        private final ModelBakery bakery;
        private final IModelConfiguration owner;
        private final FluidPacketModel parent;

        private ContainedFluidOverrideHandler(ModelBakery bakery, IModelConfiguration owner, FluidPacketModel parent) {
            this.bakery = bakery;
            this.owner = owner;
            this.parent = parent;
        }

        @Override
        public IBakedModel getOverrideModel(@Nonnull IBakedModel originalModel, @Nonnull ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity) {
            FluidStack fluidStack = ItemFluidPacket.getFluidStack(stack);
            boolean display = ItemFluidPacket.isDisplay(stack);
            Pair<ResourceLocation, Boolean> p = new Pair<>(fluidStack.getFluid().getRegistryName(), display);
            if (!cache.containsKey(p)) {
                FluidPacketModel unbaked = this.parent.set(fluidStack, display);
                IBakedModel bakedModel = unbaked.bake(owner, bakery, ModelLoader.defaultTextureGetter(), ModelRotation.X0_Y0, this, FluidCraft.resource("fluid_packet_override"));
                cache.put(p, bakedModel);
                return bakedModel;
            }
            return cache.get(p);
        }

    }

}
