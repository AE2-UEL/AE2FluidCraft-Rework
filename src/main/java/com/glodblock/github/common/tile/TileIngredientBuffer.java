package com.glodblock.github.common.tile;

import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.AEFluidStack;
import appeng.fluids.util.IAEFluidInventory;
import appeng.fluids.util.IAEFluidTank;
import appeng.tile.AEBaseInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.inv.InvOperation;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

public class TileIngredientBuffer extends AEBaseInvTile implements IAEFluidInventory {

    private final AppEngInternalInventory invItems = new AppEngInternalInventory(this, 9);
    private final AEFluidInventory invFluids = new AEFluidInventory(this, 4, 16000);

    @Nonnull
    @Override
    public IItemHandler getInternalInventory() {
        return invItems;
    }

    public IAEFluidTank getFluidInventory() {
        return invFluids;
    }

    @Override
    public boolean canBeRotated() {
        return false;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
                || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T)invItems;
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T)invFluids;
        }
        return null;
    }

    @Override
    public void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removed, ItemStack added) {
        markForUpdate();
    }

    @Override
    public void onFluidInventoryChanged(IAEFluidTank inv, int slot) {
        saveChanges();
        markForUpdate();
    }

    @Override
    protected void writeToStream(ByteBuf data) throws IOException {
        super.writeToStream(data);
        for (int i = 0; i < invItems.getSlots(); i++) {
            ByteBufUtils.writeItemStack(data, invItems.getStackInSlot(i));
        }
        int fluidMask = 0;
        for (int i = 0; i < invFluids.getSlots(); i++) {
            if (invFluids.getFluidInSlot(i) != null) {
                fluidMask |= 1 << i;
            }
        }
        data.writeByte(fluidMask);
        for (int i = 0; i < invFluids.getSlots(); i++) {
            IAEFluidStack fluid = invFluids.getFluidInSlot(i);
            if (fluid != null) {
                fluid.writeToPacket(data);
            }
        }
    }

    @Override
    protected boolean readFromStream(ByteBuf data) throws IOException {
        boolean changed = super.readFromStream(data);
        for (int i = 0; i < invItems.getSlots(); i++) {
            ItemStack stack = ByteBufUtils.readItemStack(data);
            if (!ItemStack.areItemStacksEqual(stack, invItems.getStackInSlot(i))) {
                invItems.setStackInSlot(i, stack);
                changed = true;
            }
        }
        int fluidMask = data.readByte();
        for (int i = 0; i < invFluids.getSlots(); i++) {
            if ((fluidMask & (1 << i)) != 0) {
                IAEFluidStack fluid = AEFluidStack.fromPacket(data);
                if (fluid != null) { // this shouldn't happen, but better safe than sorry
                    IAEFluidStack origFluid = invFluids.getFluidInSlot(i);
                    if (!fluid.equals(origFluid) || fluid.getStackSize() != origFluid.getStackSize()) {
                        invFluids.setFluidInSlot(i, fluid);
                        changed = true;
                    }
                }
            } else if (invFluids.getFluidInSlot(i) != null) {
                invFluids.setFluidInSlot(i, null);
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        invItems.readFromNBT(data, "ItemInv");
        invFluids.readFromNBT(data, "FluidInv");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        invItems.writeToNBT(data, "ItemInv");
        invFluids.writeToNBT(data, "FluidInv");
        return data;
    }

}