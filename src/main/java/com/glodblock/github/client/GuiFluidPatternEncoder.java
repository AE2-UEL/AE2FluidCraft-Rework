package com.glodblock.github.client;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.AEBaseGui;
import appeng.client.render.StackSizeRenderer;
import appeng.core.localization.GuiText;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.container.ContainerFluidPatternEncoder;
import com.glodblock.github.client.render.FluidRenderUtils;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.common.tile.TileFluidPatternEncoder;
import com.glodblock.github.integration.mek.FCGasItems;
import com.glodblock.github.integration.mek.GasRenderUtil;
import com.glodblock.github.interfaces.SlotFluid;
import com.glodblock.github.inventory.slot.SlotSingleItem;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.network.CPacketEncodePattern;
import com.glodblock.github.util.Ae2ReflectClient;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.MouseRegionManager;
import com.glodblock.github.util.NameConst;
import mekanism.api.gas.GasStack;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GuiFluidPatternEncoder extends AEBaseGui {

    private static final ResourceLocation TEX_BG = FluidCraft.resource("textures/gui/fluid_pattern_encoder.png");

    private final ContainerFluidPatternEncoder cont;
    private final MouseRegionManager mouseRegions = new MouseRegionManager(this);
    private final StackSizeRenderer stackSizeRenderer = Ae2ReflectClient.getStackSizeRenderer(this);

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
    protected void mouseClicked(int xCoord, int yCoord, int btn) throws IOException {
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
        fontRenderer.drawString(getGuiDisplayName(I18n.format(NameConst.GUI_FLUID_PATTERN_ENCODER)), 8, 6, 0x404040);
        fontRenderer.drawString(GuiText.inventory.getLocal(), 8, ySize - 94, 0x404040);
        mouseRegions.render(mouseX, mouseY);
    }

    @Override
    public void drawSlot(Slot slot) {
        if (slot instanceof SlotFluid) {
            IAEItemStack stack = ((SlotFluid)slot).getAeStack();
            if (FluidRenderUtils.renderFluidPacketIntoGuiSlot(slot, stack, stackSizeRenderer, fontRenderer)) {
                return;
            }
            if (ModAndClassUtil.GAS && GasRenderUtil.renderGasPacketIntoGuiSlot(slot, stack, stackSizeRenderer, fontRenderer)) {
                return;
            }
            super.drawSlot(new SlotSingleItem(slot));
            stackSizeRenderer.renderStackSize(fontRenderer, stack, slot.xPos, slot.yPos);
        } else {
            super.drawSlot(slot);
        }
    }

    @Override
    @Nonnull
    public List<String> getItemToolTip(ItemStack stack) {
        if (stack.getItem() == FCItems.FLUID_PACKET) {
            FluidStack fluid = FakeItemRegister.getStack(stack);
            if (fluid != null) {
                return Arrays.asList(fluid.getLocalizedName(), String.format(TextFormatting.GRAY + "%,d mB", fluid.amount));
            }
        }
        if (ModAndClassUtil.GAS && stack.getItem() == FCGasItems.GAS_PACKET) {
            GasStack gas = FakeItemRegister.getStack(stack);
            if (gas != null && gas.getGas() != null) {
                return Arrays.asList(gas.getGas().getLocalizedName(), String.format(TextFormatting.GRAY + "%,d mB", gas.amount));
            }
        }
        return super.getItemToolTip(stack);
    }

}
