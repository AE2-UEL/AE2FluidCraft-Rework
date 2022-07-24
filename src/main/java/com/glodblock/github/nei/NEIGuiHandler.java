package com.glodblock.github.nei;

import codechicken.nei.api.INEIGuiAdapter;
import com.glodblock.github.client.gui.GuiFCBaseMonitor;
import net.minecraft.client.gui.inventory.GuiContainer;

public class NEIGuiHandler extends INEIGuiAdapter {

    @Override
    public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int w, int h) {
        if (gui instanceof GuiFCBaseMonitor) {
            return ((GuiFCBaseMonitor) gui).hideItemPanelSlot(x, y, w, h);
        }
        return false;
    }

}
