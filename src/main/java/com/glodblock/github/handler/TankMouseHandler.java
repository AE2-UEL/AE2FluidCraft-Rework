package com.glodblock.github.handler;

import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.util.IAEFluidTank;
import com.glodblock.github.util.MouseRegionManager;
import com.glodblock.github.util.NameConst;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

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
    public List<ITextComponent> getTooltip() {
        IAEFluidStack fluid = tank.getFluidInSlot(index);
        return Arrays.asList(
                new TranslationTextComponent(fluid != null ? fluid.getFluidStack().getTranslationKey() : NameConst.TT_EMPTY),
                new StringTextComponent(
                        TextFormatting.GRAY + String.format("%,d / %,d mB",
                        fluid != null ? fluid.getStackSize() : 0L, tank.getTankCapacity(index)))
        );
    }

}
