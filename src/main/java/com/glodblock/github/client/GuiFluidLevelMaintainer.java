package com.glodblock.github.client;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiNumberBox;
import appeng.core.localization.GuiText;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.container.ContainerFluidLevelMaintainer;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.item.fake.FakeFluids;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.common.tile.TileFluidLevelMaintainer;
import com.glodblock.github.network.CPacketUpdateFluidLevel;
import com.glodblock.github.util.NameConst;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;

import java.io.IOException;

public class GuiFluidLevelMaintainer extends AEBaseGui {

    private static final ResourceLocation TEX_BG = FluidCraft.resource("textures/gui/fluid_level_maintainer.png");

    private final ContainerFluidLevelMaintainer cont;
    private final GuiNumberBox[] maintain = new GuiNumberBox[TileFluidLevelMaintainer.MAX_FLUID];
    private final GuiNumberBox[] request = new GuiNumberBox[TileFluidLevelMaintainer.MAX_FLUID];

    public GuiFluidLevelMaintainer(InventoryPlayer ipl, TileFluidLevelMaintainer tile) {
        super(new ContainerFluidLevelMaintainer(ipl, tile));
        this.cont = (ContainerFluidLevelMaintainer) inventorySlots;
        this.ySize = 223;
    }

    public void setMaintainNumber(int id, int size) {
        if (id < 0 || id >= TileFluidLevelMaintainer.MAX_FLUID || size < 0)
            return;
        this.maintain[id].setText(String.valueOf(size));
    }

    @Override
    protected void renderHoveredToolTip(int x, int y) {
        if (this.mc.player.inventory.getItemStack().isEmpty() && this.getSlotUnderMouse() != null
                && this.getSlotUnderMouse().getHasStack() && this.getSlotUnderMouse() instanceof ContainerFluidLevelMaintainer.DisplayFluidSlot)
        {
            ItemStack packet = this.getSlotUnderMouse().getStack();
            FluidStack fluid = FakeItemRegister.getStack(packet);
            this.renderToolTip(FakeFluids.displayFluid(fluid), x, y);
        }
        else {
            super.renderHoveredToolTip(x, y);
        }
    }

    @Override
    protected void keyTyped(char character, int key) throws IOException {
        if (!this.checkHotbarKeys(key)) {
            GuiNumberBox focus = null;
            int id = 0;
            for (int i = 0; i < TileFluidLevelMaintainer.MAX_FLUID; i ++) {
                if (maintain[i].isFocused()) {
                    focus = maintain[i];
                    id = i;
                }
                if (request[i].isFocused()) {
                    focus = request[i];
                    id = i + 10;
                }
            }
            if (focus != null && key != 1) {
                if ((key == 211 || key == 205 || key == 203 || key == 14 || character == '-' || Character.isDigit(character)) && focus.textboxKeyTyped(character, key)) {
                    try {
                        String out = focus.getText();

                        boolean fixed;
                        for(fixed = false; out.startsWith("0") && out.length() > 1; fixed = true) {
                            out = out.substring(1);
                        }

                        if (fixed) {
                            focus.setText(out);
                        }

                        if (out.isEmpty()) {
                            out = "0";
                        }

                        long result = Long.parseLong(out);
                        if (result < 0L) {
                            focus.setText("1");
                            result = 1;
                        }

                        if (result > Integer.MAX_VALUE) {
                            result = Integer.MAX_VALUE;
                            focus.setText(String.valueOf(result));
                        }

                        if (id >= 10 || result != 0)
                            FluidCraft.proxy.netHandler.sendToServer(new CPacketUpdateFluidLevel(id, (int) result));

                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            else {
                super.keyTyped(character, key);
            }
        }

    }

    @Override
    public void initGui() {
        super.initGui();
        for (int i = 0; i < TileFluidLevelMaintainer.MAX_FLUID; i ++) {
            maintain[i] = new GuiNumberBox(this.fontRenderer, this.guiLeft + 39, this.guiTop + 33 + i * 20, 51, 10, Integer.class);
            request[i] = new GuiNumberBox(this.fontRenderer, this.guiLeft + 102, this.guiTop + 33 + i * 20, 51, 10, Integer.class);
            maintain[i].setTextColor(16777215);
            request[i].setTextColor(16777215);
            maintain[i].setEnableBackgroundDrawing(false);
            request[i].setEnableBackgroundDrawing(false);
            maintain[i].setMaxStringLength(10);
            request[i].setMaxStringLength(10);
        }
        TileFluidLevelMaintainer tile = this.cont.getTile();
        IItemHandler inv = tile.getInventoryHandler();
        for (int i = 0; i < inv.getSlots(); i ++) {
            if (!inv.getStackInSlot(i).isEmpty()) {
                FluidStack fluid = FakeItemRegister.getStack(inv.getStackInSlot(i));
                this.maintain[i].setText(String.valueOf(fluid == null ? "0" : fluid.amount));
            }
            else {
                this.maintain[i].setText("0");
            }
            this.request[i].setText(String.valueOf(tile.getRequest()[i]));
        }
    }

    @Override
    protected void mouseClicked(int xCoord, int yCoord, int btn) throws IOException {
        for (int i = 0; i < TileFluidLevelMaintainer.MAX_FLUID; i ++) {
            this.configNumberBar(request[i], xCoord, yCoord, btn);
            this.configNumberBar(maintain[i], xCoord, yCoord, btn);
        }
        super.mouseClicked(xCoord, yCoord, btn);
    }

    private void configNumberBar(GuiNumberBox bar, int xCoord, int yCoord, int btn) {
        bar.mouseClicked(xCoord, yCoord, btn);
        if (btn == 1 && bar.isFocused()) {
            bar.setText("");
        }
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        fontRenderer.drawString(getGuiDisplayName(I18n.format(NameConst.GUI_FLUID_LEVEL_MAINTAINER)), 8, 6, 0x404040);
        fontRenderer.drawString(GuiText.inventory.getLocal(), 8, ySize - 94, 0x404040);
        fontRenderer.drawString(I18n.format(NameConst.MISC_THRESHOLD), 39, 19, 0x404040);
        fontRenderer.drawString(I18n.format(NameConst.MISC_REQ), 102, 19, 0x404040);
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEX_BG);
        drawTexturedModalRect(offsetX, offsetY, 0, 0, 176, ySize);
        for (int i = 0; i < TileFluidLevelMaintainer.MAX_FLUID ; i ++) {
            this.maintain[i].drawTextBox();
            this.request[i].drawTextBox();
        }
    }
}
