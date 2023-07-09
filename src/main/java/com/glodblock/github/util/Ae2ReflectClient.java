package com.glodblock.github.util;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.ScreenRegistration;
import appeng.client.gui.me.common.MEMonitorableScreen;
import appeng.client.gui.style.TerminalStyle;
import appeng.client.render.DelegateBakedModel;
import appeng.container.AEBaseContainer;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.inventory.container.ContainerType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SuppressWarnings("unchecked")
public class Ae2ReflectClient {

    private static final Method mScreenRegistration_register;
    private static final Field fMEMonitorableScreen_style;
    private static final Constructor<? extends DelegateBakedModel> cEncodedPatternBakedModel;

    static {
        try {
            mScreenRegistration_register = Ae2Reflect.reflectMethod(ScreenRegistration.class, "register",
                    ContainerType.class, ScreenRegistration.StyledScreenFactory.class, String.class
                    );
            fMEMonitorableScreen_style = Ae2Reflect.reflectField(MEMonitorableScreen.class, "style");
            cEncodedPatternBakedModel = (Constructor<? extends DelegateBakedModel>)Class
                    .forName("appeng.client.render.crafting.EncodedPatternBakedModel")
                    .getDeclaredConstructor(IBakedModel.class);
            cEncodedPatternBakedModel.setAccessible(true);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize AE2 reflection hacks!", e);
        }
    }

    public static <M extends AEBaseContainer, U extends AEBaseScreen<M>> void registerAEGui(ContainerType<M> type, ScreenRegistration.StyledScreenFactory<M, U> factory, String stylePath) {
        try {
            mScreenRegistration_register.invoke(null, type, factory, stylePath);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke method: " + mScreenRegistration_register, e);
        }
    }

    public static IBakedModel bakeEncodedPatternModel(IBakedModel baseModel) {
        try {
            return cEncodedPatternBakedModel.newInstance(baseModel);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke constructor: " + cEncodedPatternBakedModel, e);
        }
    }

    @SuppressWarnings("rawtypes")
    public static TerminalStyle getGuiStyle(MEMonitorableScreen gui) {
        return Ae2Reflect.readField(gui, fMEMonitorableScreen_style);
    }

}
