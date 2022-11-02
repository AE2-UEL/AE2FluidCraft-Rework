package com.glodblock.github.common.tile;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.IAEFluidInventory;
import appeng.fluids.util.IAEFluidTank;
import appeng.helpers.Reflected;
import appeng.parts.automation.UpgradeInventory;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.inv.InvOperation;
import com.glodblock.github.util.Util;
import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

public class TileFluidAssembler extends AENetworkInvTile implements IAEFluidInventory, ICraftingProvider {

    private final AppEngInternalInventory invItems = new AppEngInternalInventory(this, 9);
    private final AEFluidInventory invFluids = new AEFluidInventory(this, 9, 2000);
    // private final UpgradeInventory upgrades;
    private boolean isPowered = false;
    private double process;
    private static final double TIME = 100;

    @Reflected
    public TileFluidAssembler() {
        getProxy().setIdlePowerUsage(0.0D);
        // this.upgrades = new UpgradeInventory(assembler, this, this.getUpgradeSlots());
    }

    @Nonnull
    @Override
    public IItemHandler getInternalInventory() {
        return invItems;
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
    public void onChangeInventory(IItemHandler iItemHandler, int i, InvOperation invOperation, ItemStack itemStack, ItemStack itemStack1) {
        markForUpdate();
    }

    @Override
    protected void writeToStream(ByteBuf data) throws IOException {
        super.writeToStream(data);
        for (int i = 0; i < invItems.getSlots(); i++) {
            ByteBufUtils.writeItemStack(data, invItems.getStackInSlot(i));
        }
        Util.writeFluidInventoryToBuffer(invFluids, data);
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
        changed |= Util.readFluidInventoryToBuffer(invFluids, data);
        return changed;
    }

    private int getUpgradeSlots() {
        return 5;
    }



    @Override
    public DimensionalCoord getLocation() {
        return null;
    }

    @Nonnull
    @Override
    public AECableType getCableConnectionType(@Nonnull AEPartLocation aePartLocation) {
        return null;
    }

    @Override
    public void onFluidInventoryChanged(IAEFluidTank iaeFluidTank, int i) {
        saveChanges();
        markForUpdate();
    }

    @Override
    public void provideCrafting(ICraftingProviderHelper iCraftingProviderHelper) {

    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails iCraftingPatternDetails, InventoryCrafting inventoryCrafting) {
        return false;
    }

    @Override
    public boolean isBusy() {
        return false;
    }
}
