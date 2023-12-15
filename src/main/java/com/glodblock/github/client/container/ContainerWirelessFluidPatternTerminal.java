package com.glodblock.github.client.container;

import appeng.api.AEApi;
import appeng.api.definitions.IDefinitions;
import appeng.api.implementations.IUpgradeableCellContainer;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.guisync.GuiSync;
import appeng.container.implementations.ContainerWirelessPatternTerminal;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.container.slot.SlotPatternOutputs;
import appeng.helpers.InventoryAction;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.items.misc.ItemEncodedPattern;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.inv.InvOperation;
import appeng.util.item.AEItemStack;
import com.glodblock.github.common.item.ItemFluidCraftEncodedPattern;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import com.glodblock.github.common.item.ItemLargeEncodedPattern;
import com.glodblock.github.common.item.fake.FakeFluids;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.integration.mek.FCGasItems;
import com.glodblock.github.integration.mek.FakeGases;
import com.glodblock.github.interfaces.PatternConsumer;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.FluidCraftingPatternDetails;
import com.glodblock.github.util.FluidPatternDetails;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.Util;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mekanism.api.gas.GasStack;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContainerWirelessFluidPatternTerminal extends ContainerWirelessPatternTerminal implements PatternConsumer, IUpgradeableCellContainer, IInventorySlotAware {

    private final WirelessTerminalGuiObject wirelessTerminalGUIObject;
    @GuiSync(105)
    public boolean combine = false;
    @GuiSync(106)
    public boolean fluidFirst = false;

    public ContainerWirelessFluidPatternTerminal(InventoryPlayer ip, WirelessTerminalGuiObject gui) {
        super(ip, gui);
        this.wirelessTerminalGUIObject = gui;
        this.loadFromNBT();
    }

    @Override
    public void encode() {
        if (!checkHasFluidPattern()) {
            super.encode();
            return;
        }
        ItemStack stack = this.patternSlotOUT.getStack();
        if (stack.isEmpty()) {
            stack = this.patternSlotIN.getStack();
            if (stack.isEmpty() || !isPattern(stack)) {
                return;
            }
            if (stack.getCount() == 1) {
                this.patternSlotIN.putStack(ItemStack.EMPTY);
            } else {
                stack.shrink(1);
            }
            encodeFluidPattern();
        } else if (isPattern(stack)) {
            encodeFluidPattern();
        }
    }

    public void encodeFluidCraftPattern() {
        ItemStack output = this.patternSlotOUT.getStack();

        final ItemStack[] in = this.getInputs();
        final ItemStack[] out = this.getOutputs();
        if (in == null || out == null) {
            return;
        }

        if (!output.isEmpty() && !isPattern(output)) {
            return;
        }
        else if (output.isEmpty()) {
            output = this.patternSlotIN.getStack();
            if (output.isEmpty() || !isPattern(output)) {
                return;
            }
            output.setCount(output.getCount() - 1);
            if (output.getCount() == 0) {
                this.patternSlotIN.putStack(ItemStack.EMPTY);
            }
            Optional<ItemStack> maybePattern = AEApi.instance().definitions().items().encodedPattern().maybeStack(1);
            if (maybePattern.isPresent()) {
                output = maybePattern.get();
                this.patternSlotOUT.putStack(output);
            }
        }
        final NBTTagCompound encodedValue = new NBTTagCompound();

        final NBTTagList tagIn = new NBTTagList();
        final NBTTagList tagOut = new NBTTagList();

        for (final ItemStack i : in) {
            tagIn.appendTag(this.createItemTag(i));
        }

        for (final ItemStack i : out) {
            tagOut.appendTag(this.createItemTag(i));
        }

        encodedValue.setTag("in", tagIn);
        encodedValue.setTag("out", tagOut);
        encodedValue.setBoolean("crafting", this.isCraftingMode());
        encodedValue.setBoolean("substitute", this.substitute);
        ItemStack patternStack = new ItemStack(FCItems.DENSE_CRAFT_ENCODED_PATTERN);
        patternStack.setTagCompound(encodedValue);
        FluidCraftingPatternDetails details = FluidCraftingPatternDetails.GetFluidPattern(patternStack, getNetworkNode().getWorld());
        if (details == null || !details.isNecessary()) {
            encode();
            return;
        }
        patternSlotOUT.putStack(patternStack);
    }

    private boolean checkHasFluidPattern() {
        if (this.craftingMode) {
            return false;
        }
        boolean hasFluid = false, search = false;
        for (Slot craftingSlot : this.craftingSlots) {
            final ItemStack crafting = craftingSlot.getStack();
            if (crafting.isEmpty()) {
                continue;
            }
            search = true;
            if (crafting.getItem() == FCItems.FLUID_PACKET) {
                hasFluid = true;
                break;
            }
            if (ModAndClassUtil.GAS && crafting.getItem() == FCGasItems.GAS_PACKET) {
                hasFluid = true;
                break;
            }
        }
        if (!search) { // search=false -> inputs were empty
            return false;
        }
        // `search` should be true at this point
        for (Slot outputSlot : this.outputSlots) {
            final ItemStack out = outputSlot.getStack();
            if (out.isEmpty()) {
                continue;
            }
            search = false;
            if (hasFluid) {
                break;
            } else if (out.getItem() == FCItems.FLUID_PACKET) {
                hasFluid = true;
                break;
            } else if (ModAndClassUtil.GAS && out.getItem() == FCGasItems.GAS_PACKET) {
                hasFluid = true;
                break;
            }
        }
        return hasFluid && !search; // search=true -> outputs were empty
    }

    private void encodeFluidPattern() {
        ItemStack patternStack = new ItemStack(FCItems.DENSE_ENCODED_PATTERN);
        FluidPatternDetails pattern = new FluidPatternDetails(patternStack);
        pattern.setInputs(collectInventory(craftingSlots));
        pattern.setOutputs(collectInventory(outputSlots));
        patternSlotOUT.putStack(pattern.writeToStack());
    }

    private static IAEItemStack[] collectInventory(Slot[] slots) {
        // see note at top of DensePatternDetails
        List<IAEItemStack> acc = new ArrayList<>();
        for (Slot slot : slots) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() == FCItems.FLUID_PACKET) {
                IAEItemStack dropStack = FakeFluids.packFluid2AEDrops((FluidStack) FakeItemRegister.getStack(stack));
                if (dropStack != null) {
                    acc.add(dropStack);
                    continue;
                }
            }
            if (ModAndClassUtil.GAS && stack.getItem() == FCGasItems.GAS_PACKET) {
                IAEItemStack dropStack = FakeGases.packGas2AEDrops((GasStack) FakeItemRegister.getStack(stack));
                if (dropStack != null) {
                    acc.add(dropStack);
                    continue;
                }
            }
            IAEItemStack aeStack = AEItemStack.fromItemStack(stack);
            if (aeStack == null) {
                continue;
            }
            acc.add(aeStack);
        }
        return acc.toArray(new IAEItemStack[0]);
    }

    private static boolean isPattern(final ItemStack output) {
        if (output.isEmpty()) {
            return false;
        }
        if (output.getItem() instanceof ItemFluidEncodedPattern
                || output.getItem() instanceof ItemFluidCraftEncodedPattern
                || output.getItem() instanceof ItemLargeEncodedPattern) {
            return true;
        }
        final IDefinitions defs = AEApi.instance().definitions();
        return defs.items().encodedPattern().isSameAs(output) || defs.materials().blankPattern().isSameAs(output);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (Platform.isServer()) {
            NBTTagCompound tag = this.iGuiItemObject.getItemStack().getTagCompound();
            if (tag != null) {
                this.combine = tag.getBoolean("combine");
                this.fluidFirst = tag.getBoolean("fluidFirst");
            } else {
                this.combine = false;
                this.fluidFirst = false;
            }
        }
    }

    @Override
    public void saveChanges() {
        super.saveChanges();
        if (Platform.isServer()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setBoolean("combine", this.combine);
            tag.setBoolean("fluidFirst", this.fluidFirst);
            this.wirelessTerminalGUIObject.saveChanges(tag);
        }
    }

    private void loadFromNBT() {
        NBTTagCompound data = this.wirelessTerminalGUIObject.getItemStack().getTagCompound();
        if (data != null) {
            this.combine = data.getBoolean("combine");
            this.fluidFirst = data.getBoolean("fluidFirst");
        }
    }

    @Override
    public void doAction(EntityPlayerMP player, InventoryAction action, int slotId, long id) {
        if (this.isCraftingMode()) {
            super.doAction(player, action, slotId, id);
            return;
        }
        if (slotId < 0 || slotId >= this.inventorySlots.size()) {
            super.doAction(player, action, slotId, id);
            return;
        }
        Slot slot = getSlot(slotId);
        ItemStack stack = player.inventory.getItemStack();
        if ((slot instanceof SlotFakeCraftingMatrix || slot instanceof SlotPatternOutputs) && !stack.isEmpty()
                && stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) && Util.getFluidFromItem(stack) != null) {
            FluidStack fluid = null;
            switch (action) {
                case PICKUP_OR_SET_DOWN:
                    fluid = Util.getFluidFromItem(stack);
                    slot.putStack(FakeFluids.packFluid2Packet(fluid));
                    break;
                case SPLIT_OR_PLACE_SINGLE:
                    fluid = Util.getFluidFromItem(ItemHandlerHelper.copyStackWithSize(stack, 1));
                    FluidStack origin = FakeItemRegister.getStack(slot.getStack());
                    if (fluid != null && fluid.equals(origin)) {
                        fluid.amount += origin.amount;
                        if (fluid.amount <= 0) fluid = null;
                    }
                    slot.putStack(FakeFluids.packFluid2Packet(fluid));
                    break;
            }
            if (fluid == null) {
                super.doAction(player, action, slotId, id);
                return;
            }
            return;
        }
        if (ModAndClassUtil.GAS && (slot instanceof SlotFakeCraftingMatrix || slot instanceof SlotPatternOutputs) && !stack.isEmpty()
                && stack.hasCapability(Capabilities.GAS_HANDLER_CAPABILITY, null) && Util.getGasFromItem(stack) != null) {
            GasStack gas = null;
            switch (action) {
                case PICKUP_OR_SET_DOWN:
                    gas = (GasStack) Util.getGasFromItem(stack);
                    slot.putStack(FakeGases.packGas2Packet(gas));
                    break;
                case SPLIT_OR_PLACE_SINGLE:
                    gas = (GasStack) Util.getGasFromItem(ItemHandlerHelper.copyStackWithSize(stack, 1));
                    GasStack origin = FakeItemRegister.getStack(slot.getStack());
                    if (gas != null && gas.equals(origin)) {
                        gas.amount += origin.amount;
                        if (gas.amount <= 0) gas = null;
                    }
                    slot.putStack(FakeGases.packGas2Packet(gas));
                    break;
            }
            if (gas == null) {
                super.doAction(player, action, slotId, id);
                return;
            }
            return;
        }
        if (action == InventoryAction.SPLIT_OR_PLACE_SINGLE) {
            if (stack.isEmpty() && !slot.getStack().isEmpty() && slot.getStack().getItem() == FCItems.FLUID_PACKET) {
                FluidStack fluid = FakeItemRegister.getStack(slot.getStack());
                if (fluid != null && fluid.amount - 1000 >= 1) {
                    fluid.amount -= 1000;
                    slot.putStack(FakeFluids.packFluid2Packet(fluid));
                }
            } else if (ModAndClassUtil.GAS && stack.isEmpty() && !slot.getStack().isEmpty() && slot.getStack().getItem() == FCGasItems.GAS_PACKET) {
                GasStack gas = FakeItemRegister.getStack(slot.getStack());
                if (gas != null && gas.amount - 1000 >= 1) {
                    gas.amount -= 1000;
                    slot.putStack(FakeGases.packGas2Packet(gas));
                }
            }
        }
        super.doAction(player, action, slotId, id);
    }

    @Override
    public void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack) {
        if (slot == 1) {
            final ItemStack is = inv.getStackInSlot(1);
            if (!is.isEmpty() && (is.getItem() instanceof ItemFluidEncodedPattern || is.getItem() instanceof ItemFluidCraftEncodedPattern || is.getItem() instanceof ItemLargeEncodedPattern)) {
                final ItemEncodedPattern pattern = (ItemEncodedPattern) is.getItem();
                final ICraftingPatternDetails details = pattern.getPatternForItem(is, this.getPlayerInv().player.world);
                if( details != null )
                {
                    this.setCraftingMode( details.isCraftable() );
                    this.setSubstitute( details.canSubstitute() );

                    for( int x = 0; x < this.crafting.getSlots(); x ++ ) {
                        ((AppEngInternalInventory) this.crafting).setStackInSlot(x, ItemStack.EMPTY);
                    }

                    for( int x = 0; x < this.output.getSlots(); x ++ ) {
                        this.output.setStackInSlot(x, ItemStack.EMPTY);
                    }
                    if (details instanceof FluidCraftingPatternDetails) {
                        putPattern(((FluidCraftingPatternDetails) details).getOriginInputs(), details.getOutputs());
                        this.setCraftingMode( true );
                    } else {
                        putPattern(details.getInputs(), details.getOutputs());
                    }
                }
                this.saveChanges();
                return;
            }
        }
        super.onChangeInventory(inv, slot, mc, removedStack, newStack);
    }

    public void putPattern(IAEItemStack[] inputs, IAEItemStack[] outputs) {
        for( int x = 0; x < this.getInventoryByName("crafting").getSlots() && x < inputs.length; x++ )
        {
            final IAEItemStack item = inputs[x];
            if (item != null && item.getItem() == FCItems.FLUID_DROP) {
                ItemStack packet = FakeFluids.packFluid2Packet(FakeItemRegister.getStack(item.createItemStack()));
                ((AppEngInternalInventory) this.getInventoryByName("crafting")).setStackInSlot(x, packet);
            } else if (ModAndClassUtil.GAS && item != null && item.getItem() == FCGasItems.GAS_DROP) {
                ItemStack packet = FakeGases.packGas2Packet(FakeItemRegister.getStack(item.createItemStack()));
                ((AppEngInternalInventory) this.getInventoryByName("crafting")).setStackInSlot(x, packet);
            } else ((AppEngInternalInventory) this.getInventoryByName("crafting")).setStackInSlot( x, item == null ? ItemStack.EMPTY : item.createItemStack() );
        }

        for( int x = 0; x < this.getInventoryByName("output").getSlots() && x < outputs.length; x++ )
        {
            final IAEItemStack item = outputs[x];
            if (item != null && item.getItem() == FCItems.FLUID_DROP) {
                ItemStack packet = FakeFluids.packFluid2Packet(FakeItemRegister.getStack(item.createItemStack()));
                ((AppEngInternalInventory) this.getInventoryByName("output")).setStackInSlot(x, packet);
            } else if (ModAndClassUtil.GAS && item != null && item.getItem() == FCGasItems.GAS_DROP) {
                ItemStack packet = FakeGases.packGas2Packet(FakeItemRegister.getStack(item.createItemStack()));
                ((AppEngInternalInventory) this.getInventoryByName("output")).setStackInSlot(x, packet);
            } else ((AppEngInternalInventory) this.getInventoryByName("output")).setStackInSlot( x, item == null ? ItemStack.EMPTY : item.createItemStack() );
        }
    }

    @Override
    public void multiply(int multiple) {
        if (Util.multiplySlotCheck(this.craftingSlots, multiple) && Util.multiplySlotCheck(this.outputSlots, multiple)) {
            Util.multiplySlot(this.craftingSlots, multiple);
            Util.multiplySlot(this.outputSlots, multiple);
        }
    }

    @Override
    public void divide(int divide) {
        if (Util.divideSlotCheck(this.craftingSlots, divide) && Util.divideSlotCheck(this.outputSlots, divide)) {
            Util.divideSlot(this.craftingSlots, divide);
            Util.divideSlot(this.outputSlots, divide);
        }
    }

    @Override
    public void increase(int increase) {
        if (Util.increaseSlotCheck(this.craftingSlots, increase) && Util.increaseSlotCheck(this.outputSlots, increase)) {
            Util.increaseSlot(this.craftingSlots, increase);
            Util.increaseSlot(this.outputSlots, increase);
        }
    }

    @Override
    public void decrease(int decrease) {
        if (Util.decreaseSlotCheck(this.craftingSlots, decrease) && Util.decreaseSlotCheck(this.outputSlots, decrease)) {
            Util.decreaseSlot(this.craftingSlots, decrease);
            Util.decreaseSlot(this.outputSlots, decrease);
        }
    }

    @Override
    public IItemHandler getInventoryByName(String name) {
        if (name.equals("crafting")) {
            return this.crafting;
        } else {
            return name.equals("output") ? this.output : super.getInventoryByName(name);
        }
    }

    public void setCombineMode(boolean value) {
        NBTTagCompound data = this.wirelessTerminalGUIObject.getItemStack().getTagCompound();
        if (data != null) {
            data.setBoolean("combine", value);
        } else {
            data = new NBTTagCompound();
            data.setBoolean("combine", value);
            this.wirelessTerminalGUIObject.getItemStack().setTagCompound(data);
        }
        this.combine = value;
    }

    public void setFluidPlaceMode(boolean value) {
        NBTTagCompound data = this.wirelessTerminalGUIObject.getItemStack().getTagCompound();
        if (data != null) {
            data.setBoolean("fluidFirst", value);
        } else {
            data = new NBTTagCompound();
            data.setBoolean("fluidFirst", value);
            this.wirelessTerminalGUIObject.getItemStack().setTagCompound(data);
        }
        this.fluidFirst = value;
    }

    @Override
    public void acceptPattern(Int2ObjectMap<ItemStack[]> inputs, List<ItemStack> outputs, boolean combine) {
        IItemList<IAEItemStack> storageList = this.wirelessTerminalGUIObject.getInventory(Util.getItemChannel()) == null ?
                null : this.wirelessTerminalGUIObject.getInventory(Util.getItemChannel()).getStorageList();
        if (this.crafting instanceof AppEngInternalInventory && this.output != null) {
            Util.clearItemInventory((IItemHandlerModifiable) this.crafting);
            Util.clearItemInventory(this.output);
            ItemStack[] fuzzyFind = new ItemStack[Util.findMax(inputs.keySet()) + 1];
            for (int index : inputs.keySet()) {
                Util.fuzzyTransferItems(index, inputs.get(index), fuzzyFind, storageList);
            }
            if (combine && !this.craftingMode) {
                fuzzyFind = Util.compress(fuzzyFind);
            }
            int bound = Math.min(this.crafting.getSlots(), fuzzyFind.length);
            for (int x = 0; x < bound; x++) {
                final ItemStack item = fuzzyFind[x];
                ((AppEngInternalInventory) this.crafting).setStackInSlot(x, item == null ? ItemStack.EMPTY : item);
            }
            bound = Math.min(this.output.getSlots(), outputs.size());
            for (int x = 0; x < bound; x++) {
                final ItemStack item = outputs.get(x);
                this.output.setStackInSlot(x, item == null ? ItemStack.EMPTY : item);
            }
        }
    }

    NBTBase createItemTag(ItemStack i) {
        NBTTagCompound c = new NBTTagCompound();
        if (!i.isEmpty()) {
            i.writeToNBT(c);
            if (i.getCount() > i.getMaxStackSize()) {
                c.setInteger("stackSize", i.getCount());
            }
        }
        return c;
    }

}
