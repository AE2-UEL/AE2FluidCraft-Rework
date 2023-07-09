package com.glodblock.github.integration.jei.handlers;

import com.glodblock.github.client.container.ContainerFluidPatternTerminal;
import com.glodblock.github.common.part.PartFluidPatternTerminal;
import com.glodblock.github.integration.builder.RecipeTransferBuilder;
import com.glodblock.github.network.NetworkManager;
import com.glodblock.github.network.packets.CPacketFluidCraftBtns;
import com.glodblock.github.network.packets.CPacketLoadPattern;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.entity.player.PlayerEntity;

import javax.annotation.Nonnull;

public class FluidPatternTerminalRecipeTransferHandler implements IRecipeTransferHandler<ContainerFluidPatternTerminal> {

    @Nonnull
    @Override
    public Class<ContainerFluidPatternTerminal> getContainerClass() {
        return ContainerFluidPatternTerminal.class;
    }

    @Override
    public IRecipeTransferError transferRecipe(@Nonnull ContainerFluidPatternTerminal container, @Nonnull Object recipe, @Nonnull IRecipeLayout recipeLayout, @Nonnull PlayerEntity player, boolean maxTransfer, boolean doTransfer) {
        if (doTransfer) {
            boolean craftMode = container.craftingMode;
            if (container.isCraftingMode() && !recipeLayout.getRecipeCategory().getUid().equals(VanillaRecipeCategoryUid.CRAFTING)) {
                NetworkManager.netHandler.sendToServer(new CPacketFluidCraftBtns("craft", false));
                craftMode = false;
            }
            else if (!container.isCraftingMode() && recipeLayout.getRecipeCategory().getUid().equals(VanillaRecipeCategoryUid.CRAFTING)) {
                NetworkManager.netHandler.sendToServer(new CPacketFluidCraftBtns("craft", true));
                craftMode = true;
            }
            PartFluidPatternTerminal tile = container.getPart();
            RecipeTransferBuilder transfer = new RecipeTransferBuilder(
                    tile.getInventoryByName("crafting").getSlots(),
                    tile.getInventoryByName("output").getSlots(),
                    recipeLayout)
                    .clearEmptySlot(!craftMode)
                    .putFluidFirst(container.fluidFirst)
                    .build();
            NetworkManager.netHandler.sendToServer(new CPacketLoadPattern(transfer.getInput(), transfer.getOutput(), container.combine));
        }
        return null;
    }

}
