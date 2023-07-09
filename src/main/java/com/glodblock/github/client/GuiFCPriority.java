package com.glodblock.github.client;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.implementations.NumberEntryWidget;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.TabButton;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.SwitchGuisPacket;
import com.glodblock.github.client.container.ContainerFCPriority;
import com.glodblock.github.common.tile.TileDualInterface;
import com.glodblock.github.loader.FCBlocks;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.network.NetworkManager;
import com.glodblock.github.network.packets.CPacketFluidCraftBtns;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import java.util.OptionalInt;

public class GuiFCPriority extends AEBaseScreen<ContainerFCPriority> {
    private final ContainerType<?> originGui;
    private final TabButton back;
    private final NumberEntryWidget priority;

    public GuiFCPriority(ContainerFCPriority container, PlayerInventory playerInventory, ITextComponent title, ScreenStyle style) {
        super(container, playerInventory, title, style);
        this.originGui = container.getPriorityHost().getContainerType();
        ItemRenderer itemRender = Minecraft.getInstance().getItemRenderer();
        ItemStack dual;
        if (container.getPriorityHost() instanceof TileDualInterface) {
            dual = new ItemStack(FCBlocks.DUAL_INTERFACE);
        } else {
            dual = new ItemStack(FCItems.PART_DUAL_INTERFACE);
        }
        this.back = new TabButton(dual, dual.getDisplayName(), itemRender, btn ->
                NetworkHandler.instance().sendToServer(new SwitchGuisPacket(this.originGui)));
        this.priority = new NumberEntryWidget(NumberEntryType.PRIORITY);
        this.priority.setTextFieldBounds(62, 57, 50);
        this.priority.setMinValue(Integer.MIN_VALUE);
        this.priority.setValue(this.container.getPriorityValue());
        this.priority.setOnChange(this::savePriority);
        this.priority.setOnConfirm(() -> {
            this.savePriority();
            this.back.onPress();
        });
        this.widgets.add("priority", this.priority);
        this.widgets.add("back", this.back);
    }

    private void savePriority() {
        OptionalInt priority = this.priority.getIntValue();
        if (priority.isPresent()) {
            NetworkManager.netHandler.sendToServer(new CPacketFluidCraftBtns("priority", priority.getAsInt()));
        }
    }

    public void drawBG(MatrixStack matrixStack, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        super.drawBG(matrixStack, offsetX, offsetY, mouseX, mouseY, partialTicks);
        this.priority.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}
