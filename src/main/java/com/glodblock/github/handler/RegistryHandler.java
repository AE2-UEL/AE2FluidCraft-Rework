package com.glodblock.github.handler;

import appeng.block.AEBaseBlockItem;
import appeng.block.AEBaseTileBlock;
import appeng.core.Api;
import appeng.core.features.ActivityState;
import appeng.core.features.BlockStackSrc;
import appeng.tile.AEBaseTileEntity;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.container.*;
import com.glodblock.github.common.part.PartDualInterface;
import com.glodblock.github.common.part.PartFluidPatternTerminal;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.FCUtil;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class RegistryHandler {

    public static final RegistryHandler INSTANCE = new RegistryHandler();
    protected final List<Pair<String, Block>> blocks = new ArrayList<>();
    protected final List<Pair<String, Item>> items = new ArrayList<>();
    protected final List<Pair<String, TileEntityType<?>>> tiles = new ArrayList<>();

    public void block(String name, Block block) {
        blocks.add(Pair.of(name, block));
        if (block instanceof AEBaseTileBlock) {
            AEBaseTileBlock<?> tileBlock = (AEBaseTileBlock<?>) block;
            tile(name, tileBlock.getTileEntityClass(), block);
        }
    }

    public void item(String name, Item item) {
        items.add(Pair.of(name, item));
    }

    public void tile(String name, Class<? extends TileEntity> clazz, Block block) {
        tiles.add(Pair.of(name, FCUtil.getTileType(clazz, block)));
    }

    @SubscribeEvent
    public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        for (Pair<String, Block> entry : blocks) {
            String key = entry.getLeft();
            Block block = entry.getRight();
            block.setRegistryName(key);
            event.getRegistry().register(block);
        }
    }

    @SubscribeEvent
    public void onRegisterItems(RegistryEvent.Register<Item> event) {
        for (Pair<String, Block> entry : blocks) {
            event.getRegistry().register(initItem(entry.getLeft(), new AEBaseBlockItem(entry.getRight(), new Item.Properties().group(FCItems.TAB_AE2FC))));
        }
        for (Pair<String, Item> entry : items) {
            event.getRegistry().register(initItem(entry.getLeft(), entry.getRight()));
        }
        this.registerPartModel();
    }

    @SubscribeEvent
    public void onRegisterTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        for (Pair<String, TileEntityType<?>> entry : tiles) {
            String key = entry.getLeft();
            TileEntityType<?> tile = entry.getRight();
            tile.setRegistryName(key);
            event.getRegistry().register(tile);
        }
    }

    @SubscribeEvent
    public void onRegisterContainerTypes(RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().register(ContainerIngredientBuffer.TYPE);
        event.getRegistry().register(ContainerLargeIngredientBuffer.TYPE);
        event.getRegistry().register(ContainerItemDualInterface.TYPE);
        event.getRegistry().register(ContainerFluidPatternTerminal.TYPE);
        event.getRegistry().register(ContainerFluidDualInterface.TYPE);
        event.getRegistry().register(ContainerFluidPacketDecoder.TYPE);
        event.getRegistry().register(ContainerFCPriority.TYPE);
    }

    private static Item initItem(String key, Item item) {
        item.setRegistryName(key);
        return item;
    }

    public void onInit() {
        for (Pair<String, Block> entry : blocks) {
            Block block = ForgeRegistries.BLOCKS.getValue(FluidCraft.resource(entry.getKey()));
            if (block instanceof AEBaseTileBlock) {
                AEBaseTileEntity.registerTileItem(
                        ((AEBaseTileBlock<?>) block).getTileEntityClass(),
                        new BlockStackSrc(block, ActivityState.Enabled)
                );
            }
        }
    }

    private void registerPartModel() {
        Api.instance().registries().partModels().registerModels(PartDualInterface.MODELS);
        Api.instance().registries().partModels().registerModels(PartFluidPatternTerminal.MODELS);
    }

}