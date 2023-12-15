package com.glodblock.github.proxy;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.definitions.IItemDefinition;
import appeng.core.AppEng;
import appeng.core.features.ItemDefinition;
import appeng.recipes.game.DisassembleRecipe;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.fake.FakeFluids;
import com.glodblock.github.common.tile.*;
import com.glodblock.github.handler.RegistryHandler;
import com.glodblock.github.integration.mek.FCGasBlocks;
import com.glodblock.github.integration.mek.FCGasItems;
import com.glodblock.github.integration.mek.FakeGases;
import com.glodblock.github.integration.opencomputer.OCInit;
import com.glodblock.github.integration.pauto.PackagedFluidCrafting;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.loader.ChannelLoader;
import com.glodblock.github.loader.FCBlocks;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.Ae2Reflect;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.NameConst;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

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
        FakeFluids.init();
        if (ModAndClassUtil.GAS) {
            FCGasItems.init(regHandler);
            FCGasBlocks.init(regHandler);
            FakeGases.init();
        }
        GameRegistry.registerTileEntity(TileFluidDiscretizer.class, FluidCraft.resource(NameConst.BLOCK_FLUID_DISCRETIZER));
        GameRegistry.registerTileEntity(TileFluidPatternEncoder.class, FluidCraft.resource(NameConst.BLOCK_FLUID_PATTERN_ENCODER));
        GameRegistry.registerTileEntity(TileFluidPacketDecoder.class, FluidCraft.resource(NameConst.BLOCK_FLUID_PACKET_DECODER));
        GameRegistry.registerTileEntity(TileIngredientBuffer.class, FluidCraft.resource(NameConst.BLOCK_INGREDIENT_BUFFER));
        GameRegistry.registerTileEntity(TileLargeIngredientBuffer.class, FluidCraft.resource(NameConst.BLOCK_LARGE_INGREDIENT_BUFFER));
        GameRegistry.registerTileEntity(TileBurette.class, FluidCraft.resource(NameConst.BLOCK_BURETTE));
        GameRegistry.registerTileEntity(TileDualInterface.class, FluidCraft.resource(NameConst.BLOCK_DUAL_INTERFACE));
        GameRegistry.registerTileEntity(TileFluidLevelMaintainer.class, FluidCraft.resource(NameConst.BLOCK_FLUID_LEVEL_MAINTAINER));
        GameRegistry.registerTileEntity(TileFluidAssembler.class, FluidCraft.resource(NameConst.BLOCK_FLUID_ASSEMBLER));
        GameRegistry.registerTileEntity(TileUltimateEncoder.class, FluidCraft.resource(NameConst.BLOCK_ULTIMATE_ENCODER));
        if (ModAndClassUtil.GAS) {
            GameRegistry.registerTileEntity(TileGasDiscretizer.class, FluidCraft.resource(NameConst.BLOCK_GAS_DISCRETIZER));
        }
        (new ChannelLoader()).run();
        if (ModAndClassUtil.AUTO_P) {
            initPackagedAutoIntegration();
        }
        if (ModAndClassUtil.OC) {
            OCInit.run();
        }
    }

    protected void initPackagedAutoIntegration() {
        PackagedFluidCrafting.init();
    }

    public void init(FMLInitializationEvent event) {
        regHandler.onInit();
        AEApi.instance().registries().wireless().registerWirelessHandler(FCItems.WIRELESS_FLUID_PATTERN_TERMINAL);
        IRecipe disassembleRecipe = ForgeRegistries.RECIPES.getValue(new ResourceLocation(AppEng.MOD_ID, "disassemble"));
        if (disassembleRecipe instanceof DisassembleRecipe) {
            Ae2Reflect.getDisassemblyNonCellMap((DisassembleRecipe)disassembleRecipe).put(
                    createItemDefn(FCItems.DENSE_ENCODED_PATTERN),
                    AEApi.instance().definitions().materials().blankPattern());
            Ae2Reflect.getDisassemblyNonCellMap((DisassembleRecipe)disassembleRecipe).put(
                    createItemDefn(FCItems.DENSE_CRAFT_ENCODED_PATTERN),
                    AEApi.instance().definitions().materials().blankPattern());
            Ae2Reflect.getDisassemblyNonCellMap((DisassembleRecipe)disassembleRecipe).put(
                    createItemDefn(FCItems.LARGE_ITEM_ENCODED_PATTERN),
                    AEApi.instance().definitions().materials().blankPattern());
        }
    }

    public void postInit(FMLPostInitializationEvent event) {
        Upgrades.PATTERN_EXPANSION.registerItem(new ItemStack(FCBlocks.DUAL_INTERFACE), 3);
        Upgrades.CRAFTING.registerItem(new ItemStack(FCBlocks.DUAL_INTERFACE), 1);
        Upgrades.CAPACITY.registerItem(new ItemStack(FCBlocks.DUAL_INTERFACE), 2);
        Upgrades.PATTERN_EXPANSION.registerItem(new ItemStack(FCItems.PART_DUAL_INTERFACE), 3);
        Upgrades.CRAFTING.registerItem(new ItemStack(FCItems.PART_DUAL_INTERFACE), 1);
        Upgrades.CRAFTING.registerItem(new ItemStack(FCItems.PART_DUAL_INTERFACE), 2);
        Upgrades.CRAFTING.registerItem(AEApi.instance().definitions().parts().fluidExportBus(), 1);
        Upgrades.MAGNET.registerItem(new ItemStack(FCItems.WIRELESS_FLUID_PATTERN_TERMINAL), 1);
        NetworkRegistry.INSTANCE.registerGuiHandler(FluidCraft.INSTANCE, new InventoryHandler());
    }

    private static IItemDefinition createItemDefn(Item item) {
        return new ItemDefinition(Objects.requireNonNull(item.getRegistryName()).toString(), item);
    }

    public SimpleNetworkWrapper getNetHandler() {
        return netHandler;
    }

}