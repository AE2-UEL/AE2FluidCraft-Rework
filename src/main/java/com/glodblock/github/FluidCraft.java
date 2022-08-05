package com.glodblock.github;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import com.glodblock.github.common.Config;
import com.glodblock.github.common.storage.FluidCellHandler;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.loader.ChannelLoader;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.loader.RecipeLoader;
import com.glodblock.github.proxy.CommonProxy;
import com.glodblock.github.util.ModAndClassUtil;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@Mod(modid = FluidCraft.MODID, version = FluidCraft.VERSION, name = FluidCraft.MODNAME, dependencies = "required-after:appliedenergistics2")
public class FluidCraft {

    public static final String MODID = "GRADLETOKEN_MODID";
    public static final String VERSION = "GRADLETOKEN_VERSION";
    public static final String MODNAME = "GRADLETOKEN_MODNAME";

    @Mod.Instance(MODID)
    public static FluidCraft INSTANCE;

    @SidedProxy(clientSide = "com.glodblock.github.proxy.ClientProxy", serverSide = "com.glodblock.github.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent event) {
        Config.run();
        (new ChannelLoader()).run();
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public static void init(FMLInitializationEvent event) {
        ModAndClassUtil.init();
        proxy.init(event);
    }

    @Mod.EventHandler
    public static void postInit(FMLPostInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(FluidCraft.INSTANCE, new InventoryHandler());

        AEApi.instance().registries().cell().addCellHandler(new FluidCellHandler());
        ItemAndBlockHolder.loadSetting();

        if (!Config.removeRecipe) {
            (new RecipeLoader()).run();
        }

        if (ModAndClassUtil.isBigInterface) {
            Upgrades.PATTERN_CAPACITY.registerItem(new ItemStack(ItemAndBlockHolder.FLUID_INTERFACE), 3 );
            Upgrades.PATTERN_CAPACITY.registerItem(new ItemStack(ItemAndBlockHolder.INTERFACE), 3 );
        }
        Upgrades.CRAFTING.registerItem(new ItemStack(ItemAndBlockHolder.FLUID_INTERFACE), 1 );
        Upgrades.CRAFTING.registerItem(new ItemStack(ItemAndBlockHolder.INTERFACE), 1 );

        if (Config.fluidIOBus) {
            Upgrades.CAPACITY.registerItem(new ItemStack(ItemAndBlockHolder.FLUID_EXPORT_BUS), 2 );
            Upgrades.CAPACITY.registerItem(new ItemStack(ItemAndBlockHolder.FLUID_IMPORT_BUS), 2 );
            Upgrades.REDSTONE.registerItem(new ItemStack(ItemAndBlockHolder.FLUID_EXPORT_BUS), 1 );
            Upgrades.REDSTONE.registerItem(new ItemStack(ItemAndBlockHolder.FLUID_IMPORT_BUS), 1 );
            Upgrades.SPEED.registerItem(new ItemStack(ItemAndBlockHolder.FLUID_EXPORT_BUS), 4 );
            Upgrades.SPEED.registerItem(new ItemStack(ItemAndBlockHolder.FLUID_IMPORT_BUS), 4 );
        }

        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void onLoadComplete(FMLLoadCompleteEvent event) {
    }

    public static ResourceLocation resource(String path) {
        return new ResourceLocation(MODID, path);
    }

}
