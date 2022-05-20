package com.glodblock.github.util;

import com.glodblock.github.FluidCraft;
import net.minecraft.util.ResourceLocation;

public final class NameConst {

    public static final String BLOCK_FLUID_DISCRETIZER = "fluid_discretizer";
    public static final String BLOCK_FLUID_PATTERN_ENCODER = "fluid_pattern_encoder";
    public static final String BLOCK_FLUID_PACKET_DECODER = "fluid_packet_decoder";
    public static final String BLOCK_INGREDIENT_BUFFER = "ingredient_buffer";
    public static final String BLOCK_BURETTE = "burette";
    public static final String BLOCK_DUAL_INTERFACE = "dual_interface";

    public static final String ITEM_FLUID_DROP = "fluid_drop";
    public static final String ITEM_FLUID_PACKET = "fluid_packet";
    public static final String ITEM_DENSE_ENCODED_PATTERN = "dense_encoded_pattern";
    public static final String ITEM_PART_DUAL_INTERFACE = "part_dual_interface";
    public static final String ITEM_PART_FLUID_PATTERN_TERMINAL = "part_fluid_pattern_terminal";
    public static final String ITEM_PART_EXTENDED_FLUID_PATTERN_TERMINAL = "part_fluid_pattern_ex_terminal";

    public static final String TT_KEY = FluidCraft.MODID + ".tooltip.";
    public static final String TT_FLUID_PACKET = TT_KEY + "fluid_packet";
    public static final String TT_INVALID_FLUID = TT_KEY + "invalid_fluid";
    public static final String TT_PROCESSING_RECIPE_ONLY = TT_KEY + "processing_recipe_only";
    public static final String TT_CRAFTING_RECIPE_ONLY = TT_KEY + "crafting_recipe_only";
    public static final String TT_ENCODE_PATTERN = TT_KEY + "encode_pattern";
    public static final String TT_EMPTY = TT_KEY + "empty";
    public static final String TT_DUMP_TANK = TT_KEY + "dump_tank";
    public static final String TT_TRANSPOSE_IN = TT_KEY + "transpose_in";
    public static final String TT_TRANSPOSE_OUT = TT_KEY + "transpose_out";

    private static final String GUI_KEY = FluidCraft.MODID + ".gui.";
    public static final String GUI_FLUID_PATTERN_ENCODER = GUI_KEY + BLOCK_FLUID_PATTERN_ENCODER;
    public static final String GUI_FLUID_PACKET_DECODER = GUI_KEY + BLOCK_FLUID_PACKET_DECODER;
    public static final String GUI_INGREDIENT_BUFFER = GUI_KEY + BLOCK_INGREDIENT_BUFFER;
    public static final String GUI_BURETTE = GUI_KEY + BLOCK_BURETTE;

    public static final ResourceLocation MODEL_DENSE_ENCODED_PATTERN = FluidCraft.resource("builtin/dense_encoded_pattern");
    public static final ResourceLocation MODEL_FLUID_PACKET = FluidCraft.resource("builtin/fluid_packet");

}