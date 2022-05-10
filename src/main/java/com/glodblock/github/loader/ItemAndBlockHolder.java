package com.glodblock.github.loader;

import com.glodblock.github.common.block.BlockFluidDiscretizer;
import com.glodblock.github.common.block.BlockFluidInterface;
import com.glodblock.github.common.block.BlockFluidPacketDecoder;
import com.glodblock.github.common.block.BlockFluidPatternEncoder;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.item.ItemPartFluidPatternTerminal;

public class ItemAndBlockHolder {

    public static ItemFluidDrop DROP = new ItemFluidDrop().register();
    public static ItemFluidEncodedPattern PATTERN = new ItemFluidEncodedPattern().register();
    public static ItemPartFluidPatternTerminal FLUID_TERMINAL = new ItemPartFluidPatternTerminal().register();
    public static ItemFluidPacket PACKET = new ItemFluidPacket().register();

    public static BlockFluidDiscretizer DISCRETIZER = new BlockFluidDiscretizer().register();
    public static BlockFluidPatternEncoder ENCODER = new BlockFluidPatternEncoder().register();
    public static BlockFluidPacketDecoder DECODER = new BlockFluidPacketDecoder().register();
    public static BlockFluidInterface INTERFACE = new BlockFluidInterface().register();

}
