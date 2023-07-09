package com.glodblock.github;

import appeng.api.config.Upgrades;
import appeng.api.definitions.IItemDefinition;
import appeng.api.features.AEFeature;
import appeng.api.util.AEColor;
import appeng.core.Api;
import appeng.core.features.ItemDefinition;
import appeng.recipes.game.DisassembleRecipe;
import com.glodblock.github.client.model.FluidEncodedPatternModel;
import com.glodblock.github.client.render.DropColourHandler;
import com.glodblock.github.client.render.RenderIngredientBuffer;
import com.glodblock.github.common.block.BlockIngredientBuffer;
import com.glodblock.github.common.block.BlockLargeIngredientBuffer;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.handler.ClientRegistryHandler;
import com.glodblock.github.handler.RegistryHandler;
import com.glodblock.github.loader.ChannelLoader;
import com.glodblock.github.loader.FCBlocks;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.Ae2Reflect;
import com.glodblock.github.util.FCUtil;
import com.glodblock.github.util.ModAndClassUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;

@Mod(FluidCraft.MODID)
public class FluidCraft {

    public static final String MODID = "ae2fc";

    public static Logger log;
    public static FluidCraft INSTANCE;
    public FluidCraft() {
        assert INSTANCE == null;
        INSTANCE = this;
        FCItems.init(RegistryHandler.INSTANCE);
        FCBlocks.init(RegistryHandler.INSTANCE);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> bus.register(ClientRegistryHandler.INSTANCE));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> bus.register(DropColourHandler.INSTANCE));
        bus.register(RegistryHandler.INSTANCE);
        bus.addListener(this::commonSetup);
        bus.addListener(this::clientSetup);
        bus.addListener(this::imcWait);
        bus.addListener(this::imcProcess);
        bus.addListener(this::finish);
        ModAndClassUtil.init();
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        log = LogManager.getLogger();
        ChannelLoader.load();
        if (ModAndClassUtil.AUTO_P) {
            //PackagedFluidCrafting.init();
        }
        RegistryHandler.INSTANCE.onInit();
        IRecipeSerializer<DisassembleRecipe> disassembleRecipe = DisassembleRecipe.SERIALIZER;
        if (disassembleRecipe instanceof SpecialRecipeSerializer) {
            Map<IItemDefinition, IItemDefinition> map = Ae2Reflect.getDisassemblyNonCellMap(
                    (DisassembleRecipe) Ae2Reflect.getRecipeFactory((SpecialRecipeSerializer<?>)disassembleRecipe).apply(
                            new ResourceLocation("appliedenergistics2", "disassemble")
                    )
            );
            map.put(
                    createItemDefn(FCItems.DENSE_ENCODED_PATTERN, AEFeature.PATTERNS),
                    Api.instance().definitions().materials().blankPattern()
            );
        }
        Upgrades.CRAFTING.registerItem(FCBlocks.DUAL_INTERFACE, 1);
        Upgrades.CRAFTING.registerItem(FCItems.PART_DUAL_INTERFACE, 1);
    }

    public void clientSetup(FMLClientSetupEvent event) {
        ClientRegistryHandler.INSTANCE.onInit();
        ClientRegistry.bindTileEntityRenderer(FCUtil.getTileType(BlockIngredientBuffer.TileIngredientBuffer.class, FCBlocks.INGREDIENT_BUFFER), RenderIngredientBuffer::new);
        ClientRegistry.bindTileEntityRenderer(FCUtil.getTileType(BlockLargeIngredientBuffer.TileLargeIngredientBuffer.class, FCBlocks.LARGE_INGREDIENT_BUFFER), RenderIngredientBuffer::new);

        Minecraft.getInstance().getItemColors().register((s, i) -> {
            FluidStack fluid = ItemFluidDrop.getFluidStack(s);
            return !fluid.isEmpty() ? DropColourHandler.INSTANCE.getColour(fluid) : 0xFFFFFFFF;
        }, FCItems.FLUID_DROP);
        Minecraft.getInstance().getItemColors().register((s, i) -> {
            if (i == 0) {
                return 0xFFFFFFFF;
            }
            FluidStack fluid = ItemFluidPacket.getFluidStack(s);
            return !fluid.isEmpty() ? fluid.getFluid().getAttributes().getColor(fluid) : 0xFFFFFFFF;
        }, FCItems.FLUID_PACKET);
        Minecraft.getInstance().getItemColors().register((s, i) -> AEColor.TRANSPARENT.getVariantByTintIndex(i), FCItems.PART_FLUID_PATTERN_TERMINAL);
        Minecraft.getInstance().getItemColors().register((s, i) -> AEColor.TRANSPARENT.getVariantByTintIndex(i), FCItems.PART_DUAL_INTERFACE);
        Minecraft.getInstance().getItemColors().register(FluidEncodedPatternModel.PATTERN_ITEM_COLOR_HANDLER);
    }

    public void imcWait(InterModEnqueueEvent event) {

    }

    public void imcProcess(InterModEnqueueEvent event) {

    }

    public void finish(FMLLoadCompleteEvent event) {

    }

    private static IItemDefinition createItemDefn(Item item, AEFeature... feature) {
        return new ItemDefinition(
                Objects.requireNonNull(item.getRegistryName()).toString(),
                item,
                feature.length > 0 ?
                EnumSet.of(AEFeature.PATTERNS) : EnumSet.noneOf(AEFeature.class)
        );
    }

    public static ResourceLocation resource(String id) {
        return new ResourceLocation(MODID, id);
    }

}
