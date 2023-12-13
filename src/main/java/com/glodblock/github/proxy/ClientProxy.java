package com.glodblock.github.proxy;

import appeng.api.util.AEColor;
import com.glodblock.github.client.model.DenseEncodedPatternModel;
import com.glodblock.github.client.render.DropColourHandler;
import com.glodblock.github.client.render.RenderIngredientBuffer;
import com.glodblock.github.client.render.RenderLargeIngredientBuffer;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.common.tile.TileIngredientBuffer;
import com.glodblock.github.common.tile.TileLargeIngredientBuffer;
import com.glodblock.github.handler.ClientRegistryHandler;
import com.glodblock.github.handler.RegistryHandler;
import com.glodblock.github.integration.mek.FCGasItems;
import com.glodblock.github.integration.pauto.PackagedFluidCrafting;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.ModAndClassUtil;
import mekanism.api.gas.GasStack;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    private final DropColourHandler dropColourHandler = new DropColourHandler();

    @Override
    public RegistryHandler createRegistryHandler() {
        return new ClientRegistryHandler();
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        MinecraftForge.EVENT_BUS.register(dropColourHandler);
        ClientRegistry.bindTileEntitySpecialRenderer(TileIngredientBuffer.class, new RenderIngredientBuffer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileLargeIngredientBuffer.class, new RenderLargeIngredientBuffer());
    }

    @Override
    protected void initPackagedAutoIntegration() {
        super.initPackagedAutoIntegration();
        PackagedFluidCrafting.initClient();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler((s, i) -> {
            FluidStack fluid = FakeItemRegister.getStack(s);
            return fluid != null ? dropColourHandler.getColour(fluid) : 0xFFFFFFFF;
        }, FCItems.FLUID_DROP);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler((s, i) -> {
            if (i == 0) {
                return 0xFFFFFFFF;
            }
            FluidStack fluid = FakeItemRegister.getStack(s);
            return fluid != null ? fluid.getFluid().getColor(fluid) : 0xFFFFFFFF;
        }, FCItems.FLUID_PACKET);
        if (ModAndClassUtil.GAS) {
            Minecraft.getMinecraft().getItemColors().registerItemColorHandler((s, i) -> {
                GasStack gas = FakeItemRegister.getStack(s);
                return gas != null ? gas.getGas().getTint() | 0xFF000000 : 0xFFFFFFFF;
            }, FCGasItems.GAS_DROP);
            Minecraft.getMinecraft().getItemColors().registerItemColorHandler((s, i) -> {
                if (i == 0) {
                    return 0xFFFFFFFF;
                }
                GasStack gas = FakeItemRegister.getStack(s);
                return gas != null ? gas.getGas().getTint() | 0xFF000000 : 0xFFFFFFFF;
            }, FCGasItems.GAS_PACKET);
        }
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler((s, i) -> AEColor.TRANSPARENT.getVariantByTintIndex(i), FCItems.PART_FLUID_PATTERN_TERMINAL);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler((s, i) -> AEColor.TRANSPARENT.getVariantByTintIndex(i), FCItems.PART_EXTENDED_FLUID_PATTERN_TERMINAL);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(DenseEncodedPatternModel.PATTERN_ITEM_COLOR_HANDLER, FCItems.DENSE_ENCODED_PATTERN);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(DenseEncodedPatternModel.PATTERN_ITEM_COLOR_HANDLER, FCItems.DENSE_CRAFT_ENCODED_PATTERN);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(DenseEncodedPatternModel.PATTERN_ITEM_COLOR_HANDLER, FCItems.LARGE_ITEM_ENCODED_PATTERN);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event){
        super.postInit(event);
    }

}