package com.glodblock.github.interfaces;

import appeng.core.sync.GuiBridge;
import appeng.helpers.IPriorityHost;
import com.glodblock.github.inventory.GuiType;

public interface FCPriorityHost extends IPriorityHost {

    GuiType getGuiType();

    @Override
    default GuiBridge getGuiBridge() {
        return GuiBridge.GUI_Handler;
    }

}