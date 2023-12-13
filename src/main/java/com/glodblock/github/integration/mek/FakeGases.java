package com.glodblock.github.integration.mek;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import com.glodblock.github.common.item.ItemGasDrop;
import com.glodblock.github.common.item.ItemGasPacket;
import com.glodblock.github.common.item.fake.FakeItemHandler;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.the9grounds.aeadditions.api.gas.IAEGasStack;
import com.the9grounds.aeadditions.integration.mekanism.gas.AEGasStack;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Objects;

public class FakeGases {

    public static void init() {
        FakeItemRegister.registerHandler(
                ItemGasDrop.class,
                new FakeItemHandler<GasStack, IAEGasStack>() {

                    @Override
                    public GasStack getStack(ItemStack stack) {
                        if (stack.isEmpty() || stack.getItem() != FCGasItems.GAS_DROP || !stack.hasTagCompound()) {
                            return null;
                        }
                        NBTTagCompound tag = Objects.requireNonNull(stack.getTagCompound());
                        if (!tag.hasKey("Gas", Constants.NBT.TAG_STRING)) {
                            return null;
                        }
                        Gas gas = GasRegistry.getGas(tag.getString("Gas"));
                        if (gas == null) {
                            return null;
                        }
                        return new GasStack(gas, stack.getCount());
                    }

                    @Override
                    public GasStack getStack(@Nullable IAEItemStack stack) {
                        return stack == null ? null : getStack(stack.createItemStack());
                    }

                    @Override
                    public IAEGasStack getAEStack(ItemStack stack) {
                        return getAEStack(AEItemStack.fromItemStack(stack));
                    }

                    @Override
                    public IAEGasStack getAEStack(@Nullable IAEItemStack stack) {
                        if (stack == null) {
                            return null;
                        }
                        GasStack gas = getStack(stack.createItemStack());
                        if (gas == null || gas.getGas() == null) {
                            return null;
                        }
                        IAEGasStack gasStack = new AEGasStack(gas);
                        gasStack.setStackSize(stack.getStackSize());
                        return gasStack;
                    }

                    @Override
                    public ItemStack packStack(GasStack gas) {
                        if (gas == null || gas.amount <= 0) {
                            return ItemStack.EMPTY;
                        }
                        ItemStack stack = new ItemStack(FCGasItems.GAS_DROP, gas.amount);
                        NBTTagCompound tag = new NBTTagCompound();
                        tag.setString("Gas", gas.getGas().getName());
                        stack.setTagCompound(tag);
                        return stack;
                    }

                    @Override
                    public ItemStack displayStack(GasStack target) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public IAEItemStack packAEStack(GasStack gas) {
                        if (gas == null || gas.amount <= 0) {
                            return null;
                        }
                        IAEItemStack stack = AEItemStack.fromItemStack(packStack(gas));
                        if (stack == null) {
                            return null;
                        }
                        stack.setStackSize(gas.amount);
                        return stack;
                    }

                    @Override
                    public IAEItemStack packAEStackLong(IAEGasStack gas) {
                        if (gas == null || gas.getStackSize() <= 0) {
                            return null;
                        }
                        IAEItemStack stack = AEItemStack.fromItemStack(packStack(new GasStack((Gas) gas.getGas(), 1)));
                        if (stack == null) {
                            return null;
                        }
                        stack.setStackSize(gas.getStackSize());
                        return stack;
                    }
                }
        );
        FakeItemRegister.registerHandler(
                ItemGasPacket.class,
                new FakeItemHandler<GasStack, IAEGasStack>() {
                    @Override
                    public GasStack getStack(ItemStack stack) {
                        if (stack.isEmpty() || !stack.hasTagCompound()) {
                            return null;
                        }
                        GasStack gas = GasStack.readFromNBT(Objects.requireNonNull(stack.getTagCompound()).getCompoundTag("GasStack"));
                        return (gas != null && gas.amount > 0) ? gas : null;
                    }

                    @Override
                    public GasStack getStack(@Nullable IAEItemStack stack) {
                        return stack != null ? getStack(stack.createItemStack()) : null;
                    }

                    @Override
                    public IAEGasStack getAEStack(ItemStack stack) {
                        return getAEStack(AEItemStack.fromItemStack(stack));
                    }

                    @Override
                    public IAEGasStack getAEStack(@Nullable IAEItemStack stack) {
                        if (stack == null) {
                            return null;
                        }
                        GasStack gas = getStack(stack.createItemStack());
                        if (gas == null || gas.getGas() == null) {
                            return null;
                        }
                        IAEGasStack gasStack = new AEGasStack(gas);
                        gasStack.setStackSize(stack.getStackSize());
                        return gasStack;
                    }

                    @Override
                    public ItemStack packStack(GasStack gas) {
                        if (gas == null || gas.amount == 0) {
                            return ItemStack.EMPTY;
                        }
                        ItemStack stack = new ItemStack(FCGasItems.GAS_PACKET);
                        NBTTagCompound tag = new NBTTagCompound();
                        NBTTagCompound fluidTag = new NBTTagCompound();
                        gas.write(fluidTag);
                        tag.setTag("GasStack", fluidTag);
                        stack.setTagCompound(tag);
                        return stack;
                    }

                    @Override
                    public ItemStack displayStack(GasStack gas) {
                        if (gas == null) {
                            return ItemStack.EMPTY;
                        }
                        GasStack copy = gas.copy();
                        copy.amount = 1000;
                        ItemStack stack = new ItemStack(FCGasItems.GAS_PACKET);
                        NBTTagCompound tag = new NBTTagCompound();
                        NBTTagCompound fluidTag = new NBTTagCompound();
                        copy.write(fluidTag);
                        tag.setTag("GasStack", fluidTag);
                        tag.setBoolean("DisplayOnly", true);
                        stack.setTagCompound(tag);
                        return stack;
                    }

                    @Override
                    public IAEItemStack packAEStack(GasStack target) {
                        return AEItemStack.fromItemStack(packStack(target));
                    }

                    @Override
                    public IAEItemStack packAEStackLong(IAEGasStack target) {
                        return AEItemStack.fromItemStack(packStack((GasStack) target.getGasStack()));
                    }
                }
        );
    }

    public static boolean isGasFakeItem(ItemStack stack) {
        return stack.getItem() == FCGasItems.GAS_DROP || stack.getItem() == FCGasItems.GAS_PACKET;
    }

    public static ItemStack packGas2Drops(@Nullable GasStack stack) {
        return FakeItemRegister.packStack(stack, FCGasItems.GAS_DROP);
    }

    public static IAEItemStack packGas2AEDrops(@Nullable GasStack stack) {
        return FakeItemRegister.packAEStack(stack, FCGasItems.GAS_DROP);
    }

    public static IAEItemStack packGas2AEDrops(@Nullable IAEGasStack stack) {
        return FakeItemRegister.packAEStackLong(stack, FCGasItems.GAS_DROP);
    }

    public static ItemStack packGas2Packet(@Nullable GasStack stack) {
        return FakeItemRegister.packStack(stack, FCGasItems.GAS_PACKET);
    }

    public static IAEItemStack packGas2AEPacket(@Nullable GasStack stack) {
        return FakeItemRegister.packAEStack(stack, FCGasItems.GAS_PACKET);
    }

    public static ItemStack displayGas(@Nullable GasStack stack) {
        return FakeItemRegister.displayStack(stack, FCGasItems.GAS_PACKET);
    }

}
