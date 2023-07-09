package com.glodblock.github.loader;

import com.glodblock.github.common.block.*;
import com.glodblock.github.handler.RegistryHandler;
import com.glodblock.github.util.NameConst;

public class FCBlocks {

    public static BlockFluidDiscretizer FLUID_DISCRETIZER;
    //public static BlockFluidPatternEncoder FLUID_PATTERN_ENCODER;
    public static BlockFluidPacketDecoder FLUID_PACKET_DECODER;
    public static BlockIngredientBuffer INGREDIENT_BUFFER;
    public static BlockLargeIngredientBuffer LARGE_INGREDIENT_BUFFER;
    //public static BlockBurette BURETTE;
    public static BlockDualInterface DUAL_INTERFACE;
    //public static BlockFluidAssembler FLUID_ASSEMBLER;

    public static void init(RegistryHandler regHandler) {
        FLUID_DISCRETIZER = new BlockFluidDiscretizer();
        //FLUID_PATTERN_ENCODER = new BlockFluidPatternEncoder();
        FLUID_PACKET_DECODER = new BlockFluidPacketDecoder();
        INGREDIENT_BUFFER = new BlockIngredientBuffer();
        LARGE_INGREDIENT_BUFFER = new BlockLargeIngredientBuffer();
        //BURETTE = new BlockBurette();
        DUAL_INTERFACE = new BlockDualInterface();
        //FLUID_ASSEMBLER = new BlockFluidAssembler();
        regHandler.block(NameConst.BLOCK_FLUID_DISCRETIZER, FLUID_DISCRETIZER);
        //regHandler.block(NameConst.BLOCK_FLUID_PATTERN_ENCODER, FLUID_PATTERN_ENCODER);
        regHandler.block(NameConst.BLOCK_FLUID_PACKET_DECODER, FLUID_PACKET_DECODER);
        regHandler.block(NameConst.BLOCK_INGREDIENT_BUFFER, INGREDIENT_BUFFER);
        regHandler.block(NameConst.BLOCK_LARGE_INGREDIENT_BUFFER, LARGE_INGREDIENT_BUFFER);
        //regHandler.block(NameConst.BLOCK_BURETTE, BURETTE);
        regHandler.block(NameConst.BLOCK_DUAL_INTERFACE, DUAL_INTERFACE);
        //regHandler.block(NameConst.BLOCK_FLUID_ASSEMBLER, FLUID_ASSEMBLER);
    }

}
