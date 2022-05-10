package com.glodblock.github.client.gui;

import appeng.client.gui.AEBaseGui;
import appeng.core.localization.GuiText;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.ContainerFluidPacketDecoder;
import com.glodblock.github.common.tile.TileFluidPacketDecoder;
import com.glodblock.github.util.NameConst;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiFluidPacketDecoder extends AEBaseGui {

    private static final ResourceLocation TEX_BG = FluidCraft.resource("textures/gui/fluid_packet_decoder.png");

    public GuiFluidPacketDecoder(InventoryPlayer ipl, TileFluidPacketDecoder tile) {
        super(new ContainerFluidPacketDecoder(ipl, tile));
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEX_BG);
        drawTexturedModalRect(offsetX, offsetY, 0, 0, 176, ySize);
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        fontRendererObj.drawString(getGuiDisplayName(I18n.format(NameConst.GUI_FLUID_PACKET_DECODER)), 8, 6, 0x404040);
        fontRendererObj.drawString(GuiText.inventory.getLocal(), 8, ySize - 94, 0x404040);
    }

}
