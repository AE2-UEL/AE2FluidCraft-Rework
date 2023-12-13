package com.glodblock.github.common.item.fake;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.fluids.util.AEFluidStack;
import appeng.util.item.AEItemStack;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.loader.FCItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.Objects;

public final class FakeFluids {

    public static void init() {
        FakeItemRegister.registerHandler(
                ItemFluidDrop.class,
                new FakeItemHandler<FluidStack, IAEFluidStack>() {

                    @Override
                    public FluidStack getStack(ItemStack stack) {
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

                    @Override
                    public FluidStack getStack(@Nullable IAEItemStack stack) {
                        return stack == null ? null : getStack(stack.createItemStack());
                    }

                    @Override
                    public IAEFluidStack getAEStack(ItemStack stack) {
                        return getAEStack(AEItemStack.fromItemStack(stack));
                    }

                    @Override
                    public IAEFluidStack getAEStack(@Nullable IAEItemStack stack) {
                        if (stack == null) {
                            return null;
                        }
                        IAEFluidStack fluidStack = AEFluidStack.fromFluidStack(getStack(stack.createItemStack()));
                        if (fluidStack == null) {
                            return null;
                        }
                        fluidStack.setStackSize(stack.getStackSize());
                        return fluidStack;
                    }

                    @Override
                    public ItemStack packStack(FluidStack fluid) {
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

                    @Override
                    public ItemStack displayStack(FluidStack target) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public IAEItemStack packAEStack(FluidStack fluid) {
                        if (fluid == null || fluid.amount <= 0) {
                            return null;
                        }
                        IAEItemStack stack = AEItemStack.fromItemStack(packStack(fluid));
                        if (stack == null) {
                            return null;
                        }
                        stack.setStackSize(fluid.amount);
                        return stack;
                    }

                    @Override
                    public IAEItemStack packAEStackLong(IAEFluidStack fluid) {
                        if (fluid == null || fluid.getStackSize() <= 0) {
                            return null;
                        }
                        IAEItemStack stack = AEItemStack.fromItemStack(packStack(fluid.getFluidStack()));
                        if (stack == null) {
                            return null;
                        }
                        stack.setStackSize(fluid.getStackSize());
                        return stack;
                    }
                }
        );
        FakeItemRegister.registerHandler(
                ItemFluidPacket.class,
                new FakeItemHandler<FluidStack, IAEFluidStack>() {

                    @Override
                    public FluidStack getStack(ItemStack stack) {
                        if (stack.isEmpty() || !stack.hasTagCompound()) {
                            return null;
                        }
                        FluidStack fluid = FluidStack.loadFluidStackFromNBT(Objects.requireNonNull(stack.getTagCompound()).getCompoundTag("FluidStack"));
                        return (fluid != null && fluid.amount > 0) ? fluid : null;
                    }

                    @Override
                    public FluidStack getStack(@Nullable IAEItemStack stack) {
                        return stack != null ? getStack(stack.createItemStack()) : null;
                    }

                    @Override
                    public IAEFluidStack getAEStack(ItemStack stack) {
                        return getAEStack(AEItemStack.fromItemStack(stack));
                    }

                    @Override
                    public IAEFluidStack getAEStack(@Nullable IAEItemStack stack) {
                        if (stack == null) {
                            return null;
                        }
                        IAEFluidStack fluidStack = AEFluidStack.fromFluidStack(getStack(stack.createItemStack()));
                        if (fluidStack == null) {
                            return null;
                        }
                        fluidStack.setStackSize(stack.getStackSize());
                        return fluidStack;
                    }

                    @Override
                    public ItemStack packStack(FluidStack fluid) {
                        if (fluid == null || fluid.amount == 0) {
                            return ItemStack.EMPTY;
                        }
                        ItemStack stack = new ItemStack(FCItems.FLUID_PACKET);
                        NBTTagCompound tag = new NBTTagCompound();
                        NBTTagCompound fluidTag = new NBTTagCompound();
                        fluid.writeToNBT(fluidTag);
                        tag.setTag("FluidStack", fluidTag);
                        stack.setTagCompound(tag);
                        return stack;
                    }

                    @Override
                    public ItemStack displayStack(FluidStack fluid) {
                        if (fluid == null) {
                            return ItemStack.EMPTY;
                        }
                        FluidStack copy = fluid.copy();
                        copy.amount = 1000;
                        ItemStack stack = new ItemStack(FCItems.FLUID_PACKET);
                        NBTTagCompound tag = new NBTTagCompound();
                        NBTTagCompound fluidTag = new NBTTagCompound();
                        copy.writeToNBT(fluidTag);
                        tag.setTag("FluidStack", fluidTag);
                        tag.setBoolean("DisplayOnly", true);
                        stack.setTagCompound(tag);
                        return stack;
                    }

                    @Override
                    public IAEItemStack packAEStack(FluidStack target) {
                        return AEItemStack.fromItemStack(packStack(target));
                    }

                    @Override
                    public IAEItemStack packAEStackLong(IAEFluidStack target) {
                        return AEItemStack.fromItemStack(packStack(target.getFluidStack()));
                    }
                }
        );
    }

    public static boolean isFluidFakeItem(ItemStack stack) {
        return stack.getItem() == FCItems.FLUID_PACKET || stack.getItem() == FCItems.FLUID_DROP;
    }

    public static ItemStack packFluid2Drops(@Nullable FluidStack stack) {
        return FakeItemRegister.packStack(stack, FCItems.FLUID_DROP);
    }

    public static IAEItemStack packFluid2AEDrops(@Nullable FluidStack stack) {
        return FakeItemRegister.packAEStack(stack, FCItems.FLUID_DROP);
    }

    public static IAEItemStack packFluid2AEDrops(@Nullable IAEFluidStack stack) {
        return FakeItemRegister.packAEStackLong(stack, FCItems.FLUID_DROP);
    }

    public static ItemStack packFluid2Packet(@Nullable FluidStack stack) {
        return FakeItemRegister.packStack(stack, FCItems.FLUID_PACKET);
    }

    public static IAEItemStack packFluid2AEPacket(@Nullable FluidStack stack) {
        return FakeItemRegister.packAEStack(stack, FCItems.FLUID_PACKET);
    }

    public static ItemStack displayFluid(@Nullable FluidStack stack) {
        return FakeItemRegister.displayStack(stack, FCItems.FLUID_PACKET);
    }

}
