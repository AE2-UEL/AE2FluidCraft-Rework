package com.glodblock.github.integration.jei;

import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.container.ContainerWirelessFluidPatternTerminal;
import com.glodblock.github.network.CPacketLoadPattern;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

public class WirelessFluidPatternTerminalRecipeTransferHandler implements IRecipeTransferHandler<ContainerWirelessFluidPatternTerminal> {

    @Override
    @Nonnull
    public Class<ContainerWirelessFluidPatternTerminal> getContainerClass() {
        return ContainerWirelessFluidPatternTerminal.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@Nonnull ContainerWirelessFluidPatternTerminal container, @Nonnull IRecipeLayout recipeLayout,
                                               @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        if (doTransfer) {
            boolean craftMode = container.craftingMode;
            try {
                if (container.isCraftingMode() && !recipeLayout.getRecipeCategory().getUid().equals(VanillaRecipeCategoryUid.CRAFTING)) {
                    NetworkHandler.instance().sendToServer(new PacketValueConfig("PatternTerminal.CraftMode", "0"));
                    craftMode = false;
                }
                else if (!container.isCraftingMode() && recipeLayout.getRecipeCategory().getUid().equals(VanillaRecipeCategoryUid.CRAFTING)) {
                    NetworkHandler.instance().sendToServer(new PacketValueConfig("PatternTerminal.CraftMode", "1"));
                    craftMode = true;
                }
            } catch (IOException ignore) {
            }
            RecipeTransferBuilder transfer = new RecipeTransferBuilder(
                    recipeLayout)
                    .clearEmptySlot(!craftMode)
                    .putFluidFirst(container.fluidFirst)
                    .build();
            FluidCraft.proxy.netHandler.sendToServer(new CPacketLoadPattern(transfer.getInput(), transfer.getOutput(), container.combine));
        }
        return null;
    }

}
