package com.glodblock.github.client.container;

import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotFake;
import appeng.helpers.InventoryAction;
import appeng.tile.inventory.AppEngInternalAEInventory;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.tile.TileFluidLevelMaintainer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ContainerFluidLevelMaintainer extends AEBaseContainer {

    private final TileFluidLevelMaintainer tile;

    public ContainerFluidLevelMaintainer(InventoryPlayer ip, TileFluidLevelMaintainer tile) {
        super(ip, tile);
        this.tile = tile;
        IItemHandler inv = tile.getInventoryHandler();
        for (int i = 0; i < 5; i++) {
            addSlotToContainer(new DisplayFluidSlot(inv, i, 17, 19 + i * 20));
        }
        bindPlayerInventory(ip, 0, 132);
    }

    public TileFluidLevelMaintainer getTile() {
        return tile;
    }

    @Override
    public void doAction(EntityPlayerMP player, InventoryAction action, int slotId, long id) {
        Slot slot = getSlot(slotId);
        if (slot instanceof DisplayFluidSlot) {
            final ItemStack stack = player.inventory.getItemStack();
            slot.putStack(stack.isEmpty() ? stack : stack.copy());
        } else {
            super.doAction(player, action, slotId, id);
        }
    }

    public static class DisplayFluidSlot extends SlotFake {

        final AppEngInternalAEInventory handler;

        public DisplayFluidSlot(IItemHandler inv, int idx, int x, int y) {
            super(inv, idx, x, y);
            this.handler = (AppEngInternalAEInventory) inv;
        }

        @Override
        public void putStack(ItemStack is) {
            if (is.isEmpty()) {
                super.putStack(is);
                return;
            }
            if (is.getItem() instanceof ItemFluidPacket) {
                super.putStack(is);
                return;
            }
            if (is.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
                IFluidTankProperties[] tanks = Objects.requireNonNull(
                        is.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null))
                        .getTankProperties();
                for (IFluidTankProperties tank : tanks) {
                    ItemStack packet = ItemFluidPacket.newStack(tank.getContents());
                    super.putStack(packet);
                    return;
                }
            }
            super.putStack(ItemStack.EMPTY);
        }

        @Override
        public int getSlotStackLimit() {
            return Integer.MAX_VALUE;
        }

        @Override
        public int getItemStackLimit(@Nonnull ItemStack stack) {
            return Integer.MAX_VALUE;
        }

    }

}
