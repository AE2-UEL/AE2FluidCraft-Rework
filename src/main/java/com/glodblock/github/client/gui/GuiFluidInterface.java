package com.glodblock.github.client.gui;

import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.core.localization.GuiText;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.ContainerFluidInterface;
import com.glodblock.github.common.parts.PartFluidInterface;
import com.glodblock.github.common.tile.TileFluidInterface;
import com.glodblock.github.inventory.IAEFluidTank;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.network.CPacketSwitchGuis;
import com.glodblock.github.util.NameConst;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

public class GuiFluidInterface extends AEBaseGui {

    private static final ResourceLocation TEX_BG = FluidCraft.resource("textures/gui/interface_fluid.png");
    private static final int TANK_X = 35, TANK_X_OFF = 18, TANK_Y = 53;
    private static final int TANK_WIDTH = 16, TANK_HEIGHT = 68;

    private final ContainerFluidInterface cont;

    private GuiTabButton switcher;

    public GuiFluidInterface(InventoryPlayer ipl, TileFluidInterface tile) {
        super(new ContainerFluidInterface(ipl, tile));
        this.cont = (ContainerFluidInterface) inventorySlots;
        this.ySize = 231;
    }

    public GuiFluidInterface(InventoryPlayer ipl, PartFluidInterface tile) {
        super(new ContainerFluidInterface(ipl, tile));
        this.cont = (ContainerFluidInterface) inventorySlots;
        this.ySize = 231;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui()
    {
        super.initGui();
        this.switcher = new GuiTabButton( this.guiLeft + 154, this.guiTop, isPart() ? ItemAndBlockHolder.FLUID_INTERFACE.stack() : ItemAndBlockHolder.INTERFACE.stack(), StatCollector.translateToLocal("ae2fc.tooltip.switch_fluid_interface"), itemRender );
        this.buttonList.add( this.switcher );
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        fontRendererObj.drawString(getGuiDisplayName(I18n.format(NameConst.GUI_FLUID_INTERFACE)), 8, 6, 0x404040);
        fontRendererObj.drawString(GuiText.inventory.getLocal(), 8, ySize - 94, 0x404040);
        GL11.glColor4f(1F, 1F, 1F, 1F);

        IAEFluidTank fluidInv;
        if (isPart()) {
            fluidInv = ((PartFluidInterface) cont.getTile()).getInternalFluid();
        } else {
            fluidInv = ((TileFluidInterface) cont.getTile()).getInternalFluid();
        }

        mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        for (int i = 0; i < 6; i++) {
            renderFluidIntoGui(TANK_X + i * TANK_X_OFF, TANK_Y, TANK_WIDTH, TANK_HEIGHT,
                fluidInv.getFluidInSlot(i), fluidInv.getTankInfo(ForgeDirection.UNKNOWN)[i].capacity);
        }
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEX_BG);
        drawTexturedModalRect(offsetX, offsetY, 0, 0, 176, ySize);
    }

    public void renderFluidIntoGui(int x, int y, int width, int height,
                                   @Nullable IAEFluidStack aeFluidStack, int capacity) {
        if (aeFluidStack != null) {
            GL11.glDisable(2896);
            GL11.glColor3f(1.0F, 1.0F, 1.0F);
            int hi = (int) (height * ((double) aeFluidStack.getStackSize() / capacity));
            if (aeFluidStack.getStackSize() > 0 && hi > 0) {
                Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
                IIcon fluidIcon = aeFluidStack.getFluid().getStillIcon();
                GL11.glColor3f((float)(aeFluidStack.getFluid().getColor() >> 16 & 255) / 255.0F, (float)(aeFluidStack.getFluid().getColor() >> 8 & 255) / 255.0F, (float)(aeFluidStack.getFluid().getColor() & 255) / 255.0F);
                for (int th = 0; th <= hi; th += 16) {
                    if (hi - th <= 0) break;
                    this.drawTexturedModelRectFromIcon(x, y + height - Math.min(16, hi - th) - th, fluidIcon, width, Math.min(16, hi - th));
                }
                GL11.glColor3f(1.0F, 1.0F, 1.0F);
            }
        }
    }

    @Override
    protected void actionPerformed( final GuiButton btn )
    {
        super.actionPerformed(btn);
        if (btn == this.switcher) {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketSwitchGuis(isPart() ? GuiType.DUAL_INTERFACE_PART : GuiType.DUAL_INTERFACE));
        }
    }

    public void update(int id, IAEFluidStack stack) {
        if (id >= 100) {
            id -= 100;
            if (isPart()) {
                ((PartFluidInterface) cont.getTile()).setConfig(id, stack);
            } else {
                ((TileFluidInterface) cont.getTile()).setConfig(id, stack);
            }
        }
        else {
            if(isPart()) {
                ((PartFluidInterface) cont.getTile()).setFluidInv(id, stack);
            } else {
                ((TileFluidInterface) cont.getTile()).setFluidInv(id, stack);
            }
        }
    }

    private boolean isPart() {
        return this.cont.getTile() instanceof PartFluidInterface;
    }

}

