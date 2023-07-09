package com.glodblock.github.loader;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.*;
import com.glodblock.github.handler.RegistryHandler;
import com.glodblock.github.util.NameConst;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class FCItems {

    public static final ItemGroup TAB_AE2FC = new ItemGroup(FluidCraft.MODID) {
        @Nonnull
        @Override
        public ItemStack createIcon() {
            return new ItemStack(FCBlocks.INGREDIENT_BUFFER);
        }
    };

    public static ItemFluidDrop FLUID_DROP;
    public static ItemFluidPacket FLUID_PACKET;
    public static ItemFluidEncodedPattern DENSE_ENCODED_PATTERN;
    //public static ItemFluidCraftEncodedPattern DENSE_CRAFT_ENCODED_PATTERN;
    public static ItemPartDualInterface PART_DUAL_INTERFACE;
    public static ItemPartFluidPatternTerminal PART_FLUID_PATTERN_TERMINAL;

    public static void init(RegistryHandler regHandler) {
        FLUID_DROP = new ItemFluidDrop();
        FLUID_PACKET = new ItemFluidPacket();
        DENSE_ENCODED_PATTERN = new ItemFluidEncodedPattern();
        //DENSE_CRAFT_ENCODED_PATTERN = new ItemFluidCraftEncodedPattern();
        PART_DUAL_INTERFACE = new ItemPartDualInterface();
        PART_FLUID_PATTERN_TERMINAL = new ItemPartFluidPatternTerminal();
        regHandler.item(NameConst.ITEM_FLUID_DROP, FLUID_DROP);
        regHandler.item(NameConst.ITEM_FLUID_PACKET, FLUID_PACKET);
        regHandler.item(NameConst.ITEM_DENSE_ENCODED_PATTERN, DENSE_ENCODED_PATTERN);
        //regHandler.item(NameConst.ITEM_DENSE_CRAFT_ENCODED_PATTERN, DENSE_CRAFT_ENCODED_PATTERN);
        regHandler.item(NameConst.ITEM_PART_DUAL_INTERFACE, PART_DUAL_INTERFACE);
        regHandler.item(NameConst.ITEM_PART_FLUID_PATTERN_TERMINAL, PART_FLUID_PATTERN_TERMINAL);
    }

    public static Item.Properties defaultProps() {
        return new Item.Properties().group(TAB_AE2FC);
    }

}
