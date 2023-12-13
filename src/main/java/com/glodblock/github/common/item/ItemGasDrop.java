package com.glodblock.github.common.item;

import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.integration.mek.FakeGases;
import com.glodblock.github.util.NameConst;
import mekanism.api.gas.GasStack;
import mekanism.common.MekanismFluids;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemGasDrop extends Item {

    @Override
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            items.add(FakeGases.packGas2Drops(new GasStack(MekanismFluids.Hydrogen, 1)));
            items.add(FakeGases.packGas2Drops(new GasStack(MekanismFluids.Ethene, 1)));
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    @Nonnull
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        GasStack gas = FakeItemRegister.getStack(stack);
        return I18n.translateToLocalFormatted(getTranslationKey(stack) + ".name", gas != null ? gas.getGas().getLocalizedName() : "???");
    }

    @SuppressWarnings("deprecation")
    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flags) {
        GasStack gas = FakeItemRegister.getStack(stack);
        if (gas != null) {
            tooltip.add(String.format(TextFormatting.GRAY + "%s, 1 mB", gas.getGas().getLocalizedName()));
        } else {
            tooltip.add(TextFormatting.RED + I18n.translateToLocal(NameConst.TT_INVALID_FLUID));
        }
    }

}