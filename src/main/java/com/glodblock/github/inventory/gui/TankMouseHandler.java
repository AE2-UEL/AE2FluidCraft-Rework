package com.glodblock.github.inventory.gui;

import appeng.api.storage.data.IAEFluidStack;
import com.glodblock.github.inventory.IAEFluidTank;
import com.glodblock.github.util.NameConst;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class TankMouseHandler implements MouseRegionManager.Handler {

    private final IAEFluidTank tank;
    private final int index;

    public TankMouseHandler(IAEFluidTank tank, int index) {
        this.tank = tank;
        this.index = index;
    }

    @Nullable
    @Override
    public List<String> getTooltip() {
        IAEFluidStack fluid = tank.getFluidInSlot(index);
        return Arrays.asList(
            fluid != null ? fluid.getFluidStack().getLocalizedName() : I18n.format(NameConst.TT_EMPTY),
            EnumChatFormatting.GRAY + String.format("%,d / %,d mB",
                fluid != null ? fluid.getStackSize() : 0L, tank.getTankInfo(ForgeDirection.UNKNOWN)[index].capacity));
    }

}
