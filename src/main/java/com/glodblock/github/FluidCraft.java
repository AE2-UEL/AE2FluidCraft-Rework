package com.glodblock.github;

import com.glodblock.github.proxy.CommonProxy;
import com.glodblock.github.util.ModAndClassUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = FluidCraft.MODID, version = FluidCraft.VERSION, useMetadata = true)
public class FluidCraft {

    public static final String MODID = "ae2fc";
    public static final String VERSION = "2.0.0-r";

    @Mod.Instance(MODID)
    public static FluidCraft INSTANCE;

    @SidedProxy(clientSide = "com.glodblock.github.proxy.ClientProxy", serverSide = "com.glodblock.github.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        ModAndClassUtil.init();
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    public static ResourceLocation resource(String path) {
        return new ResourceLocation(MODID, path);
    }

}
