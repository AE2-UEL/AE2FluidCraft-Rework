package com.glodblock.github.integration.jei;

import appeng.api.storage.data.IAEItemStack;
import appeng.container.slot.SlotFake;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.helpers.InventoryAction;
import com.glodblock.github.common.item.fake.FakeFluids;
import com.glodblock.github.integration.mek.FakeGases;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.Util;
import mekanism.api.gas.GasStack;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;

public class FluidPacketTarget implements IGhostIngredientHandler.Target<Object> {

    private final int left;
    private final int top;
    private final Slot slot;

    public FluidPacketTarget(int guiLeft, int guiTop, Slot slot) {
        this.left = guiLeft;
        this.top = guiTop;
        this.slot = slot;
    }

    @Nonnull
    @Override
    public Rectangle getArea() {
        return new Rectangle(left + slot.xPos, top + slot.yPos, 16, 16);
    }

    @Override
    public void accept(@Nonnull Object ingredient) {
        FluidStack fluid = covertFluid(ingredient);
        Object gas = covertGas(ingredient);
        if (fluid == null && gas == null) {
            return;
        }
        IAEItemStack packet;
        if (fluid != null) {
            packet = FakeFluids.packFluid2AEPacket(fluid);
        } else {
            packet = FakeGases.packGas2AEPacket((GasStack) gas);
        }
        final PacketInventoryAction p;
        try {
            p = new PacketInventoryAction(InventoryAction.PLACE_JEI_GHOST_ITEM, (SlotFake) slot, packet);
            NetworkHandler.instance().sendToServer(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static FluidStack covertFluid(Object ingredient) {
        if (ingredient instanceof FluidStack) {
            return (FluidStack) ingredient;
        } else if (ingredient instanceof ItemStack) {
            return Util.getFluidFromItem((ItemStack) ingredient);
        }
        return null;
    }

    public static Object covertGas(Object ingredient) {
        if (ModAndClassUtil.GAS) {
            if (ingredient instanceof GasStack) {
                return ingredient;
            } else if (ingredient instanceof ItemStack) {
                return Util.getGasFromItem((ItemStack) ingredient);
            }
        }
        return null;
    }

}
