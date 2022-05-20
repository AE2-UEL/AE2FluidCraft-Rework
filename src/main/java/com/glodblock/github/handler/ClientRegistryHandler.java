package com.glodblock.github.handler;

import appeng.api.AEApi;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.model.DenseEncodedPatternModel;
import com.glodblock.github.client.model.FluidPacketModel;
import com.glodblock.github.common.part.PartDualInterface;
import com.glodblock.github.common.part.PartExtendedFluidPatternTerminal;
import com.glodblock.github.common.part.PartFluidPatternTerminal;
import com.glodblock.github.interfaces.HasCustomModel;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;

public class ClientRegistryHandler extends RegistryHandler {

    @SubscribeEvent
    public void onRegisterModels(ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(new DenseEncodedPatternModel.Loader());
        ModelLoaderRegistry.registerLoader(new FluidPacketModel.Loader());
        for (Pair<String, Block> entry : blocks) {
            registerModel(entry.getLeft(), Item.getItemFromBlock(entry.getRight()));
        }
        for (Pair<String, Item> entry : items) {
            registerModel(entry.getLeft(), entry.getRight());
        }
        AEApi.instance().registries().partModels().registerModels(PartDualInterface.MODELS);
        AEApi.instance().registries().partModels().registerModels(PartFluidPatternTerminal.MODELS);
        AEApi.instance().registries().partModels().registerModels(PartExtendedFluidPatternTerminal.MODELS);
    }

    private static void registerModel(String key, Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(
                item instanceof HasCustomModel ? ((HasCustomModel)item).getCustomModelPath() : FluidCraft.resource(key),
                "inventory"));
    }

}
