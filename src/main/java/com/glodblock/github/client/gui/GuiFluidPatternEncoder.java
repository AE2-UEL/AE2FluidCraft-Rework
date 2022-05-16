package com.glodblock.github.client.gui;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.AEBaseGui;
import appeng.client.render.AppEngRenderItem;
import appeng.core.localization.GuiText;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.ContainerFluidPatternEncoder;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.tile.TileFluidPatternEncoder;
import com.glodblock.github.inventory.gui.MouseRegionManager;
import com.glodblock.github.inventory.slot.SlotFluid;
import com.glodblock.github.inventory.slot.SlotSingleItem;
import com.glodblock.github.network.CPacketEncodePattern;
import com.glodblock.github.util.Ae2Reflect;
import com.glodblock.github.util.NameConst;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GuiFluidPatternEncoder extends AEBaseGui {

    private static final ResourceLocation TEX_BG = FluidCraft.resource("textures/gui/fluid_pattern_encoder.png");

    private final ContainerFluidPatternEncoder cont;
    private final MouseRegionManager mouseRegions = new MouseRegionManager(this);
    private final AppEngRenderItem stackSizeRenderer = Ae2Reflect.getStackSizeRenderer(this);

    public GuiFluidPatternEncoder(InventoryPlayer ipl, TileFluidPatternEncoder tile) {
        super(new ContainerFluidPatternEncoder(ipl, tile));
        this.cont = (ContainerFluidPatternEncoder)inventorySlots;
        mouseRegions.addRegion(141, 38, 10, 10, new MouseRegionManager.Handler() {
            @Override
            public List<String> getTooltip() {
                return Collections.singletonList(I18n.format(NameConst.TT_ENCODE_PATTERN));
            }

            @Override
            public boolean onClick(int button) {
                if (button == 0) {
                    if (cont.canEncodePattern()) {
                        FluidCraft.proxy.netHandler.sendToServer(new CPacketEncodePattern());
                    }
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void mouseClicked(int xCoord, int yCoord, int btn) {
        if (mouseRegions.onClick(xCoord, yCoord, btn)) {
            super.mouseClicked(xCoord, yCoord, btn);
        }
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEX_BG);
        drawTexturedModalRect(offsetX, offsetY, 0, 0, 176, ySize);
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        fontRendererObj.drawString(getGuiDisplayName(I18n.format(NameConst.GUI_FLUID_PATTERN_ENCODER)), 8, 6, 0x404040);
        fontRendererObj.drawString(GuiText.inventory.getLocal(), 8, ySize - 94, 0x404040);
        mouseRegions.render(mouseX, mouseY);
    }

    @Override
    public void func_146977_a( final Slot s )
    {
        this.drawSlot0( s );
    }

    public void drawSlot0(Slot slot) {
        if (slot instanceof SlotFluid) {
            IAEItemStack stack = ((SlotFluid)slot).getAeStack();
            super.func_146977_a(new SlotSingleItem(slot));
            if (stack == null) return;
            IAEItemStack fake = stack.copy();
            if (fake.getItemStack().getItem() instanceof ItemFluidPacket) {
                if (ItemFluidPacket.getFluidStack(stack) != null && ItemFluidPacket.getFluidStack(stack).amount > 0)
                    fake.setStackSize(ItemFluidPacket.getFluidStack(stack).amount);
            }
            stackSizeRenderer.setAeStack(fake);
            stackSizeRenderer.renderItemOverlayIntoGUI(fontRendererObj, mc.getTextureManager(), stack.getItemStack(), slot.xDisplayPosition, slot.yDisplayPosition);
        } else {
            super.func_146977_a(slot);
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void renderToolTip(ItemStack stack, int x, int y) {
        List list;
        if (stack.getItem() instanceof ItemFluidPacket) {
            FluidStack fluid = ItemFluidPacket.getFluidStack(stack);
            if (fluid != null) {
                list = Arrays.asList(fluid.getLocalizedName(), String.format(EnumChatFormatting.GRAY + "%,d mB", fluid.amount));
            }
            else {
                list = stack.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);
            }
        }
        else {
            list = stack.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);
        }

        for (int k = 0; k < list.size(); ++k)
        {
            if (k == 0)
            {
                list.set(k, stack.getRarity().rarityColor + (String)list.get(k));
            }
            else
            {
                list.set(k, EnumChatFormatting.GRAY + (String)list.get(k));
            }
        }
        FontRenderer font = stack.getItem().getFontRenderer(stack);
        drawHoveringText(list, x, y, (font == null ? fontRendererObj : font));
    }

}
