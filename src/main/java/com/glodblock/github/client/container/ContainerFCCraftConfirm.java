package com.glodblock.github.client.container;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.ITerminalHost;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.me.helpers.PlayerSource;
import com.glodblock.github.common.part.PartExtendedFluidPatternTerminal;
import com.glodblock.github.common.part.PartFluidPatternTerminal;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.Ae2Reflect;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class ContainerFCCraftConfirm extends ContainerCraftConfirm {

    public ContainerFCCraftConfirm(InventoryPlayer ip, ITerminalHost te) {
        super(ip, te);
    }

    @Override
    public void startJob() {
        GuiType originalGui = null;
        IActionHost ah = this.getActionHost();

        if (ah instanceof WirelessTerminalGuiObject) {
            ItemStack tool = ((WirelessTerminalGuiObject) ah).getItemStack();
            if (tool.getItem() == FCItems.WIRELESS_FLUID_PATTERN_TERMINAL) {
                originalGui = GuiType.WIRELESS_FLUID_PATTERN_TERMINAL;
            }
        }

        if (ah instanceof PartFluidPatternTerminal) {
            originalGui = GuiType.FLUID_PATTERN_TERMINAL;
        }

        if (ah instanceof PartExtendedFluidPatternTerminal) {
            originalGui = GuiType.FLUID_EXTENDED_PATTERN_TERMINAL;
        }

        IActionHost h = (IActionHost)this.getTarget();
        if (h != null) {
            IGridNode node = h.getActionableNode();
            IGrid grid = node.getGrid();
            if (Ae2Reflect.getCraftJob(this) != null && !this.isSimulation()) {
                ICraftingGrid cc = grid.getCache(ICraftingGrid.class);
                ICraftingLink g = cc.submitJob(Ae2Reflect.getCraftJob(this), null, this.getSelectedCpu() == -1 ? null : Ae2Reflect.getCraftingCPU(Ae2Reflect.getCPUs(this).get(this.getSelectedCpu())), true, this.getActionSrc());
                this.setAutoStart(false);
                if (g == null) {
                    this.setJob(cc.beginCraftingJob(this.getWorld(), grid, this.getActionSrc(), Ae2Reflect.getCraftJob(this).getOutput(), null));
                } else if (originalGui != null && this.getOpenContext() != null) {
                    ContainerOpenContext context = this.getOpenContext();
                    InventoryHandler.openGui(
                            this.getInventoryPlayer().player,
                            this.getInventoryPlayer().player.world,
                            new BlockPos(Ae2Reflect.getContextX(context), Ae2Reflect.getContextY(context), Ae2Reflect.getContextZ(context)),
                            this.getOpenContext().getSide().getFacing(),
                            originalGui
                    );
                }
            }
        }
    }

    private IActionSource getActionSrc() {
        return new PlayerSource(this.getPlayerInv().player, (IActionHost)this.getTarget());
    }

}
