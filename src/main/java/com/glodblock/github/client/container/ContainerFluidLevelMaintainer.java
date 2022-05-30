package com.glodblock.github.client.container;

import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotFake;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.tile.TileFluidLevelMaintainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandler;

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

    private static class DisplayFluidSlot extends SlotFake {

        public DisplayFluidSlot(IItemHandler inv, int idx, int x, int y) {
            super(inv, idx, x, y);
        }

        @Override
        public void putStack(ItemStack is) {
            //System.out.print(is + "\n");
            if (is.isEmpty()) {
                super.putStack(is);
                return;
            }
            if (is.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
                IFluidTankProperties[] tanks = Objects.requireNonNull(
                        is.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null))
                        .getTankProperties();
                for (IFluidTankProperties tank : tanks) {
                    ItemStack drops = ItemFluidDrop.newStack(tank.getContents());
                    super.putStack(drops);
                    return;
                }
            }
            super.putStack(ItemStack.EMPTY);
        }

        /*@Override
        public ItemStack getDisplayStack() {
            ItemStack drops = super.getDisplayStack();
            System.out.print(drops + "\n");
            FluidStack fluid = ItemFluidDrop.getFluidStack(drops);
            System.out.print(fluid + "\n");
            return ItemFluidPacket.newDisplayStack(fluid);
        }*/

    }

}
