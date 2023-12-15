package com.glodblock.github.util;

import appeng.api.AEApi;
import appeng.api.features.ILocatable;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.features.IWirelessTermRegistry;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.core.localization.PlayerMessages;
import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.AEFluidStack;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.item.ItemGasPacket;
import com.glodblock.github.common.item.fake.FakeFluids;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.integration.mek.FakeGases;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTankInfo;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class Util {

    public static IStorageChannel<IAEFluidStack> getFluidChannel() {
        return AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
    }

    public static IStorageChannel<IAEItemStack> getItemChannel() {
        return AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
    }

    public static FluidStack getFluidFromItem(ItemStack stack) {
        if (!stack.isEmpty() && stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            if (stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) != null) {
                IFluidTankProperties[] tanks = Objects.requireNonNull(stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)).getTankProperties();
                for (IFluidTankProperties tank : tanks) {
                    if (tank != null && tank.getContents() != null) {
                        return tank.getContents().copy();
                    }
                }
            }
        }
        return null;
    }

    public static Object getGasFromItem(ItemStack stack) {
        if (!stack.isEmpty() && stack.hasCapability(Capabilities.GAS_HANDLER_CAPABILITY, null)) {
            if (stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY, null) != null) {
                GasTankInfo[] tanks = Objects.requireNonNull(stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY, null)).getTankInfo();
                for (GasTankInfo tank : tanks) {
                    if (tank != null && tank.getGas() != null && tank.getGas().amount > 0) {
                        return tank.getGas().copy();
                    }
                }
            }
        }
        return null;
    }

    public static ItemStack getEmptiedContainer(ItemStack stack) {
        if (!stack.isEmpty() && stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            ItemStack dummy = stack.copy();
            IFluidHandlerItem fh = dummy.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fh != null) {
                fh.drain(Integer.MAX_VALUE, true);
                return fh.getContainer();
            }
        }
        return stack;
    }

    public static void writeFluidInventoryToBuffer(@Nonnull AEFluidInventory inv, ByteBuf data) throws IOException {
        int fluidMask = 0;
        for (int i = 0; i < inv.getSlots(); i++) {
            if (inv.getFluidInSlot(i) != null) {
                fluidMask |= 1 << i;
            }
        }
        data.writeByte(fluidMask);
        for (int i = 0; i < inv.getSlots(); i++) {
            IAEFluidStack fluid = inv.getFluidInSlot(i);
            if (fluid != null) {
                fluid.writeToPacket(data);
            }
        }
    }

    public static boolean readFluidInventoryToBuffer(@Nonnull AEFluidInventory inv, ByteBuf data) throws IOException {
        boolean changed = false;
        int fluidMask = data.readByte();
        for (int i = 0; i < inv.getSlots(); i++) {
            if ((fluidMask & (1 << i)) != 0) {
                IAEFluidStack fluid = AEFluidStack.fromPacket(data);
                if (fluid != null) { // this shouldn't happen, but better safe than sorry
                    IAEFluidStack origFluid = inv.getFluidInSlot(i);
                    if (!fluid.equals(origFluid) || fluid.getStackSize() != origFluid.getStackSize()) {
                        inv.setFluidInSlot(i, fluid);
                        changed = true;
                    }
                }
            } else if (inv.getFluidInSlot(i) != null) {
                inv.setFluidInSlot(i, null);
                changed = true;
            }
        }
        return changed;
    }

    public static void fuzzyTransferItems(int slot, ItemStack[] inputs, ItemStack[] des, IItemList<IAEItemStack> storage) {
        if (slot < des.length && inputs.length > 0) {
            if (storage != null) {
                IAEItemStack select = AEItemStack.fromItemStack(inputs[0]);
                for (ItemStack item : inputs) {
                    IAEItemStack result = storage.findPrecise(AEItemStack.fromItemStack(item));
                    if (result != null) {
                        select = AEItemStack.fromItemStack(item);
                        break;
                    }
                }
                if (select != null) {
                    des[slot] = select.createItemStack();
                }
            } else {
                des[slot] = inputs[0];
            }
        }
    }

    public static void clearItemInventory(IItemHandlerModifiable inv) {
        for (int i = 0; i < inv.getSlots(); i ++) {
            inv.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    public static ItemStack[] compress(ItemStack[] list) {
        List<ItemStack> comp = new LinkedList<>();
        for (ItemStack item : list) {
            if (item == null) continue;
            ItemStack currentStack = item.copy();
            if (currentStack.isEmpty() || currentStack.getCount() == 0) continue;
            boolean find = false;
            for (ItemStack storedStack : comp) {
                if (storedStack.isEmpty()) continue;
                boolean areItemStackEqual = storedStack.isItemEqual(currentStack) && ItemStack.areItemStackTagsEqual(storedStack, currentStack);
                if (areItemStackEqual && (storedStack.getCount() + currentStack.getCount()) <= storedStack.getMaxStackSize()) {
                    find = true;
                    storedStack.setCount(storedStack.getCount() + currentStack.getCount());
                }
            }
            if (!find) {
                comp.add(item.copy());
            }
        }
        return comp.stream().filter(Objects::nonNull).toArray(ItemStack[]::new);
    }

    public static int findMax(Collection<Integer> list) {
        int a = Integer.MIN_VALUE;
        for (int x : list) {
            a = Math.max(x, a);
        }
        return a;
    }

    public static void writeNBTToBytes(ByteBuf buf, NBTTagCompound nbt) {
        PacketBuffer pb = new PacketBuffer(buf);
        try {
            pb.writeCompoundTag(nbt);
        } catch (EncoderException ignore) {
        }
    }

    public static NBTTagCompound readNBTFromBytes(ByteBuf from)
    {
        PacketBuffer pb = new PacketBuffer(from);
        try {
            return pb.readCompoundTag();
        } catch (IOException e) {
            return new NBTTagCompound();
        }
    }

    public static boolean multiplySlotCheck(Slot[] slots, int multiple) {
        for (Slot slot : slots) {
            if (ItemFluidPacket.isFluidPacket(slot.getStack()) && FakeItemRegister.getStack(slot.getStack()) != null) {
                long amt = Objects.requireNonNull((FluidStack) FakeItemRegister.getStack(slot.getStack())).amount;
                if (amt * multiple > Integer.MAX_VALUE) {
                    return false;
                }
            } else if (ModAndClassUtil.GAS && ItemGasPacket.isGasPacket(slot.getStack()) && FakeItemRegister.getStack(slot.getStack()) != null) {
                long amt = Objects.requireNonNull((GasStack) FakeItemRegister.getStack(slot.getStack())).amount;
                if (amt * multiple > Integer.MAX_VALUE) {
                    return false;
                }
            } else if (!slot.getStack().isEmpty()) {
                long amt = slot.getStack().getCount();
                if (amt * multiple > Integer.MAX_VALUE) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void multiplySlot(Slot[] slots, int multiple) {
        for (Slot slot : slots) {
            if (ItemFluidPacket.isFluidPacket(slot.getStack()) && FakeItemRegister.getStack(slot.getStack()) != null) {
                FluidStack fluid = Objects.requireNonNull(FakeItemRegister.getStack(slot.getStack()));
                fluid.amount *= multiple;
                ItemStack packet = FakeFluids.packFluid2Packet(fluid);
                slot.putStack(packet);
            } else if (ModAndClassUtil.GAS && ItemGasPacket.isGasPacket(slot.getStack()) && FakeItemRegister.getStack(slot.getStack()) != null) {
                GasStack gas = Objects.requireNonNull(FakeItemRegister.getStack(slot.getStack()));
                gas.amount *= multiple;
                ItemStack packet = FakeGases.packGas2Packet(gas);
                slot.putStack(packet);
            } else if (!slot.getStack().isEmpty()) {
                ItemStack stack = slot.getStack();
                stack.setCount(stack.getCount() * multiple);
            }
        }
    }

    public static boolean divideSlotCheck(Slot[] slots, int divide) {
        for (Slot slot : slots) {
            if (ItemFluidPacket.isFluidPacket(slot.getStack()) && FakeItemRegister.getStack(slot.getStack()) != null) {
                long amt = Objects.requireNonNull((FluidStack) FakeItemRegister.getStack(slot.getStack())).amount;
                if (amt % divide != 0) {
                    return false;
                }
            } else if (ModAndClassUtil.GAS && ItemGasPacket.isGasPacket(slot.getStack()) && FakeItemRegister.getStack(slot.getStack()) != null) {
                long amt = Objects.requireNonNull((GasStack) FakeItemRegister.getStack(slot.getStack())).amount;
                if (amt % divide != 0) {
                    return false;
                }
            } else if (!slot.getStack().isEmpty()) {
                long amt = slot.getStack().getCount();
                if (amt % divide != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void divideSlot(Slot[] slots, int divide) {
        for (Slot slot : slots) {
            if (ItemFluidPacket.isFluidPacket(slot.getStack()) && FakeItemRegister.getStack(slot.getStack()) != null) {
                FluidStack fluid = Objects.requireNonNull(FakeItemRegister.getStack(slot.getStack()));
                fluid.amount /= divide;
                ItemStack packet = FakeFluids.packFluid2Packet(fluid);
                slot.putStack(packet);
            } else if (ModAndClassUtil.GAS && ItemGasPacket.isGasPacket(slot.getStack()) && FakeItemRegister.getStack(slot.getStack()) != null) {
                GasStack gas = Objects.requireNonNull(FakeItemRegister.getStack(slot.getStack()));
                gas.amount /= divide;
                ItemStack packet = FakeGases.packGas2Packet(gas);
                slot.putStack(packet);
            } else if (!slot.getStack().isEmpty()) {
                ItemStack stack = slot.getStack();
                stack.setCount(stack.getCount() / divide);
            }
        }
    }

    public static boolean increaseSlotCheck(Slot[] slots, int increase) {
        for (Slot slot : slots) {
            if (ItemFluidPacket.isFluidPacket(slot.getStack()) && FakeItemRegister.getStack(slot.getStack()) != null) {
                long amt = Objects.requireNonNull((FluidStack) FakeItemRegister.getStack(slot.getStack())).amount;
                if (amt + increase * 1000L > Integer.MAX_VALUE) {
                    return false;
                }
            } else if (ModAndClassUtil.GAS && ItemGasPacket.isGasPacket(slot.getStack()) && FakeItemRegister.getStack(slot.getStack()) != null) {
                long amt = Objects.requireNonNull((GasStack) FakeItemRegister.getStack(slot.getStack())).amount;
                if (amt + increase * 1000L > Integer.MAX_VALUE) {
                    return false;
                }
            } else if (!slot.getStack().isEmpty()) {
                long amt = slot.getStack().getCount();
                if (amt + increase > Integer.MAX_VALUE) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void increaseSlot(Slot[] slots, int increase) {
        for (Slot slot : slots) {
            if (ItemFluidPacket.isFluidPacket(slot.getStack()) && FakeItemRegister.getStack(slot.getStack()) != null) {
                FluidStack fluid = Objects.requireNonNull(FakeItemRegister.getStack(slot.getStack()));
                fluid.amount += increase * 1000;
                ItemStack packet = FakeFluids.packFluid2Packet(fluid);
                slot.putStack(packet);
            } else if (ModAndClassUtil.GAS && ItemGasPacket.isGasPacket(slot.getStack()) && FakeItemRegister.getStack(slot.getStack()) != null) {
                GasStack gas = Objects.requireNonNull(FakeItemRegister.getStack(slot.getStack()));
                gas.amount += increase * 1000;
                ItemStack packet = FakeGases.packGas2Packet(gas);
                slot.putStack(packet);
            } else if (!slot.getStack().isEmpty()) {
                ItemStack stack = slot.getStack();
                stack.setCount(stack.getCount() + increase);
            }
        }
    }

    public static boolean decreaseSlotCheck(Slot[] slots, int decrease) {
        for (Slot slot : slots) {
            if (ItemFluidPacket.isFluidPacket(slot.getStack()) && FakeItemRegister.getStack(slot.getStack()) != null) {
                long amt = Objects.requireNonNull((FluidStack) FakeItemRegister.getStack(slot.getStack())).amount;
                if (amt - decrease * 1000L < 1) {
                    return false;
                }
            } else if (ModAndClassUtil.GAS && ItemGasPacket.isGasPacket(slot.getStack()) && FakeItemRegister.getStack(slot.getStack()) != null) {
                long amt = Objects.requireNonNull((GasStack) FakeItemRegister.getStack(slot.getStack())).amount;
                if (amt - decrease * 1000L < 1) {
                    return false;
                }
            } else if (!slot.getStack().isEmpty()) {
                long amt = slot.getStack().getCount();
                if (amt - decrease < 1) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void decreaseSlot(Slot[] slots, int decrease) {
        for (Slot slot : slots) {
            if (ItemFluidPacket.isFluidPacket(slot.getStack()) && FakeItemRegister.getStack(slot.getStack()) != null) {
                FluidStack fluid = Objects.requireNonNull(FakeItemRegister.getStack(slot.getStack()));
                fluid.amount -= decrease * 1000;
                ItemStack packet = FakeFluids.packFluid2Packet(fluid);
                slot.putStack(packet);
            } else if (ModAndClassUtil.GAS && ItemGasPacket.isGasPacket(slot.getStack()) && FakeItemRegister.getStack(slot.getStack()) != null) {
                GasStack gas = Objects.requireNonNull(FakeItemRegister.getStack(slot.getStack()));
                gas.amount -= decrease * 1000;
                ItemStack packet = FakeGases.packGas2Packet(gas);
                slot.putStack(packet);
            } else if (!slot.getStack().isEmpty()) {
                ItemStack stack = slot.getStack();
                stack.setCount(stack.getCount() - decrease);
            }
        }
    }

    public static void openWirelessTerminal(ItemStack item, EnumHand hand, World w, EntityPlayer player, GuiType gui) {
        IWirelessTermRegistry registry = AEApi.instance().registries().wireless();
        if (Platform.isClient()) {
            return;
        }
        if (!registry.isWirelessTerminal(item)) {
            player.sendMessage(PlayerMessages.DeviceNotWirelessTerminal.get());
            return;
        }
        final IWirelessTermHandler handler = registry.getWirelessTerminalHandler(item);
        final String unparsedKey = handler.getEncryptionKey(item);
        if (unparsedKey.isEmpty()) {
            player.sendMessage(PlayerMessages.DeviceNotLinked.get());
            return;
        }
        final long parsedKey = Long.parseLong(unparsedKey);
        final ILocatable securityStation = AEApi.instance().registries().locatable().getLocatableBy(parsedKey);
        if (securityStation == null) {
            player.sendMessage(PlayerMessages.StationCanNotBeLocated.get());
            return;
        }
        if (handler.hasPower(player, 0.5, item)) {
            int x, y;
            if (player.openContainer instanceof IInventorySlotAware) {
                x = ((IInventorySlotAware) player.openContainer).getInventorySlot();
                y = ((IInventorySlotAware) player.openContainer).isBaubleSlot() ? 1 : 0;
            } else {
                x = player.inventory.currentItem;
                y = 0;
            }
            InventoryHandler.openGui(player, w, new BlockPos(x, y, Integer.MIN_VALUE), EnumFacing.values()[hand.ordinal()], gui);
        } else {
            player.sendMessage(PlayerMessages.DeviceNotPowered.get());
        }
    }

}
