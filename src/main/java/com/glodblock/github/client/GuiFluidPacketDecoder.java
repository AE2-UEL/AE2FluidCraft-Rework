package com.glodblock.github.client;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import com.glodblock.github.client.container.ContainerFluidPacketDecoder;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiFluidPacketDecoder extends AEBaseScreen<ContainerFluidPacketDecoder> {

    public GuiFluidPacketDecoder(ContainerFluidPacketDecoder container, PlayerInventory playerInventory, ITextComponent title, ScreenStyle style) {
        super(container, playerInventory, title, style);
    }

}
