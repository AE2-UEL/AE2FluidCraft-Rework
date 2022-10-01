package com.glodblock.github.integration.jei;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.container.ContainerFluidPatternEncoder;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.tile.TileFluidPatternEncoder;
import com.glodblock.github.network.CPacketLoadPattern;
import com.glodblock.github.util.NameConst;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.transfer.RecipeTransferErrorTooltip;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class FluidPatternEncoderRecipeTransferHandler implements IRecipeTransferHandler<ContainerFluidPatternEncoder> {

    private final ExtraExtractors ext;

    public FluidPatternEncoderRecipeTransferHandler(ExtraExtractors ext) {
        this.ext = ext;
    }

    @Override
    @Nonnull
    public Class<ContainerFluidPatternEncoder> getContainerClass() {
        return ContainerFluidPatternEncoder.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@Nonnull ContainerFluidPatternEncoder container, IRecipeLayout recipeLayout,
                                               @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        if (recipeLayout.getRecipeCategory().getUid().equals(VanillaRecipeCategoryUid.CRAFTING)) {
            return new RecipeTransferErrorTooltip(I18n.format(NameConst.TT_PROCESSING_RECIPE_ONLY));
        }
        if (doTransfer) {
            TileFluidPatternEncoder tile = container.getTile();
            IAEItemStack[] crafting = new IAEItemStack[tile.getCraftingSlots().getSlotCount()];
            IAEItemStack[] output = new IAEItemStack[tile.getOutputSlots().getSlotCount()];
            transferRecipeSlots(recipeLayout, crafting, output, false, false, false, ext);
            FluidCraft.proxy.netHandler.sendToServer(new CPacketLoadPattern(crafting, output));
        }
        return null;
    }

    public static void transferRecipeSlots(IRecipeLayout recipeLayout, IAEItemStack[] crafting, IAEItemStack[] output,
                                           boolean retainEmptyInputs, boolean doCompress, boolean fluidFirst, ExtraExtractors ext) {
        //Clear Current Terminal
        Arrays.fill(crafting, null);
        Arrays.fill(output, null);

        int ndxCrafting = 0, ndxOutput = 0;

        List<ItemStack> inputItems = new ArrayList<>();
        List<ItemStack> outputItems = new ArrayList<>();

        for (IGuiIngredient<ItemStack> ing : recipeLayout.getItemStacks().getGuiIngredients().values()) {
            if (ing.isInput()) {
                inputItems.add(ing.getDisplayedIngredient());
            }
            else {
                outputItems.add(ing.getDisplayedIngredient());
            }
        }

        if (!retainEmptyInputs) {
            if (doCompress) {
                inputItems = compress(inputItems);
                outputItems = compress(outputItems);
            } else {
                inputItems = inputItems.stream().filter(Objects::nonNull).collect(Collectors.toList());
                outputItems = outputItems.stream().filter(Objects::nonNull).collect(Collectors.toList());
            }
        }

        if (fluidFirst) {
            ndxCrafting = encodeFluid(recipeLayout, crafting, ext, 0, true);
            ndxCrafting = encodeItem(inputItems, crafting, ndxCrafting);
            ndxOutput = encodeFluid(recipeLayout, output, ext, 0, false);
            ndxOutput = encodeItem(outputItems, output, ndxOutput);
        } else {
            ndxCrafting = encodeItem(inputItems, crafting, 0);
            ndxCrafting = encodeFluid(recipeLayout, crafting, ext, ndxCrafting, true);
            ndxOutput = encodeItem(outputItems, output, 0);
            ndxOutput = encodeFluid(recipeLayout, output, ext, ndxOutput, false);
        }

    }

    public static List<ItemStack> compress(Collection<ItemStack> list) {
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
        return comp.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private static int encodeFluid(IRecipeLayout recipeLayout, IAEItemStack[] array, ExtraExtractors ext, int index, boolean isInput) {
        for (IGuiIngredient<FluidStack> ing : recipeLayout.getFluidStacks().getGuiIngredients().values()) {
            if (ing.isInput() == isInput) {
                if (index < array.length && ing.getDisplayedIngredient() != null) {
                    array[index++] = ItemFluidPacket.newAeStack(ing.getDisplayedIngredient());
                }
            }
        }
        Iterator<WrappedIngredient<FluidStack>> iter = ext.extractFluids(recipeLayout).iterator();
        while (iter.hasNext()) {
            WrappedIngredient<FluidStack> ing = iter.next();
            if (ing.isInput() == isInput) {
                if (index < array.length && ing.getIngredient() != null) {
                    array[index++] = ItemFluidPacket.newAeStack(ing.getIngredient());
                }
            }
        }
        return index;
    }

    private static int encodeItem(Collection<ItemStack> itemList, IAEItemStack[] array, int index) {
        for (ItemStack item : itemList) {
            if (item != null && index < array.length) {
                array[index] = AEItemStack.fromItemStack(item);
            }
            index ++;
        }
        return index;
    }

}
