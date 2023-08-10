package com.glodblock.github.handler;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.*;
import com.glodblock.github.client.container.*;
import com.glodblock.github.client.model.FluidEncodedPatternModel;
import com.glodblock.github.client.model.FluidPacketModel;
import com.glodblock.github.interfaces.HasCustomModel;
import com.glodblock.github.loader.FCBlocks;
import com.glodblock.github.util.Ae2ReflectClient;
import com.glodblock.github.util.FluidRenderUtils;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;

public class ClientRegistryHandler extends RegistryHandler {

    public static final ClientRegistryHandler INSTANCE = new ClientRegistryHandler();

    @Override
    public void onInit() {
        RenderTypeLookup.setRenderLayer(FCBlocks.INGREDIENT_BUFFER, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(FCBlocks.LARGE_INGREDIENT_BUFFER, RenderType.getCutout());
        Ae2ReflectClient.registerAEGui(ContainerIngredientBuffer.TYPE, GuiIngredientBuffer::new, "/screens/ingredient_buffer.json");
        Ae2ReflectClient.registerAEGui(ContainerLargeIngredientBuffer.TYPE, GuiLargeIngredientBuffer::new, "/screens/large_ingredient_buffer.json");
        Ae2ReflectClient.registerAEGui(ContainerItemDualInterface.TYPE, GuiItemDualInterface::new, "/screens/dual_item_interface.json");
        Ae2ReflectClient.registerAEGui(ContainerFluidDualInterface.TYPE, GuiFluidDualInterface::new, "/screens/dual_fluid_interface.json");
        Ae2ReflectClient.registerAEGui(ContainerFluidPatternTerminal.TYPE, GuiFluidPatternTerminal::new, "/screens/fluid_pattern_terminal.json");
        Ae2ReflectClient.registerAEGui(ContainerFluidPacketDecoder.TYPE, GuiFluidPacketDecoder::new, "/screens/fluid_packet_decoder.json");
        Ae2ReflectClient.registerAEGui(ContainerFCPriority.TYPE, GuiFCPriority::new, "/screens/fc_priority.json");
    }

    @SubscribeEvent
    public void onRegisterModels(ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(FluidCraft.resource("fluid_encoded_pattern"), new FluidEncodedPatternModel.Loader());
        //ModelLoaderRegistry.registerLoader(new DenseCraftEncodedPatternModel.Loader());
        ModelLoaderRegistry.registerLoader(FluidCraft.resource("fluid_packet"), new FluidPacketModel.Loader());
        for (Pair<String, Block> entry : blocks) {
            registerModel(entry.getLeft(), entry.getRight().asItem());
        }
        for (Pair<String, Item> entry : items) {
            registerModel(entry.getLeft(), entry.getRight());
        }
    }

    private static void registerModel(String key, Item item) {
        ModelLoader.addSpecialModel(new ModelResourceLocation(
                item instanceof HasCustomModel ?
                        ((HasCustomModel)item).getCustomModelPath() : FluidCraft.resource(key),
                "inventory"));
    }

    @SubscribeEvent
    public void onTextureMapStitch(TextureStitchEvent event) {
        if (event.getMap().getTextureLocation().equals(PlayerContainer.LOCATION_BLOCKS_TEXTURE)) {
            FluidRenderUtils.resetCache();
        }
    }

}
