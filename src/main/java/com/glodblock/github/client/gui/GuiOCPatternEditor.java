package com.glodblock.github.client.gui;

import appeng.client.gui.AEBaseGui;
import appeng.core.localization.GuiText;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.ContainerOCPatternEditor;
import com.glodblock.github.common.tile.TileOCPatternEditor;
import com.glodblock.github.util.NameConst;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiOCPatternEditor extends AEBaseGui {

    private static final ResourceLocation TEX_BG = FluidCraft.resource("textures/gui/oc_pattern_editor.png");

    public GuiOCPatternEditor(InventoryPlayer ipl, TileOCPatternEditor tile) {
        super(new ContainerOCPatternEditor(ipl, tile));
        this.ySize = 186;
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEX_BG);
        drawTexturedModalRect(offsetX, offsetY, 0, 0, 176, ySize);
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        fontRendererObj.drawString(getGuiDisplayName(I18n.format(NameConst.GUI_OC_PATTERN_EDITOR)), 8, 6, 0x404040);
        fontRendererObj.drawString(GuiText.inventory.getLocal(), 8, ySize - 94, 0x404040);
    }
}
