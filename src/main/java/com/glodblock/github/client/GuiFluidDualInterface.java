package com.glodblock.github.client;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.TabButton;
import appeng.container.SlotSemantic;
import appeng.core.Api;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.SwitchGuisPacket;
import appeng.fluids.client.gui.widgets.FluidSlotWidget;
import appeng.fluids.client.gui.widgets.FluidTankWidget;
import appeng.fluids.util.IAEFluidTank;
import com.glodblock.github.client.container.ContainerFCPriority;
import com.glodblock.github.client.container.ContainerFluidDualInterface;
import com.glodblock.github.client.container.ContainerItemDualInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class GuiFluidDualInterface extends UpgradeableScreen<ContainerFluidDualInterface> {

    private final TabButton switchInterface;
    private final TabButton priorityBtn;

    public GuiFluidDualInterface(ContainerFluidDualInterface container, PlayerInventory playerInventory, ITextComponent title, ScreenStyle style) {
        super(container, playerInventory, title, style);
        IAEFluidTank configFluids = this.container.getFluidConfigInventory();
        for(int i = 0; i < 6; ++i) {
            this.addSlot(new FluidSlotWidget(configFluids, i), SlotSemantic.CONFIG);
        }
        IAEFluidTank fluidTank = this.container.getTanks();
        for(int i = 0; i < 6; ++i) {
            this.widgets.add("tank" + (i + 1), new FluidTankWidget(fluidTank, i));
        }
        ItemRenderer itemRender = Minecraft.getInstance().getItemRenderer();
        ItemStack iconItem = Api.instance().definitions().blocks().iface().maybeStack(1).orElse(ItemStack.EMPTY);
        switchInterface = new TabButton(iconItem, iconItem.getDisplayName(), itemRender, btn ->
                NetworkHandler.instance().sendToServer(new SwitchGuisPacket(ContainerItemDualInterface.TYPE)));
        this.widgets.add("switchInterface", switchInterface);
        ItemStack iconWrench = Api.instance().definitions().items().certusQuartzWrench().maybeStack(1).orElse(ItemStack.EMPTY);
        this.priorityBtn = new TabButton(iconWrench, GuiText.Priority.text(), itemRender, btn ->
                NetworkHandler.instance().sendToServer(new SwitchGuisPacket(ContainerFCPriority.TYPE)));
        this.widgets.add("priorityBtn", priorityBtn);
    }

}
