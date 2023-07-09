package com.glodblock.github.network;

import com.glodblock.github.FluidCraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class NetworkManager {

    private static final ResourceLocation channel = FluidCraft.resource("network");

    public static SimpleChannel netHandler = NetworkRegistry.newSimpleChannel(
            channel, ()-> "v1.0", s -> true, s -> true
    );

}
