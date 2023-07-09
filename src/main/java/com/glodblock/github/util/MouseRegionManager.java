package com.glodblock.github.util;

import appeng.client.gui.AEBaseScreen;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MouseRegionManager {

    private final AEBaseScreen<?> gui;
    private final List<Region> regions = new ArrayList<>();

    public MouseRegionManager(AEBaseScreen<?> gui) {
        this.gui = gui;
    }

    public void addRegion(int x, int y, int width, int height, Handler handler) {
        regions.add(new Region(x, y, width, height, handler));
    }

    public boolean onClick(double mX, double mY, int button) {
        mX -= gui.getGuiLeft();
        mY -= gui.getGuiTop();
        for (Region region : regions) {
            if (region.containsMouse(mX, mY) && region.handler.onClick(button)) {
                gui.getMinecraft().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1F));
                return false;
            }
        }
        return true;
    }

    public void render(MatrixStack mStack, int mX, int mY) {
        mX -= gui.getGuiLeft();
        mY -= gui.getGuiTop();
        for (Region region : regions) {
            if (region.containsMouse(mX, mY)) {
                List<ITextComponent> tooltip = region.handler.getTooltip();
                if (tooltip != null) {
                    gui.drawTooltip(mStack, mX, mY, tooltip);
                    return;
                }
            }
        }
    }

    private static class Region {

        private final int x, y, width, height;
        private final Handler handler;

        Region(int x, int y, int width, int height, Handler handler) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.handler = handler;
        }

        boolean containsMouse(double mX, double mY) {
            return mX >= x && mX < x + width && mY >= y && mY < y + height;
        }

    }

    public interface Handler {

        @Nullable
        default List<ITextComponent> getTooltip() {
            return null;
        }

        default boolean onClick(int button) {
            return false;
        }

    }

}
