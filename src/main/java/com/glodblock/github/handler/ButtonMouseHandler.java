package com.glodblock.github.handler;

import com.glodblock.github.interfaces.TankDumpable;
import com.glodblock.github.network.NetworkManager;
import com.glodblock.github.network.packets.CPacketDumpTank;
import com.glodblock.github.util.MouseRegionManager;
import com.glodblock.github.util.NameConst;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

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
    public List<ITextComponent> getTooltip() {
        return tooltipKey != null ? Collections.singletonList(new TranslationTextComponent(tooltipKey)) : null;
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
                NetworkManager.netHandler.sendToServer(new CPacketDumpTank(index));
            }
        });
    }

}