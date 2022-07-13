package com.glodblock.github.common;

import cpw.mods.fml.relauncher.FMLInjectionData;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class Config {

    private static final Configuration Config = new Configuration(new File(new File((File) FMLInjectionData.data()[6], "config"), "ae2fc.cfg"));

    public static boolean fluidCells;
    public static boolean noFluidPacket;
    public static boolean fluidIOBus;
    public static boolean removeRecipe;
    public static boolean blacklistEssentiaGas;

    public static void run() {
        loadCategory();
        loadProperty();
    }

    private static void loadProperty() {
        fluidCells = Config.getBoolean("Enable Fluid Storage Cell", "Fluid Craft for AE2", true, "Enable this to generate the fluid storage cells. If you are playing with EC2, you can turn it off.");
        noFluidPacket = Config.getBoolean("No Fluid Packet", "Fluid Craft for AE2", false, "Enable this to make normal ME Interface can emit fluid with fluid pattern, like the ME Dual Interface.");
        fluidIOBus = Config.getBoolean("Enable AE2FC's Fluid I/O Bus", "Fluid Craft for AE2", true, "Enable this to add Fluid Import/Export Bus like EC2's one.");
        removeRecipe = Config.getBoolean("Disable all recipes", "Fluid Craft for AE2", false, "Disable all recipes, for quick tweaker.");
        blacklistEssentiaGas = Config.getBoolean("Blacklist Essentia Gas", "Fluid Craft for AE2", true, "Blacklist Essentia Gas from Thaumic Energistics, so they won't be stored in Fluid Storage Cells.");

        if (Config.hasChanged())
            Config.save();
    }

    private static void loadCategory() {
        Config.addCustomCategoryComment("Fluid Craft for AE2", "Settings for AE2FC.");
    }
}
