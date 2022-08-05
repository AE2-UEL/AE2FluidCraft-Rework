package com.glodblock.github.client.gui;

import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.render.AppEngRenderItem;
import appeng.container.slot.SlotFake;
import appeng.util.item.AEItemStack;
import com.glodblock.github.client.gui.container.ContainerFluidPatternTerminalEx;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.slot.SlotSingleItem;
import com.glodblock.github.util.Ae2Reflect;
import com.glodblock.github.util.Ae2ReflectClient;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;

public class GuiFluidPatternTerminalEx extends GuiBaseFluidPatternTerminalEx {

    private GuiTabButton craftingStatusBtn;
    private final AppEngRenderItem stackSizeRenderer = Ae2ReflectClient.getStackSizeRenderer(this);

    public GuiFluidPatternTerminalEx(InventoryPlayer inventoryPlayer, ITerminalHost te) {
        super(inventoryPlayer, te);
        ContainerFluidPatternTerminalEx container = new ContainerFluidPatternTerminalEx(inventoryPlayer, te);
        container.setGui(this);
        this.inventorySlots = container;
        this.container = container;
        this.monitorableContainer = container;
        this.configSrc = container.getConfigManager();
    }

    @Override
    public void initGui() {
        super.initGui();
        craftingStatusBtn = super.craftingStatusBtn;
    }

    @Override
    public void func_146977_a( final Slot s )
    {
        if (drawSlot0(s))
            super.func_146977_a( s );
    }

    public boolean drawSlot0(Slot slot) {
        if (slot instanceof SlotFake) {
            AEItemStack stack = AEItemStack.create(slot.getStack());
            super.func_146977_a(new SlotSingleItem(slot));
            if (stack == null) return true;
            IAEItemStack fake = stack.copy();
            if (fake.getItemStack().getItem() instanceof ItemFluidPacket) {
                if (ItemFluidPacket.getFluidStack(stack) != null && ItemFluidPacket.getFluidStack(stack).amount > 0)
                    fake.setStackSize(ItemFluidPacket.getFluidStack(stack).amount);
            }
            else
                return true;
            stackSizeRenderer.setAeStack(fake);
            stackSizeRenderer.renderItemOverlayIntoGUI(fontRendererObj, mc.getTextureManager(), stack.getItemStack(), slot.xDisplayPosition, slot.yDisplayPosition);
            return false;
        }
        return true;
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        if (btn == craftingStatusBtn) {
            InventoryHandler.switchGui(GuiType.FLUID_PAT_TERM_CRAFTING_STATUS);
        } else {
            super.actionPerformed(btn);
        }
    }

}
