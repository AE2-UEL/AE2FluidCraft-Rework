package com.glodblock.github.util;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.implementations.GuiCraftAmount;
import appeng.client.gui.implementations.GuiCraftConfirm;
import appeng.client.gui.implementations.GuiCraftingStatus;
import appeng.client.gui.implementations.GuiExpandedProcessingPatternTerm;
import appeng.client.gui.implementations.GuiInterface;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.client.gui.implementations.GuiPriority;
import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.client.gui.widgets.GuiNumberBox;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.render.StackSizeRenderer;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.container.implementations.ContainerPatternEncoder;
import appeng.container.implementations.ContainerUpgradeable;
import appeng.fluids.client.gui.GuiFluidInterface;
import com.glodblock.github.client.container.ContainerExtendedFluidPatternTerminal;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.model.TRSRTransformation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SuppressWarnings("unchecked")
public class Ae2ReflectClient {

    private static final Method mGuiCraftAmount_addQty;
    private static final Field fAEBaseGui_stackSizeRenderer;
    private static final Constructor<? extends IBakedModel> cItemEncodedPatternBakedModel;
    private static final Field fGuiPriority_originalGuiBtn;
    private static final Field fGuiCraftingStatus_originalGuiBtn;
    private static final Field fGuiCraftingStatus_myIcon;
    private static final Field fGuiPatternTerm_container;
    private static final Field fGuiMEMonitorable_monitorableContainer;
    private static final Field fGuiMEMonitorable_configSrc;
    private static final Field fGuiMEMonitorable_craftingStatusBtn;
    private static final Field fGuiInterface_priority;
    private static final Field fGuiFluidInterface_priority;
    private static final Field fGuiUpgradeable_cvb;
    private static final Field fGuiCraftAmount_next;
    private static final Field fGuiCraftAmount_amountToCraft;
    private static final Field fGuiCraftAmount_originalGuiBtn;
    private static final Field[] fGuiCraftAmount_minus = new Field[4];
    private static final Field[] fGuiCraftAmount_plus = new Field[4];
    private static final Field fGuiCraftConfirm_ccc;
    private static final Field fGuiCraftConfirm_cancel;

    static {
        try {
            mGuiCraftAmount_addQty = Ae2Reflect.reflectMethod(GuiCraftAmount.class, "addQty", int.class);
            fAEBaseGui_stackSizeRenderer = Ae2Reflect.reflectField(AEBaseGui.class, "stackSizeRenderer");
            cItemEncodedPatternBakedModel = (Constructor<? extends IBakedModel>)Class
                    .forName("appeng.client.render.crafting.ItemEncodedPatternBakedModel")
                    .getDeclaredConstructor(IBakedModel.class, ImmutableMap.class);
            cItemEncodedPatternBakedModel.setAccessible(true);
            fGuiPriority_originalGuiBtn = Ae2Reflect.reflectField(GuiPriority.class, "originalGuiBtn");
            fGuiCraftingStatus_originalGuiBtn = Ae2Reflect.reflectField(GuiCraftingStatus.class, "originalGuiBtn");
            fGuiCraftingStatus_myIcon = Ae2Reflect.reflectField(GuiCraftingStatus.class, "myIcon");
            fGuiPatternTerm_container = Ae2Reflect.reflectField(GuiPatternTerm.class, "container");
            fGuiMEMonitorable_monitorableContainer = Ae2Reflect.reflectField(GuiMEMonitorable.class, "monitorableContainer");
            fGuiMEMonitorable_configSrc = Ae2Reflect.reflectField(GuiMEMonitorable.class, "configSrc");
            fGuiMEMonitorable_craftingStatusBtn = Ae2Reflect.reflectField(GuiMEMonitorable.class, "craftingStatusBtn");
            fGuiInterface_priority = Ae2Reflect.reflectField(GuiInterface.class, "priority");
            fGuiFluidInterface_priority = Ae2Reflect.reflectField(GuiFluidInterface.class, "priority");
            fGuiUpgradeable_cvb = Ae2Reflect.reflectField(GuiUpgradeable.class, "cvb");
            fGuiCraftAmount_next = Ae2Reflect.reflectField(GuiCraftAmount.class, "next");
            fGuiCraftAmount_amountToCraft = Ae2Reflect.reflectField(GuiCraftAmount.class, "amountToCraft");
            fGuiCraftAmount_originalGuiBtn = Ae2Reflect.reflectField(GuiCraftAmount.class, "originalGuiBtn");
            for (int i = 1, j = 0; i <= 1000; i *= 10, j ++) {
                fGuiCraftAmount_minus[j] = Ae2Reflect.reflectField(GuiCraftAmount.class, "minus" + i);
                fGuiCraftAmount_plus[j] = Ae2Reflect.reflectField(GuiCraftAmount.class, "plus" + i);
            }
            fGuiCraftConfirm_ccc = Ae2Reflect.reflectField(GuiCraftConfirm.class, "ccc");
            fGuiCraftConfirm_cancel = Ae2Reflect.reflectField(GuiCraftConfirm.class, "cancel");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize AE2 reflection hacks!", e);
        }
    }

    public static StackSizeRenderer getStackSizeRenderer(AEBaseGui gui) {
        return Ae2Reflect.readField(gui, fAEBaseGui_stackSizeRenderer);
    }

    public static IBakedModel bakeEncodedPatternModel(IBakedModel baseModel,
                                                      ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms) {
        try {
            return cItemEncodedPatternBakedModel.newInstance(baseModel, transforms);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke constructor: " + cItemEncodedPatternBakedModel, e);
        }
    }

    public static GuiTabButton getOriginalGuiButton(GuiPriority gui) {
        return Ae2Reflect.readField(gui, fGuiPriority_originalGuiBtn);
    }

    public static GuiTabButton getOriginalGuiButton(GuiCraftingStatus gui) {
        return Ae2Reflect.readField(gui, fGuiCraftingStatus_originalGuiBtn);
    }

    public static void setIconItem(GuiCraftingStatus gui, ItemStack icon) {
        Ae2Reflect.writeField(gui, fGuiCraftingStatus_myIcon, icon);
    }

    public static void setGuiContainer(GuiPatternTerm instance, ContainerPatternEncoder container) {
        Ae2Reflect.writeField(instance, fGuiPatternTerm_container, container);
        Ae2Reflect.writeField(instance, fGuiMEMonitorable_monitorableContainer, container);
        Ae2Reflect.writeField(instance, fGuiMEMonitorable_configSrc, container.getConfigManager());
    }

    public static void setGuiExContainer(GuiExpandedProcessingPatternTerm instance, ContainerExtendedFluidPatternTerminal container) {
        Ae2Reflect.writeField(instance, fGuiMEMonitorable_monitorableContainer, container);
        Ae2Reflect.writeField(instance, fGuiMEMonitorable_configSrc, container.getConfigManager());
    }

    public static GuiTabButton getCraftingStatusButton(GuiMEMonitorable gui) {
        return Ae2Reflect.readField(gui, fGuiMEMonitorable_craftingStatusBtn);
    }

    public static GuiTabButton getPriorityButton(GuiInterface gui) {
        return Ae2Reflect.readField(gui, fGuiInterface_priority);
    }

    public static GuiTabButton getPriorityButton(GuiFluidInterface gui) {
        return Ae2Reflect.readField(gui, fGuiFluidInterface_priority);
    }

    public static void setInterfaceContainer(GuiUpgradeable instance, ContainerUpgradeable container) {
        Ae2Reflect.writeField(instance, fGuiUpgradeable_cvb, container);
    }

    public static GuiButton getGuiCraftAmountNextButton(GuiCraftAmount gui) {
        return Ae2Reflect.readField(gui, fGuiCraftAmount_next);
    }

    public static GuiNumberBox getGuiCraftAmountTextBox(GuiCraftAmount gui) {
        return Ae2Reflect.readField(gui, fGuiCraftAmount_amountToCraft);
    }

    public static GuiButton getGuiCraftAmountAddButton(GuiCraftAmount gui, int index) {
        return index < 0 ?
                Ae2Reflect.readField(gui, fGuiCraftAmount_minus[-index - 1]) :
                Ae2Reflect.readField(gui, fGuiCraftAmount_plus[index - 1]);
    }

    public static void setGuiCraftAmountAddQty(GuiCraftAmount gui, int amount) {
        try {
            mGuiCraftAmount_addQty.invoke(gui, amount);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke method: " + mGuiCraftAmount_addQty, e);
        }
    }

    public static GuiTabButton getGuiCraftAmountBackButton(GuiCraftAmount gui) {
        return Ae2Reflect.readField(gui, fGuiCraftAmount_originalGuiBtn);
    }

    public static void writeCraftConfirmContainer(GuiCraftConfirm gui, ContainerCraftConfirm ccc) {
        Ae2Reflect.writeField(gui, fGuiCraftConfirm_ccc, ccc);
    }

    public static GuiButton getCraftConfirmBackButton(GuiCraftConfirm gui) {
        return Ae2Reflect.readField(gui, fGuiCraftConfirm_cancel);
    }

}
