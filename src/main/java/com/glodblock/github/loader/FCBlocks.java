package com.glodblock.github.loader;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.block.*;
import com.glodblock.github.handler.RegistryHandler;
import com.glodblock.github.util.NameConst;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class FCBlocks {

    @GameRegistry.ObjectHolder(FluidCraft.MODID + ":" + NameConst.BLOCK_FLUID_DISCRETIZER)
    public static BlockFluidDiscretizer FLUID_DISCRETIZER;
    @GameRegistry.ObjectHolder(FluidCraft.MODID + ":" + NameConst.BLOCK_FLUID_PATTERN_ENCODER)
    public static BlockFluidPatternEncoder FLUID_PATTERN_ENCODER;
    @GameRegistry.ObjectHolder(FluidCraft.MODID + ":" + NameConst.BLOCK_FLUID_PACKET_DECODER)
    public static BlockFluidPacketDecoder FLUID_PACKET_DECODER;
    @GameRegistry.ObjectHolder(FluidCraft.MODID + ":" + NameConst.BLOCK_INGREDIENT_BUFFER)
    public static BlockIngredientBuffer INGREDIENT_BUFFER;
    @GameRegistry.ObjectHolder(FluidCraft.MODID + ":" + NameConst.BLOCK_BURETTE)
    public static BlockBurette BURETTE;
    @GameRegistry.ObjectHolder(FluidCraft.MODID + ":" + NameConst.BLOCK_DUAL_INTERFACE)
    public static BlockDualInterface DUAL_INTERFACE;

    public static void init(RegistryHandler regHandler) {
        regHandler.block(NameConst.BLOCK_FLUID_DISCRETIZER, new BlockFluidDiscretizer());
        regHandler.block(NameConst.BLOCK_FLUID_PATTERN_ENCODER, new BlockFluidPatternEncoder());
        regHandler.block(NameConst.BLOCK_FLUID_PACKET_DECODER, new BlockFluidPacketDecoder());
        regHandler.block(NameConst.BLOCK_INGREDIENT_BUFFER, new BlockIngredientBuffer());
        regHandler.block(NameConst.BLOCK_BURETTE, new BlockBurette());
        regHandler.block(NameConst.BLOCK_DUAL_INTERFACE, new BlockDualInterface());
    }

}
