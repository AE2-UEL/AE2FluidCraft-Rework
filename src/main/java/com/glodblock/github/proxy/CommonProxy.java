package com.glodblock.github.proxy;

import appeng.api.AEApi;
import appeng.api.definitions.IItemDefinition;
import appeng.core.AppEng;
import appeng.core.features.ItemDefinition;
import appeng.recipes.game.DisassembleRecipe;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.tile.*;
import com.glodblock.github.handler.RegistryHandler;
import com.glodblock.github.integration.pauto.PackagedFluidCrafting;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.loader.ChannelLoader;
import com.glodblock.github.loader.FCBlocks;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.Ae2Reflect;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.NameConst;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Objects;

public class CommonProxy {

    public final RegistryHandler regHandler = createRegistryHandler();
    public final SimpleNetworkWrapper netHandler = NetworkRegistry.INSTANCE.newSimpleChannel(FluidCraft.MODID);

    public RegistryHandler createRegistryHandler() {
        return new RegistryHandler();
    }

    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(regHandler);
        FCBlocks.init(regHandler);
        FCItems.init(regHandler);
        GameRegistry.registerTileEntity(TileFluidDiscretizer.class, FluidCraft.resource(NameConst.BLOCK_FLUID_DISCRETIZER));
        GameRegistry.registerTileEntity(TileFluidPatternEncoder.class, FluidCraft.resource(NameConst.BLOCK_FLUID_PATTERN_ENCODER));
        GameRegistry.registerTileEntity(TileFluidPacketDecoder.class, FluidCraft.resource(NameConst.BLOCK_FLUID_PACKET_DECODER));
        GameRegistry.registerTileEntity(TileIngredientBuffer.class, FluidCraft.resource(NameConst.BLOCK_INGREDIENT_BUFFER));
        GameRegistry.registerTileEntity(TileLargeIngredientBuffer.class, FluidCraft.resource(NameConst.BLOCK_LARGE_INGREDIENT_BUFFER));
        GameRegistry.registerTileEntity(TileBurette.class, FluidCraft.resource(NameConst.BLOCK_BURETTE));
        GameRegistry.registerTileEntity(TileDualInterface.class, FluidCraft.resource(NameConst.BLOCK_DUAL_INTERFACE));
        GameRegistry.registerTileEntity(TileFluidLevelMaintainer.class, FluidCraft.resource(NameConst.BLOCK_FLUID_LEVEL_MAINTAINER));
        (new ChannelLoader()).run();
        if (ModAndClassUtil.AUTO_P) {
            initPackagedAutoIntegration();
        }
    }

    protected void initPackagedAutoIntegration() {
        PackagedFluidCrafting.init();
    }

    public void init(FMLInitializationEvent event) {
        regHandler.onInit();
        IRecipe disassembleRecipe = ForgeRegistries.RECIPES.getValue(new ResourceLocation(AppEng.MOD_ID, "disassemble"));
        if (disassembleRecipe instanceof DisassembleRecipe) {
            Ae2Reflect.getDisassemblyNonCellMap((DisassembleRecipe)disassembleRecipe).put(
                    createItemDefn(FCItems.DENSE_ENCODED_PATTERN),
                    AEApi.instance().definitions().materials().blankPattern());
        }
    }

    public void postInit(FMLPostInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(FluidCraft.INSTANCE, new InventoryHandler());
    }

    private static IItemDefinition createItemDefn(Item item) {
        return new ItemDefinition(Objects.requireNonNull(item.getRegistryName()).toString(), item);
    }

    public SimpleNetworkWrapper getNetHandler() {
        return netHandler;
    }

}