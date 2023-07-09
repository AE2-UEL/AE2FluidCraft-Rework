package com.glodblock.github.common.item;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.fluids.util.AEFluidStack;
import appeng.util.item.AEItemStack;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.NameConst;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static com.glodblock.github.loader.FCItems.defaultProps;

public class ItemFluidDrop extends Item {

    public ItemFluidDrop() {
        super(defaultProps());
    }

    @Override
    public void fillItemGroup(@Nonnull ItemGroup tab, @Nonnull NonNullList<ItemStack> items) {
        if (isInGroup(tab)) {
            items.add(newStack(new FluidStack(Fluids.WATER, 1)));
            items.add(newStack(new FluidStack(Fluids.LAVA, 1)));
        }
    }

    @Override
    @Nonnull
    public ITextComponent getDisplayName(@Nonnull ItemStack stack) {
        FluidStack fluid = getFluidStack(stack);
        return new TranslationTextComponent(getTranslationKey(stack),
                !fluid.isEmpty() ? fluid.getDisplayName() : "???"
        );
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn) {
        FluidStack fluid = getFluidStack(stack);
        if (!fluid.isEmpty()) {
            tooltip.add(new TranslationTextComponent("%s, 1 mB", fluid.getDisplayName()).mergeStyle(TextFormatting.GRAY));
        } else {
            tooltip.add(new TranslationTextComponent(NameConst.TT_INVALID_FLUID).mergeStyle(TextFormatting.RED));
        }
    }

    @Nonnull
    public static FluidStack getFluidStack(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != FCItems.FLUID_DROP || !stack.hasTag()) {
            return FluidStack.EMPTY;
        }
        CompoundNBT tag = Objects.requireNonNull(stack.getTag());
        if (!tag.contains("Fluid", Constants.NBT.TAG_STRING)) {
            return FluidStack.EMPTY;
        }
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(tag.getString("Fluid")));
        if (fluid == null || fluid == Fluids.EMPTY) {
            return FluidStack.EMPTY;
        }
        FluidStack fluidStack = new FluidStack(fluid, stack.getCount());
        if (tag.contains("FluidTag", Constants.NBT.TAG_COMPOUND)) {
            fluidStack.setTag(tag.getCompound("FluidTag"));
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

    @Nonnull
    public static ItemStack newStack(@Nonnull FluidStack fluid) {
        if (fluid.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(FCItems.FLUID_DROP, fluid.getAmount());
        CompoundNBT tag = new CompoundNBT();
        tag.putString("Fluid", String.valueOf(fluid.getFluid().getRegistryName()));
        if (fluid.hasTag()) {
            tag.put("FluidTag", fluid.getTag());
        }
        stack.setTag(tag);
        return stack;
    }

    @Nullable
    public static IAEItemStack newAeStack(@Nonnull FluidStack fluid) {
        if (fluid.isEmpty()) {
            return null;
        }
        IAEItemStack stack = AEItemStack.fromItemStack(newStack(fluid));
        if (stack == null) {
            return null;
        }
        stack.setStackSize(fluid.getAmount());
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