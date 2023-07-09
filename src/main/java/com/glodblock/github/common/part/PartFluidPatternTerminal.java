package com.glodblock.github.common.part;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.parts.IPartModel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.me.items.ItemTerminalContainer;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.reporting.PatternTerminalPart;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.inv.InvOperation;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.container.ContainerFluidPatternTerminal;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.util.FCUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

public class PartFluidPatternTerminal extends PatternTerminalPart {

    private boolean combine = false;
    private boolean fluidFirst = false;
    private final AppEngInternalInventory crafting;
    private final AppEngInternalInventory output;
    private final AppEngInternalInventory pattern;

    @PartModels
    public static ResourceLocation[] MODELS = new ResourceLocation[] {
            new ResourceLocation(FluidCraft.MODID, "part/f_pattern_term_on"), // 0
            new ResourceLocation(FluidCraft.MODID, "part/f_pattern_term_off"), // 1
    };

    private static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODELS[0], MODEL_STATUS_ON);
    private static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODELS[1], MODEL_STATUS_OFF);
    private static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODELS[0], MODEL_STATUS_HAS_CHANNEL);

    public PartFluidPatternTerminal(ItemStack is) {
        super(is);
        this.crafting = (AppEngInternalInventory) getInventoryByName("crafting");
        this.output = (AppEngInternalInventory) getInventoryByName("output");
        this.pattern = (AppEngInternalInventory) getInventoryByName("pattern");
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }

    @Override
    public void readFromNBT(CompoundNBT data) {
        super.readFromNBT(data);
        combine = data.getBoolean("combineMode");
        fluidFirst = data.getBoolean("fluidFirst");
    }

    public void setCombineMode(boolean value) {
        this.combine = value;
    }

    public boolean getCombineMode() {
        return this.combine;
    }

    public void setFluidPlaceMode(boolean value) {
        this.fluidFirst = value;
    }

    public boolean getFluidPlaceMode() {
        return this.fluidFirst;
    }

    @Override
    public void writeToNBT(CompoundNBT data) {
        super.writeToNBT(data);
        data.putBoolean("combineMode", combine);
        data.putBoolean("fluidFirst", fluidFirst);
    }

    @Override
    public ContainerType<?> getContainerType(PlayerEntity p) {
        return Platform.checkPermissions(p, this, SecurityPermissions.CRAFT, false) ?
                ContainerFluidPatternTerminal.TYPE : ItemTerminalContainer.TYPE;
    }

    @Override
    public void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removedStack,
                                  ItemStack newStack) {
        if (slot == 1 && inv == this.pattern) {
            final ItemStack is = inv.getStackInSlot(1);
            if (!is.isEmpty() && is.getItem() instanceof ItemFluidEncodedPattern) {
                final ItemFluidEncodedPattern pattern = (ItemFluidEncodedPattern) is.getItem();
                final ICraftingPatternDetails details = pattern.getDetails(is);
                if(details != null) {
                    this.setCraftingRecipe(details.isCraftable());
                    this.setSubstitution(details.canSubstitute());
                    for(int x = 0; x < this.crafting.getSlots(); x ++) {
                        this.crafting.setStackInSlot(x, ItemStack.EMPTY);
                    }
                    for(int x = 0; x < this.output.getSlots(); x ++) {
                        this.output.setStackInSlot(x, ItemStack.EMPTY);
                    }
                    putPattern(details.getInputs().toArray(new IAEItemStack[0]), details.getOutputs().toArray(new IAEItemStack[0]));
                }
                this.getHost().markForSave();
                return;
            }
        }
        super.onChangeInventory(inv, slot, mc, removedStack, newStack);
    }

    public void onChangeCrafting(Int2ObjectMap<ItemStack[]> inputs, ItemStack[] outputs, boolean combine) {
        IItemHandler crafting = this.getInventoryByName("crafting");
        IItemHandler output = this.getInventoryByName("output");
        IItemList<IAEItemStack> storageList = this.getInventory(FCUtil.ITEM) == null ?
                null : this.getInventory(FCUtil.ITEM).getStorageList();
        if (crafting instanceof AppEngInternalInventory && output instanceof AppEngInternalInventory) {
            FCUtil.clearItemInventory((IItemHandlerModifiable) crafting);
            FCUtil.clearItemInventory((IItemHandlerModifiable) output);
            ItemStack[] fuzzyFind = new ItemStack[FCUtil.findMax(inputs.keySet()) + 1];
            for (int index : inputs.keySet()) {
                FCUtil.fuzzyTransferItems(index, inputs.get(index), fuzzyFind, storageList);
            }
            if (combine && !this.isCraftingRecipe()) {
                fuzzyFind = FCUtil.compress(fuzzyFind);
            }
            int bound = Math.min(crafting.getSlots(), fuzzyFind.length);
            for (int x = 0; x < bound; x++) {
                final ItemStack item = fuzzyFind[x];
                ((AppEngInternalInventory) crafting).setStackInSlot(x, item == null ? ItemStack.EMPTY : item);
            }
            bound = Math.min(output.getSlots(), outputs.length);
            for (int x = 0; x < bound; x++) {
                final ItemStack item = outputs[x];
                ((AppEngInternalInventory) output).setStackInSlot(x, item == null ? ItemStack.EMPTY : item);
            }
        }
    }

    public void putPattern(IAEItemStack[] inputs, IAEItemStack[] outputs) {
        for( int x = 0; x < this.getInventoryByName("crafting").getSlots() && x < inputs.length; x++ )
        {
            final IAEItemStack item = inputs[x];
            if (item != null && item.getItem() instanceof ItemFluidDrop) {
                ItemStack packet = ItemFluidPacket.newStack(ItemFluidDrop.getFluidStack(item.createItemStack()));
                ((AppEngInternalInventory) this.getInventoryByName("crafting")).setStackInSlot(x, packet);
            }
            else ((AppEngInternalInventory) this.getInventoryByName("crafting")).setStackInSlot( x, item == null ? ItemStack.EMPTY : item.createItemStack() );
        }

        for( int x = 0; x < this.getInventoryByName("output").getSlots() && x < outputs.length; x++ )
        {
            final IAEItemStack item = outputs[x];
            if (item != null && item.getItem() instanceof ItemFluidDrop) {
                ItemStack packet = ItemFluidPacket.newStack(ItemFluidDrop.getFluidStack(item.createItemStack()));
                ((AppEngInternalInventory) this.getInventoryByName("output")).setStackInSlot(x, packet);
            }
            else ((AppEngInternalInventory) this.getInventoryByName("output")).setStackInSlot( x, item == null ? ItemStack.EMPTY : item.createItemStack() );
        }
    }

}
