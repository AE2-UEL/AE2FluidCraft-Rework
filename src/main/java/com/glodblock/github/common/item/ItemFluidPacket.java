package com.glodblock.github.common.item;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.NameConst;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class ItemFluidPacket extends Item {

    public ItemFluidPacket() {
        setUnlocalizedName(NameConst.ITEM_FLUID_PACKET);
        setMaxStackSize(1);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        FluidStack fluid = getFluidStack(stack);
        return fluid != null ? String.format("%s, %,d mB", fluid.getLocalizedName(), fluid.amount)
            : super.getItemStackDisplayName(stack);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean flags) {
        FluidStack fluid = getFluidStack(stack);
        if (fluid != null) {
            for (String line : I18n.format(NameConst.TT_FLUID_PACKET).split("\\\\n")) {
                tooltip.add(EnumChatFormatting.GRAY + line);
            }
        } else {
            tooltip.add(EnumChatFormatting.RED + I18n.format(NameConst.TT_INVALID_FLUID));
        }
    }

    public static FluidStack getFluidStack(ItemStack stack) {
        if (stack == null || !stack.hasTagCompound()) {
            return null;
        }
        FluidStack fluid = FluidStack.loadFluidStackFromNBT(Objects.requireNonNull(stack.getTagCompound()).getCompoundTag("FluidStack"));
        return (fluid != null && fluid.amount > 0) ? fluid : null;
    }

    public static FluidStack getFluidStack(@Nullable IAEItemStack stack) {
        return stack != null ? getFluidStack(stack.getItemStack()) : null;
    }

    public static ItemStack newStack(@Nullable FluidStack fluid) {
        if (fluid == null || fluid.amount == 0) {
            return null;
        }
        ItemStack stack = new ItemStack(ItemAndBlockHolder.PACKET);
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound fluidTag = new NBTTagCompound();
        fluid.writeToNBT(fluidTag);
        tag.setTag("FluidStack", fluidTag);
        stack.setTagCompound(tag);
        return stack;
    }

    @Nullable
    public static IAEItemStack newAeStack(@Nullable FluidStack fluid) {
        return AEItemStack.create(newStack(fluid));
    }

    @Override
    public int getSpriteNumber() {
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister aIconRegister) {
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int p_77617_1_) {
        return FluidRegistry.WATER.getStillIcon();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack aStack, int aRenderPass) {
        Fluid tFluid = FluidRegistry.getFluid(aStack.getItemDamage());
        return tFluid == null ? 16777215 : tFluid.getColor();
    }

    public ItemFluidPacket register() {
        GameRegistry.registerItem(this, NameConst.ITEM_FLUID_PACKET, FluidCraft.MODID);
        return this;
    }
}
