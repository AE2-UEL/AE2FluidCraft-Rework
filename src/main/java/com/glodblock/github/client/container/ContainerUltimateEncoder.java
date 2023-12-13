package com.glodblock.github.client.container;

import appeng.api.AEApi;
import appeng.api.definitions.IDefinitions;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.container.slot.SlotPatternOutputs;
import appeng.container.slot.SlotPlayerHotBar;
import appeng.container.slot.SlotPlayerInv;
import appeng.container.slot.SlotRestrictedInput;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.item.fake.FakeFluids;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.common.tile.TileUltimateEncoder;
import com.glodblock.github.integration.mek.FCGasItems;
import com.glodblock.github.integration.mek.FakeGases;
import com.glodblock.github.interfaces.PatternConsumer;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.FluidPatternDetails;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.Util;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mekanism.api.gas.GasStack;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public class ContainerUltimateEncoder extends AEBaseContainer implements IOptionalSlotHost, PatternConsumer {

    private final TileUltimateEncoder encoder;
    protected final SlotRestrictedInput patternSlotIN;
    protected final SlotRestrictedInput patternSlotOUT;
    protected final SlotFakeCraftingMatrix[] craftingSlots = new SlotFakeCraftingMatrix[42];
    protected final SlotPatternOutputs[] outputSlots = new SlotPatternOutputs[8];
    @GuiSync(105)
    public boolean combine = false;
    @GuiSync(106)
    public boolean fluidFirst = false;


    public ContainerUltimateEncoder(InventoryPlayer ipl, TileUltimateEncoder encoder) {
        super(ipl, encoder);
        this.encoder = encoder;
        this.patternSlotIN = new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.BLANK_PATTERN, this.encoder.getPattern(), 0, 137, 91, this.getInventoryPlayer());
        this.patternSlotOUT = new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.ENCODED_PATTERN, this.encoder.getPattern(), 1, 137, 134, this.getInventoryPlayer());
        for(int y = 0; y < 7; ++y) {
            for(int x = 0; x < 6; ++x) {
                this.addSlotToContainer(this.craftingSlots[x + y * 6] = new SlotFakeCraftingMatrix(this.encoder.getCraft(), x + y * 6, 8 + x * 18, 29 + y * 18));
            }
        }
        for(int y = 0; y < 4; ++y) {
            for(int x = 0; x < 2; ++x) {
                this.addSlotToContainer(this.outputSlots[x + y * 2] = new SlotPatternOutputs(this.encoder.getOutput(), this, x + y * 2, 134 + x * 18, 12 + y * 18, 0, 0, 1));
                this.outputSlots[x + y * 2].setRenderDisabled(false);
                this.outputSlots[x + y * 2].setIIcon(-1);
            }
        }
        this.addSlotToContainer(this.patternSlotIN);
        this.addSlotToContainer(this.patternSlotOUT);
        this.bindPlayerInventory(ipl, 0, 167);
    }

    public void setCombine(boolean val) {
        this.combine = val;
        this.encoder.combine = val;
    }

    public void setFluidFirst(boolean val) {
        this.fluidFirst = val;
        this.encoder.fluidFirst = val;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (Platform.isServer()) {
            this.combine = this.encoder.combine;
            this.fluidFirst = this.encoder.fluidFirst;
        }
    }

    public TileUltimateEncoder getEncoder() {
        return this.encoder;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer p, int idx) {
        if (Platform.isClient()) {
            return ItemStack.EMPTY;
        } else {
            if (this.inventorySlots.get(idx) instanceof SlotPlayerInv || this.inventorySlots.get(idx) instanceof SlotPlayerHotBar) {
                AppEngSlot clickSlot = (AppEngSlot)this.inventorySlots.get(idx);
                ItemStack itemStack = clickSlot.getStack();
                if (AEApi.instance().definitions().materials().blankPattern().isSameAs(itemStack)) {
                    IItemHandler patternInv = this.encoder.getPattern();
                    ItemStack remainder = patternInv.insertItem(0, itemStack, false);
                    clickSlot.putStack(remainder);
                }
            }
            return super.transferStackInSlot(p, idx);
        }
    }

    @Override
    public void onSlotChange(final Slot s) {
        if (s == this.patternSlotOUT && Platform.isServer()) {
            for (final IContainerListener listener : this.listeners) {
                for (final Slot slot : this.inventorySlots) {
                    if (slot instanceof OptionalSlotFake || slot instanceof SlotFakeCraftingMatrix) {
                        listener.sendSlotContents(this, slot.slotNumber, slot.getStack());
                    }
                }
                if (listener instanceof EntityPlayerMP) {
                    ((EntityPlayerMP) listener).isChangingQuantityOnly = false;
                }
            }
            this.detectAndSendChanges();
        }
    }

    @Override
    public void doAction(EntityPlayerMP player, InventoryAction action, int slotId, long id) {
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
            if (stack.isEmpty() && !slot.getStack().isEmpty() && slot.getStack().getItem() instanceof ItemFluidPacket) {
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

    public void encodeAndMoveToInventory() {
        encode();
        ItemStack output = this.patternSlotOUT.getStack();
        if (!output.isEmpty()) {
            if (!getPlayerInv().addItemStackToInventory(output)) {
                getPlayerInv().player.dropItem(output, false);
            }
            this.patternSlotOUT.putStack(ItemStack.EMPTY);
        }
    }

    public void encode() {
        if (!checkHasFluidPattern()) {
            encodeItem();
            return;
        }
        ItemStack stack = this.patternSlotOUT.getStack();
        if (stack.isEmpty()) {
            stack = this.patternSlotIN.getStack();
            if (stack.isEmpty() || notPattern(stack)) {
                return;
            }
            if (stack.getCount() == 1) {
                this.patternSlotIN.putStack(ItemStack.EMPTY);
            } else {
                stack.shrink(1);
            }
            encodeFluidPattern();
        } else if (!notPattern(stack)) {
            encodeFluidPattern();
        }
    }

    public void encodeItem() {
        ItemStack patternStack = new ItemStack(FCItems.LARGE_ITEM_ENCODED_PATTERN);
        FluidPatternDetails pattern = new FluidPatternDetails(patternStack);
        pattern.setInputs(collectInventory(craftingSlots));
        pattern.setOutputs(collectInventory(outputSlots));
        patternSlotOUT.putStack(pattern.writeToStack());
    }

    private boolean checkHasFluidPattern() {
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

    private boolean notPattern(final ItemStack output) {
        if (output.isEmpty()) {
            return true;
        }
        if (output.getItem() instanceof ItemFluidEncodedPattern) {
            return false;
        }
        final IDefinitions defs = AEApi.instance().definitions();
        return !defs.items().encodedPattern().isSameAs(output) && !defs.materials().blankPattern().isSameAs(output);
    }

    public void multiply(int multiple) {
        if (Util.multiplySlotCheck(this.craftingSlots, multiple) && Util.multiplySlotCheck(this.outputSlots, multiple)) {
            Util.multiplySlot(this.craftingSlots, multiple);
            Util.multiplySlot(this.outputSlots, multiple);
        }
    }

    public void clear() {
        for (Slot slot : this.craftingSlots) {
            slot.putStack(ItemStack.EMPTY);
        }
        for (Slot slot : this.outputSlots) {
            slot.putStack(ItemStack.EMPTY);
        }
    }

    public void divide(int divide) {
        if (Util.divideSlotCheck(this.craftingSlots, divide) && Util.divideSlotCheck(this.outputSlots, divide)) {
            Util.divideSlot(this.craftingSlots, divide);
            Util.divideSlot(this.outputSlots, divide);
        }
    }

    public void increase(int increase) {
        if (Util.increaseSlotCheck(this.craftingSlots, increase) && Util.increaseSlotCheck(this.outputSlots, increase)) {
            Util.increaseSlot(this.craftingSlots, increase);
            Util.increaseSlot(this.outputSlots, increase);
        }
    }

    public void decrease(int decrease) {
        if (Util.decreaseSlotCheck(this.craftingSlots, decrease) && Util.decreaseSlotCheck(this.outputSlots, decrease)) {
            Util.decreaseSlot(this.craftingSlots, decrease);
            Util.decreaseSlot(this.outputSlots, decrease);
        }
    }

    @Override
    public boolean isSlotEnabled(int i) {
        return true;
    }

    @Override
    public void acceptPattern(Int2ObjectMap<ItemStack[]> inputs, List<ItemStack> outputs, boolean combine) {
        this.encoder.onChangeCrafting(inputs, outputs, combine);
    }
}
