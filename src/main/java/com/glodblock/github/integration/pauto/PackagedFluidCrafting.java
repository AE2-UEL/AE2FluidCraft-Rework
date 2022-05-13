package com.glodblock.github.integration.pauto;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thelm.packagedauto.api.RecipeTypeRegistry;

public class PackagedFluidCrafting {

    public static void init() {
        RecipeTypeRegistry.registerRecipeType(RecipeTypeFluidProcessing.INSTANCE);
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        MinecraftForge.EVENT_BUS.register(new RecipeEncoderFluidTooltipHandler());
    }

}
