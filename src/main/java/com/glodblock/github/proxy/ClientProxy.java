package com.glodblock.github.proxy;

import appeng.api.util.AEColor;
import com.glodblock.github.client.render.DropColourHandler;
import com.glodblock.github.client.render.RenderIngredientBuffer;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.tile.TileIngredientBuffer;
import com.glodblock.github.handler.ClientRegistryHandler;
import com.glodblock.github.handler.RegistryHandler;
import com.glodblock.github.integration.pauto.PackagedFluidCrafting;
import com.glodblock.github.loader.FCItems;
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
    public void preInit(FMLPreInitializationEvent event){
        super.preInit(event);
        MinecraftForge.EVENT_BUS.register(dropColourHandler);
        ClientRegistry.bindTileEntitySpecialRenderer(TileIngredientBuffer.class, new RenderIngredientBuffer());
    }

    @Override
    protected void initPackagedAutoIntegration() {
        super.initPackagedAutoIntegration();
        PackagedFluidCrafting.initClient();
    }

    @Override
    public void init(FMLInitializationEvent event){
        super.init(event);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler((s, i) -> {
            FluidStack fluid = ItemFluidDrop.getFluidStack(s);
            return fluid != null ? dropColourHandler.getColour(fluid) : -1;
        }, FCItems.FLUID_DROP);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler((s, i) -> {
            if (i == 0) {
                return -1;
            }
            FluidStack fluid = ItemFluidPacket.getFluidStack(s);
            return fluid != null ? fluid.getFluid().getColor(fluid) : -1;
        }, FCItems.FLUID_PACKET);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler((s, i) -> AEColor.TRANSPARENT.getVariantByTintIndex(i), FCItems.PART_FLUID_PATTERN_TERMINAL);

    }

    @Override
    public void postInit(FMLPostInitializationEvent event){
        super.postInit(event);
    }

}