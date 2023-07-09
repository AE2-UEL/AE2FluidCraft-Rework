package com.glodblock.github.common.tile;

import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.IAEFluidInventory;
import appeng.fluids.util.IAEFluidTank;
import appeng.tile.AEBaseInvTileEntity;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.inv.InvOperation;
import com.glodblock.github.util.FCUtil;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.io.IOException;

public abstract class TileSimpleBuffer extends AEBaseInvTileEntity implements IAEFluidInventory {

    private final AppEngInternalInventory invItems = createItemBuffer();
    private final AEFluidInventory invFluids = createFluidBuffer();

    public TileSimpleBuffer(TileEntityType<?> type) {
        super(type);
    }

    abstract protected AppEngInternalInventory createItemBuffer();

    abstract protected AEFluidInventory createFluidBuffer();

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

    @Nonnull
    @SuppressWarnings("unchecked")
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return LazyOptional.of(() -> (T)invItems);
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return LazyOptional.of(() -> (T)invFluids);
        }
        return super.getCapability(capability, facing);
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
    protected void writeToStream(PacketBuffer data) throws IOException {
        super.writeToStream(data);
        FCUtil.writeFluidInventoryToBuffer(invFluids, data);
    }

    @Override
    protected boolean readFromStream(PacketBuffer data) throws IOException {
        boolean changed = super.readFromStream(data);
        changed |= FCUtil.readFluidInventoryToBuffer(invFluids, data);
        return changed;
    }

    @Override
    public void read(BlockState blockState, CompoundNBT data) {
        super.read(blockState, data);
        invFluids.readFromNBT(data, "FluidInv");
    }

    @Override
    public CompoundNBT write(CompoundNBT data) {
        super.write(data);
        invFluids.writeToNBT(data, "FluidInv");
        return data;
    }

}
