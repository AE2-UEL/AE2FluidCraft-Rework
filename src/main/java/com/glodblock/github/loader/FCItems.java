package com.glodblock.github.loader;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.ItemFluidCraftEncodedPattern;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.item.ItemLargeEncodedPattern;
import com.glodblock.github.common.item.ItemPartDualInterface;
import com.glodblock.github.common.item.ItemPartExtendedFluidPatternTerminal;
import com.glodblock.github.common.item.ItemPartFluidPatternTerminal;
import com.glodblock.github.common.item.ItemWirelessFluidPatternTerminal;
import com.glodblock.github.handler.RegistryHandler;
import com.glodblock.github.util.NameConst;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;

public class FCItems {

    public static final CreativeTabs TAB_AE2FC = new CreativeTabs(FluidCraft.MODID) {
        @Nonnull
        @Override
        public ItemStack createIcon() {
            return new ItemStack(DENSE_ENCODED_PATTERN);
        }
    };

    @GameRegistry.ObjectHolder(FluidCraft.MODID + ":" + NameConst.ITEM_FLUID_DROP)
    public static ItemFluidDrop FLUID_DROP;
    @GameRegistry.ObjectHolder(FluidCraft.MODID + ":" + NameConst.ITEM_FLUID_PACKET)
    public static ItemFluidPacket FLUID_PACKET;
    @GameRegistry.ObjectHolder(FluidCraft.MODID + ":" + NameConst.ITEM_DENSE_ENCODED_PATTERN)
    public static ItemFluidEncodedPattern DENSE_ENCODED_PATTERN;
    @GameRegistry.ObjectHolder(FluidCraft.MODID + ":" + NameConst.ITEM_DENSE_CRAFT_ENCODED_PATTERN)
    public static ItemFluidCraftEncodedPattern DENSE_CRAFT_ENCODED_PATTERN;
    @GameRegistry.ObjectHolder(FluidCraft.MODID + ":" + NameConst.ITEM_LARGE_ITEM_ENCODED_PATTERN)
    public static ItemLargeEncodedPattern LARGE_ITEM_ENCODED_PATTERN;
    @GameRegistry.ObjectHolder(FluidCraft.MODID + ":" + NameConst.ITEM_PART_DUAL_INTERFACE)
    public static ItemPartDualInterface PART_DUAL_INTERFACE;
    @GameRegistry.ObjectHolder(FluidCraft.MODID + ":" + NameConst.ITEM_PART_FLUID_PATTERN_TERMINAL)
    public static ItemPartFluidPatternTerminal PART_FLUID_PATTERN_TERMINAL;
    @GameRegistry.ObjectHolder(FluidCraft.MODID + ":" + NameConst.ITEM_PART_EXTENDED_FLUID_PATTERN_TERMINAL)
    public static ItemPartExtendedFluidPatternTerminal PART_EXTENDED_FLUID_PATTERN_TERMINAL;
    @GameRegistry.ObjectHolder(FluidCraft.MODID + ":" + NameConst.ITEM_WIRELESS_FLUID_PATTERN_TERMINAL)
    public static ItemWirelessFluidPatternTerminal WIRELESS_FLUID_PATTERN_TERMINAL;

    public static void init(RegistryHandler regHandler) {
        regHandler.item(NameConst.ITEM_FLUID_DROP, new ItemFluidDrop());
        regHandler.item(NameConst.ITEM_FLUID_PACKET, new ItemFluidPacket());
        regHandler.item(NameConst.ITEM_DENSE_ENCODED_PATTERN, new ItemFluidEncodedPattern());
        regHandler.item(NameConst.ITEM_DENSE_CRAFT_ENCODED_PATTERN, new ItemFluidCraftEncodedPattern());
        regHandler.item(NameConst.ITEM_PART_DUAL_INTERFACE, new ItemPartDualInterface());
        regHandler.item(NameConst.ITEM_PART_FLUID_PATTERN_TERMINAL, new ItemPartFluidPatternTerminal());
        regHandler.item(NameConst.ITEM_PART_EXTENDED_FLUID_PATTERN_TERMINAL, new ItemPartExtendedFluidPatternTerminal());
        regHandler.item(NameConst.ITEM_LARGE_ITEM_ENCODED_PATTERN, new ItemLargeEncodedPattern());
        regHandler.item(NameConst.ITEM_WIRELESS_FLUID_PATTERN_TERMINAL, new ItemWirelessFluidPatternTerminal());
    }

}
