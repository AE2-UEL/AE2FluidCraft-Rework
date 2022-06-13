package com.glodblock.github;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import com.glodblock.github.loader.FCBlocks;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.proxy.CommonProxy;
import com.glodblock.github.util.ModAndClassUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import org.apache.logging.log4j.Logger;

@Mod(modid = FluidCraft.MODID, version = FluidCraft.VERSION, useMetadata = true)
public class FluidCraft {

    public static final String MODID = "ae2fc";
    public static final String VERSION = "2.3.1-r";

    @Mod.Instance(MODID)
    public static FluidCraft INSTANCE;

    @SidedProxy(clientSide = "com.glodblock.github.proxy.ClientProxy", serverSide = "com.glodblock.github.proxy.CommonProxy")
    public static CommonProxy proxy;

    public static Logger log;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        log = event.getModLog();
        ModAndClassUtil.init();
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        Upgrades.PATTERN_EXPANSION.registerItem(new ItemStack(FCBlocks.DUAL_INTERFACE), 3 );
        Upgrades.CRAFTING.registerItem(new ItemStack(FCBlocks.DUAL_INTERFACE), 1 );
        Upgrades.PATTERN_EXPANSION.registerItem(new ItemStack(FCItems.PART_DUAL_INTERFACE), 3 );
        Upgrades.CRAFTING.registerItem(new ItemStack(FCItems.PART_DUAL_INTERFACE), 1 );
        Upgrades.CRAFTING.registerItem( AEApi.instance().definitions().parts().fluidExportBus(), 1 );
        proxy.postInit(event);
    }

    public static ResourceLocation resource(String path) {
        return new ResourceLocation(MODID, path);
    }

}
