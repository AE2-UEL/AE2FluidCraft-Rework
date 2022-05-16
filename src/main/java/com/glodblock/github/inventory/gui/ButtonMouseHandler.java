package com.glodblock.github.inventory.gui;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.TankDumpable;
import com.glodblock.github.network.CPacketDumpTank;
import com.glodblock.github.util.NameConst;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class ButtonMouseHandler implements MouseRegionManager.Handler {

    @Nullable
    private final String tooltipKey;
    private final Runnable callback;

    public ButtonMouseHandler(@Nullable String tooltipKey, Runnable callback) {
        this.tooltipKey = tooltipKey;
        this.callback = callback;
    }

    @Nullable
    @Override
    public List<String> getTooltip() {
        return tooltipKey != null ? Collections.singletonList(I18n.format(tooltipKey)) : null;
    }

    @Override
    public boolean onClick(int button) {
        if (button == 0) {
            callback.run();
            return true;
        }
        return false;
    }

    public static ButtonMouseHandler dumpTank(TankDumpable host, int index) {
        return new ButtonMouseHandler(NameConst.TT_DUMP_TANK, () -> {
            if (host.canDumpTank(index)) {
                FluidCraft.proxy.netHandler.sendToServer(new CPacketDumpTank(index));
            }
        });
    }

}
