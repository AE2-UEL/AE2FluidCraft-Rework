package com.glodblock.github.common.part;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.parts.IPartModel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.sync.GuiBridge;
import appeng.items.misc.ItemEncodedPattern;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.inv.InvOperation;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.ItemFluidCraftEncodedPattern;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.ExAppEngInternalInventory;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.util.FluidCraftingPatternDetails;
import com.glodblock.github.util.Util;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.List;

public class PartFluidPatternTerminal extends PartPatternTerminal {

    private boolean combine = false;
    private boolean fluidFirst = false;

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
        ExAppEngInternalInventory exCraft = new ExAppEngInternalInventory((AppEngInternalInventory) getInventoryByName("crafting"));
        ExAppEngInternalInventory exOutput = new ExAppEngInternalInventory((AppEngInternalInventory) getInventoryByName("output"));
        this.crafting = exCraft;
        this.output = exOutput;
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
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
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("combineMode", combine);
        data.setBoolean("fluidFirst", fluidFirst);
    }

    @Override
    public boolean onPartActivate(final EntityPlayer player, final EnumHand hand, final Vec3d pos) {
        TileEntity te = this.getTile();
        BlockPos tePos = te.getPos();
        if (Platform.isWrench(player, player.inventory.getCurrentItem(), tePos)) {
            return super.onPartActivate(player, hand, pos);
        }
        if (Platform.isServer()) {
            if (GuiBridge.GUI_PATTERN_TERMINAL.hasPermissions(te, tePos.getX(), tePos.getY(), tePos.getZ(), getSide(), player)) {
                InventoryHandler.openGui(player, te.getWorld(), tePos, getSide().getFacing(), GuiType.FLUID_PATTERN_TERMINAL);
            } else {
                Platform.openGUI(player, this.getHost().getTile(), this.getSide(), GuiBridge.GUI_ME);
            }
        }
        return true;
    }

    @Override
    public void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removedStack,
                                  ItemStack newStack) {
        if (slot == 1) {
            final ItemStack is = inv.getStackInSlot(1);
            if (!is.isEmpty() && (is.getItem() instanceof ItemFluidEncodedPattern || is.getItem() instanceof ItemFluidCraftEncodedPattern)) {
                final ItemEncodedPattern pattern = (ItemEncodedPattern) is.getItem();
                final ICraftingPatternDetails details = pattern.getPatternForItem( is, this.getHost().getTile().getWorld() );
                if( details != null )
                {
                    this.setCraftingRecipe( details.isCraftable() );
                    this.setSubstitution( details.canSubstitute() );

                    for( int x = 0; x < this.getInventoryByName("crafting").getSlots(); x ++ ) {
                        ((AppEngInternalInventory) this.getInventoryByName("crafting")).setStackInSlot(x, ItemStack.EMPTY);
                    }

                    for( int x = 0; x < this.getInventoryByName("output").getSlots(); x ++ ) {
                        ((AppEngInternalInventory) this.getInventoryByName("output")).setStackInSlot(x, ItemStack.EMPTY);
                    }
                    if (details instanceof FluidCraftingPatternDetails) {
                        putPattern(((FluidCraftingPatternDetails) details).getOriginInputs(), details.getOutputs());
                        this.setCraftingRecipe( true );
                    } else {
                        putPattern(details.getInputs(), details.getOutputs());
                    }
                }
                this.getHost().markForSave();
                return;
            }
        }
        super.onChangeInventory(inv, slot, mc, removedStack, newStack);
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

    public void onChangeCrafting(Int2ObjectMap<ItemStack[]> inputs, List<ItemStack> outputs, boolean combine) {
        IItemHandler crafting = this.getInventoryByName("crafting");
        IItemHandler output = this.getInventoryByName("output");
        IItemList<IAEItemStack> storageList = this.getInventory(Util.getItemChannel()) == null ?
                null : this.getInventory(Util.getItemChannel()).getStorageList();
        if (crafting instanceof AppEngInternalInventory && output instanceof AppEngInternalInventory) {
            Util.clearItemInventory((IItemHandlerModifiable) crafting);
            Util.clearItemInventory((IItemHandlerModifiable) output);
            ItemStack[] fuzzyFind = new ItemStack[Util.findMax(inputs.keySet()) + 1];
            for (int index : inputs.keySet()) {
                Util.fuzzyTransferItems(index, inputs.get(index), fuzzyFind, storageList);
            }
            if (combine && !this.craftingMode) {
                fuzzyFind = Util.compress(fuzzyFind);
            }
            int bound = Math.min(crafting.getSlots(), fuzzyFind.length);
            for (int x = 0; x < bound; x++) {
                final ItemStack item = fuzzyFind[x];
                ((AppEngInternalInventory) crafting).setStackInSlot(x, item == null ? ItemStack.EMPTY : item);
            }
            bound = Math.min(output.getSlots(), outputs.size());
            for (int x = 0; x < bound; x++) {
                final ItemStack item = outputs.get(x);
                ((AppEngInternalInventory) output).setStackInSlot(x, item == null ? ItemStack.EMPTY : item);
            }
        }
    }

}
