package com.glodblock.github.client.container;

import appeng.api.storage.data.IAEFluidStack;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotNormal;
import appeng.fluids.container.IFluidSyncContainer;
import appeng.fluids.helper.FluidSyncHelper;
import appeng.fluids.util.IAEFluidTank;
import appeng.util.Platform;
import com.glodblock.github.common.tile.TileBurette;
import com.glodblock.github.interfaces.TankDumpable;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class ContainerBurette extends AEBaseContainer implements IFluidSyncContainer, TankDumpable {

    private final TileBurette tile;
    private final FluidSyncHelper fluidSync;

    public ContainerBurette(InventoryPlayer ipl, TileBurette tile) {
        super(ipl, tile);
        this.tile = tile;
        this.fluidSync = new FluidSyncHelper(tile.getFluidInventory(), 0);
        addSlotToContainer(new SlotNormal(tile.getInternalInventory(), 0, 52, 53));
        bindPlayerInventory(ipl, 0, 84);
    }

    public TileBurette getTile() {
        return tile;
    }

    public boolean canTranferFluid(boolean into) {
        IAEFluidTank tileTank = tile.getFluidInventory();
        IFluidTankProperties tileTankInfo = tileTank.getTankProperties()[0];
        IAEFluidStack tileFluid = tileTank.getFluidInSlot(0);
        if (into) {
            if (tileFluid != null && tileFluid.getStackSize() >= tileTankInfo.getCapacity()) {
                return false;
            }
        } else if (tileFluid == null) {
            return false;
        }
        ItemStack stack = tile.getInternalInventory().getStackInSlot(0);
        if (stack.isEmpty() || !stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            return false;
        }
        IFluidHandlerItem itemTank = Objects.requireNonNull(
                stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null));
        for (IFluidTankProperties itemTankInfo : itemTank.getTankProperties()) {
            if (into) {
                if (itemTankInfo.canDrain() && tileTankInfo.canFillFluidType(itemTankInfo.getContents())) {
                    return true;
                }
            } else if (itemTankInfo.canFillFluidType(tileFluid.getFluidStack())) {
                return true;
            }
        }
        return false;
    }

    public void tryTransferFluid(int amount, boolean into) {
        ItemStack stack = tile.getInternalInventory().getStackInSlot(0);
        if (stack.isEmpty() || !stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            return;
        }
        IAEFluidTank tileTank = tile.getFluidInventory();
        IFluidHandlerItem itemTank = Objects.requireNonNull(
                stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null));
        if (into) {
            transferFluidBetween(itemTank, tileTank, amount);
        } else {
            transferFluidBetween(tileTank, itemTank, amount);
        }
        tile.getInternalInventory().setStackInSlot(0, itemTank.getContainer());
    }

    private void transferFluidBetween(IFluidHandler from, IFluidHandler to, int amount) {
        // simulated pass to figure out the real amount we can transfer
        FluidStack fluid = from.drain(amount, false);
        if (fluid == null) {
            return;
        }
        amount = Math.min(amount, to.fill(fluid, false));
        // actually do the transfer
        fluid = from.drain(amount, true);
        if (fluid != null) {
            fluid.amount -= to.fill(fluid, true);
            if (fluid.amount > 0) { // just in case the tanks don't behave exactly as simulated
                from.fill(fluid, true);
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (Platform.isServer()) {
            fluidSync.sendDiff(listeners);
        }
    }

    @Override
    public void addListener(@Nonnull IContainerListener listener) {
        super.addListener(listener);
        fluidSync.sendFull(Collections.singleton(listener));
    }

    @Override
    public void receiveFluidSlots(Map<Integer, IAEFluidStack> fluids) {
        fluidSync.readPacket(fluids);
    }

    @Override
    public boolean canDumpTank(int index) {
        return tile.getFluidInventory().getFluidInSlot(0) != null;
    }

    @Override
    public void dumpTank(int index) {
        tile.getFluidInventory().setFluidInSlot(0, null);
    }

}
