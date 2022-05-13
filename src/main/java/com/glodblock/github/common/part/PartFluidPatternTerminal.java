package com.glodblock.github.common.part;

import appeng.api.parts.IPartModel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.sync.GuiBridge;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.inv.InvOperation;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class PartFluidPatternTerminal extends PartPatternTerminal {

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
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
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
            if (!is.isEmpty() && is.getItem() instanceof ItemFluidEncodedPattern) {
                return;
            }
        }
        super.onChangeInventory(inv, slot, mc, removedStack, newStack);
    }

    public void onChangeCrafting(IAEItemStack[] newCrafting, IAEItemStack[] newOutput) {
        IItemHandler crafting = this.getInventoryByName("crafting");
        IItemHandler output = this.getInventoryByName("output");
        if (crafting instanceof AppEngInternalInventory && output instanceof AppEngInternalInventory) {
            for (int x = 0; x < crafting.getSlots() && x < newCrafting.length; x++) {
                final IAEItemStack item = newCrafting[x];
                ((AppEngInternalInventory)crafting)
                        .setStackInSlot(x, item == null ? ItemStack.EMPTY : item.createItemStack());
            }

            for (int x = 0; x < output.getSlots() && x < newOutput.length; x++) {
                final IAEItemStack item = newOutput[x];
                ((AppEngInternalInventory)output)
                        .setStackInSlot(x, item == null ? ItemStack.EMPTY : item.createItemStack());
            }
        }
    }

}
