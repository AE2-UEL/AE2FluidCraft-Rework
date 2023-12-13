package com.glodblock.github.inventory;

import com.glodblock.github.common.item.fake.FakeFluids;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.integration.mek.FCGasItems;
import com.glodblock.github.integration.mek.FakeGases;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.ModAndClassUtil;
import mekanism.api.gas.GasStack;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class FluidConvertingInventoryCrafting extends InventoryCrafting {

    public FluidConvertingInventoryCrafting(Container container, int width, int height) {
        super(container, width, height);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (stack.getItem() == FCItems.FLUID_DROP) {
            FluidStack fluid = FakeItemRegister.getStack(stack);
            if (fluid != null) {
                super.setInventorySlotContents(index, FakeFluids.packFluid2Packet(new FluidStack(fluid, stack.getCount())));
            } else {
                // wtf?
                super.setInventorySlotContents(index, FakeFluids.packFluid2Packet(new FluidStack(FluidRegistry.WATER, 1000)));
            }
        } else if (ModAndClassUtil.GAS && stack.getItem() == FCGasItems.GAS_DROP) {
            GasStack gas = FakeItemRegister.getStack(stack);
            if (gas != null && gas.getGas() != null) {
                super.setInventorySlotContents(index, FakeGases.packGas2Packet(new GasStack(gas.getGas(), stack.getCount())));
            } else {
                // wtf?
                super.setInventorySlotContents(index, FakeFluids.packFluid2Packet(new FluidStack(FluidRegistry.WATER, 1000)));
            }
        }  else {
            super.setInventorySlotContents(index, stack);
        }
    }

}