package com.glodblock.github.client;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.Icon;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.TabButton;
import appeng.client.gui.widgets.ToggleButton;
import appeng.core.Api;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigButtonPacket;
import appeng.core.sync.packets.SwitchGuisPacket;
import com.glodblock.github.client.button.BlitMap;
import com.glodblock.github.client.button.FCToggleButton;
import com.glodblock.github.client.container.ContainerFCPriority;
import com.glodblock.github.client.container.ContainerFluidDualInterface;
import com.glodblock.github.client.container.ContainerItemDualInterface;
import com.glodblock.github.network.NetworkManager;
import com.glodblock.github.network.packets.CPacketFluidCraftBtns;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class GuiItemDualInterface extends UpgradeableScreen<ContainerItemDualInterface> {

    private final SettingToggleButton<YesNo> blockMode;
    private final ToggleButton interfaceMode;
    private final TabButton switchInterface;
    private final TabButton priorityBtn;
    private final FCToggleButton fluidPacketBtn;
    private final FCToggleButton splittingBtn;
    private final FCToggleButton blockingBtn;

    public GuiItemDualInterface(ContainerItemDualInterface container, PlayerInventory playerInventory, ITextComponent title, ScreenStyle style) {
        super(container, playerInventory, title, style);
        this.blockMode = new ServerSettingToggleButton<>(Settings.BLOCK, YesNo.NO);
        this.addToLeftToolbar(this.blockMode);
        this.interfaceMode = new ToggleButton(Icon.INTERFACE_TERMINAL_SHOW, Icon.INTERFACE_TERMINAL_HIDE, GuiText.InterfaceTerminal.text(), GuiText.InterfaceTerminalHint.text(), (btn) -> {
            this.selectNextInterfaceMode();
        });
        this.addToLeftToolbar(this.interfaceMode);
        ItemRenderer itemRender = Minecraft.getInstance().getItemRenderer();
        ItemStack iconFluid = Api.instance().definitions().blocks().fluidIface().maybeStack(1).orElse(ItemStack.EMPTY);
        this.switchInterface = new TabButton(iconFluid, iconFluid.getDisplayName(), itemRender, btn ->
                NetworkHandler.instance().sendToServer(new SwitchGuisPacket(ContainerFluidDualInterface.TYPE)));
        this.widgets.add("switchInterface", switchInterface);
        ItemStack iconWrench = Api.instance().definitions().items().certusQuartzWrench().maybeStack(1).orElse(ItemStack.EMPTY);
        this.priorityBtn = new TabButton(iconWrench, GuiText.Priority.text(), itemRender, btn ->
                NetworkHandler.instance().sendToServer(new SwitchGuisPacket(ContainerFCPriority.TYPE)));
        this.widgets.add("priorityBtn", priorityBtn);
        fluidPacketBtn = new FCToggleButton(
                btn -> {
                    FCToggleButton fbtn = (FCToggleButton) btn;
                    NetworkManager.netHandler.sendToServer(new CPacketFluidCraftBtns("fluidPacket", fbtn.getActive() == 1));
                }, BlitMap.SEND_FLUID, BlitMap.SEND_PACKET);
        this.addToLeftToolbar(fluidPacketBtn);
        splittingBtn = new FCToggleButton(
                btn -> {
                    FCToggleButton fbtn = (FCToggleButton) btn;
                    NetworkManager.netHandler.sendToServer(new CPacketFluidCraftBtns("allowSplitting", fbtn.getActive() == 1));
                }, BlitMap.NOT_SPLITTING, BlitMap.SPLITTING);
        this.addToLeftToolbar(splittingBtn);
        blockingBtn = new FCToggleButton(
                btn -> {
                    FCToggleButton fbtn = (FCToggleButton) btn;
                    NetworkManager.netHandler.sendToServer(new CPacketFluidCraftBtns("blockModeEx", fbtn.getActive()));
                }, BlitMap.BLOCK_ALL, BlitMap.BLOCK_ITEM, BlitMap.BLOCK_FLUID);
        this.addToLeftToolbar(blockingBtn);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        this.blockMode.set(this.container.getBlockingMode());
        this.interfaceMode.setState(this.container.getInterfaceTerminalMode() == YesNo.YES);
        this.fluidPacketBtn.forceActive(this.container.fluidPacket ? 1 : 0);
        this.splittingBtn.forceActive(this.container.allowSplitting ? 1 : 0);
        this.blockingBtn.forceActive(this.container.blockModeEx);
        this.blockingBtn.visible = this.blockMode.getCurrentValue() == YesNo.YES;
    }

    private void selectNextInterfaceMode() {
        boolean backwards = this.isHandlingRightClick();
        NetworkHandler.instance().sendToServer(new ConfigButtonPacket(Settings.INTERFACE_TERMINAL, backwards));
    }

}