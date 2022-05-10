package com.glodblock.github.common.item;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.NameConst;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class ItemFluidDrop extends Item {

    @SideOnly(Side.CLIENT)
    public IIcon shape;

    @Override
    @SuppressWarnings("unchecked")
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        if (CreativeTabs.tabMisc.equals(tab)) {
            list.add(newStack(new FluidStack(FluidRegistry.WATER, 1)));
            list.add(newStack(new FluidStack(FluidRegistry.LAVA, 1)));
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        FluidStack fluid = getFluidStack(stack);
        return I18n.format("item.fluid_drop.name", fluid == null ? "???" : fluid.getLocalizedName());
    }

    @Override
    @SuppressWarnings("unchecked")
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean flag) {
        FluidStack fluid = getFluidStack(stack);
        if (fluid != null) {
            tooltip.add(String.format(EnumChatFormatting.GRAY + "%s, 1 mB", fluid.getLocalizedName()));
        } else {
            tooltip.add(EnumChatFormatting.RED + I18n.format(NameConst.TT_INVALID_FLUID));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister aIconRegister) {
        super.registerIcons(aIconRegister);
        shape = aIconRegister.registerIcon(NameConst.RES_KEY + NameConst.ITEM_FLUID_DROP);
    }

    public static ItemStack newStack(@Nullable FluidStack fluid) {
        if (fluid == null || fluid.amount <= 0) {
            return null;
        }
        ItemStack stack = new ItemStack(ItemAndBlockHolder.DROP, fluid.amount);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Fluid", fluid.getFluid().getName());
        stack.setTagCompound(tag);
        return stack;
    }

    public static FluidStack getFluidStack(ItemStack stack) {
        if (stack == null|| stack.getItem() != ItemAndBlockHolder.DROP || !stack.hasTagCompound()) {
            return null;
        }
        NBTTagCompound tag = Objects.requireNonNull(stack.getTagCompound());
        if (!tag.hasKey("Fluid", Constants.NBT.TAG_STRING)) {
            return null;
        }
        Fluid fluid = FluidRegistry.getFluid(tag.getString("Fluid").toLowerCase());
        if (fluid == null) {
            return null;
        }
        FluidStack fluidStack = new FluidStack(fluid, stack.stackSize);
        if (tag.hasKey("FluidTag", Constants.NBT.TAG_COMPOUND)) {
            fluidStack.tag = tag.getCompoundTag("FluidTag");
        }
        return fluidStack;
    }

    public static IAEFluidStack getAeFluidStack(@Nullable IAEItemStack stack) {
        if (stack == null) {
            return null;
        }
        IAEFluidStack fluidStack = AEFluidStack.create(getFluidStack(stack.getItemStack()));
        if (fluidStack == null) {
            return null;
        }
        fluidStack.setStackSize(stack.getStackSize());
        return fluidStack;
    }

    public static IAEItemStack newAeStack(@Nullable FluidStack fluid) {
        if (fluid == null || fluid.amount <= 0) {
            return null;
        }
        IAEItemStack stack = AEItemStack.create(newStack(fluid));
        if (stack == null) {
            return null;
        }
        stack.setStackSize(fluid.amount);
        return stack;
    }

    public static IAEItemStack newAeStack(@Nullable IAEFluidStack fluid) {
        if (fluid == null || fluid.getStackSize() <= 0) {
            return null;
        }
        IAEItemStack stack = AEItemStack.create(newStack(fluid.getFluidStack()));
        if (stack == null) {
            return null;
        }
        stack.setStackSize(fluid.getStackSize());
        return stack;
    }

    public ItemFluidDrop register() {
        GameRegistry.registerItem(this, NameConst.ITEM_FLUID_DROP, FluidCraft.MODID);
        return this;
    }

}
