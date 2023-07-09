package com.glodblock.github.util;

import net.minecraftforge.fml.ModList;

public final class ModAndClassUtil {

    public static boolean AUTO_P = false;
    public static boolean NEE = false;
    public static boolean JEI = false;
    public static boolean REI = false;

    public static void init() {
        if (ModList.get().isLoaded("packagedauto")) {
            AUTO_P = true;
        }

        if (ModList.get().isLoaded("neenergistics")) {
            NEE = true;
        }

        if (ModList.get().isLoaded("jei")) {
            JEI = true;
        }

        if (ModList.get().isLoaded("roughlyenoughitems")) {
            REI = true;
        }
    }

}
