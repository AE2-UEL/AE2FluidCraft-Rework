package com.glodblock.github.common.item;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.fluids.util.AEFluidStack;
import appeng.util.item.AEItemStack;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.NameConst;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class ItemFluidDrop extends Item {

    @Override
    public void getSubItems(@Nonnull CreativeTabs tab,@Nonnull NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            items.add(newStack(new FluidStack(FluidRegistry.WATER, 1)));
            items.add(newStack(new FluidStack(FluidRegistry.LAVA, 1)));
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    @Nonnull
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        FluidStack fluid = getFluidStack(stack);
        // would like to use I18n::format instead of this deprecated function, but that only exists on the client :/
        return I18n.translateToLocalFormatted(getTranslationKey(stack) + ".name", fluid != null ? fluid.getLocalizedName() : "???");
    }

    @SuppressWarnings("deprecation")
    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flags) {
        FluidStack fluid = getFluidStack(stack);
        if (fluid != null) {
            tooltip.add(String.format(TextFormatting.GRAY + "%s, 1 mB", fluid.getLocalizedName()));
        } else {
            tooltip.add(TextFormatting.RED + I18n.translateToLocal(NameConst.TT_INVALID_FLUID));
        }
    }

    @Nullable
    public static FluidStack getFluidStack(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != FCItems.FLUID_DROP || !stack.hasTagCompound()) {
            return null;
        }
        NBTTagCompound tag = Objects.requireNonNull(stack.getTagCompound());
        if (!tag.hasKey("Fluid", Constants.NBT.TAG_STRING)) {
            return null;
        }
        Fluid fluid = FluidRegistry.getFluid(tag.getString("Fluid"));
        if (fluid == null) {
            return null;
        }
        FluidStack fluidStack = new FluidStack(fluid, stack.getCount());
        if (tag.hasKey("FluidTag", Constants.NBT.TAG_COMPOUND)) {
            fluidStack.tag = tag.getCompoundTag("FluidTag");
        }
        return fluidStack;
    }

    @Nullable
    public static IAEFluidStack getAeFluidStack(@Nullable IAEItemStack stack) {
        if (stack == null) {
            return null;
        }
        IAEFluidStack fluidStack = AEFluidStack.fromFluidStack(getFluidStack(stack.getDefinition()));
        if (fluidStack == null) {
            return null;
        }
        fluidStack.setStackSize(stack.getStackSize());
        return fluidStack;
    }

    public static ItemStack newStack(@Nullable FluidStack fluid) {
        if (fluid == null || fluid.amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(FCItems.FLUID_DROP, fluid.amount);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Fluid", fluid.getFluid().getName());
        if (fluid.tag != null) {
            tag.setTag("FluidTag", fluid.tag);
        }
        stack.setTagCompound(tag);
        return stack;
    }

    @Nullable
    public static IAEItemStack newAeStack(@Nullable FluidStack fluid) {
        if (fluid == null || fluid.amount <= 0) {
            return null;
        }
        IAEItemStack stack = AEItemStack.fromItemStack(newStack(fluid));
        if (stack == null) {
            return null;
        }
        stack.setStackSize(fluid.amount);
        return stack;
    }

    @Nullable
    public static IAEItemStack newAeStack(@Nullable IAEFluidStack fluid) {
        if (fluid == null || fluid.getStackSize() <= 0) {
            return null;
        }
        IAEItemStack stack = AEItemStack.fromItemStack(newStack(fluid.getFluidStack()));
        if (stack == null) {
            return null;
        }
        stack.setStackSize(fluid.getStackSize());
        return stack;
    }

}