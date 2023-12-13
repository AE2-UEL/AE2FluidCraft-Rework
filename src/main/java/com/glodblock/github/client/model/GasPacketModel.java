package com.glodblock.github.client.model;

import com.glodblock.github.common.item.ItemGasPacket;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.util.NameConst;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.common.MekanismFluids;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class GasPacketModel extends FluidPacketModel {

    @Override
    @Nonnull
    public IBakedModel bake(@Nonnull IModelState state, @Nonnull VertexFormat format, @Nonnull Function<ResourceLocation, TextureAtlasSprite> textureBakery) {
        return new BakedGasPacketModel(state, format);
    }

    public static class Loader implements ICustomModelLoader {

        @Override
        public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {
            // NO-OP
        }

        @Override
        public boolean accepts(ResourceLocation modelLocation) {
            return modelLocation.compareTo(NameConst.MODEL_GAS_PACKET) == 0;
        }

        @Override
        @Nonnull
        public IModel loadModel(@Nonnull ResourceLocation modelLocation) {
            return new GasPacketModel();
        }

    }

    protected static class BakedGasPacketModel extends BakedFluidPacketModel {

        public BakedGasPacketModel(IModelState modelState, VertexFormat vertexFormat) {
            super(modelState, vertexFormat);
        }

        protected OverrideCache genOverrides() {
            return new OverrideCache();
        }

        protected OverrideCache.OverrideModel genDefaultOverrides() {
            return ((OverrideCache) this.overrides).resolve(new GasStack(MekanismFluids.Hydrogen, Fluid.BUCKET_VOLUME));
        }

        protected class OverrideCache extends ItemOverrideList {

            private final Cache<Gas, OverrideCache.OverrideModel> cache = CacheBuilder.newBuilder()
                    .maximumSize(1000)
                    .expireAfterWrite(5, TimeUnit.MINUTES)
                    .build();

            OverrideCache() {
                super(Collections.emptyList());
            }

            @Override
            @Nonnull
            public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, ItemStack stack,
                                               @Nullable World world, @Nullable EntityLivingBase entity) {
                if (!(stack.getItem() instanceof ItemGasPacket)) {
                    return originalModel;
                }
                GasStack gas = FakeItemRegister.getStack(stack);
                return gas != null ? resolve(gas) : originalModel;
            }

            OverrideCache.OverrideModel resolve(GasStack gas) {
                try {
                    return cache.get(gas.getGas(), () -> new OverrideCache.OverrideModel(gas));
                } catch (ExecutionException e) {
                    throw new IllegalStateException(e);
                }
            }

            class OverrideModel implements IBakedModel {

                private final TextureAtlasSprite texture;
                private final List<BakedQuad> quads;

                OverrideModel(GasStack gasStack) {
                    this.texture = gasStack.getGas().getSprite();
                    this.quads = ItemLayerModel.getQuadsForSprite(1, texture, vertexFormat, modelTransform);
                }

                @Override
                @Nonnull
                public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
                    return quads;
                }

                @Override
                public boolean isAmbientOcclusion() {
                    return false;
                }

                @Override
                public boolean isGui3d() {
                    return false;
                }

                @Override
                public boolean isBuiltInRenderer() {
                    return false;
                }

                @Override
                @Nonnull
                public TextureAtlasSprite getParticleTexture() {
                    return texture;
                }

                @Override
                @Nonnull
                @SuppressWarnings("deprecation")
                public ItemCameraTransforms getItemCameraTransforms() {
                    return CAMERA_TRANSFORMS;
                }

                @Override
                @Nonnull
                public ItemOverrideList getOverrides() {
                    return OverrideCache.this;
                }

            }

        }

    }

}
