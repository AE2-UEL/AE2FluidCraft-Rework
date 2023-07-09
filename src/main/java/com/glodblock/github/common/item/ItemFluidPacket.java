package com.glodblock.github.common.item;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import com.glodblock.github.interfaces.HasCustomModel;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.NameConst;
import net.minecraft.client.util.ITooltipFlag;
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
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static com.glodblock.github.loader.FCItems.defaultProps;

public class ItemFluidPacket extends Item implements HasCustomModel {

    public ItemFluidPacket() {
        super(defaultProps().maxStackSize(1));
    }

    @Override
    public void fillItemGroup(@Nonnull ItemGroup tab, @Nonnull NonNullList<ItemStack> items) {
        // NO-OP
    }

    @Override
    @Nonnull
    public ITextComponent getDisplayName(@Nonnull ItemStack stack) {
        FluidStack fluid = getFluidStack(stack);
        boolean display = isDisplay(stack);
        if (display) {
            return !fluid.isEmpty() ? fluid.getDisplayName() : super.getDisplayName(stack);
        }
        return !fluid.isEmpty() ?
                new TranslationTextComponent(NameConst.TT_FLUID_PACKET_INFO, fluid.getDisplayName(), String.format("%,d", fluid.getAmount()))
                : super.getDisplayName(stack);
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn) {
        FluidStack fluid = getFluidStack(stack);
        boolean display = isDisplay(stack);
        if (display) return;
        if (!fluid.isEmpty()) {
            tooltip.add(new TranslationTextComponent(NameConst.TT_FLUID_PACKET).mergeStyle(TextFormatting.GRAY));
        } else {
            tooltip.add(new TranslationTextComponent(NameConst.TT_INVALID_FLUID).mergeStyle(TextFormatting.RED));
        }
    }

    @Nonnull
    public static FluidStack getFluidStack(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return FluidStack.EMPTY;
        }
        return FluidStack.loadFluidStackFromNBT(Objects.requireNonNull(stack.getTag()).getCompound("FluidStack"));
    }

    public static boolean isDisplay(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return false;
        }
        assert stack.getTag() != null;
        return stack.getTag().getBoolean("DisplayOnly");
    }

    @Nonnull
    public static FluidStack getFluidStack(@Nullable IAEItemStack stack) {
        return stack != null ? getFluidStack(stack.getDefinition()) : FluidStack.EMPTY;
    }

    @Nonnull
    public static ItemStack newStack(@Nonnull FluidStack fluid) {
        if (fluid.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(FCItems.FLUID_PACKET);
        CompoundNBT tag = new CompoundNBT();
        CompoundNBT fluidTag = new CompoundNBT();
        fluid.writeToNBT(fluidTag);
        tag.put("FluidStack", fluidTag);
        stack.setTag(tag);
        return stack;
    }

    @Nonnull
    public static ItemStack newDisplayStack(@Nonnull FluidStack fluid) {
        if (fluid.isEmpty()) {
            return ItemStack.EMPTY;
        }
        FluidStack copy = fluid.copy();
        copy.setAmount(1000);
        ItemStack stack = new ItemStack(FCItems.FLUID_PACKET);
        CompoundNBT tag = new CompoundNBT();
        CompoundNBT fluidTag = new CompoundNBT();
        copy.writeToNBT(fluidTag);
        tag.put("FluidStack", fluidTag);
        tag.putBoolean("DisplayOnly", true);
        stack.setTag(tag);
        return stack;
    }

    @Nullable
    public static IAEItemStack newAeStack(@Nonnull FluidStack fluid) {
        return AEItemStack.fromItemStack(newStack(fluid));
    }

    @Override
    public ResourceLocation getCustomModelPath() {
        return NameConst.MODEL_FLUID_PACKET;
    }

    public static boolean isFluidPacket(ItemStack is) {
        return !is.isEmpty() && is.getItem() instanceof ItemFluidPacket;
    }

}