package com.glodblock.github.client.container;

import appeng.api.AEApi;
import appeng.api.definitions.IDefinitions;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.util.item.AEItemStack;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.part.PartFluidPatternTerminal;
import com.glodblock.github.interfaces.PatternConsumer;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.Ae2Reflect;
import com.glodblock.github.util.FluidPatternDetails;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ContainerFluidPatternTerminal extends ContainerPatternTerm implements PatternConsumer {

    private final Slot[] craftingSlots;
    private final Slot[] outputSlots;
    private final Slot patternSlotIN;
    private final Slot patternSlotOUT;

    public ContainerFluidPatternTerminal(InventoryPlayer ip, ITerminalHost monitorable) {
        super(ip, monitorable);
        craftingSlots = Ae2Reflect.getCraftingSlots(this);
        outputSlots = Ae2Reflect.getOutputSlots(this);
        patternSlotIN = Ae2Reflect.getPatternSlotIn(this);
        patternSlotOUT = Ae2Reflect.getPatternSlotOut(this);
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

    private static boolean isPattern(final ItemStack output) {
        if (output.isEmpty()) {
            return false;
        }
        if (output.getItem() instanceof ItemFluidEncodedPattern) {
            return true;
        }
        final IDefinitions defs = AEApi.instance().definitions();
        return defs.items().encodedPattern().isSameAs(output) || defs.materials().blankPattern().isSameAs(output);
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
            if (out.isEmpty()) {
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
            if (stack.getItem() instanceof ItemFluidPacket) {
                IAEItemStack dropStack = ItemFluidDrop.newAeStack(ItemFluidPacket.getFluidStack(stack));
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

    @Override
    public void acceptPattern(IAEItemStack[] inputs, IAEItemStack[] outputs) {
        if (getPatternTerminal() instanceof PartFluidPatternTerminal) {
            ((PartFluidPatternTerminal)getPatternTerminal()).onChangeCrafting(inputs, outputs);
        }
    }

}
