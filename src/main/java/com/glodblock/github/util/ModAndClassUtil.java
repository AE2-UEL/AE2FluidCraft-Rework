package com.glodblock.github.util;

import appeng.api.config.ActionItems;
import cpw.mods.fml.common.Loader;

public final class ModAndClassUtil {

    public static boolean GT5 = false;
    public static boolean GT6 = false;

    public static boolean isDoubleButton;

    public static void init() {
        try {
            ActionItems x = ActionItems.DOUBLE;
            isDoubleButton = true;
        } catch (Exception e) {
            isDoubleButton = false;
        }

        if (Loader.isModLoaded("gregtech") && !Loader.isModLoaded("gregapi"))
            GT5 = true;
        if (Loader.isModLoaded("gregapi") && Loader.isModLoaded("gregapi_post"))
            GT6 = true;

    }



}
