package com.glodblock.github.common.item;

import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.interfaces.HasCustomModel;
import com.glodblock.github.util.NameConst;
import mekanism.api.gas.GasStack;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemGasPacket extends Item implements HasCustomModel {

    public ItemGasPacket() {
        setMaxStackSize(1);
    }

    @Override
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
        // NO-OP
    }

    @Override
    @Nonnull
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        GasStack gas = FakeItemRegister.getStack(stack);
        boolean display = isDisplay(stack);
        if (display) {
            return gas != null ? gas.getGas().getLocalizedName() : super.getItemStackDisplayName(stack);
        }
        return gas != null ? String.format("%s, %,d mB", gas.getGas().getLocalizedName(), gas.amount)
                : super.getItemStackDisplayName(stack);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flags) {
        GasStack gas = FakeItemRegister.getStack(stack);
        boolean display = isDisplay(stack);
        if (display) return;
        if (gas != null) {
            for (String line : I18n.translateToLocal(NameConst.TT_GAS_PACKET).split("\\\\n")) {
                tooltip.add(TextFormatting.GRAY + line);
            }
        } else {
            tooltip.add(TextFormatting.RED + I18n.translateToLocal(NameConst.TT_INVALID_FLUID));
        }
    }

    public static boolean isDisplay(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound() || stack.getTagCompound() == null) {
            return false;
        }
        return stack.getTagCompound().getBoolean("DisplayOnly");
    }

    @Override
    public ResourceLocation getCustomModelPath() {
        return NameConst.MODEL_GAS_PACKET;
    }

    public static boolean isGasPacket(ItemStack is) {
        return !is.isEmpty() && is.getItem() instanceof ItemGasPacket;
    }

}