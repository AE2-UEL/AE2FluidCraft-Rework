package com.glodblock.github.client.gui.container;

import appeng.api.AEApi;
import appeng.api.definitions.IDefinitions;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.slot.OptionalSlotFake;
import appeng.helpers.InventoryAction;
import appeng.util.item.AEItemStack;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.PatternConsumer;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.FluidPatternDetails;
import com.glodblock.github.util.Util;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

public class ContainerFluidPatternTerminalEx extends FCBasePartContainerEx implements PatternConsumer {

    public ContainerFluidPatternTerminalEx(InventoryPlayer ip, ITerminalHost monitorable) {
        super(ip, monitorable);
    }

    @Override
    public void encode() {
        if (!checkHasFluidPattern()) {
            super.encode();
            return;
        }
        ItemStack stack = this.patternSlotOUT.getStack();
        if (stack == null) {
            stack = this.patternSlotIN.getStack();
            if (!isPattern(stack)) {
                return;
            }
            if (stack.stackSize == 1) {
                this.patternSlotIN.putStack(null);
            } else {
                stack.stackSize --;
            }
            encodeFluidPattern();
        } else if (isPattern(stack)) {
            encodeFluidPattern();
        }
    }

    private static boolean isPattern(final ItemStack output) {
        if (output == null) {
            return false;
        }
        if (output.getItem() instanceof ItemFluidEncodedPattern) {
            return true;
        }
        final IDefinitions defs = AEApi.instance().definitions();
        return defs.items().encodedPattern().isSameAs(output) || defs.materials().blankPattern().isSameAs(output);
    }

    private boolean checkHasFluidPattern() {
        boolean hasFluid = false, search = false;
        for (Slot craftingSlot : this.craftingSlots) {
            final ItemStack crafting = craftingSlot.getStack();
            if (crafting == null) {
                continue;
            }
            search = true;
            if (crafting.getItem() instanceof ItemFluidPacket) {
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
            if (out == null) {
                continue;
            }
            search = false;
            if (hasFluid) {
                break;
            } else if (out.getItem() instanceof ItemFluidPacket) {
                hasFluid = true;
                break;
            }
        }
        return hasFluid && !search; // search=true -> outputs were empty
    }

    private void encodeFluidPattern() {
        ItemStack patternStack = new ItemStack(ItemAndBlockHolder.PATTERN);
        FluidPatternDetails pattern = new FluidPatternDetails(patternStack);
        pattern.setInputs(collectInventory(craftingSlots));
        pattern.setOutputs(collectInventory(outputSlots));
        patternSlotOUT.putStack(pattern.writeToStack());
    }

    private static IAEItemStack[] collectInventory(Slot[] slots) {
        // see note at top of FluidPatternDetails
        IAEItemStack[] stacks = new IAEItemStack[slots.length];
        for (int i = 0; i < stacks.length; i++) {
            ItemStack stack = slots[i].getStack();
            if (stack != null) {
                if (stack.getItem() instanceof ItemFluidPacket) {
                    IAEItemStack dropStack = ItemFluidDrop.newAeStack(ItemFluidPacket.getFluidStack(stack));
                    if (dropStack != null) {
                        stacks[i] = dropStack;
                        continue;
                    }
                }
            }
            IAEItemStack aeStack = AEItemStack.create(stack);
            stacks[i] = aeStack;
        }
        return stacks;
    }

    @Override
    public void acceptPattern(IAEItemStack[] inputs, IAEItemStack[] outputs) {
        if (getPatternTerminal() != null) {
            getPatternTerminal().onChangeCrafting(inputs, outputs);
        }
    }

    @Override
    public void doAction(EntityPlayerMP player, InventoryAction action, int slotId, long id) {
        if (slotId < 0 || slotId >= this.inventorySlots.size()) {
            super.doAction(player, action, slotId, id);
            return;
        }
        if (action == InventoryAction.MOVE_REGION) {
            super.doAction(player, InventoryAction.SPLIT_OR_PLACE_SINGLE, slotId, id);
            return;
        }
        if (action == InventoryAction.PICKUP_SINGLE) {
            super.doAction(player, InventoryAction.PICKUP_OR_SET_DOWN, slotId, id);
            return;
        }
        Slot slot = getSlot(slotId);
        ItemStack stack = player.inventory.getItemStack();
        if (Util.getFluidFromItem(stack) == null || Util.getFluidFromItem(stack).amount <= 0) {
            super.doAction(player, action, slotId, id);
            return;
        }
        if ((slot instanceof OptionalSlotFake) && stack != null && (stack.getItem() instanceof IFluidContainerItem || FluidContainerRegistry.isContainer(stack))) {
            FluidStack fluid = null;
            switch (action) {
                case PICKUP_OR_SET_DOWN:
                    fluid = Util.getFluidFromItem(stack);
                    slot.putStack(ItemFluidPacket.newStack(fluid));
                    break;
                case SPLIT_OR_PLACE_SINGLE:
                    fluid = Util.getFluidFromItem(Util.copyStackWithSize(stack, 1));
                    FluidStack origin = ItemFluidPacket.getFluidStack(slot.getStack());
                    if (fluid != null && fluid.equals(origin)) {
                        fluid.amount += origin.amount;
                        if (fluid.amount <= 0) fluid = null;
                    }
                    slot.putStack(ItemFluidPacket.newStack(fluid));
                    break;
            }
            if (fluid == null) {
                super.doAction(player, action, slotId, id);
                return;
            }
            return;
        }
        if (action == InventoryAction.SPLIT_OR_PLACE_SINGLE) {
            if (stack == null && slot.getStack() != null && slot.getStack().getItem() instanceof ItemFluidPacket) {
                return;
            }
        }
        super.doAction(player, action, slotId, id);
    }

}
