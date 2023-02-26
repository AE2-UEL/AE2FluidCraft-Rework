package com.glodblock.github.integration.jei;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.container.ContainerExtendedFluidPatternTerminal;
import com.glodblock.github.common.part.PartExtendedFluidPatternTerminal;
import com.glodblock.github.network.CPacketLoadPattern;
import com.glodblock.github.util.NameConst;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.transfer.RecipeTransferErrorTooltip;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ExtendedFluidPatternTerminalRecipeTransferHandler implements IRecipeTransferHandler<ContainerExtendedFluidPatternTerminal> {
    @Override
    @Nonnull
    public Class<ContainerExtendedFluidPatternTerminal> getContainerClass() {
        return ContainerExtendedFluidPatternTerminal.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@Nonnull ContainerExtendedFluidPatternTerminal container, @Nonnull IRecipeLayout recipeLayout,
                                               @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {

       if (recipeLayout.getRecipeCategory().getUid().equals(VanillaRecipeCategoryUid.CRAFTING)) {
           return new RecipeTransferErrorTooltip(I18n.format(NameConst.TT_CRAFTING_RECIPE_ONLY));
       }

       if (doTransfer && container.part instanceof PartExtendedFluidPatternTerminal) {
           PartExtendedFluidPatternTerminal tile = (PartExtendedFluidPatternTerminal)container.part;
           RecipeTransferBuilder transfer = new RecipeTransferBuilder(
                   tile.getInventoryByName("crafting").getSlots(),
                   tile.getInventoryByName("output").getSlots(),
                   recipeLayout)
                   .clearEmptySlot(true)
                   .putFluidFirst(container.fluidFirst)
                   .build();
           FluidCraft.proxy.netHandler.sendToServer(new CPacketLoadPattern(transfer.getInput(), transfer.getOutput(), container.combine));
       }
       return null;
    }

}
