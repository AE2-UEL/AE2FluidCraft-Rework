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
import java.util.Iterator;

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
            transferRecipeSlots(recipeLayout, crafting, output, false, ext);
            FluidCraft.proxy.netHandler.sendToServer(new CPacketLoadPattern(crafting, output));
        }
        return null;
    }

    public static void transferRecipeSlots(IRecipeLayout recipeLayout, IAEItemStack[] crafting, IAEItemStack[] output,
                                           boolean retainEmptyInputs, ExtraExtractors ext) {
        int ndxCrafting = 0, ndxOutput = 0;
        for (IGuiIngredient<ItemStack> ing : recipeLayout.getItemStacks().getGuiIngredients().values()) {
            if (ing.isInput()) {
                if (ndxCrafting < crafting.length) {
                    ItemStack stack = ing.getDisplayedIngredient();
                    if (stack != null) {
                        crafting[ndxCrafting++] = AEItemStack.fromItemStack(stack);
                    } else if (retainEmptyInputs) {
                        crafting[ndxCrafting++] = null;
                    }
                }
            } else {
                if (ndxOutput < output.length) {
                    ItemStack stack = ing.getDisplayedIngredient();
                    if (stack != null) {
                        output[ndxOutput++] = AEItemStack.fromItemStack(stack);
                    }
                }
            }
        }
        for (IGuiIngredient<FluidStack> ing : recipeLayout.getFluidStacks().getGuiIngredients().values()) {
            if (ing.isInput()) {
                if (ndxCrafting < crafting.length) {
                    crafting[ndxCrafting++] = ItemFluidPacket.newAeStack(ing.getDisplayedIngredient());
                }
            } else {
                if (ndxOutput < output.length) {
                    output[ndxOutput++] = ItemFluidPacket.newAeStack(ing.getDisplayedIngredient());
                }
            }
        }
        Iterator<WrappedIngredient<FluidStack>> iter = ext.extractFluids(recipeLayout).iterator();
        while (iter.hasNext()) {
            WrappedIngredient<FluidStack> ing = iter.next();
            if (ing.isInput()) {
                if (ndxCrafting < crafting.length) {
                    crafting[ndxCrafting++] = ItemFluidPacket.newAeStack(ing.getIngredient());
                }
            } else {
                if (ndxOutput < output.length) {
                    output[ndxOutput++] = ItemFluidPacket.newAeStack(ing.getIngredient());
                }
            }
        }
    }

}
