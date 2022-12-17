package com.glodblock.github.client;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiProgressBar;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.container.ContainerFluidAssembler;
import com.glodblock.github.common.tile.TileFluidAssembler;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiFluidAssembler extends AEBaseGui {

    private final ContainerFluidAssembler container;
    private GuiProgressBar pb;
    private static final ResourceLocation[] TEX_BG = new ResourceLocation[] {
            FluidCraft.resource("textures/gui/fluid_assembler.png"),
            FluidCraft.resource("textures/gui/fluid_assembler2.png"),
            FluidCraft.resource("textures/gui/fluid_assembler3.png"),
            FluidCraft.resource("textures/gui/fluid_assembler4.png"),
    };

    public GuiFluidAssembler(InventoryPlayer ipl, TileFluidAssembler tile) {
        super(new ContainerFluidAssembler(ipl, tile));
        this.container = (ContainerFluidAssembler) inventorySlots;
        this.ySize = 249;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.pb = new GuiProgressBar(this.container, "guis/mac.png", 148, 34, 148, 201, 6, 18, GuiProgressBar.Direction.VERTICAL);
        this.buttonList.add(this.pb);
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.pb.setFullMsg((this.container.getCurrentProgress() * 100 / TileFluidAssembler.TIME) + "%");
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEX_BG[cap()]);
        this.pb.x = 148 + this.guiLeft;
        this.pb.y = 34 + this.guiTop;
        drawTexturedModalRect(offsetX, offsetY, 0, 0, 176, ySize);
    }

    private int cap() {
        if (this.container.patternCap > 3) {
            return 3;
        } else {
            return this.container.patternCap;
        }
    }
}
